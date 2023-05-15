package connor135246.simplyhotsprings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SimplyHotSpringsConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SimplyHotSpringsConfig.clientSpec);
    }

    // what if there was a hot spring water bottle?

    // TODO ISSUES WITH FORGE'S FLUIDS
    // if our fluid is NOT in the #water tag:
    // - fishing bobbers will sink to the bottom
    // - lava flowing on top doesn't make stone
    // - dirt below it can have grass grow on it if the fluid level is below 8 (non-full block)
    // - respawn anchors will damage blocks even if they have the fluid next to it
    // being in the #water tag doesn't fix these:
    // - squids, etc suffocate in it. blazes and endermen don't take damage in it. (#9154)
    // - undead mobs will drown in it
    // - witches don't drink water breathing in it
    // - conduits don't give conduit power while in the fluid; turtle helmet doesn't count down water breathing
    //   - related: air bar doesn't appear in the fluid if your air bar is full (such as due to conduit power or turtle helmet) even though the fluid can drown you (inconsistent with water)
    // - riptide doesn't work in it
    // - dig speed isn't reduced
    // - waterVisionTime does not advance
    // - no underwater ambience or music
    // other issues:
    // - physics applied to your movement are twice as strong as they should be (#8897)

}
