package connor135246.simplyhotsprings.client.particles;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.BlockPos;

/**
 * a lot copy-pasted {@link net.minecraft.client.particle.UnderwaterParticle}
 */
public class HotSpringWaterUnderwaterParticle extends SpriteTexturedParticle
{

    protected static final Fluid FLUID = SimplyHotSpringsCommon.HOT_SPRING_WATER.orElse(null);

    public HotSpringWaterUnderwaterParticle(ClientWorld cworld, double x, double y, double z)
    {
        super(cworld, x, y - 0.125D, z);
        this.particleRed = 0.1F;
        this.particleGreen = 0.7F;
        this.particleBlue = 0.7F;
        this.setSize(0.01F, 0.01F);
        this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
        this.maxAge = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        this.canCollide = false;
    }

    public HotSpringWaterUnderwaterParticle(ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        this(cworld, x, y, z);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.maxAge-- <= 0)
            this.setExpired();
        else
        {
            this.move(this.motionX, this.motionY, this.motionZ);
            checkBlockState();
        }
    }

    protected void checkBlockState()
    {
        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
        FluidState fluidstate = this.world.getFluidState(blockpos);
        if (fluidstate.getFluid() != FLUID || this.posY > blockpos.getY() + fluidstate.getActualHeight(this.world, blockpos))
            this.setExpired();
    }

    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterUnderwaterParticle particle = new HotSpringWaterUnderwaterParticle(cworld, x, y, z, motionX, motionY, motionZ);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }

}
