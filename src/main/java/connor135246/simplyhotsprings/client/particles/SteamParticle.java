package connor135246.simplyhotsprings.client.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

/**
 * Steam textures are campfire smoke textures but brightened up a bit.
 */
public class SteamParticle extends SpriteTexturedParticle
{

    private SteamParticle(ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        super(cworld, x, y, z);

        this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.multiplyParticleScaleBy(2.5F + rand.nextFloat() * 1.0F);
        this.setSize(0.25F, 0.25F);
        this.particleScale *= 0.2F;

        this.motionX = motionX + (double) (this.rand.nextFloat() / 250.0F * (float) (this.rand.nextBoolean() ? 1 : -1));
        this.motionZ = motionZ + (double) (this.rand.nextFloat() / 250.0F * (float) (this.rand.nextBoolean() ? 1 : -1));
        this.motionY = motionY + 0.025 + (double) (this.rand.nextFloat() / 250.0F);
        this.particleGravity = 0.0005F;

        this.maxAge = 100;
    }

    @Override
    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.age++ < this.maxAge && this.particleAlpha > 0.02F)
        {
            this.motionX += (double) (this.rand.nextFloat() / 1000.0F * (float) (this.rand.nextBoolean() ? 1 : -1));
            this.motionZ += (double) (this.rand.nextFloat() / 1000.0F * (float) (this.rand.nextBoolean() ? 1 : -1));
            this.motionY -= (double) this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);

            this.particleAlpha -= 0.01F + rand.nextFloat() * 0.01F;
        }
        else
            this.setExpired();
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_MASK;
    }

    /**
     * a copy of {@link IParticleRenderType#PARTICLE_SHEET_TRANSLUCENT} with depth mask set to false.
     */
    public static final IParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_MASK = new IParticleRenderType() {
        @SuppressWarnings("deprecation")
        public void beginRender(BufferBuilder bufferBuilder, TextureManager textureManager)
        {
            RenderSystem.depthMask(false);
            textureManager.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(516, 0.003921569F);
            bufferBuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        public void finishRender(Tessellator tesselator)
        {
            tesselator.draw();
            RenderSystem.depthMask(true);
        }

        public String toString()
        {
            return "simplyhotsprings:PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_MASK";
        }
    };

    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, ClientWorld cworld, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            SteamParticle particle = new SteamParticle(cworld, x, y, z, motionX, motionY, motionZ);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }

}
