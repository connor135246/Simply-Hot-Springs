package connor135246.simplyhotsprings.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.HashBasedTable;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.world.gen.AddHotSpringsBiomeModifier;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
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

    // SERVER

    public static class Server
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

        public final BooleanValue worldGenDefault;
        public final BooleanValue worldGenBig;
        public final BooleanValue worldGenWellsprings;
        public final BooleanValue debug;

        public final ConfigValue<List<? extends String>> biomeGrasses;

        Server(ForgeConfigSpec.Builder builder)
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

            builder.comment("Some of these settings require any open world to be closed and reopened.")
                    .push("worldgen");

            info = builder
                    .translation(LANG_CONFIG_WORLDGEN + "info")
                    .comment("By default, Hot Springs only generate in biomes that are not in the \"simplyhotsprings:hot_springs_blacklist\" biome tag.",
                            "The command \"/simplyhotsprings\" has a few subcommands that will help you test your Hot Springs."
                            + "To see how to use \"/simplyhotsprings\", run the command \"/simplyhotsprings help\".",
                            "For more useful information, visit the wiki at https://github.com/connor135246/Simply-Hot-Springs/wiki",
                            "(Also, this config setting does nothing, so put whatever you want in here.)")
                    .defineListAllowEmpty(Arrays.asList("#HOW TO"),
                            () -> Arrays.asList("You can find screenshots of the different types of Hot Springs at https://github.com/connor135246/Simply-Hot-Springs/wiki/Hot-Spring-Types"),
                            input -> true);

            worldGenDefault = builder
                    .translation(LANG_CONFIG_WORLDGEN + "worldGenDefault")
                    .comment("Controls whether \"default\" Hot Springs generate.",
                            "These are the normal Hot Springs. They look just like vanilla's pre-1.18 water lakes. They generate on the surface in most biomes and are somewhat rare.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .define("Enable \"Default\" Hot Springs", true);

            worldGenBig = builder
                    .translation(LANG_CONFIG_WORLDGEN + "worldGenBig")
                    .comment("Controls whether \"big\" Hot Springs generate.",
                            "These look like the normal Hot Springs, but are much wider and deeper. They're also rarer than normal Hot Springs.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .define("Enable \"Big\" Hot Springs", false);

            worldGenWellsprings = builder
                    .translation(LANG_CONFIG_WORLDGEN + "worldGenWellsprings")
                    .comment("Controls whether \"wellspring\" and \"big_wellspring\" Hot Springs generate.",
                            "Wellsprings are geysers of Hot Spring Water that pour out of the ground and into the air. Follow them underground to find large pockets of Hot Spring Water. Big wellsprings are rarer and may have lava lakes below them.",
                            "If you change this setting with a world open, it must be closed and reopened for the changes to take effect.")
                    .worldRestart()
                    .define("Enable \"Wellspring\" Hot Springs", false);

            debug = builder
                    .translation(LANG_CONFIG_WORLDGEN + "debug")
                    .comment("When a Hot Spring generates in the world, prints the location to console.")
                    .define("Generation Debug", false);

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
                            Server::biomeGrassesPat);

            builder.pop();
        }

        private static final Pattern biomeGrassesPat = Pattern.compile("[\\w\\.\\-]+:[\\w\\.\\-/]+;[\\w\\.\\-]+:[\\w\\.\\-/]+(\\[[\\w\\.\\-/=,]+])?");

        private static boolean biomeGrassesPat(Object object)
        {
            return object instanceof String ? biomeGrassesPat.matcher((String) object).matches() : false;
        }

    }

    //

    private static void warnInvalidEntry(String config, String input)
    {
        SimplyHotSprings.log.warn("Config: \"" + config + "\" entry with \"" + input + "\" is invalid");
    }

    //

    private static @Nullable MobEffect potionEffect = null;

    /**
     * parses user input potion effect into actual potion effect
     */
    private static void updateEffect()
    {
        String input = SERVER.potionEffect.get();
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
                : new MobEffectInstance(potionEffect, SERVER.potionEffectTimer.get(), SERVER.potionEffectAmplifier.get(), true, true);
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

    /**
     * the table of biomes and hot spring modifiers to the reasons they can or cannot generate. emptied and recreated when loading a world. <br>
     * this field is only used from the logical server side, from {@link SimplyHotSpringsCommand} and {@link AddHotSpringsBiomeModifier}.
     */
    public static final HashBasedTable<Holder<Biome>, ResourceLocation, GenerationReason> biomeModifierReasons = HashBasedTable.create(180, 1);

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onServerAboutToStart}
     */
    @SuppressWarnings("deprecation")
    public static void finalizeSpringsGeneration(ServerAboutToStartEvent event)
    {
        // parses user input biomes & grasses into actual biomes & grasses
        event.getServer().registryAccess().registry(Registry.BIOME_REGISTRY).ifPresent(biomeReg -> {
            for (String input : SERVER.biomeGrasses.get())
            {
                String[] parts = input.split(";");
                if (parts.length < 2)
                    warnInvalidEntry("Biome Grasses", input);

                Holder<Biome> holder;
                try
                {
                    holder = biomeReg.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(parts[0])));
                }
                catch (IllegalStateException excep)
                {
                    warnInvalidEntry("Biome Grasses", parts[0]);
                    continue;
                }

                BlockState state;
                try
                {
                    state = BlockStateParser.parseForBlock(Registry.BLOCK, parts[1], false).blockState();
                }
                catch (CommandSyntaxException excep)
                {
                    warnInvalidEntry("Biome Grasses", parts[1]);
                    continue;
                }

                HotSpringsFeature.addBiomeGrass(holder, state);
            }
        });
    }

    /**
     * Called from {@link SimplyHotSpringsEventHandler#onServerStopped}
     */
    public static void resetSpringsGeneration(ServerStoppedEvent event)
    {
        biomeModifierReasons.clear();
        AddHotSpringsBiomeModifier.warned.clear();
        HotSpringsFeature.clearBiomeGrasses();
    }

    // CLIENT

    public static class Client
    {

        public static final String LANG_CLIENT = Server.LANG_CONFIG + "client.";
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

    public static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;
    public static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;
    static
    {
        final Pair<Server, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = commonSpecPair.getRight();
        SERVER = commonSpecPair.getLeft();
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
        if (event.getConfig().getType() == ModConfig.Type.SERVER)
        {
            updateEffect();
        }
    }

}
