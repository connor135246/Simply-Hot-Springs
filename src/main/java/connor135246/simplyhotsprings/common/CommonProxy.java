package connor135246.simplyhotsprings.common;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.common.fluids.FluidHotSpringWater;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class CommonProxy
{

    /** the fluid that will be used to register the block. usually is just our fluid, but if another mod has already registered hot_spring_water, it will be that fluid instead. */
    public static Fluid fluidToUse = FluidHotSpringWater.FLUID_INSTANCE;

    public void preInit(FMLPreInitializationEvent event)
    {
        if (!FluidRegistry.registerFluid(FluidHotSpringWater.FLUID_INSTANCE))
        {
            SimplyHotSprings.modlog.warn("Another mod has already registered the fluid \"hot_spring_water\". Their fluid will be used instead. FML will show you an warning message now.");
            fluidToUse = FluidRegistry.getFluid(FluidHotSpringWater.FLUID_NAME);
        }

        FluidRegistry.addBucketForFluid(FluidHotSpringWater.FLUID_INSTANCE);
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

    public void serverLoad(FMLServerStartingEvent event)
    {
        BlockHotSpringWater.updateConfigSettings();
    }

}
