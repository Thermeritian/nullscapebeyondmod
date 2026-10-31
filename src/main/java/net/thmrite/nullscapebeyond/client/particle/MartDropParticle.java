package net.thmrite.nullscapebeyond.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class MartDropParticle extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    protected MartDropParticle(ClientLevel level, double x, double y, double z,
                               double xs, double ys, double zs, SpriteSet sprites) {
        super(level, x, y, z, xs, ys, zs);
        this.pickSprite(sprites);
        this.gravity = 1.0f;
        this.quadSize /= 2.0f;
        this.uo = this.random.nextFloat() * 3.0f; // random cut of the texture
        this.vo = this.random.nextFloat() * 3.0f;
        this.xd = this.xd * 0.1 + xs;
        this.yd = this.yd * 0.1 + ys;
        this.zd = this.zd * 0.1 + zs;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU(this.uo / 4.0f);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU((this.uo + 1.0f) / 4.0f);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0f);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0f) / 4.0f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double xs, double ys, double zs) {
            return new MartDropParticle(level, x, y, z, xs, ys, zs, sprites);
        }
    }
}