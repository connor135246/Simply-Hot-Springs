package connor135246.simplyhotsprings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SimplyHotSprings.MODID)
public class SimplyHotSprings
{

    public static final String MODID = "simplyhotsprings";

    public static Logger log = LogManager.getLogger();

    public SimplyHotSprings()
    {
        SimplyHotSpringsCommon.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SimplyHotSpringsConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SimplyHotSpringsConfig.clientSpec);
    }

    /**
     * The method our mixins redirect <b>FluidState.is(FluidTags.WATER)</b> to instead
     */
    public static boolean redirectWaterTag(FluidState fluidstate, TagKey<Fluid> waterTag)
    {
        return fluidstate.is(waterTag) && (waterTag == FluidTags.WATER ? !fluidstate.is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER) : true);
    }

    // TODO things to fix due to using FluidTags.WATER
    // - glass bottles fill with water (also from dispenser) 
    //   - i guess it still makes enough sense to leave it the way it is, but... what if there was a hot spring water bottle?

}
