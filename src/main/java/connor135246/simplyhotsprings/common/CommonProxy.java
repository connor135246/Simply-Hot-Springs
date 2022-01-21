package connor135246.simplyhotsprings.common;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.common.fluids.FluidHotSpringWater;
import connor135246.simplyhotsprings.common.world.HotSpringsWorldGen;
import connor135246.simplyhotsprings.util.CommandSimplyHotSprings;
import connor135246.simplyhotsprings.util.Reference;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber(modid = Reference.MODID)
public class CommonProxy
{

    /** the fluid that will be used to register the block. usually is just our fluid, but if another mod has already registered hot_spring_water, it will be that fluid instead. */
    public static Fluid fluidToUse = FluidHotSpringWater.FLUID_INSTANCE;

    public void preInit(FMLPreInitializationEvent event)
    {
        if (!FluidRegistry.registerFluid(FluidHotSpringWater.FLUID_INSTANCE))
        {
            SimplyHotSprings.modlog.warn("Another mod has already registered the fluid \"hot_spring_water\". Their fluid will take priority over this one. "
                    + "FML will show you an warning message now.");
            fluidToUse = FluidRegistry.getFluid(FluidHotSpringWater.FLUID_NAME);
        }

        FluidRegistry.addBucketForFluid(FluidHotSpringWater.FLUID_INSTANCE);

        // dependencies = "before:biomesoplenty;"
        if (Loader.isModLoaded("biomesoplenty"))
        {
            SimplyHotSprings.modlog.info("Biomes O' Plenty detected. "
                    + "Reflecting and registering their Hot Spring Water, so that it doesn't cause a crash later.");

            try
            {
                // is this a little janky?
                Fluid bop_fluid = (Fluid) Class.forName("biomesoplenty.common.fluids.HotSpringWaterFluid").getField("instance").get(null);
                Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get("biomesoplenty"));
                FluidRegistry.registerFluid(bop_fluid);
                Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(Reference.MODID));
            }
            catch (Exception excep)
            {
                SimplyHotSprings.modlog.error("An error occurred while performing Biomes O' Plenty integration. Report this bug!");
                SimplyHotSprings.modlog.catching(excep);
            }
        }

        GameRegistry.registerWorldGenerator(HotSpringsWorldGen.INSTANCE, 5);

        SimplyHotSpringsConfig.removeOldOptions();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BlockHotSpringWater.BLOCK_INSTANCE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(BlockHotSpringWater.ITEMBLOCK_INSTANCE);
    }

    public void init(FMLInitializationEvent event)
    {

    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        BlockHotSpringWater.updateConfigSettings();
        SimplyHotSpringsConfig.fillBiomeSets();
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        SimplyHotSpringsConfig.serverAboutToStart(event);
    }

    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(CommandSimplyHotSprings.INSTANCE);
    }

}
