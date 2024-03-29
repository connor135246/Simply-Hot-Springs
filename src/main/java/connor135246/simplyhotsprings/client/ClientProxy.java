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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT, modid = Reference.MODID)
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

    @SubscribeEvent
    public static void onSetFogDensity(EntityViewRenderEvent.FogDensity event)
    {
        if (event.getState().getBlock() == BlockHotSpringWater.BLOCK_INSTANCE)
        {
            GlStateManager.setFog(GlStateManager.FogMode.EXP);

            if (event.getEntity() instanceof EntityLivingBase)
            {
                EntityLivingBase entityLivingBase = (EntityLivingBase) event.getEntity();
                if (entityLivingBase.isPotionActive(MobEffects.WATER_BREATHING))
                    event.setDensity(0.025F);
                else
                    event.setDensity(0.04F - EnchantmentHelper.getRespirationModifier(entityLivingBase) * 0.005F);
            }
            else
                event.setDensity(0.04F);

            event.setCanceled(true);
        }
    }

}
