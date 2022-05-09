package connor135246.simplyhotsprings.client.particles;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * a lot copy-pasted {@link net.minecraft.client.particle.SuspendedParticle}
 */
public class HotSpringWaterUnderwaterParticle extends TextureSheetParticle
{

    protected static final Fluid FLUID = SimplyHotSpringsCommon.HOT_SPRING_WATER.orElse(null);

    public HotSpringWaterUnderwaterParticle(ClientLevel clevel, double x, double y, double z)
    {
        super(clevel, x, y - 0.125D, z);
        this.rCol = 0.1F;
        this.gCol = 0.7F;
        this.bCol = 0.7F;
        this.setSize(0.01F, 0.01F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.lifetime = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        this.friction = 1.0F;
        this.hasPhysics = false;
    }

    public HotSpringWaterUnderwaterParticle(ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        this(clevel, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.lifetime-- <= 0)
            this.remove();
        else
        {
            this.move(this.xd, this.yd, this.zd);
            checkBlockState();
        }
    }

    protected void checkBlockState()
    {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        FluidState fluidstate = this.level.getFluidState(blockpos);
        if (fluidstate.getType() != FLUID || this.y > blockpos.getY() + fluidstate.getHeight(this.level, blockpos))
            this.remove();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            HotSpringWaterUnderwaterParticle particle = new HotSpringWaterUnderwaterParticle(clevel, x, y, z, motionX, motionY, motionZ);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

}
