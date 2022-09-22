package connor135246.simplyhotsprings.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * A copy-paste of {@link PlayerCloudParticle}, but without the weird bit that checks for nearby players and moves the particle down.
 */
public class SmallSteamParticle extends TextureSheetParticle
{
    private final SpriteSet sprites;

    public SmallSteamParticle(ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites)
    {
        super(clevel, x, y, z, 0.0D, 0.0D, 0.0D);
        this.friction = 0.96F;
        this.sprites = sprites;
        float f = 2.5F;
        this.xd *= (double) 0.1F;
        this.yd *= (double) 0.1F;
        this.zd *= (double) 0.1F;
        this.xd += motionX;
        this.yd += motionY;
        this.zd += motionZ;
        float f1 = 1.0F - (float) (Math.random() * (double) 0.3F);
        this.rCol = f1;
        this.gCol = f1;
        this.bCol = f1;
        this.quadSize *= 1.875F;
        int i = (int) (8.0D / (Math.random() * 0.8D + 0.3D));
        this.lifetime = (int) Math.max((float) i * 2.5F, 1.0F);
        // this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks)
    {
        return this.quadSize * Mth.clamp(((float) this.age + partialTicks) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!this.removed)
            this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites)
        {
            this.sprites = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            return new SmallSteamParticle(clevel, x, y, z, motionX, motionY, motionZ, this.sprites);
        }
    }

}
