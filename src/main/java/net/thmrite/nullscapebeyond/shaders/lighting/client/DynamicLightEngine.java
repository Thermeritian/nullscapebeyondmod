package net.thmrite.nullscapebeyond.shaders.lighting.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.shaders.lighting.ColoredLights;
import net.thmrite.nullscapebeyond.shaders.lighting.LightingSettings;
import net.thmrite.nullscapebeyond.shaders.lighting.api.LightEmission;
import net.thmrite.nullscapebeyond.shaders.lighting.api.LightSource;
import net.thmrite.nullscapebeyond.shaders.lighting.client.LightGridBuilder.Point;
import net.thmrite.nullscapebeyond.shaders.lighting.client.LightGridBuilder.Result;
import net.thmrite.nullscapebeyond.shaders.lighting.client.LightGridBuilder.SectionOpacity;

/**
 * Client-side brain of the coloured lighting library.
 *
 * Every tick (main thread): tracks a 64^3 region around the camera, collects light points from
 * blocks (cached per chunk), block entities and entities, and when anything changed hands an
 * immutable snapshot to a worker thread that flood-fills the light volume. The render thread
 * uploads the finished volume ({@link LightRenderer}).
 */
public final class DynamicLightEngine {
    public static final DynamicLightEngine INSTANCE = new DynamicLightEngine();

    private static final int GRID = LightGridBuilder.GRID;
    private static final int SNAP = 8;
    private static final int SPAN = LightGridBuilder.SECTION_SPAN;
    private static final int CHUNK_SCANS_PER_TICK = 6;
    private static final int SECTION_BUILDS_PER_SNAPSHOT = 24;

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NullscapeBeyond-Lighting");
        t.setDaemon(true);
        return t;
    });
    private final LightGridBuilder builder = new LightGridBuilder(); // worker thread only
    private final AtomicReference<Result> result = new AtomicReference<>();

    // main-thread state
    private final Long2ObjectOpenHashMap<SectionOpacity> opacityCache = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<List<Point>> blockSources = new Long2ObjectOpenHashMap<>();
    private final LongOpenHashSet dirtyChunks = new LongOpenHashSet();
    private ClientLevel level;
    private boolean hasOrigin;
    private int ox, oy, oz;
    private boolean gridDirty = true;
    private long lastSignature;
    private int sectionBudget;
    private boolean snapshotIncomplete;

    private volatile boolean running;
    private volatile int generation;

    private DynamicLightEngine() {}

    // ------------------------------------------------------------------ public hooks

    /** Called by the ClientLevel mixin whenever a block changes (opacity and emitters may change). */
    public void onBlockChanged(BlockPos pos) {
        if (!hasOrigin || level == null) return;
        if (pos.getX() < ox - 16 || pos.getX() > ox + GRID + 16
                || pos.getY() < oy - 16 || pos.getY() > oy + GRID + 16
                || pos.getZ() < oz - 16 || pos.getZ() > oz + GRID + 16) return;
        opacityCache.remove(SectionPos.asLong(pos));
        dirtyChunks.add(ChunkPos.asLong(pos));
        gridDirty = true;
    }

    @Nullable
    Result pollResult() {
        return result.getAndSet(null);
    }

    public void reset() {
        generation++;
        level = null;
        hasOrigin = false;
        opacityCache.clear();
        blockSources.clear();
        dirtyChunks.clear();
        gridDirty = true;
        lastSignature = 0L;
        result.set(new Result(0, 0, 0, true, null)); // tells the renderer to stop drawing
    }

    // ------------------------------------------------------------------ tick

    public void tick(Minecraft mc) {
        ClientLevel lvl = mc.level;
        Entity cam = mc.getCameraEntity();
        if (!LightingSettings.enabled() || lvl == null || cam == null) {
            if (level != null) reset();
            return;
        }
        if (lvl != level) {
            reset();
            level = lvl;
        }

        Vec3 c = cam.getEyePosition();
        int nox = Mth.floor(c.x / SNAP) * SNAP - GRID / 2;
        int noy = Mth.floor(c.y / SNAP) * SNAP - GRID / 2;
        int noz = Mth.floor(c.z / SNAP) * SNAP - GRID / 2;
        if (!hasOrigin || nox != ox || noy != oy || noz != oz) {
            ox = nox; oy = noy; oz = noz;
            hasOrigin = true;
            gridDirty = true;
            pruneCaches();
        }

        rescanBlockChunks();

        List<Point> points = new ArrayList<>();
        for (List<Point> list : blockSources.values()) points.addAll(list);
        collectBlockEntities(points);
        collectEntities(points);
        points.removeIf(p -> !inRegion(p));

        int max = Math.max(16, LightingSettings.maxSources);
        if (points.size() > max) {
            points.sort(Comparator.comparingDouble(p -> distSq(p, c)));
            points.subList(max, points.size()).clear();
        }

        long sig = ((long) ox * 31 + oy) * 31 + oz;
        for (Point p : points) {
            sig = sig * 31 + Math.round(p.x() * 16);
            sig = sig * 31 + Math.round(p.y() * 16);
            sig = sig * 31 + Math.round(p.z() * 16);
            sig = sig * 31 + Float.floatToIntBits(p.r() + p.g() * 3f + p.b() * 7f);
            sig = sig * 31 + Float.floatToIntBits(p.radius() * 5f + p.intensity());
        }

        if ((gridDirty || sig != lastSignature) && !running) submit(points, sig);
    }

    // ------------------------------------------------------------------ job submission

    private void submit(List<Point> points, long sig) {
        snapshotIncomplete = false;
        sectionBudget = SECTION_BUILDS_PER_SNAPSHOT;
        int bsx = Math.floorDiv(ox, 16), bsy = Math.floorDiv(oy, 16), bsz = Math.floorDiv(oz, 16);
        SectionOpacity[] sections = new SectionOpacity[SPAN * SPAN * SPAN];
        for (int sy = 0; sy < SPAN; sy++)
            for (int sz = 0; sz < SPAN; sz++)
                for (int sx = 0; sx < SPAN; sx++)
                    sections[(sy * SPAN + sz) * SPAN + sx] = opacityFor(bsx + sx, bsy + sy, bsz + sz);

        // if some sections were deferred (build budget) or unloaded, run again next tick
        gridDirty = snapshotIncomplete;
        lastSignature = sig;
        running = true;

        final int gen = generation;
        final int fox = ox, foy = oy, foz = oz;
        worker.execute(() -> {
            try {
                Result r = builder.build(points, fox, foy, foz, bsx, bsy, bsz, sections);
                if (gen == generation) result.set(r);
            } catch (Throwable t) {
                NullscapeBeyond.LOGGER.error("Coloured lighting job failed", t);
            } finally {
                running = false;
            }
        });
    }

    private SectionOpacity opacityFor(int sx, int sy, int sz) {
        if (sy < level.getMinSection() || sy >= level.getMaxSection()) return SectionOpacity.CLEAR;
        long key = SectionPos.asLong(sx, sy, sz);
        SectionOpacity cached = opacityCache.get(key);
        if (cached != null) return cached;

        LevelChunk chunk = level.getChunkSource().getChunkNow(sx, sz);
        if (chunk == null) {
            snapshotIncomplete = true; // chunk not loaded yet: treated as solid, retried later
            return SectionOpacity.SOLID;
        }
        LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sy));
        SectionOpacity built;
        if (section.hasOnlyAir()) {
            built = SectionOpacity.CLEAR;
        } else {
            if (sectionBudget-- <= 0) {
                snapshotIncomplete = true;
                return SectionOpacity.SOLID;
            }
            built = SectionOpacity.build(level, section, sx, sy, sz);
        }
        opacityCache.put(key, built);
        return built;
    }

    // ------------------------------------------------------------------ light point collection

    private int minChunkX() { return Math.floorDiv(ox, 16); }
    private int maxChunkX() { return Math.floorDiv(ox + GRID - 1, 16); }
    private int minChunkZ() { return Math.floorDiv(oz, 16); }
    private int maxChunkZ() { return Math.floorDiv(oz + GRID - 1, 16); }

    private boolean inRegion(Point p) {
        return p.x() >= ox && p.x() < ox + GRID && p.y() >= oy && p.y() < oy + GRID && p.z() >= oz && p.z() < oz + GRID;
    }

    private static double distSq(Point p, Vec3 c) {
        double dx = p.x() - c.x, dy = p.y() - c.y, dz = p.z() - c.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static Point point(double x, double y, double z, LightEmission e) {
        return new Point(x, y, z, e.color().r(), e.color().g(), e.color().b(), e.radius(), e.intensity());
    }

    private void pruneCaches() {
        int bsx = Math.floorDiv(ox, 16), bsy = Math.floorDiv(oy, 16), bsz = Math.floorDiv(oz, 16);
        LongIterator it = opacityCache.keySet().iterator();
        while (it.hasNext()) {
            long k = it.nextLong();
            int x = SectionPos.x(k), y = SectionPos.y(k), z = SectionPos.z(k);
            if (x < bsx - 1 || x > bsx + SPAN || y < bsy - 1 || y > bsy + SPAN || z < bsz - 1 || z > bsz + SPAN) it.remove();
        }
        LongIterator cit = blockSources.keySet().iterator();
        while (cit.hasNext()) {
            long k = cit.nextLong();
            if (!chunkInRange(ChunkPos.getX(k), ChunkPos.getZ(k))) cit.remove();
        }
        dirtyChunks.removeIf((long k) -> !chunkInRange(ChunkPos.getX(k), ChunkPos.getZ(k)));
    }

    private boolean chunkInRange(int cx, int cz) {
        return cx >= minChunkX() && cx <= maxChunkX() && cz >= minChunkZ() && cz <= maxChunkZ();
    }

    /** Keeps per-chunk lists of registered emitter blocks up to date (budgeted per tick). */
    private void rescanBlockChunks() {
        int budget = CHUNK_SCANS_PER_TICK;
        for (int cx = minChunkX(); cx <= maxChunkX(); cx++) {
            for (int cz = minChunkZ(); cz <= maxChunkZ(); cz++) {
                long key = ChunkPos.asLong(cx, cz);
                boolean known = blockSources.containsKey(key);
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    if (known) { blockSources.remove(key); gridDirty = true; }
                    continue;
                }
                if (known && !dirtyChunks.contains(key)) continue;
                if (budget-- <= 0) return;
                blockSources.put(key, scanChunk(chunk));
                dirtyChunks.remove(key);
                gridDirty = true;
            }
        }
    }

    private List<Point> scanChunk(LevelChunk chunk) {
        List<Point> out = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        ChunkPos cp = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection sec = sections[i];
            if (sec.hasOnlyAir() || !sec.maybeHas(ColoredLights::hasBlock)) continue;
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState st = sec.getBlockState(x, y, z);
                        LightEmission em = ColoredLights.blockEmission(st);
                        if (em == null || !em.isActive()) continue;
                        pos.set(cp.getMinBlockX() + x, baseY + y, cp.getMinBlockZ() + z);
                        if (isBuried(pos, st)) continue;
                        out.add(point(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, em));
                    }
                }
            }
        }
        return out;
    }

    /** A source whose every neighbour is the same block or fully opaque can't light anything. */
    private boolean isBuried(BlockPos pos, BlockState st) {
        BlockPos.MutableBlockPos n = new BlockPos.MutableBlockPos();
        for (Direction d : Direction.values()) {
            n.setWithOffset(pos, d);
            BlockState ns = level.getBlockState(n);
            if (ns.getBlock() != st.getBlock() && ns.getLightBlock(level, n) < 15) return false;
        }
        return true;
    }

    private void collectBlockEntities(List<Point> out) {
        for (int cx = minChunkX(); cx <= maxChunkX(); cx++) {
            for (int cz = minChunkZ(); cz <= maxChunkZ(); cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.isRemoved()) continue;
                    LightEmission em = null;
                    Vec3 off = Vec3.ZERO;
                    if (be instanceof LightSource ls) {
                        em = ls.getLightEmission();
                        off = ls.getLightOffset();
                    }
                    if (em == null) em = ColoredLights.blockEntityEmission(be);
                    if (em == null || !em.isActive()) continue;
                    BlockPos bp = be.getBlockPos();
                    out.add(point(bp.getX() + 0.5 + off.x, bp.getY() + 0.5 + off.y, bp.getZ() + 0.5 + off.z, em));
                }
            }
        }
    }

    private void collectEntities(List<Point> out) {
        for (Entity e : level.entitiesForRendering()) {
            if (e.isRemoved()) continue;
            LightEmission em = null;
            Vec3 off = Vec3.ZERO;
            if (e instanceof LightSource ls) {
                em = ls.getLightEmission();
                off = ls.getLightOffset();
            }
            if (em == null) em = ColoredLights.entityEmission(e);
            if (em == null && LightingSettings.heldItems && e instanceof LivingEntity le) em = ColoredLights.heldEmission(le);
            if (em == null || !em.isActive()) continue;
            Vec3 c = e.getBoundingBox().getCenter();
            out.add(point(c.x + off.x, c.y + off.y, c.z + off.z, em));
        }
    }
}