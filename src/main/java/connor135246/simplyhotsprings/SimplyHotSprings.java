package connor135246.simplyhotsprings;

import org.apache.logging.log4j.Logger;

import connor135246.simplyhotsprings.common.CommonProxy;
import connor135246.simplyhotsprings.util.Reference;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, dependencies = "before:biomesoplenty;")
public class SimplyHotSprings
{

    @Instance
    public static SimplyHotSprings instance = new SimplyHotSprings();
    @SidedProxy(clientSide = "connor135246.simplyhotsprings.client.ClientProxy", serverSide = "connor135246.simplyhotsprings.common.CommonProxy")
    public static CommonProxy proxy;

    public static Logger modlog;

    static
    {
        FluidRegistry.enableUniversalBucket();
    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        modlog = event.getModLog();

        proxy.preInit(event);
    }

    @EventHandler
    public static void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }

    @EventHandler
    public static void loadComplete(FMLLoadCompleteEvent event)
    {
        proxy.loadComplete(event);
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        proxy.serverAboutToStart(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        proxy.serverStarting(event);
    }

}
