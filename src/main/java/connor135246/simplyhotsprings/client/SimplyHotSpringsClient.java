package connor135246.simplyhotsprings.client;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.*;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.client.particles.HotSpringWaterDripParticle;
import connor135246.simplyhotsprings.client.particles.HotSpringWaterUnderwaterParticle;
import connor135246.simplyhotsprings.client.particles.SmallSteamParticle;
import connor135246.simplyhotsprings.client.particles.SteamParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = SimplyHotSprings.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class SimplyHotSpringsClient
{

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            HOT_SPRING_WATER.ifPresent(render -> RenderTypeLookup.setRenderLayer(render, RenderType.getTranslucent()));
            FLOWING_HOT_SPRING_WATER.ifPresent(render -> RenderTypeLookup.setRenderLayer(render, RenderType.getTranslucent()));
            HOT_SPRING_WATER_BLOCK.ifPresent(render -> RenderTypeLookup.setRenderLayer(render, RenderType.getTranslucent()));
        });
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        HOT_SPRING_WATER_STEAM.ifPresent(type -> mc.particles.registerFactory(type, SteamParticle.Factory::new));
        HOT_SPRING_WATER_STEAM_SMALL.ifPresent(type -> mc.particles.registerFactory(type, SmallSteamParticle.Factory::new));
        DRIPPING_HOT_SPRING_WATER.ifPresent(type -> mc.particles.registerFactory(type, HotSpringWaterDripParticle.DrippingFactory::new));
        FALLING_HOT_SPRING_WATER.ifPresent(type -> mc.particles.registerFactory(type, HotSpringWaterDripParticle.FallingFactory::new));
        SPLASHING_HOT_SPRING_WATER.ifPresent(type -> mc.particles.registerFactory(type, HotSpringWaterDripParticle.SplashingFactory::new));
        HOT_SPRING_WATER_UNDERWATER.ifPresent(type -> mc.particles.registerFactory(type, HotSpringWaterUnderwaterParticle.Factory::new));
    }

}
