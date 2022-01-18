package connor135246.simplyhotsprings.util;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MODID)
@EventBusSubscriber
public class SimplyHotSpringsConfig
{

    @Ignore
    public static final String LANG_CONFIG = Reference.MODID + ".config.";

    @Name("Creates Source Blocks")
    @Comment({ "If true, Hot Spring Water makes infinite water sources, like vanilla water does." })
    @LangKey(LANG_CONFIG + "createsSources")
    public static boolean createsSources = true;

    @Name("Potion Effect")
    @Comment({ "The namespaced effect that Hot Spring Water applies to entities. If the given effect isn't found (or left blank), no effect is applied." })
    @LangKey(LANG_CONFIG + "potionEffect")
    public static String potionEffect = "minecraft:regeneration";

    @Name("Potion Effect Settings")
    @Comment({ "The first value is the duration of the effect (in ticks). The second value is the amplifier of the effect (0 is level 1)." })
    @RangeInt(min = 0)
    @LangKey(LANG_CONFIG + "potionEffectSettings")
    public static int[] potionEffectSettings = { 50, 0 };

    @Comment({ "Most of these settings can't be changed while a world is open." })
    @LangKey(LANG_CONFIG + "worldgen")
    public static WorldGen worldgen;

    public static class WorldGen
    {
        @Ignore
        public static final String LANG_CONFIG_WORLDGEN = LANG_CONFIG + "worldgen.";

        @Name("#How to fill Whitelists and Blacklists")
        @Comment({ "The command \"/simplyhotsprings locationinfo\" will tell you your current dimension ID as well as "
                + "the name and types of the biome you're currently in. Use those results to fill in your whitelists and blacklists. "
                + "It will also tell you whether or not a hot spring can generate there, so you can use it for testing your whitelists and blacklists.",
                "(This config setting does nothing other than list all biome types for your convenience, so put whatever you want in here.)" })
        @LangKey(LANG_CONFIG_WORLDGEN + "info")
        public static String[] info = { "HOT", "COLD", "SPARSE", "DENSE", "WET", "DRY", "SAVANNA", "CONIFEROUS", "JUNGLE", "SPOOKY", "DEAD", "LUSH", "NETHER",
                "END", "MUSHROOM", "MAGICAL", "RARE", "OCEAN", "RIVER", "WATER", "MESA", "FOREST", "PLAINS", "MOUNTAIN", "HILLS", "SWAMP", "SANDY", "SNOWY",
                "WASTELAND", "BEACH", "VOID", "HOT, COLD, SPARSE, DENSE, WET, DRY, SAVANNA, CONIFEROUS, JUNGLE, SPOOKY, DEAD, LUSH, NETHER, END, MUSHROOM, "
                        + "MAGICAL, RARE, OCEAN, RIVER, WATER, MESA, FOREST, PLAINS, MOUNTAIN, HILLS, SWAMP, SANDY, SNOWY, WASTELAND, BEACH, VOID" };

        @RequiresWorldRestart
        @Name("World Generation")
        @Comment({ "Set to false to stop hot springs from generating." })
        @LangKey(LANG_CONFIG_WORLDGEN + "worldGen")
        public static boolean worldGen = true;

        // changed config option
        @Ignore
        public static final String oldBOPGenConfigName = "Generate in Biomes O' Plenty World Type";
        @Ignore
        public static final String newBOPGenConfigName = "Generate Alongside Biomes O' Plenty Hot Springs";

        @RequiresWorldRestart
        @Name(newBOPGenConfigName)
        @Comment({ "If a world has Biomes O' Plenty hot springs enabled, the hot springs from this mod won't generate. "
                + "Set this to true to make them generate anyway." })
        @LangKey(LANG_CONFIG_WORLDGEN + "worldGenIfBOPSprings")
        public static boolean worldGenIfBOPSprings = false;

        @RequiresWorldRestart
        @Name("Generate in Superflat World Type")
        @Comment({ "If the world type is Superflat, the hot springs from this mod won't generate. "
                + "Set this to true to make them generate anyway." })
        @LangKey(LANG_CONFIG_WORLDGEN + "worldGenIfSuperflat")
        public static boolean worldGenIfSuperflat = false;

        @RequiresWorldRestart
        @Name("Generation Chance")
        @Comment({ "The chance for a hot spring to generate is 1 over this value. So lower values are more likely." })
        @RangeInt(min = 1)
        @LangKey(LANG_CONFIG_WORLDGEN + "chance")
        public static int chance = 275;

        @Name("Generation Debug")
        @Comment({ "When a hot spring generates, prints the location to console." })
        @LangKey(LANG_CONFIG_WORLDGEN + "debug")
        public static boolean debug = false;

        @RequiresWorldRestart
        @Name("Dimension Whitelist")
        @Comment({ "The dimension IDs where hot springs are allowed to generate. If empty, all dimensions are allowed." })
        @LangKey(LANG_CONFIG_WORLDGEN + "dimWhitelist")
        public static int[] dimWhitelist = { 0 };

        @RequiresWorldRestart
        @Name("Dimension Blacklist")
        @Comment({ "The dimension IDs where hot springs are NOT allowed to generate." })
        @LangKey(LANG_CONFIG_WORLDGEN + "dimBlacklist")
        public static int[] dimBlacklist = {};

        @RequiresWorldRestart
        @Name("Biome Type Whitelist")
        @Comment({ "The biome types where hot springs are allowed to generate. If empty, all biome types are allowed. ",
                "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied." })
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeTypeWhitelist")
        public static String[] biomeTypeWhitelist = {};

        @RequiresWorldRestart
        @Name("Biome Type Blacklist")
        @Comment({ "The biome types where hot springs are NOT allowed to generate." })
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeTypeBlacklist")
        public static String[] biomeTypeBlacklist = { "DRY", "SAVANNA", "NETHER", "END", "OCEAN", "RIVER", "SANDY", "BEACH", "VOID" };

        @RequiresWorldRestart
        @Name("Biome Name Whitelist")
        @Comment({ "The biome names where hot springs are allowed to generate. If empty, all biomes are allowed. ",
                "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied." })
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeNameWhitelist")
        public static String[] biomeNameWhitelist = {};

        @RequiresWorldRestart
        @Name("Biome Name Blacklist")
        @Comment({ "The biome names where hot springs are NOT allowed to generate." })
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeNameBlacklist")
        public static String[] biomeNameBlacklist = {};

        /**
         * @return a real GenerationReason if not allowed, or {@link GenerationReason#ALLOW_WORLD} if you can pass on to checking {@link #generationReasonBiome(Biome)}
         */
        public static GenerationReason generationReasonWorld(World world)
        {
            if (!worldGen)
                return GenerationReason.NO_WORLD_GEN;
            if (!worldGenIfBOPSprings && areBOPHotSpringsEnabled(world))
                return GenerationReason.BOP_SPRINGS;
            if (!worldGenIfSuperflat && world.getWorldType() == WorldType.FLAT)
                return GenerationReason.SUPERFLAT;

            if (arrayContains(dimWhitelist, world.provider.getDimension()))
                return GenerationReason.ALLOW_WORLD;
            if (arrayContains(dimBlacklist, world.provider.getDimension()))
                return GenerationReason.IN_DIM_BLACKLIST;
            if (dimWhitelist.length != 0)
                return GenerationReason.NOT_DIM_WHITELISTED;

            return GenerationReason.ALLOW_WORLD;
        }

        public static GenerationReason generationReasonBiome(Biome biome)
        {
            if (arrayContains(biomeNameWhitelist, biome.getRegistryName().toString()))
                return GenerationReason.IN_BIOME_NAME_WHITELIST;
            if (arrayContains(biomeNameBlacklist, biome.getRegistryName().toString()))
                return GenerationReason.IN_BIOME_NAME_BLACKLIST;

            if (arrayContains(biomeTypeBlacklist, BiomeDictionary.getTypes(biome)))
                return GenerationReason.IN_BIOME_TYPE_BLACKLIST;
            if (arrayContains(biomeTypeWhitelist, BiomeDictionary.getTypes(biome)))
                return GenerationReason.IN_BIOME_TYPE_WHITELIST;

            if (biomeNameWhitelist.length == 0 || biomeTypeWhitelist.length == 0)
                return GenerationReason.NOT_BIOME_BLACKLISTED;

            return GenerationReason.NOT_BIOME_WHITELISTED;
        }

        /**
         * @return a GenerationReason of why the hot spring can or cannot generate.
         */
        public static GenerationReason getGenerationReason(World world, Biome biome)
        {
            GenerationReason worldReason = generationReasonWorld(world);
            if (worldReason.allowsGeneration())
                return generationReasonBiome(biome);
            else
                return worldReason;
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

        /* parser for BOP world generator options */
        private static final JsonParser jsonParser = new JsonParser();

        /* cache the result of parsing a world's generator options so that we don't have to parse json every time a hot spring tries to generate */
        private static Object2BooleanMap<World> parsedBOPWorlds = new Object2BooleanArrayMap<World>();

        private static boolean areBOPHotSpringsEnabled(World world)
        {
            if (world.getWorldType().getName().equals("BIOMESOP"))
            {
                String genOptions = world.getWorldInfo().getGeneratorOptions();
                if (StringUtils.isNullOrEmpty(genOptions))
                    return true;
                else if (parsedBOPWorlds.containsKey(world))
                    return parsedBOPWorlds.getBoolean(world);
                else
                {
                    try
                    {
                        JsonElement parsed = jsonParser.parse(genOptions);
                        if (parsed.isJsonObject())
                        {
                            boolean generateHotSprings = JsonUtils.getBoolean(parsed.getAsJsonObject(), "generateHotSprings");
                            if (parsedBOPWorlds.size() > 50)
                                parsedBOPWorlds.clear();
                            parsedBOPWorlds.put(world, generateHotSprings);
                            return generateHotSprings;
                        }
                    }
                    catch (JsonParseException excep)
                    {
                        ;
                    }
                }
            }
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

            WorldGen.parsedBOPWorlds.clear();
        }
    }

    /**
     * Removes old config options from previous versions.
     */
    public static void removeOldOptions()
    {
        // why do i have to use reflection to access my own config???? annotation configs were a mistake
        try
        {
            Method getMethod = ConfigManager.class.getDeclaredMethod("getConfiguration", String.class, String.class);
            getMethod.setAccessible(true);
            Configuration config = (Configuration) getMethod.invoke(null, Reference.MODID, "");
            if (config.renameProperty(Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "worldgen",
                    WorldGen.oldBOPGenConfigName, WorldGen.newBOPGenConfigName))
            {
                ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);
            }
        }
        catch (Exception excep)
        {
            ;
        }
    }

}
