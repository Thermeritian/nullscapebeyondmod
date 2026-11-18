package net.thmrite.nullscapebeyond.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.thmrite.nullscapebeyond.registry.ModItems;

public class ResinDripParticle extends BreakingItemParticle {
    protected ResinDripParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, new ItemStack(ModItems.VIRIDIAN_RESIN.get()));
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.25F;
        this.lifetime = 100;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            this.remove();
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new ResinDripParticle(level, x, y, z);
        }
    }
}
