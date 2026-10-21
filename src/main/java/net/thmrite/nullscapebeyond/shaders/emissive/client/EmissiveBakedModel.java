package net.thmrite.nullscapebeyond.shaders.emissive.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.thmrite.nullscapebeyond.shaders.emissive.EmissiveSettings;

/**
 * Block models: for every quad whose sprite has a "_e" companion, adds a second quad that uses the emissive
 * sprite, lightEmission 15 (fullbright) and no directional shading. Terrain is meshed by the chunk renderer
 * (vanilla or Sodium), which cannot use a custom shader, so this is the block-side equivalent.
 */
public class EmissiveBakedModel extends BakedModelWrapper<BakedModel> {
    private static final float EPS = 0.001f; // lift the overlay off the surface to avoid z-fighting
    private static final int STRIDE = 8;      // ints per vertex in a baked quad

    private final Map<BakedQuad, Optional<BakedQuad>> cache = new ConcurrentHashMap<>();

    public EmissiveBakedModel(BakedModel original) {
        super(original);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        ChunkRenderTypeSet base = super.getRenderTypes(state, rand, data);
        // overlay texels can be transparent, so they go through the cutout pass
        return EmissiveSettings.enabled() ? ChunkRenderTypeSet.union(base, ChunkRenderTypeSet.of(RenderType.cutout())) : base;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable RenderType renderType) {
        if (!EmissiveSettings.enabled() || (renderType != null && renderType != RenderType.cutout())) {
            return super.getQuads(state, side, rand, data, renderType);
        }

        boolean includeBase = renderType == null || state == null
                || super.getRenderTypes(state, rand, data).contains(renderType);
        List<BakedQuad> base = includeBase ? super.getQuads(state, side, rand, data, renderType) : List.of();

        List<BakedQuad> overlay = null;
        for (BakedQuad q : originalModel.getQuads(state, side, rand, data, null)) {
            BakedQuad e = cache.computeIfAbsent(q, EmissiveBakedModel::toEmissive).orElse(null);
            if (e != null) {
                if (overlay == null) overlay = new ArrayList<>(base);
                overlay.add(e);
            }
        }
        return overlay != null ? overlay : base;
    }

    private static Optional<BakedQuad> toEmissive(BakedQuad q) {
        TextureAtlasSprite s = q.getSprite();
        TextureAtlasSprite es = EmissiveTextures.emissiveSprite(s);
        if (es == null) return Optional.empty();

        int[] v = q.getVertices().clone();
        Direction d = q.getDirection();
        float du = s.getU1() - s.getU0(), dv = s.getV1() - s.getV0();
        float edu = es.getU1() - es.getU0(), edv = es.getV1() - es.getV0();
        for (int i = 0; i < 4; i++) {
            int o = i * STRIDE;
            v[o] = bits(f(v[o]) + d.getStepX() * EPS);
            v[o + 1] = bits(f(v[o + 1]) + d.getStepY() * EPS);
            v[o + 2] = bits(f(v[o + 2]) + d.getStepZ() * EPS);
            float lu = (f(v[o + 4]) - s.getU0()) / du;
            float lv = (f(v[o + 5]) - s.getV0()) / dv;
            v[o + 4] = bits(es.getU0() + lu * edu);
            v[o + 5] = bits(es.getV0() + lv * edv);
            v[o + 6] = LightTexture.pack(15, 15); // baked light: block 15 / sky 15 = fullbright
        }
        return Optional.of(new BakedQuad(v, q.getTintIndex(), d, es, false, false));
    }

    private static float f(int bits) {
        return Float.intBitsToFloat(bits);
    }

    private static int bits(float f) {
        return Float.floatToRawIntBits(f);
    }
}