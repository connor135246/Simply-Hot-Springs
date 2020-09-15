package connor135246.simplyhotsprings.util;

import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MODID)
@EventBusSubscriber
public class SimplyHotSpringsConfig
{

    @Name(value = "Creates Source Blocks")
    @Comment(value = { "If true, Hot Spring Water makes infinite water sources, like vanilla water does." })
    public static boolean createsSources = true;

    @Name(value = "Potion Effect")
    @Comment(value = {
            "The namespaced effect that Hot Spring Water applies to entities. If the given effect isn't found (or left blank), no effect is applied." })
    public static String potionEffect = "minecraft:regeneration";

    @Name(value = "Potion Effect Settings")
    @Comment(value = { "The first value is the duration of the effect (in ticks). The second value is the amplifier of the effect (0 is level 1)." })
    @RangeInt(min = 0)
    public static int[] potionEffectSettings = { 50, 0 };

    // @Name(value = "World Generation")
    // @Comment(value = { "Set to false to stop Hot Springs from generating." })
    // @RequiresWorldRestart
    // public static boolean worldGen = true;

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Reference.MODID))
        {
            ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);

            BlockHotSpringWater.updateConfigSettings();
        }
    }

}
