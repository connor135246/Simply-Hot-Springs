package connor135246.simplyhotsprings.util;

import java.util.Set;

import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MODID)
@EventBusSubscriber
public class SimplyHotSpringsConfig
{

    @Name("Creates Source Blocks")
    @Comment({ "If true, Hot Spring Water makes infinite water sources, like vanilla water does." })
    public static boolean createsSources = true;

    @Name("Potion Effect")
    @Comment({ "The namespaced effect that Hot Spring Water applies to entities. If the given effect isn't found (or left blank), no effect is applied." })
    public static String potionEffect = "minecraft:regeneration";

    @Name("Potion Effect Settings")
    @Comment({ "The first value is the duration of the effect (in ticks). The second value is the amplifier of the effect (0 is level 1)." })
    @RangeInt(min = 0)
    public static int[] potionEffectSettings = { 50, 0 };

    @RequiresWorldRestart
    public static WorldGen worldgen;

    public static class WorldGen
    {
        @Name("#How to fill Whitelists and Blacklists")
        @Comment({ "The command \"/simplyhotsprings locationinfo\" will tell you your current dimension ID as well as "
                + "the name and types of the biome you're currently in. Use those results to fill in your whitelists and blacklists. "
                + "It will also tell you whether or not a hot spring can generate there, so you can use it for testing your whitelists and blacklists. "
                + "(By the way, this config setting does nothing, so put whatever you want in here.)",
                "Full list of biome types: HOT, COLD, SPARSE, DENSE, WET, DRY, SAVANNA, CONIFEROUS, JUNGLE, SPOOKY, DEAD, LUSH, NETHER, END, MUSHROOM, "
                        + "MAGICAL, RARE, OCEAN, RIVER, WATER, MESA, FOREST, PLAINS, MOUNTAIN, HILLS, SWAMP, SANDY, SNOWY, WASTELAND, BEACH, VOID" })
        public static String[] info = {};

        @Name("World Generation")
        @Comment({ "Set to false to stop hot springs from generating." })
        public static boolean worldGen = true;

        @Name("Generate in Biomes O' Plenty World Type")
        @Comment({ "If the world type is Biomes O' Plenty, these hot springs won't generate. Set this to true to make them generate anyway." })
        public static boolean worldGenIfBOP = false;

        @Name("Generation Chance")
        @Comment({ "The chance for a Hot Spring to generate. Lower values are more likely." })
        @RangeInt(min = 1)
        public static int chance = 275;

        @Name("Generation Debug")
        @Comment({ "When a hot spring generates, prints the location to console." })
        public static boolean debug = false;

        @Name("Dimension Whitelist")
        @Comment({ "The dimension IDs where Hot Springs are allowed to generate. If empty, all dimensions are allowed." })
        public static int[] dimWhitelist = { 0 };

        @Name("Dimension Blacklist")
        @Comment({ "The dimension IDs where Hot Springs are NOT allowed to generate." })
        public static int[] dimBlacklist = {};

        @Name("Biome Type Whitelist")
        @Comment({ "The biome types where Hot Springs are allowed to generate. If empty, all biome types are allowed. ",
                "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied." })
        public static String[] biomeTypeWhitelist = {};

        @Name("Biome Type Blacklist")
        @Comment({ "The biome types where Hot Springs are NOT allowed to generate." })
        public static String[] biomeTypeBlacklist = { "DRY", "SAVANNA", "NETHER", "END", "OCEAN", "RIVER", "SANDY", "BEACH", "VOID" };

        @Name("Biome Name Whitelist")
        @Comment({ "The biome names where Hot Springs are allowed to generate. If empty, all biomes are allowed. ",
                "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied." })
        public static String[] biomeNameWhitelist = {};

        @Name("Biome Name Blacklist")
        @Comment({ "The biome names where Hot Springs are NOT allowed to generate. " })
        public static String[] biomeNameBlacklist = {};

        public static boolean canGenerateInGeneral(World world)
        {
            return worldGen && (world.getWorldType().getName().equals("BIOMESOP") ? worldGenIfBOP : true) && world.getWorldType() != WorldType.FLAT
                    && !arrayContains(dimBlacklist, world.provider.getDimension())
                    && (dimWhitelist.length == 0 || arrayContains(dimWhitelist, world.provider.getDimension()));
        }

        /**
         * doesn't check if it {@link #canGenerateInGeneral(World)}
         */
        public static boolean canGenerateAtPosition(World world, BlockPos pos)
        {
            Biome biome = world.getBiomeForCoordsBody(pos);

            if (arrayContains(biomeNameWhitelist, biome.getRegistryName().toString()))
                return true;
            if (arrayContains(biomeNameBlacklist, biome.getRegistryName().toString()))
                return false;

            if (arrayContains(biomeTypeBlacklist, BiomeDictionary.getTypes(biome)))
                return false;
            if (arrayContains(biomeTypeWhitelist, BiomeDictionary.getTypes(biome)))
                return true;

            if (biomeNameWhitelist.length == 0 || biomeTypeWhitelist.length == 0)
                return true;

            return false;
        }

        /**
         * @return an explanation of why the hot spring did or did not generate.
         */
        public static String generateReason(World world, BlockPos pos)
        {
            if (!worldGen)
                return TextFormatting.DARK_RED + "\"World Generation\" is false.";
            if (world.getWorldType().getName().equals("BIOMESOP") && !worldGenIfBOP)
                return TextFormatting.DARK_RED + "This world type is Biomes O' Plenty, and \"Generate in Biomes O' Plenty World Type\" is false.";
            if (world.getWorldType() == WorldType.FLAT)
                return TextFormatting.DARK_RED + "Hot Springs don't generate in Superflat worlds.";
            if (arrayContains(dimBlacklist, world.provider.getDimension()))
                return TextFormatting.DARK_RED + "This dimension is in the \"Dimension Blacklist\".";
            if (dimWhitelist.length != 0 && !arrayContains(dimWhitelist, world.provider.getDimension()))
                return TextFormatting.DARK_RED + "This dimension isn't in the \"Dimension Whitelist\".";

            Biome biome = world.getBiomeForCoordsBody(pos);

            if (arrayContains(biomeNameWhitelist, biome.getRegistryName().toString()))
                return TextFormatting.GREEN + "This biome is in the \"Biome Name Whitelist\".";
            if (arrayContains(biomeNameBlacklist, biome.getRegistryName().toString()))
                return TextFormatting.DARK_RED + "This biome is in the \"Biome Name Blacklist\".";

            if (arrayContains(biomeTypeBlacklist, BiomeDictionary.getTypes(biome)))
                return TextFormatting.DARK_RED + "This biome has a type in the \"Biome Type Blacklist\".";
            if (arrayContains(biomeTypeWhitelist, BiomeDictionary.getTypes(biome)))
                return TextFormatting.GREEN + "This biome has a type in the \"Biome Type Whitelist\".";

            if (biomeNameWhitelist.length == 0 || biomeTypeWhitelist.length == 0)
                return TextFormatting.GREEN + "This biome wasn't excluded by any blacklists.";

            return TextFormatting.DARK_RED + "This biome was excluded by whitelists.";
        }

        private static boolean arrayContains(int[] array, int value)
        {
            for (int element : array)
                if (element == value)
                    return true;
            return false;
        }

        private static boolean arrayContains(String[] array, String value)
        {
            for (String element : array)
                if (element.equalsIgnoreCase(value))
                    return true;
            return false;
        }

        private static boolean arrayContains(String[] array, Set<BiomeDictionary.Type> values)
        {
            for (String element : array)
                for (BiomeDictionary.Type value : values)
                    if (element.equalsIgnoreCase(value.getName()))
                        return true;
            return false;
        }
    }

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
