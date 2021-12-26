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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Config;
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
    @LangKey(LANG_CONFIG + "createsSources")
    public static boolean createsSources = true;

    @Name("Potion Effect")
    @LangKey(LANG_CONFIG + "potionEffect")
    public static String potionEffect = "minecraft:regeneration";

    @Name("Potion Effect Settings")
    @RangeInt(min = 0)
    @LangKey(LANG_CONFIG + "potionEffectSettings")
    public static int[] potionEffectSettings = { 50, 0 };

    @LangKey(LANG_CONFIG + "worldgen")
    public static WorldGen worldgen;

    public static class WorldGen
    {
        @Ignore
        public static final String LANG_CONFIG_WORLDGEN = LANG_CONFIG + "worldgen.";

        @Name("#How to fill Whitelists and Blacklists")
        @LangKey(LANG_CONFIG_WORLDGEN + "info")
        public static String[] info = { "HOT", "COLD", "SPARSE", "DENSE", "WET", "DRY", "SAVANNA", "CONIFEROUS", "JUNGLE", "SPOOKY", "DEAD", "LUSH", "NETHER",
                "END", "MUSHROOM", "MAGICAL", "RARE", "OCEAN", "RIVER", "WATER", "MESA", "FOREST", "PLAINS", "MOUNTAIN", "HILLS", "SWAMP", "SANDY", "SNOWY",
                "WASTELAND", "BEACH", "VOID", "HOT, COLD, SPARSE, DENSE, WET, DRY, SAVANNA, CONIFEROUS, JUNGLE, SPOOKY, DEAD, LUSH, NETHER, END, MUSHROOM, "
                        + "MAGICAL, RARE, OCEAN, RIVER, WATER, MESA, FOREST, PLAINS, MOUNTAIN, HILLS, SWAMP, SANDY, SNOWY, WASTELAND, BEACH, VOID" };

        @RequiresWorldRestart
        @Name("World Generation")
        @LangKey(LANG_CONFIG_WORLDGEN + "worldGen")
        public static boolean worldGen = true;

        // changed config option
        @Ignore
        public static final String oldBOPGenConfigName = "Generate in Biomes O' Plenty World Type";
        @Ignore
        public static final String newBOPGenConfigName = "Generate Alongside Biomes O' Plenty Hot Springs";

        @RequiresWorldRestart
        @Name(newBOPGenConfigName)
        @LangKey(LANG_CONFIG_WORLDGEN + "worldGenIfBOPSprings")
        public static boolean worldGenIfBOPSprings = false;

        @RequiresWorldRestart
        @Name("Generate in Superflat World Type")
        @LangKey(LANG_CONFIG_WORLDGEN + "worldGenIfSuperflat")
        public static boolean worldGenIfSuperflat = false;

        @RequiresWorldRestart
        @Name("Generation Chance")
        @RangeInt(min = 1)
        @LangKey(LANG_CONFIG_WORLDGEN + "chance")
        public static int chance = 275;

        @Name("Generation Debug")
        @LangKey(LANG_CONFIG_WORLDGEN + "debug")
        public static boolean debug = false;

        @RequiresWorldRestart
        @Name("Dimension Whitelist")
        @LangKey(LANG_CONFIG_WORLDGEN + "dimWhitelist")
        public static int[] dimWhitelist = { 0 };

        @RequiresWorldRestart
        @Name("Dimension Blacklist")
        @LangKey(LANG_CONFIG_WORLDGEN + "dimBlacklist")
        public static int[] dimBlacklist = {};

        @RequiresWorldRestart
        @Name("Biome Type Whitelist")
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeTypeWhitelist")
        public static String[] biomeTypeWhitelist = {};

        @RequiresWorldRestart
        @Name("Biome Type Blacklist")
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeTypeBlacklist")
        public static String[] biomeTypeBlacklist = { "DRY", "SAVANNA", "NETHER", "END", "OCEAN", "RIVER", "SANDY", "BEACH", "VOID" };

        @RequiresWorldRestart
        @Name("Biome Name Whitelist")
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeNameWhitelist")
        public static String[] biomeNameWhitelist = {};

        @RequiresWorldRestart
        @Name("Biome Name Blacklist")
        @LangKey(LANG_CONFIG_WORLDGEN + "biomeNameBlacklist")
        public static String[] biomeNameBlacklist = {};

        public static boolean canGenerateInGeneral(World world)
        {
            return worldGen
                    && (worldGenIfBOPSprings ? true : !areBOPHotSpringsEnabled(world))
                    && (worldGenIfSuperflat ? true : world.getWorldType() != WorldType.FLAT)
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
                return translateGenReason("no_world_gen", false);
            if (!worldGenIfBOPSprings && areBOPHotSpringsEnabled(world))
                return translateGenReason("bop_springs", false);
            if (!worldGenIfSuperflat && world.getWorldType() == WorldType.FLAT)
                return translateGenReason("superflat", false);
            if (arrayContains(dimBlacklist, world.provider.getDimension()))
                return translateGenReason("dim_blacklist", false);
            if (dimWhitelist.length != 0 && !arrayContains(dimWhitelist, world.provider.getDimension()))
                return translateGenReason("dim_whitelist", false);

            Biome biome = world.getBiomeForCoordsBody(pos);

            if (arrayContains(biomeNameWhitelist, biome.getRegistryName().toString()))
                return translateGenReason("biome_name_whitelist", true);
            if (arrayContains(biomeNameBlacklist, biome.getRegistryName().toString()))
                return translateGenReason("biome_name_blacklist", false);

            if (arrayContains(biomeTypeBlacklist, BiomeDictionary.getTypes(biome)))
                return translateGenReason("biome_type_blacklist", false);
            if (arrayContains(biomeTypeWhitelist, BiomeDictionary.getTypes(biome)))
                return translateGenReason("biome_type_whitelist", true);

            if (biomeNameWhitelist.length == 0 || biomeTypeWhitelist.length == 0)
                return translateGenReason("not_blacklisted", true);

            return translateGenReason("not_whitelisted", false);
        }

        private static String translateGenReason(String key, boolean yes)
        {
            return (yes ? TextFormatting.GREEN : TextFormatting.DARK_RED) + I18n.translateToLocal(Reference.MODID + ".gen.reason." + key);
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
