package connor135246.simplyhotsprings.client;

import connor135246.simplyhotsprings.client.particles.ParticleSteam;
import connor135246.simplyhotsprings.common.CommonProxy;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.common.fluids.FluidHotSpringWater;
import connor135246.simplyhotsprings.util.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
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

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("simply_hot_springs_steam_particles");
        ParticleSteam.dispatchQueuedRenders(Tessellator.getInstance());
        profiler.endSection();
    }

}
