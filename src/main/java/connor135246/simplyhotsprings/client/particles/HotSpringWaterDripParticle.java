package connor135246.simplyhotsprings.client.particles;

import javax.annotation.Nullable;

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
import net.minecraft.particles.IParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * a lot copy-pasted from {@link net.minecraft.client.particle.DripParticle} and {@link net.minecraft.client.particle.SplashParticle}
 */
public abstract class HotSpringWaterDripParticle extends SpriteTexturedParticle
{

    protected static final Fluid FLUID = SimplyHotSpringsCommon.HOT_SPRING_WATER.orElse(null);

    protected final @Nullable IParticleData nextParticle;

    public HotSpringWaterDripParticle(ClientWorld cworld, double x, double y, double z, @Nullable IParticleData nextParticle)
    {
        super(cworld, x, y, z);
        this.nextParticle = nextParticle;
        this.setup();
    }

    public HotSpringWaterDripParticle(ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ,
            @Nullable IParticleData nextParticle)
    {
        super(cworld, x, y, z, motionX, motionY, motionZ);
        this.nextParticle = nextParticle;
        this.setup();
    }

    private void setup()
    {
        this.setSize(0.01F, 0.01F);
        this.setColor(0.01F, 1.0F, 1.0F);
        this.particleGravity = 0.06F;
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.ageParticle();
        if (!this.isExpired)
        {
            this.motionY -= this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.updateMotion();
            if (!this.isExpired)
            {
                this.motionX *= 0.98F;
                this.motionY *= 0.98F;
                this.motionZ *= 0.98F;
                this.checkBlockState();
            }
        }
    }

    protected void ageParticle()
    {
        if (this.maxAge-- <= 0)
            this.setExpired();
    }

    protected void updateMotion()
    {
        ;
    }

    protected void checkBlockState()
    {
        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
        FluidState fluidstate = this.world.getFluidState(blockpos);
        if (fluidstate.getFluid() == FLUID && this.posY < blockpos.getY() + fluidstate.getActualHeight(this.world, blockpos))
            this.setExpired();
    }

    protected void spawnNextParticle(boolean copyMotion)
    {
        if (this.nextParticle != null)
        {
            this.world.addParticle(this.nextParticle, this.posX, this.posY, this.posZ,
                    copyMotion ? this.motionX : 0.0D, copyMotion ? this.motionY : 0.0D, copyMotion ? this.motionZ : 0.0D);
        }
    }

    public static class Dripping extends HotSpringWaterDripParticle
    {
        public Dripping(ClientWorld cworld, double x, double y, double z, @Nullable IParticleData nextParticle)
        {
            super(cworld, x, y, z, nextParticle);
            this.particleGravity *= 0.02F;
            this.maxAge = 40;
        }

        @Override
        protected void ageParticle()
        {
            if (this.maxAge-- <= 0)
            {
                this.setExpired();
                this.spawnNextParticle(true);
            }
        }

        @Override
        protected void updateMotion()
        {
            this.motionX *= 0.02D;
            this.motionY *= 0.02D;
            this.motionZ *= 0.02D;
        }
    }

    public static class Falling extends HotSpringWaterDripParticle
    {
        public Falling(ClientWorld cworld, double x, double y, double z, @Nullable IParticleData nextParticle)
        {
            super(cworld, x, y, z, nextParticle);
            this.maxAge = (int) (64.0D / (Math.random() * 0.8D + 0.2D));
        }

        @Override
        protected void updateMotion()
        {
            if (this.onGround)
            {
                this.setExpired();
                this.spawnNextParticle(false);
            }
        }
    }

    /**
     * The vanilla dripping and falling particles are used for various fluids so they're grayscale and can be easily colored with {@link #setColor}. <br>
     * However, the vanilla splashing particles are used for only water so the textures are dark blue. <br>
     * These splashing particles use a copy of the splashing textures I made that are grayscale.
     */
    public static class Splashing extends HotSpringWaterDripParticle
    {
        protected Splashing(ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            super(cworld, x, y, z, 0.0D, 0.0D, 0.0D, null);
            if (motionY == 0.0D && (motionX != 0.0D || motionZ != 0.0D))
            {
                this.motionX = motionX;
                this.motionY = 0.1D;
                this.motionZ = motionZ;
            }
            else
            {
                this.motionX *= 0.3F;
                this.motionY = Math.random() * 0.2F + 0.1F;
                this.motionZ *= 0.3F;
            }
            this.particleGravity = 0.04F;
            this.maxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        }

        @Override
        protected void updateMotion()
        {
            if (this.onGround)
            {
                if (Math.random() < 0.5D)
                    this.setExpired();
                else
                {
                    this.motionX *= 0.7F;
                    this.motionZ *= 0.7F;
                }
            }
        }

        @Override
        protected void checkBlockState()
        {
            BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
            double collisionHeight = Math.max(this.world.getBlockState(blockpos).getCollisionShapeUncached(this.world, blockpos).max(Direction.Axis.Y,
                    this.posX - blockpos.getX(), this.posZ - blockpos.getZ()), this.world.getFluidState(blockpos).getActualHeight(this.world, blockpos));
            if (collisionHeight > 0.0D && this.posY < (double) blockpos.getY() + collisionHeight)
                this.setExpired();
        }
    }

    public static class DrippingFactory implements IParticleFactory<BasicParticleType>
    {
        protected final IAnimatedSprite spriteSet;

        public DrippingFactory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterDripParticle particle = new HotSpringWaterDripParticle.Dripping(cworld, x, y, z,
                    SimplyHotSpringsCommon.FALLING_HOT_SPRING_WATER.orElse(null));
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }

    public static class FallingFactory implements IParticleFactory<BasicParticleType>
    {
        protected final IAnimatedSprite spriteSet;

        public FallingFactory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterDripParticle particle = new HotSpringWaterDripParticle.Falling(cworld, x, y, z,
                    SimplyHotSpringsCommon.SPLASHING_HOT_SPRING_WATER.orElse(null));
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }

    public static class SplashingFactory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite spriteSet;

        public SplashingFactory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterDripParticle particle = new HotSpringWaterDripParticle.Splashing(world, x, y, z, motionX, motionY, motionZ);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }

}
