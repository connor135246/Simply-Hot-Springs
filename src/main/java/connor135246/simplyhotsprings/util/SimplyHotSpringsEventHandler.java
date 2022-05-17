package connor135246.simplyhotsprings.util;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoading(BiomeLoadingEvent event)
    {
        SimplyHotSpringsConfig.addSpringsGeneration(event);
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

    @SuppressWarnings({ "deprecation", "removal" })
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onSetFogDensity(EntityViewRenderEvent.FogDensity event)
    {
        if (event.getCamera().getBlockAtCamera().getFluidState().is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
        {

            float fogDensity = 192.0F;
            if (event.getCamera().getEntity() instanceof LocalPlayer)
            {
                LocalPlayer player = (LocalPlayer) event.getCamera().getEntity();
                fogDensity *= 0.15F + Math.max(0.25F, player.getWaterVision()) * 0.85F;
            }

            event.setDensity(fogDensity);
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onSetFogColors(EntityViewRenderEvent.FogColors event)
    {
        if (event.getCamera().getBlockAtCamera().getFluidState().is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
        {
            event.setGreen(event.getBlue());
            event.setRed(event.getRed() * 0.01F);
        }
    }

}
