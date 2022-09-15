package connor135246.simplyhotsprings.client.particles;

import net.minecraft.client.particle.CloudParticle;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;

/**
 * A copy-paste of {@link CloudParticle}, but without the weird bit that checks for nearby players and moves the particle down.
 */
public class SmallSteamParticle extends SpriteTexturedParticle
{
    private final IAnimatedSprite spriteSetWithAge;

    public SmallSteamParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, IAnimatedSprite spriteSetWithAge)
    {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.spriteSetWithAge = spriteSetWithAge;
        float f = 2.5F;
        this.motionX *= (double) 0.1F;
        this.motionY *= (double) 0.1F;
        this.motionZ *= (double) 0.1F;
        this.motionX += motionX;
        this.motionY += motionY;
        this.motionZ += motionZ;
        float f1 = 1.0F - (float) (Math.random() * (double) 0.3F);
        this.particleRed = f1;
        this.particleGreen = f1;
        this.particleBlue = f1;
        this.particleScale *= 1.875F;
        int i = (int) (8.0D / (Math.random() * 0.8D + 0.3D));
        this.maxAge = (int) Math.max((float) i * 2.5F, 1.0F);
        // this.canCollide = false;
        this.selectSpriteWithAge(spriteSetWithAge);
    }

    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getScale(float scaleFactor)
    {
        return this.particleScale * MathHelper.clamp(((float) this.age + scaleFactor) / (float) this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge)
            this.setExpired();
        else
        {
            this.selectSpriteWithAge(this.spriteSetWithAge);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double) 0.96F;
            this.motionY *= (double) 0.96F;
            this.motionZ *= (double) 0.96F;

            if (this.onGround)
            {
                this.motionX *= (double) 0.7F;
                this.motionZ *= (double) 0.7F;
            }
        }
    }

    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new SmallSteamParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

}