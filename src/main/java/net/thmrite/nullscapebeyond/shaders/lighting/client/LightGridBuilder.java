package net.thmrite.nullscapebeyond.shaders.lighting.client;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

final class LightGridBuilder {
    static final int GRID = 64;
    static final int TILES = 8;
    static final int ATLAS = GRID * TILES; // 512
    static final int SECTION_SPAN = 5;     // sections per axis covered by the grid (origin snaps to 8 blocks)
    static final float ENCODE_RANGE = 2.0f;

    private static final int CELLS = GRID * GRID * GRID;

    record Point(double x, double y, double z, float r, float g, float b, float radius, float intensity) {}

    record Result(int ox, int oy, int oz, boolean empty, byte[] atlas) {}

    static final class SectionOpacity {
        static final SectionOpacity CLEAR = new SectionOpacity(null, false);
        static final SectionOpacity SOLID = new SectionOpacity(null, true);

        private final boolean[] data;
        private final boolean solid;

        private SectionOpacity(boolean[] data, boolean solid) {
            this.data = data;
            this.solid = solid;
        }

        boolean isOpaque(int x, int y, int z) {
            return data == null ? solid : data[(y << 8) | (z << 4) | x];
        }

        /** Main thread only. */
        static SectionOpacity build(ClientLevel level, LevelChunkSection section, int sx, int sy, int sz) {
            boolean[] d = new boolean[4096];
            boolean any = false, all = true;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState st = section.getBlockState(x, y, z);
                        pos.set((sx << 4) + x, (sy << 4) + y, (sz << 4) + z);
                        boolean o = st.getLightBlock(level, pos) >= 15;
                        d[(y << 8) | (z << 4) | x] = o;
                        any |= o;
                        all &= o;
                    }
                }
            }
            if (!any) return CLEAR;
            if (all) return SOLID;
            return new SectionOpacity(d, false);
        }
    }

    private final float[] acc = new float[CELLS * 3];
    private final int[] stamp = new int[CELLS];
    private final int[] queue = new int[CELLS];
    private int stampCounter;

    private int ox, oy, oz, bsx, bsy, bsz;
    private SectionOpacity[] sections;

    Result build(List<Point> points, int ox, int oy, int oz, int bsx, int bsy, int bsz, SectionOpacity[] sections) {
        if (points.isEmpty()) return new Result(ox, oy, oz, true, null);
        this.ox = ox; this.oy = oy; this.oz = oz;
        this.bsx = bsx; this.bsy = bsy; this.bsz = bsz;
        this.sections = sections;

        Arrays.fill(acc, 0f);
        for (Point p : points) propagate(p);
        return new Result(ox, oy, oz, false, encode());
    }

    private boolean opaque(int x, int y, int z) {
        int wx = ox + x, wy = oy + y, wz = oz + z;
        SectionOpacity s = sections[(((wy >> 4) - bsy) * SECTION_SPAN + ((wz >> 4) - bsz)) * SECTION_SPAN + ((wx >> 4) - bsx)];
        return s.isOpaque(wx & 15, wy & 15, wz & 15);
    }

    private void propagate(Point p) {
        float lx = (float) (p.x() - ox), ly = (float) (p.y() - oy), lz = (float) (p.z() - oz);
        int sx = (int) Math.floor(lx), sy = (int) Math.floor(ly), sz = (int) Math.floor(lz);
        if (sx < 0 || sy < 0 || sz < 0 || sx >= GRID || sy >= GRID || sz >= GRID) return;
        if (p.radius() <= 0f) return;

        if (stampCounter == Integer.MAX_VALUE) {
            Arrays.fill(stamp, 0);
            stampCounter = 0;
        }
        int mark = ++stampCounter;
        float radius = p.radius();
        float r2 = radius * radius;
        float invR = 1f / radius;

        int start = (sy * GRID + sz) * GRID + sx;
        int head = 0, tail = 0;
        queue[tail++] = start;
        stamp[start] = mark;

        while (head < tail) {
            int cur = queue[head++];
            int x = cur & 63, z = (cur >> 6) & 63, y = cur >> 12;

            float dx = x + 0.5f - lx, dy = y + 0.5f - ly, dz = z + 0.5f - lz;
            float t = 1f - (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * invR;
            if (t > 0f) {
                float att = t * t * p.intensity();
                int a = cur * 3;
                acc[a] += p.r() * att;
                acc[a + 1] += p.g() * att;
                acc[a + 2] += p.b() * att;
            }

            if (cur != start && opaque(x, y, z)) continue;

            for (int d = 0; d < 6; d++) {
                int nx = x + (d == 0 ? 1 : d == 1 ? -1 : 0);
                int ny = y + (d == 2 ? 1 : d == 3 ? -1 : 0);
                int nz = z + (d == 4 ? 1 : d == 5 ? -1 : 0);
                if (nx < 0 || ny < 0 || nz < 0 || nx >= GRID || ny >= GRID || nz >= GRID) continue;
                int ni = (ny * GRID + nz) * GRID + nx;
                if (stamp[ni] == mark) continue;
                float ex = nx + 0.5f - lx, ey = ny + 0.5f - ly, ez = nz + 0.5f - lz;
                if (ex * ex + ey * ey + ez * ez >= r2) continue;
                stamp[ni] = mark;
                queue[tail++] = ni;
            }
        }
    }

    private byte[] encode() {
        byte[] atlas = new byte[ATLAS * ATLAS * 4];
        float k = 255f / ENCODE_RANGE;
        for (int y = 0; y < GRID; y++) {
            int tx = y & (TILES - 1), ty = y / TILES;
            for (int z = 0; z < GRID; z++) {
                int src = (y * GRID + z) * GRID;
                int dst = ((ty * GRID + z) * ATLAS + tx * GRID) * 4;
                for (int x = 0; x < GRID; x++) {
                    int s = (src + x) * 3;
                    atlas[dst] = toByte(acc[s] * k);
                    atlas[dst + 1] = toByte(acc[s + 1] * k);
                    atlas[dst + 2] = toByte(acc[s + 2] * k);
                    atlas[dst + 3] = (byte) 255;
                    dst += 4;
                }
            }
        }
        return atlas;
    }

    private static byte toByte(float v) {
        int i = (int) (v + 0.5f);
        return (byte) (i < 0 ? 0 : Math.min(i, 255));
    }
}