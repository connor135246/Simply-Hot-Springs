package connor135246.simplyhotsprings.client.particles;

import java.util.ArrayDeque;

import connor135246.simplyhotsprings.client.ClientProxy;
import connor135246.simplyhotsprings.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSteam extends Particle
{
    // thanks again vazkii...

    private int texIndex = rand.nextInt(12);
    private float partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ;

    @SuppressWarnings("unchecked")
    public static ArrayDeque<ParticleSteam>[] queuedRenders = new ArrayDeque[] {
            new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(),
            new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(),
            new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(),
            new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>(), new ArrayDeque<ParticleSteam>() };

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

    /**
     * Saves rendering variables and queues a particle for rendering later.
     */
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY,
            float rotationXZ)
    {
        this.partialTicks = partialTicks;
        this.rotationX = rotationX;
        this.rotationZ = rotationZ;
        this.rotationYZ = rotationYZ;
        this.rotationXY = rotationXY;
        this.rotationXZ = rotationXZ;
        queuedRenders[this.texIndex].add(this);
    }

    /**
     * Goes through each queue of particles, setting the texture and rendering them.
     */
    public static void dispatchQueuedRenders(Tessellator tess)
    {
        ClientProxy.steamCount = 0;

        for (int i = 0; i < queuedRenders.length; ++i)
        {
            Minecraft.getMinecraft().renderEngine.bindTexture(getThisTexture(i));
            
            ParticleSteam steam;
            while (ClientProxy.steamCount < 100 && (steam = queuedRenders[i].poll()) != null)
            {
                steam.renderQueued(tess);
            }
        }
    }

    /**
     * Actually renders the particle.
     */
    public void renderQueued(Tessellator tess)
    {
        ++ClientProxy.steamCount;
        
        float f4 = 0.2F * this.particleScale;
        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;

        Vec3d[] avec3d = new Vec3d[] {
                new Vec3d((double) (-rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (-rotationYZ * f4 - rotationXZ * f4)),
                new Vec3d((double) (-rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (-rotationYZ * f4 + rotationXZ * f4)),
                new Vec3d((double) (rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (rotationYZ * f4 + rotationXZ * f4)),
                new Vec3d((double) (rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (rotationYZ * f4 - rotationXZ * f4)) };

        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

        buffer.pos((double) f5 + avec3d[0].x, (double) f6 + avec3d[0].y, (double) f7 + avec3d[0].z).tex(1, 1)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[1].x, (double) f6 + avec3d[1].y, (double) f7 + avec3d[1].z).tex(1, 0)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[2].x, (double) f6 + avec3d[2].y, (double) f7 + avec3d[2].z).tex(0, 0)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[3].x, (double) f6 + avec3d[3].y, (double) f7 + avec3d[3].z).tex(0, 1)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();

        tess.draw();
    }

    /**
     * Gets the particle texture. index should be from 0 to 11.
     */
    public static ResourceLocation getThisTexture(int index)
    {
        return new ResourceLocation(Reference.MODID, "textures/particles/steam" + index + ".png");
    }

}
