package connor135246.simplyhotsprings.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
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
    public static void onServerAboutToStart(FMLServerAboutToStartEvent event)
    {
        SimplyHotSpringsConfig.finalizeSpringsGeneration(event);
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event)
    {
        SimplyHotSpringsConfig.resetSpringsGeneration(event);
    }

    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onSetFogDensity(EntityViewRenderEvent.FogDensity event)
    {
        if (event.getInfo().getFluidState().isTagged(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
        {
            float fogDensity = 0.04F;
            if (event.getInfo().getRenderViewEntity() instanceof ClientPlayerEntity)
            {
                ClientPlayerEntity player = (ClientPlayerEntity) event.getInfo().getRenderViewEntity();
                fogDensity -= player.getWaterBrightness() * player.getWaterBrightness() * 0.02F;
            }

            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);

            event.setDensity(fogDensity);
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onSetFogColors(EntityViewRenderEvent.FogColors event)
    {
        if (event.getInfo().getFluidState().isTagged(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
        {
            event.setGreen(event.getBlue());
            event.setRed(event.getRed() * 0.01F);
        }
    }

}
