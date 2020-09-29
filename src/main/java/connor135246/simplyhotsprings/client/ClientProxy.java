package connor135246.simplyhotsprings.client;

import org.lwjgl.opengl.GL11;

import connor135246.simplyhotsprings.client.particles.ParticleSteam;
import connor135246.simplyhotsprings.common.CommonProxy;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.common.fluids.FluidHotSpringWater;
import connor135246.simplyhotsprings.util.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class ClientProxy extends CommonProxy
{

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomStateMapper(BlockHotSpringWater.BLOCK_INSTANCE, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState istate)
            {
                return new ModelResourceLocation(Reference.MODID + ":" + FluidHotSpringWater.FLUID_NAME, FluidHotSpringWater.FLUID_NAME);
            }
        });

        ModelLoader.setCustomModelResourceLocation(BlockHotSpringWater.ITEMBLOCK_INSTANCE, 0,
                new ModelResourceLocation(BlockHotSpringWater.ITEMBLOCK_INSTANCE.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event)
    {
        // thanks again vazkii...

        GlStateManager.pushMatrix();

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;

        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

        profiler.startSection("simply_steam_particles");
        ParticleSteam.dispatchQueuedRenders(Tessellator.getInstance());
        profiler.endSection();

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

}
