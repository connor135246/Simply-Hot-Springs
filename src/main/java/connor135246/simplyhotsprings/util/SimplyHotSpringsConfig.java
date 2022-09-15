package connor135246.simplyhotsprings.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.Config;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import connor135246.simplyhotsprings.common.world.gen.placement.ConfigChancePlacement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = SimplyHotSprings.MODID, bus = Bus.MOD)
public class SimplyHotSpringsConfig
{

    // i keep having issues where changing the config file while the game is open sometimes makes forge read the entire config as null values and resets the whole thing.
    // am i the only one? at least configured works.

    // COMMON

    public static class Common
    {

        // default
        public static final String LANG_CONFIG = SimplyHotSprings.MODID + ".config.";

        public final BooleanValue createsSources;

        public final ConfigValue<String> potionEffect;
        public final IntValue potionEffectTimer;
        public final IntValue potionEffectAmplifier;

        // worldgen
        public static final String LANG_CONFIG_WORLDGEN = LANG_CONFIG + "worldgen.";

        public final ConfigValue<List<? extends String>> info;

        public final BooleanValue worldGen;
        public final IntValue chance;
        public final BooleanValue debug;

        public final ConfigValue<List<? extends String>> biomeTypeWhitelist;
        public final ConfigValue<List<? extends String>> biomeTypeBlacklist;
        public final ConfigValue<List<? extends String>> biomeNameWhitelist;
        public final ConfigValue<List<? extends String>> biomeNameBlacklist;

        Common(ForgeConfigSpec.Builder builder)
        {
            Config.setInsertionOrderPreserved(true);

            createsSources = builder
                    .translation(LANG_CONFIG + "createsSources")
                    .comment("If true, Hot Spring Water makes infinite water sources, like vanilla water does.")
                    .define("Creates Source Blocks", true);

            potionEffect = builder
                    .translation(LANG_CONFIG + "potionEffect")
                    .comment("The namespaced effect that Hot Spring Water applies to entities. "
                            + "If the given effect isn't found (or left blank), no effect is applied.")
                    .define("Potion Effect", "minecraft:regeneration");

            potionEffectTimer = builder
                    .translation(LANG_CONFIG + "potionEffectTimer")
                    .comment("The duration of the effect, in ticks. 20 ticks = 1 second.")
                    .defineInRange("Potion Effect Timer", 50, 1, Integer.MAX_VALUE);

            potionEffectAmplifier = builder
                    .translation(LANG_CONFIG + "potionEffectAmplifier")
                    .comment("The amplifier of the effect. 0 is level 1.")
                    .defineInRange("Potion Effect Amplifier", 0, 0, 255);

            builder.comment("Most of these settings require any open world to be closed and reopened.")
                    .push("worldgen");

            // for the string lists, the default supplier apparently can't be an empty list or else configured thinks it's a boolean list instead of a string list and turns all
            // your inputs into "false". very strange.

            // there are 3 new biome dictionary types since 1.12: PLATEAU, MODIFIED, and OVERWORLD
            info = builder
                    .translation(LANG_CONFIG_WORLDGEN + "info")
                    .comment("The command \"/simplyhotsprings\" has a few subcommands that will help you fill in your whitelists and blacklists "
                            + "and test to see if they work. "
                            + "For example, \"/simplyhotsprings locationinfo\" will tell you the name and types of the biome you're currently in "
                            + "and whether Hot Springs can generate there. "
                            + "To see how to use \"/simplyhotsprings\", run the command \"/simplyhotsprings help\".",
                            "(Also, this config setting does nothing, so put whatever you want in here.)")
                    .defineListAllowEmpty(Arrays.asList("#HOW TO FILL WHITELISTS AND BLACKLISTS"),
                            () -> Arrays.asList("Run \"/simplyhotsprings biometypes\" to see a list of all Biome Types!"),
                            input -> true);

            worldGen = builder
                    .translation(LANG_CONFIG_WORLDGEN + "worldGen")
                    .comment("Set to false to stop Hot Springs from generating.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .define("World Generation", true);

            chance = builder
                    .translation(LANG_CONFIG_WORLDGEN + "chance")
                    .comment("The chance for a Hot Spring to generate is 1 over this value. So lower values are more likely.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineInRange("Generation Chance", 275, 1, Integer.MAX_VALUE);

            debug = builder
                    .translation(LANG_CONFIG_WORLDGEN + "debug")
                    .comment("When a Hot Spring generates in the world, prints the location to console.")
                    .define("Generation Debug", false);

            biomeTypeWhitelist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeTypeWhitelist")
                    .comment("The biome types where Hot Springs are allowed to generate. If empty, all biome types are allowed. ",
                            "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied. You can use a period as a dummy entry. ",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Type Whitelist"),
                            () -> Arrays.asList(),
                            Common::isString);

            biomeTypeBlacklist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeTypeBlacklist")
                    .comment("The biome types where Hot Springs are NOT allowed to generate.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Type Blacklist"),
                            () -> Arrays.asList("DRY", "SAVANNA", "NETHER", "END", "OCEAN", "RIVER", "SANDY", "BEACH", "VOID"),
                            Common::isString);

            biomeNameWhitelist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeNameWhitelist")
                    .comment("The individual biomes where Hot Springs are allowed to generate. If empty, all biomes are allowed. ",
                            "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied. You can use a period as a dummy entry. ",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Name Whitelist"),
                            () -> Arrays.asList(),
                            Common::isString);

            biomeNameBlacklist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeNameBlacklist")
                    .comment("The individual biomes where Hot Springs are NOT allowed to generate.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Name Blacklist"),
                            () -> Arrays.asList("biomesoplenty:origin_valley"),
                            Common::isString);

            builder.pop();
        }

        private static boolean isString(Object object)
        {
            return object instanceof String;
        }

    }

    //

    private static void warnInvalidEntry(String config, String input)
    {
        SimplyHotSprings.log.warn("Config: \"" + config + "\" entry with \"" + input + "\" is invalid");
    }

    //

    private static @Nullable Effect potionEffect = null;

    /**
     * parses user input potion effect into actual potion effect
     */
    private static void updateEffect()
    {
        String input = COMMON.potionEffect.get();
        if (StringUtils.isBlank(input))
            potionEffect = null;
        else
        {
            potionEffect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(input));
            if (potionEffect == null)
                warnInvalidEntry("Potion Effect", input);
        }
    }

    public static @Nullable EffectInstance getNewHotSpringsEffect()
    {
        return potionEffect == null ? null : new EffectInstance(potionEffect, COMMON.potionEffectTimer.get(), COMMON.potionEffectAmplifier.get(), true, true);
    }

    /**
     * adds the hot springs effect to the entity, if it's not null and the entity doesn't already have the hot springs effect
     */
    public static boolean addHotSpringsEffect(LivingEntity livingEntity)
    {
        if (potionEffect != null && !livingEntity.isPotionActive(potionEffect))
            return livingEntity.addPotionEffect(getNewHotSpringsEffect());
        return false;
    }

    //

    private static final Set<BiomeDictionary.Type> biomeTypeWhitelist = new HashSet<BiomeDictionary.Type>();
    private static final Set<BiomeDictionary.Type> biomeTypeBlacklist = new HashSet<BiomeDictionary.Type>();

    private static final Set<RegistryKey<Biome>> biomeNameWhitelist = new HashSet<RegistryKey<Biome>>();
    private static final Set<RegistryKey<Biome>> biomeNameBlacklist = new HashSet<RegistryKey<Biome>>();

    private static final String NO_WARN_DUMMY_ENTRY = ".";

    /**
     * parses user input whitelists & blacklists into actual whitelists & blacklists
     */
    private static void fillBiomeSets()
    {
        if (!COMMON.worldGen.get())
            return;

        biomeTypeWhitelist.clear();
        boolean invalidEntries = false;
        inputLoop: for (String input : COMMON.biomeTypeWhitelist.get())
        {
            if (!StringUtils.isBlank(input))
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
        }

        // whitelists have a specific behaviour if they're empty - they'll match everything.
        // i'm assuming that if the whitelist contains only invalid entries or the dummy entry,
        // a user probably doesn't want me to treat it like it was empty.
        // so i add a useless entry here.
        if (biomeTypeWhitelist.isEmpty() && invalidEntries)
            biomeTypeWhitelist.add(null);

        biomeTypeBlacklist.clear();
        inputLoop: for (String input : COMMON.biomeTypeBlacklist.get())
        {
            if (!StringUtils.isBlank(input))
            {
                for (BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
                    if (type.getName().equalsIgnoreCase(input))
                    {
                        biomeTypeBlacklist.add(type);
                        continue inputLoop;
                    }
                warnInvalidEntry("Biome Type Blacklist", input);
            }
        }

        biomeNameWhitelist.clear();
        invalidEntries = false;
        for (String input : COMMON.biomeNameWhitelist.get())
        {
            if (!StringUtils.isBlank(input))
            {
                if (!input.equals(NO_WARN_DUMMY_ENTRY))
                {
                    ResourceLocation name = new ResourceLocation(input);
                    biomeNameWhitelist.add(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, name));
                    mentionedBiomes.add(name);
                }
                else
                    invalidEntries = true; // see above.
            }
        }

        // see above.
        if (biomeNameWhitelist.isEmpty() && invalidEntries)
            biomeNameWhitelist.add(null);

        biomeNameBlacklist.clear();
        for (String input : COMMON.biomeNameBlacklist.get())
        {
            if (!StringUtils.isBlank(input))
            {
                ResourceLocation name = new ResourceLocation(input);
                biomeNameBlacklist.add(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, name));
                mentionedBiomes.add(name);
            }
        }
    }

    /**
     * the map of biomes to reasons they can or cannot generate. emptied and recreated when loading a world. <br>
     * this field is only used from the logical server side, from {@link SimplyHotSpringsCommand}.
     */
    public static final Object2ObjectOpenHashMap<RegistryKey<Biome>, GenerationReason> biomeReasons = Util.make(
            new Object2ObjectOpenHashMap<RegistryKey<Biome>, GenerationReason>(255, 1.0F), map -> map.defaultReturnValue(GenerationReason.UNKNOWN_BIOME));

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onBiomeLoading} <br>
     * note: the client's biome features may not be accurate to the dedicated server's biome features, since the Common config is not synced.
     */
    public static void addSpringsGeneration(BiomeLoadingEvent event)
    {
        if (event.getName() != null)
        {
            RegistryKey<Biome> biomeLoading = RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, event.getName());
            if (SimplyHotSpringsCommon.CONFIGURED_HOT_SPRINGS_FEATURE == null)
                biomeReasons.put(biomeLoading, GenerationReason.CONFIGURED_ERROR);
            else
            {
                GenerationReason reason = SimplyHotSpringsConfig.getGenerationReason(biomeLoading);
                if (reason.allowsGeneration())
                    event.getGeneration().withFeature(GenerationStage.Decoration.LAKES, SimplyHotSpringsCommon.CONFIGURED_HOT_SPRINGS_FEATURE);
                biomeReasons.put(biomeLoading, reason);
            }
        }
    }

    /**
     * the list of biomes that are in the config. after biomes are loaded, the player is notified of any invalid ones.
     */
    private static final List<ResourceLocation> mentionedBiomes = new ArrayList<ResourceLocation>();

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onServerAboutToStart}
     */
    public static void finalizeSpringsGeneration(FMLServerAboutToStartEvent event)
    {
        event.getServer().getDynamicRegistries().func_230521_a_(Registry.BIOME_KEY).ifPresent(biomeReg -> {
            for (ResourceLocation biome : mentionedBiomes)
            {
                if (!biomeReg.getOptional(biome).isPresent())
                    SimplyHotSprings.log.warn("Config: Biome \"" + biome + "\" was not found");
            }
        });

        biomeReasons.trim();
        ConfigChancePlacement.updateChance(COMMON.chance.get());
    }

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onServerStopped}
     */
    public static void resetSpringsGeneration(FMLServerStoppedEvent event)
    {
        biomeReasons.clear();
    }

    /**
     * @return a GenerationReason of why the hot spring can or cannot generate.
     */
    public static GenerationReason getGenerationReason(RegistryKey<Biome> biomeKey)
    {
        if (!COMMON.worldGen.get())
            return GenerationReason.NO_WORLD_GEN;

        if (biomeNameWhitelist.contains(biomeKey))
            return GenerationReason.IN_NAME_WHITELIST;
        if (biomeNameBlacklist.contains(biomeKey))
            return GenerationReason.IN_NAME_BLACKLIST;

        Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(biomeKey);
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

    // CLIENT

    public static class Client
    {

        public static final String LANG_CLIENT = Common.LANG_CONFIG + "client.";
        public final BooleanValue alternateParticles;

        Client(ForgeConfigSpec.Builder builder)
        {
            alternateParticles = builder
                    .translation(LANG_CLIENT + "alternateParticles")
                    .comment("If true, Hot Spring Water makes smaller, less obtrusive steam particles instead.")
                    .define("Alternate Particles", false);
        }

    }

    //

    public static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;
    public static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;
    static
    {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(ModConfig.Loading event)
    {
        onReOrLoad(event);
    }

    @SubscribeEvent
    public static void onReload(ModConfig.Reloading event)
    {
        onReOrLoad(event);
    }

    private static void onReOrLoad(ModConfigEvent event)
    {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
        {
            updateEffect();
            mentionedBiomes.clear();
            fillBiomeSets();
        }
    }

}
