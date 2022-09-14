package connor135246.simplyhotsprings.client;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.*;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.client.particles.HotSpringWaterDripParticle;
import connor135246.simplyhotsprings.client.particles.HotSpringWaterUnderwaterParticle;
import connor135246.simplyhotsprings.client.particles.SmallSteamParticle;
import connor135246.simplyhotsprings.client.particles.SteamParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
            HOT_SPRING_WATER.ifPresent(render -> ItemBlockRenderTypes.setRenderLayer(render, RenderType.translucent()));
            FLOWING_HOT_SPRING_WATER.ifPresent(render -> ItemBlockRenderTypes.setRenderLayer(render, RenderType.translucent()));
            HOT_SPRING_WATER_BLOCK.ifPresent(render -> ItemBlockRenderTypes.setRenderLayer(render, RenderType.translucent()));
        });
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        HOT_SPRING_WATER_STEAM.ifPresent(type -> mc.particleEngine.register(type, SteamParticle.Provider::new));
        HOT_SPRING_WATER_STEAM_SMALL.ifPresent(type -> mc.particleEngine.register(type, SmallSteamParticle.Provider::new));
        DRIPPING_HOT_SPRING_WATER.ifPresent(type -> mc.particleEngine.register(type, HotSpringWaterDripParticle.DrippingProvider::new));
        FALLING_HOT_SPRING_WATER.ifPresent(type -> mc.particleEngine.register(type, HotSpringWaterDripParticle.FallingProvider::new));
        SPLASHING_HOT_SPRING_WATER.ifPresent(type -> mc.particleEngine.register(type, HotSpringWaterDripParticle.SplashingProvider::new));
        HOT_SPRING_WATER_UNDERWATER.ifPresent(type -> mc.particleEngine.register(type, HotSpringWaterUnderwaterParticle.Provider::new));
    }

}
