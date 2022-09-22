package connor135246.simplyhotsprings.client.particles;

import javax.annotation.Nullable;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.FluidState;
/**
 * a lot copy-pasted from {@link net.minecraft.client.particle.DripParticle} and {@link net.minecraft.client.particle.SplashParticle}
 */
public abstract class HotSpringWaterDripParticle extends TextureSheetParticle
{

    protected final @Nullable ParticleOptions nextParticle;

    public HotSpringWaterDripParticle(ClientLevel clevel, double x, double y, double z, @Nullable ParticleOptions nextParticle)
    {
        super(clevel, x, y, z);
        this.nextParticle = nextParticle;
        this.setup();
    }

    public HotSpringWaterDripParticle(ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ,
            @Nullable ParticleOptions nextParticle)
    {
        super(clevel, x, y, z, motionX, motionY, motionZ);
        this.nextParticle = nextParticle;
        this.setup();
    }

    private void setup()
    {
        this.setSize(0.01F, 0.01F);
        this.setColor(0.01F, 1.0F, 1.0F);
        this.gravity = 0.06F;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.ageParticle();
        if (this.isAlive())
        {
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.updateMotion();
            if (this.isAlive())
            {
                this.xd *= 0.98F;
                this.yd *= 0.98F;
                this.zd *= 0.98F;
                this.checkBlockState();
            }
        }
    }

    protected void ageParticle()
    {
        if (this.lifetime-- <= 0)
            this.remove();
    }

    protected void updateMotion()
    {
        ;
    }

    protected void checkBlockState()
    {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        FluidState fluidstate = this.level.getFluidState(blockpos);
        if (fluidstate.is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER) && this.y < blockpos.getY() + fluidstate.getHeight(this.level, blockpos))
            this.remove();
    }

    protected void spawnNextParticle(boolean copyMotion)
    {
        if (this.nextParticle != null)
        {
            this.level.addParticle(this.nextParticle, this.x, this.y, this.z,
                    copyMotion ? this.xd : 0.0D, copyMotion ? this.yd : 0.0D, copyMotion ? this.zd : 0.0D);
        }
    }

    public static class Dripping extends HotSpringWaterDripParticle
    {
        public Dripping(ClientLevel clevel, double x, double y, double z, @Nullable ParticleOptions nextParticle)
        {
            super(clevel, x, y, z, nextParticle);
            this.gravity *= 0.02F;
            this.lifetime = 40;
        }

        @Override
        protected void ageParticle()
        {
            if (this.lifetime-- <= 0)
            {
                this.remove();
                this.spawnNextParticle(true);
            }
        }

        @Override
        protected void updateMotion()
        {
            this.xd *= 0.02D;
            this.yd *= 0.02D;
            this.zd *= 0.02D;
        }
    }

    public static class Falling extends HotSpringWaterDripParticle
    {
        public Falling(ClientLevel clevel, double x, double y, double z, @Nullable ParticleOptions nextParticle)
        {
            super(clevel, x, y, z, nextParticle);
            this.lifetime = (int) (64.0D / (Math.random() * 0.8D + 0.2D));
        }

        @Override
        protected void updateMotion()
        {
            if (this.onGround)
            {
                this.remove();
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
        protected Splashing(ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            super(clevel, x, y, z, 0.0D, 0.0D, 0.0D, null);
            if (motionY == 0.0D && (motionX != 0.0D || motionZ != 0.0D))
            {
                this.xd = motionX;
                this.yd = 0.1D;
                this.zd = motionZ;
            }
            else
            {
                this.xd *= 0.3F;
                this.yd = Math.random() * 0.2F + 0.1F;
                this.zd *= 0.3F;
            }
            this.gravity = 0.04F;
            this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        }

        @Override
        protected void updateMotion()
        {
            if (this.onGround)
            {
                if (Math.random() < 0.5D)
                    this.remove();
                else
                {
                    this.xd *= 0.7F;
                    this.zd *= 0.7F;
                }
            }
        }

        @Override
        protected void checkBlockState()
        {
            BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
            double collisionHeight = Math.max(this.level.getBlockState(blockpos).getCollisionShape(level, blockpos).max(Direction.Axis.Y,
                    this.x - blockpos.getX(), this.z - blockpos.getZ()), this.level.getFluidState(blockpos).getHeight(this.level, blockpos));
            if (collisionHeight > 0.0D && this.y < (double) blockpos.getY() + collisionHeight)
                this.remove();
        }
    }

    public static class DrippingProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet spriteSet;

        public DrippingProvider(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterDripParticle particle = new HotSpringWaterDripParticle.Dripping(clevel, x, y, z,
                    SimplyHotSpringsCommon.FALLING_HOT_SPRING_WATER.orElse(null));
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

    public static class FallingProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet spriteSet;

        public FallingProvider(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterDripParticle particle = new HotSpringWaterDripParticle.Falling(clevel, x, y, z,
                    SimplyHotSpringsCommon.SPLASHING_HOT_SPRING_WATER.orElse(null));
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

    public static class SplashingProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteSet;

        public SplashingProvider(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterDripParticle particle = new HotSpringWaterDripParticle.Splashing(clevel, x, y, z, motionX, motionY, motionZ);
            particle.pickSprite(spriteSet);
            return particle;
        }
    }

}
