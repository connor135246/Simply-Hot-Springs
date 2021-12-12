package connor135246.simplyhotsprings.client.particles;

import java.util.ArrayDeque;

import org.lwjgl.opengl.GL11;

import connor135246.simplyhotsprings.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSteam extends Particle
{
    // thanks again vazkii...

    public static int steamCount = 0;
    public static final int TEXTURE_COUNT = 12;
    public static final ResourceLocation TEXTURES = new ResourceLocation(Reference.MODID + ":" + "textures/particles/big_steams.png");

    private static final ArrayDeque<ParticleSteam> queuedRenders = new ArrayDeque<ParticleSteam>();

    protected float partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ;
    protected final int texIndex;

    public ParticleSteam(World world, double xCoord, double yCoord, double zCoord)
    {
        super(world, xCoord, yCoord, zCoord);

        this.multipleParticleScaleBy(2.5F + rand.nextFloat() * 1.0F);
        this.setSize(0.25F, 0.25F);

        this.motionX = (double) (this.rand.nextFloat() / 250.0F * (float) (this.rand.nextBoolean() ? 1 : -1));
        this.motionZ = (double) (this.rand.nextFloat() / 250.0F * (float) (this.rand.nextBoolean() ? 1 : -1));
        this.motionY = 0.025 + (double) (this.rand.nextFloat() / 250.0F);
        this.particleGravity = 0.0005F;

        this.particleMaxAge = 100;

        this.texIndex = rand.nextInt(12);
        this.setParticleTextureIndex(this.texIndex);
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ < this.particleMaxAge && this.particleAlpha > 0.02F)
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
    public void setParticleTextureIndex(int texIndex)
    {
        this.particleTextureIndexX = texIndex / 8;
        this.particleTextureIndexY = texIndex % 8;
    }

    /**
     * Saves rendering variables and queues a particle for rendering later.
     */
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ)
    {
        this.partialTicks = partialTicks;
        this.rotX = rotX;
        this.rotZ = rotZ;
        this.rotYZ = rotYZ;
        this.rotXY = rotXY;
        this.rotXZ = rotXZ;
        queuedRenders.add(this);
    }

    /**
     * Goes through each queue of particles, setting the texture and rendering them.
     */
    public static void dispatchQueuedRenders(Tessellator tess)
    {
        steamCount = 0;

        GlStateManager.pushMatrix();

        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURES);

        tess.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

        ParticleSteam steam;
        while (steamCount < 250 && (steam = queuedRenders.poll()) != null)
            steam.renderQueued(tess);

        tess.draw();

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GlStateManager.popMatrix();
    }

    /**
     * Actually renders the particle.
     */
    public void renderQueued(Tessellator tess)
    {
        ++steamCount;

        float scale = 0.2F * this.particleScale;
        float partialPosX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float partialPosY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float partialPosZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

        double minU = this.particleTextureIndexX * 0.5;
        double maxU = minU + 0.5;
        double minV = this.particleTextureIndexY * 0.125;
        double maxV = minV + 0.125;

        int bright = this.getBrightnessForRender(partialTicks);
        int brightU = bright >> 16 & 65535;
        int brightV = bright & 65535;

        BufferBuilder buffer = tess.getBuffer();

        buffer.pos(partialPosX - rotX * scale - rotXY * scale, partialPosY - rotZ * scale, partialPosZ - rotYZ * scale - rotXZ * scale).tex(maxU, maxV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightU, brightV).endVertex();
        buffer.pos(partialPosX - rotX * scale + rotXY * scale, partialPosY + rotZ * scale, partialPosZ - rotYZ * scale + rotXZ * scale).tex(maxU, minV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightU, brightV).endVertex();
        buffer.pos(partialPosX + rotX * scale + rotXY * scale, partialPosY + rotZ * scale, partialPosZ + rotYZ * scale + rotXZ * scale).tex(minU, minV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightU, brightV).endVertex();
        buffer.pos(partialPosX + rotX * scale - rotXY * scale, partialPosY - rotZ * scale, partialPosZ + rotYZ * scale - rotXZ * scale).tex(minU, maxV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightU, brightV).endVertex();
    }

}
