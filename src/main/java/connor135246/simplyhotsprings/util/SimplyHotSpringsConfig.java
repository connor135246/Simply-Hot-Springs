package connor135246.simplyhotsprings.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.CommonProxy;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.common.world.HotSpringsWorldGen;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
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
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@EventBusSubscriber(modid = Reference.MODID)
public class SimplyHotSpringsConfig
{

    @net.minecraftforge.common.config.Config(modid = Reference.MODID)
    public static class Config
    {
        @Ignore
        public static final String LANG_CONFIG = Reference.MODID + ".config.";

        @Name("Alternate Particles")
        @Comment({ "Client-side option. If true, Hot Spring Water makes smaller, less obtrusive steam particles instead. ",
                "Note: these particles are affected by your particle video setting, and won't appear at all if it's set to Minimal." })
        @LangKey(LANG_CONFIG + "client.alternateParticles")
        public static boolean alternateParticles = false;

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
            @Comment({ "The command \"/simplyhotsprings\" has a few subcommands that will help you fill in your whitelists and blacklists "
                    + "and test to see if they work. "
                    + "For example, \"/simplyhotsprings locationinfo\" will tell you your current dimension ID, "
                    + "the name and types of the biome you're currently in, and whether hot springs can generate there. "
                    + "To see how to use \"/simplyhotsprings\", run the command \"/simplyhotsprings help\".",
                    "(Also, this config setting does nothing, so put whatever you want in here.)" })
            @LangKey(LANG_CONFIG_WORLDGEN + "info")
            public static String[] info = { "Run \"/simplyhotsprings biometypes\" to see a list of all Biome Types!",
                    "Run \"/forge dimensions\" to see a list of all currently registered dimensions!" };

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
            @Comment({ "If the world type is Superflat, the hot springs from this mod won't generate in the overworld. "
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
                    "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied. You can use a period as a dummy entry. " })
            @LangKey(LANG_CONFIG_WORLDGEN + "biomeTypeWhitelist")
            public static String[] biomeTypeWhitelist = {};

            @RequiresWorldRestart
            @Name("Biome Type Blacklist")
            @Comment({ "The biome types where hot springs are NOT allowed to generate." })
            @LangKey(LANG_CONFIG_WORLDGEN + "biomeTypeBlacklist")
            public static String[] biomeTypeBlacklist = { "DRY", "SAVANNA", "NETHER", "END", "OCEAN", "RIVER", "SANDY", "BEACH", "VOID" };

            @RequiresWorldRestart
            @Name("Biome Name Whitelist")
            @Comment({ "The individual biomes where hot springs are allowed to generate. If empty, all biomes are allowed. ",
                    "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied. You can use a period as a dummy entry. " })
            @LangKey(LANG_CONFIG_WORLDGEN + "biomeNameWhitelist")
            public static String[] biomeNameWhitelist = {};

            @RequiresWorldRestart
            @Name("Biome Name Blacklist")
            @Comment({ "The individual biomes where hot springs are NOT allowed to generate." })
            @LangKey(LANG_CONFIG_WORLDGEN + "biomeNameBlacklist")
            public static String[] biomeNameBlacklist = { "biomesoplenty:origin_beach", "biomesoplenty:origin_island" };

        }
    }

    //

    public static void warnInvalidEntry(String config, String input)
    {
        SimplyHotSprings.modlog.warn("Config: \"" + config + "\" entry with \"" + input + "\" is invalid");
    }

    //

    private static final Set<BiomeDictionary.Type> biomeTypeWhitelist = new HashSet<BiomeDictionary.Type>();
    private static final Set<BiomeDictionary.Type> biomeTypeBlacklist = new HashSet<BiomeDictionary.Type>();

    private static final Set<ResourceLocation> biomeNameWhitelist = new HashSet<ResourceLocation>();
    private static final Set<ResourceLocation> biomeNameBlacklist = new HashSet<ResourceLocation>();

    private static final String NO_WARN_DUMMY_ENTRY = ".";

    /**
     * parses user input whitelists & blacklists into actual whitelists & blacklists <br>
     * called after postInit or when the config changes from the main menu
     */
    public static void fillBiomeSets()
    {
        if (!Config.WorldGen.worldGen)
            return;

        biomeTypeWhitelist.clear();
        boolean invalidEntries = false;
        inputLoop: for (String input : Config.WorldGen.biomeTypeWhitelist)
        {
            if (!input.equals(NO_WARN_DUMMY_ENTRY))
            {
                for (BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
                    if (type.getName().equalsIgnoreCase(input))
                    {
                        biomeTypeWhitelist.add(type);
                        continue inputLoop;
                    }
                warnInvalidEntry("Biome Type Whitelist", input);
                invalidEntries = true;
            }
            else
                invalidEntries = true; // so you can use '.' as a dummy entry that doesn't warn you every time
        }

        // whitelists have a specific behaviour if they're empty - they'll match everything.
        // i'm assuming that if the whitelist contains only invalid entries or the dummy entry,
        // a user probably doesn't want me to treat it like it was empty.
        // so i add a useless entry here.
        if (biomeTypeWhitelist.isEmpty() && invalidEntries)
            biomeTypeWhitelist.add(null);

        biomeTypeBlacklist.clear();
        inputLoop: for (String input : Config.WorldGen.biomeTypeBlacklist)
        {
            for (BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
                if (type.getName().equalsIgnoreCase(input))
                {
                    biomeTypeBlacklist.add(type);
                    continue inputLoop;
                }
            warnInvalidEntry("Biome Type Blacklist", input);
        }

        biomeNameWhitelist.clear();
        invalidEntries = false;
        for (String input : Config.WorldGen.biomeNameWhitelist)
        {
            if (!input.equals(NO_WARN_DUMMY_ENTRY))
            {
                ResourceLocation name = new ResourceLocation(input);
                if (ForgeRegistries.BIOMES.containsKey(name))
                    biomeNameWhitelist.add(name);
                else
                {
                    warnInvalidEntry("Biome Name Whitelist", name.toString());
                    invalidEntries = true;
                }
            }
            else
                invalidEntries = true; // see above.
        }

        // see above.
        if (biomeNameWhitelist.isEmpty() && invalidEntries)
            biomeNameWhitelist.add(null);

        biomeNameBlacklist.clear();
        for (String input : Config.WorldGen.biomeNameBlacklist)
        {
            ResourceLocation name = new ResourceLocation(input);
            if (ForgeRegistries.BIOMES.containsKey(name))
                biomeNameBlacklist.add(name);
            else
                warnInvalidEntry("Biome Name Blacklist", name.toString());
        }
    }

    /**
     * the map of biomes to reasons they can or cannot generate. emptied and recreated when loading a world. <br>
     * this field is only used from the logical server side, from {@link CommandSimplyHotSprings} and {@link HotSpringsWorldGen}.
     */
    public static final Object2ObjectOpenHashMap<ResourceLocation, GenerationReason> biomeReasons;
    static
    {
        biomeReasons = new Object2ObjectOpenHashMap<ResourceLocation, GenerationReason>(255, 1.0F);
        biomeReasons.defaultReturnValue(GenerationReason.UNKNOWN_BIOME);
    }

    /**
     * Called from {@link CommonProxy#serverAboutToStart}
     */
    public static void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        parsedBOPWorlds.clear();

        biomeReasons.clear();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection())
            biomeReasons.put(biome.getRegistryName(), getGenerationReasonBiome(biome));
        biomeReasons.trim();
    }

    /**
     * @return a real GenerationReason if not allowed, or {@link GenerationReason#ALLOW_WORLD} if you can pass on to checking {@link SimplyHotSpringsConfig#getGenerationReasonBiome}
     */
    public static GenerationReason getGenerationReasonWorld(World world)
    {
        if (!Config.WorldGen.worldGen)
            return GenerationReason.NO_WORLD_GEN;
        if (!Config.WorldGen.worldGenIfBOPSprings && areBOPHotSpringsEnabled(world))
            return GenerationReason.BOP_SPRINGS;
        if (!Config.WorldGen.worldGenIfSuperflat && world.getWorldType() == WorldType.FLAT && world.provider.getDimensionType() == DimensionType.OVERWORLD)
            return GenerationReason.SUPERFLAT;

        if (ArrayUtils.contains(Config.WorldGen.dimWhitelist, world.provider.getDimension()))
            return GenerationReason.ALLOW_WORLD;
        if (ArrayUtils.contains(Config.WorldGen.dimBlacklist, world.provider.getDimension()))
            return GenerationReason.IN_DIM_BLACKLIST;
        if (Config.WorldGen.dimWhitelist.length != 0)
            return GenerationReason.NOT_DIM_WHITELISTED;

        return GenerationReason.ALLOW_WORLD;
    }

    public static GenerationReason getGenerationReasonBiome(Biome biome)
    {
        if (biomeNameWhitelist.contains(biome.getRegistryName()))
            return GenerationReason.IN_NAME_WHITELIST;
        if (biomeNameBlacklist.contains(biome.getRegistryName()))
            return GenerationReason.IN_NAME_BLACKLIST;

        Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(biome);
        if (biomeTypeBlacklist.stream().anyMatch(biomeTypes::contains))
            return GenerationReason.IN_TYPE_BLACKLIST;
        if (biomeTypeWhitelist.stream().anyMatch(biomeTypes::contains))
            return GenerationReason.IN_TYPE_WHITELIST;

        if (biomeNameWhitelist.isEmpty())
            return GenerationReason.EMPTY_NAME_WHITELIST;
        if (biomeTypeWhitelist.isEmpty())
            return GenerationReason.EMPTY_TYPE_WHITELIST;

        return GenerationReason.NOT_WHITELISTED;
    }

    /**
     * @return true if {@link #getGenerationReasonWorld(World)} returns {@link GenerationReason#ALLOW_WORLD} for this world
     */
    public static boolean allowedWorld(World world)
    {
        return getGenerationReasonWorld(world).allowsGeneration();
    }

    /**
     * @return true if this biome is in {@link #biomeReasons} and is allowed to have hot springs
     */
    public static boolean allowedBiome(Biome biome)
    {
        return biomeReasons.get(biome.getRegistryName()).allowsGeneration();
    }

    /** parser for BOP world generator options */
    private static final JsonParser jsonParser = new JsonParser();

    /** cache the result of parsing a world's generator options so that we don't have to parse json every time a hot spring tries to generate */
    private static Object2BooleanMap<World> parsedBOPWorlds = new Object2BooleanArrayMap<World>();

    private static boolean areBOPHotSpringsEnabled(World world)
    {
        if (world.getWorldType().getName().equals("BIOMESOP"))
        {
            String genOptions = world.getWorldInfo().getGeneratorOptions();
            if (StringUtils.isEmpty(genOptions))
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

    //

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Reference.MODID))
        {
            ConfigManager.sync(Reference.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);

            BlockHotSpringWater.updateConfigSettings();

            if (!event.isWorldRunning())
                fillBiomeSets();
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
                    Config.WorldGen.oldBOPGenConfigName, Config.WorldGen.newBOPGenConfigName))
            {
                ConfigManager.sync(Reference.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
            }
        }
        catch (Exception excep)
        {
            ;
        }
    }

}
