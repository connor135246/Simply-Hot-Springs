package connor135246.simplyhotsprings.util;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.command.ConfigCommand;

@EventBusSubscriber(modid = SimplyHotSprings.MODID)
public class SimplyHotSpringsEventHandler
{

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event)
    {
        SimplyHotSpringsCommand.register(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event)
    {
        SimplyHotSpringsConfig.finalizeSpringsGeneration(event);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        SimplyHotSpringsConfig.resetSpringsGeneration(event);
    }

}
