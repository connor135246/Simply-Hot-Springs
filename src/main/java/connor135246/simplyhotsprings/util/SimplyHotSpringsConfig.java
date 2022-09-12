package connor135246.simplyhotsprings.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.Config;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import connor135246.simplyhotsprings.common.world.gen.placement.ConfigChanceFilter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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

        public final ConfigValue<List<? extends String>> biomeGrasses;

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

            // there are 3 new biome dictionary types since 1.12: PLATEAU, MODIFIED, and OVERWORLD
            // there are 3 new biome dictionary types since 1.16: PEAK, SLOPE, and UNDERGROUND
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
                            () -> Arrays.asList(""),
                            Common::nonBlankString);

            biomeTypeBlacklist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeTypeBlacklist")
                    .comment("The biome types where Hot Springs are NOT allowed to generate.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Type Blacklist"),
                            () -> Arrays.asList("DRY", "SAVANNA", "NETHER", "END", "OCEAN", "RIVER", "SANDY", "BEACH", "VOID"),
                            Common::nonBlankString);

            biomeNameWhitelist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeNameWhitelist")
                    .comment("The individual biomes where Hot Springs are allowed to generate. If empty, all biomes are allowed. ",
                            "The Biome Type Whitelist and Biome Name Whitelist are not both required to be satisfied. You can use a period as a dummy entry. ",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Name Whitelist"),
                            () -> Arrays.asList(""),
                            Common::nonBlankString);

            biomeNameBlacklist = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeNameBlacklist")
                    .comment("The individual biomes where Hot Springs are NOT allowed to generate.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Name Blacklist"),
                            () -> Arrays.asList("biomesoplenty:origin_valley"),
                            Common::nonBlankString);

            biomeGrasses = builder
                    .translation(LANG_CONFIG_WORLDGEN + "biomeGrasses")
                    .comment("A list of biomes and their specific types of grass.",
                            "By default, dirt around Hot Springs will be turned into grass. But some biomes have their own specific grass type which you need to tell Simply Hot Springs about.",
                            "The format is \"modid:biome;modid:block[states]\".",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .defineListAllowEmpty(Arrays.asList("Biome Grasses"),
                            () -> Arrays.asList("minecraft:mushroom_fields;minecraft:mycelium", "biomesoplenty:origin_valley;biomesoplenty:origin_grass_block", 
                                    "biomesoplenty:redwood_forest;minecraft:podzol"),
                            Common::biomeGrassesPat);

            builder.pop();
        }

        private static final Pattern biomeGrassesPat = Pattern.compile("\\w+:\\w+;\\w+:\\w+");

        private static boolean biomeGrassesPat(Object object)
        {
            return object instanceof String ? biomeGrassesPat.matcher((String) object).matches() : false;
        }

        private static boolean nonBlankString(Object object)
        {
            return object instanceof String ? !StringUtils.isBlank(object.toString()) : false;
        }

    }

    //

    private static void warnInvalidEntry(String config, String input)
    {
        SimplyHotSprings.log.warn("\"" + config + "\" config entry \"" + input + "\" was not found");
    }

    //

    private static @Nullable MobEffect potionEffect = null;

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
            potionEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(input));
            if (potionEffect == null)
                warnInvalidEntry("Potion Effect", input);
        }
    }

    public static @Nullable MobEffectInstance getNewHotSpringsEffect()
    {
        return potionEffect == null ? null
                : new MobEffectInstance(potionEffect, COMMON.potionEffectTimer.get(), COMMON.potionEffectAmplifier.get(), true, true);
    }

    /**
     * adds the hot springs effect to the entity, if it's not null and the entity doesn't already have the hot springs effect
     */
    public static boolean addHotSpringsEffect(LivingEntity livingEntity)
    {
        if (potionEffect != null && !livingEntity.hasEffect(potionEffect))
            return livingEntity.addEffect(getNewHotSpringsEffect());
        return false;
    }

    //

    private static final Set<BiomeDictionary.Type> biomeTypeWhitelist = new HashSet<BiomeDictionary.Type>();
    private static final Set<BiomeDictionary.Type> biomeTypeBlacklist = new HashSet<BiomeDictionary.Type>();

    private static final Set<ResourceKey<Biome>> biomeNameWhitelist = new HashSet<ResourceKey<Biome>>();
    private static final Set<ResourceKey<Biome>> biomeNameBlacklist = new HashSet<ResourceKey<Biome>>();

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
            if (!StringUtils.isEmpty(input))
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
            if (!StringUtils.isEmpty(input))
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
            if (!StringUtils.isEmpty(input))
            {
                if (!input.equals(NO_WARN_DUMMY_ENTRY))
                {
                    ResourceLocation name = new ResourceLocation(input);
                    if (ForgeRegistries.BIOMES.containsKey(name))
                        biomeNameWhitelist.add(ResourceKey.create(ForgeRegistries.Keys.BIOMES, name));
                    else
                    {
                        warnInvalidEntry("Biome Name Whitelist", name.toString());
                        invalidEntries = true;
                    }
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
            if (!StringUtils.isEmpty(input))
            {
                ResourceLocation name = new ResourceLocation(input);
                if (ForgeRegistries.BIOMES.containsKey(name))
                    biomeNameBlacklist.add(ResourceKey.create(ForgeRegistries.Keys.BIOMES, name));
                else
                    warnInvalidEntry("Biome Name Blacklist", name.toString());
            }
        }
    }

    //

    /**
     * the map of biomes to biome-specific grasses. damn you surface rules!!!
     */
    public static final Object2ObjectOpenHashMap<ResourceKey<Biome>, BlockState> biomeGrasses = Util.make(
            new Object2ObjectOpenHashMap<ResourceKey<Biome>, BlockState>(10, 0.95F), map -> map.defaultReturnValue(Blocks.GRASS_BLOCK.defaultBlockState()));

    /**
     * parses user input biomes & grasses into actual biomes & grasses
     */
    private static void fillBiomeGrasses()
    {
        biomeGrasses.clear();

        for (String input : COMMON.biomeGrasses.get())
        {
            String[] parts = input.split(";");
            if (parts.length < 2)
                warnInvalidEntry("Biome Grasses", input);

            ResourceLocation name = new ResourceLocation(parts[0]);

            if (ForgeRegistries.BIOMES.containsKey(name))
            {
                try
                {
                    ResourceKey<Biome> biomeKey = ResourceKey.create(ForgeRegistries.Keys.BIOMES, name);

                    BlockStateParser stateParser = new BlockStateParser(new StringReader(parts[1]), false).parse(false);
                    BlockState state = stateParser.getState();
                    if (state == null)
                        throw BlockStateParser.ERROR_UNKNOWN_BLOCK.create(parts[1]);

                    biomeGrasses.put(biomeKey, state);
                }
                catch (CommandSyntaxException excep)
                {
                    warnInvalidEntry("Biome Grasses", parts[1]);
                }
            }
            else
                warnInvalidEntry("Biome Grasses", name.toString());
        }

        biomeGrasses.trim();
    }

    //

    /**
     * the map of biomes to reasons they can or cannot generate. emptied and recreated when loading a world. <br>
     * this field is only used from the logical server side, from {@link SimplyHotSpringsCommand}.
     */
    public static final Object2ObjectOpenHashMap<ResourceKey<Biome>, GenerationReason> biomeReasons = Util.make(
            new Object2ObjectOpenHashMap<ResourceKey<Biome>, GenerationReason>(255, 0.95F), map -> map.defaultReturnValue(GenerationReason.UNKNOWN_BIOME));

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onBiomeLoading} <br>
     * note: the client's biome features may not be accurate to the dedicated server's biome features, since the Common config is not synced.
     */
    public static void addSpringsGeneration(BiomeLoadingEvent event)
    {
        if (event.getName() != null)
        {
            ResourceKey<Biome> biomeLoading = ResourceKey.create(ForgeRegistries.Keys.BIOMES, event.getName());
            if (!SimplyHotSpringsCommon.PLACED_HOT_SPRINGS_FEATURE.isPresent())
                biomeReasons.put(biomeLoading, GenerationReason.REGISTER_ERROR);
            else
            {
                GenerationReason reason = SimplyHotSpringsConfig.getGenerationReason(biomeLoading);
                if (reason.allowsGeneration())
                    event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, SimplyHotSpringsCommon.PLACED_HOT_SPRINGS_FEATURE.getHolder().get());
                biomeReasons.put(biomeLoading, reason);
            }
        }
    }

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onServerAboutToStart}
     */
    public static void finalizeSpringsGeneration(ServerAboutToStartEvent event)
    {
        biomeReasons.trim();
        ConfigChanceFilter.updateChance(COMMON.chance.get());
        HotSpringsFeature.updateBiomeGrasses(biomeGrasses);
    }

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onServerStopped}
     */
    public static void resetSpringsGeneration(ServerStoppedEvent event)
    {
        biomeReasons.clear();
    }

    /**
     * @return a GenerationReason of why the hot spring can or cannot generate.
     */
    public static GenerationReason getGenerationReason(ResourceKey<Biome> biomeKey)
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
                    .comment("If true, Hot Spring Water makes small smoke particles instead. ",
                            "Note: these particles are affected by your particle video setting, and won't appear at all if it's set to Minimal.")
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
    public static void onLoad(ModConfigEvent.Loading event)
    {
        onReOrLoad(event);
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event)
    {
        onReOrLoad(event);
    }

    private static void onReOrLoad(ModConfigEvent event)
    {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
        {
            updateEffect();
            fillBiomeSets();
            fillBiomeGrasses();
        }
    }

}
