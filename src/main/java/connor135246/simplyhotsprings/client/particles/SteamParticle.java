package connor135246.simplyhotsprings.client.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Steam textures are campfire smoke textures but brightened up a bit.
 */
public class SteamParticle extends TextureSheetParticle
{

    private SteamParticle(ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        super(clevel, x, y, z);

        this.quadSize = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.scale(2.5F + random.nextFloat() * 1.0F);
        this.setSize(0.25F, 0.25F);
        this.quadSize *= 0.2F;

        this.xd = motionX + (double) (this.random.nextFloat() / 250.0F * (float) (this.random.nextBoolean() ? 1 : -1));
        this.zd = motionZ + (double) (this.random.nextFloat() / 250.0F * (float) (this.random.nextBoolean() ? 1 : -1));
        this.yd = motionY + 0.025 + (double) (this.random.nextFloat() / 250.0F);
        this.gravity = 0.0005F;

        this.lifetime = 100;
    }

    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ < this.lifetime && this.alpha > 0.02F)
        {
            this.xd += (double) (this.random.nextFloat() / 1000.0F * (float) (this.random.nextBoolean() ? 1 : -1));
            this.zd += (double) (this.random.nextFloat() / 1000.0F * (float) (this.random.nextBoolean() ? 1 : -1));
            this.yd -= (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);

            this.alpha -= 0.01F + random.nextFloat() * 0.01F;
        }
        else
            this.remove();
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_MASK;
    }

    /**
     * a copy of {@link ParticleRenderType#PARTICLE_SHEET_TRANSLUCENT} with depth mask set to false.
     */
    public static final ParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_MASK = new ParticleRenderType() {
        @SuppressWarnings("deprecation")
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager)
        {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tesselator)
        {
            tesselator.end();
            RenderSystem.depthMask(true);
        }

        public String toString()
        {
            return "simplyhotsprings:PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_MASK";
        }
    };

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel clevel, double x, double y, double z, double motionX, double motionY, double motionZ)
        {
            SteamParticle particle = new SteamParticle(clevel, x, y, z, motionX, motionY, motionZ);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

}
