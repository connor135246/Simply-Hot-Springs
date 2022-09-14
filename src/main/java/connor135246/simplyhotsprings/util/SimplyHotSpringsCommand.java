package connor135246.simplyhotsprings.util;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class SimplyHotSpringsCommand
{

    /** base command */
    public static final String COMMAND = SimplyHotSprings.MODID;
    /** command options */
    public static final String HELP = "help",
            LOCATIONINFO = "locationinfo",
            BIOMESLIST = "biomeslist", ALL = "all", WITH = "with", WITHOUT = "without",
            BIOMETYPES = "biometypes",
            PLACESPRING = "placespring";

    public static final String LANG_COMMAND = "commands." + SimplyHotSprings.MODID + ".";
    public static final String LANG_LOCATIONINFO = LANG_COMMAND + LOCATIONINFO + ".";
    public static final String LANG_BIOMESLIST = LANG_COMMAND + BIOMESLIST + ".";
    public static final String LANG_BIOMETYPES = LANG_COMMAND + BIOMETYPES + ".";
    public static final String LANG_PLACESPRING = LANG_COMMAND + PLACESPRING + ".";
    public static final String LANG_HELP = LANG_COMMAND + HELP + ".";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal(COMMAND).requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.literal(HELP).executes((context) -> {
            return sendHelp(context.getSource());
        }).then(Commands.literal(LOCATIONINFO).executes((context) -> {
            return sendHelpForSubcommand(context.getSource(), LOCATIONINFO, 7);
        })).then(Commands.literal(BIOMESLIST).executes((context) -> {
            return sendHelpForSubcommand(context.getSource(), BIOMESLIST, 5);
        })).then(Commands.literal(BIOMETYPES).executes((context) -> {
            return sendHelpForSubcommand(context.getSource(), BIOMETYPES, 3);
        })).then(Commands.literal(PLACESPRING).executes((context) -> {
            return sendHelpForSubcommand(context.getSource(), PLACESPRING, 1);
        }))).then(Commands.literal(LOCATIONINFO).executes((context) -> {
            return sendLocationInfo(context.getSource(), new BlockPos(context.getSource().getPosition()));
        }).then(Commands.argument("target", EntityArgument.entity()).executes((context) -> {
            return sendLocationInfo(context.getSource(), EntityArgument.getEntity(context, "target").blockPosition());
        })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
            return sendLocationInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
        })).then(Commands.argument("biome", ResourceLocationArgument.id()).suggests((context, builder) -> {
            return SharedSuggestionProvider.suggestResource(context.getSource().getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet(), builder);
        }).executes((context) -> {
            return sendLocationInfo(context.getSource(), context.getArgument("biome", ResourceLocation.class));
        }))).then(Commands.literal(BIOMESLIST)
                .then(Commands.literal(ALL).executes((context) -> {
                    return sendAllKnownBiomes(context.getSource());
                })).then(Commands.literal(WITH).executes((context) -> {
                    return sendKnownBiomes(context.getSource(), true);
                })).then(Commands.literal(WITHOUT).executes((context) -> {
                    return sendKnownBiomes(context.getSource(), false);
                }))).then(Commands.literal(BIOMETYPES).executes((context) -> {
                    return sendAllBiomeTypes(context.getSource());
                }).then(Commands.argument("biome_type", BiomeTypeArgument.biomeTypeArgument()).executes((context) -> {
                    return sendBiomesOfType(context.getSource(), context.getArgument("biome_type", BiomeDictionary.Type.class));
                }))).then(Commands.literal(PLACESPRING).executes((context) -> {
                    return placeSpring(context.getSource(), new BlockPos(context.getSource().getPosition()));
                }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
                    return placeSpring(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
                }))));
    }

    // help

    private static int sendHelp(CommandSourceStack source)
    {
        source.sendSuccess(new TextComponent("--- " + "/" + COMMAND + " " + HELP + " ---").withStyle(ChatFormatting.GOLD), true);

        source.sendSuccess(makeHelpComponent(LOCATIONINFO), true);
        source.sendSuccess(makeHelpComponent(BIOMESLIST), true);
        source.sendSuccess(makeHelpComponent(BIOMETYPES), true);
        source.sendSuccess(makeHelpComponent(PLACESPRING), true);

        return 4;
    }

    private static int sendHelpForSubcommand(CommandSourceStack source, String subcommand, int helps)
    {
        source.sendSuccess(new TextComponent("--- " + "/" + COMMAND + " " + HELP + " " + subcommand + " ---").withStyle(ChatFormatting.GOLD), true);

        for (int i = 0; i <= helps; ++i)
            source.sendSuccess(new TranslatableComponent(LANG_HELP + subcommand + "." + i)
                    .withStyle(i % 2 == 1 ? ChatFormatting.GRAY : ChatFormatting.WHITE), true);

        return helps + 1;

    }

    // locationinfo

    private static int sendLocationInfo(CommandSourceStack source, BlockPos pos)
    {
        ResourceLocation biomeId = source.getLevel().getBiome(pos).value().getRegistryName();

        if (biomeId != null)
            return sendLocationInfo(source, biomeId);
        else
        {
            source.sendFailure(new TranslatableComponent(LANG_LOCATIONINFO + "no_biome_key"));
            return 0;
        }
    }

    private static int sendLocationInfo(CommandSourceStack source, ResourceLocation biomeId)
    {
        if (source.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).containsKey(biomeId))
            return sendLocationInfo(source, ResourceKey.create(Registry.BIOME_REGISTRY, biomeId));
        else
        {
            source.sendFailure(new TranslatableComponent(LANG_LOCATIONINFO + "biome_not_found", biomeId));
            return 0;
        }
    }

    private static int sendLocationInfo(CommandSourceStack source, ResourceKey<Biome> biomeKey)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_name")
                .append(makeSuggestComponent(biomeKey.location().toString())), true);

        if (!BiomeDictionary.hasAnyType(biomeKey))
            source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types")
                    .append(noneComponent().withStyle(ChatFormatting.WHITE)), true);
        else
            source.sendSuccess(makeMultiComponent(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types"),
                    BiomeDictionary.getTypes(biomeKey), type -> type.getName(), string -> makeSuggestComponent(string)), true);

        GenerationReason reason = SimplyHotSpringsConfig.biomeReasons.get(biomeKey);
        source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                .append(makeHotSpringsReasonComponent(reason)), true);

        return reason.allowsGeneration() ? 1 : 0;
    }

    // biomeslist

    private static int sendAllKnownBiomes(CommandSourceStack source)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMESLIST + "all"), true);

        if (SimplyHotSpringsConfig.biomeReasons.isEmpty())
            source.sendSuccess(noneComponent(), true);
        else
            source.sendSuccess(makeMultiComponent(SimplyHotSpringsConfig.biomeReasons.keySet(),
                    key -> key.location(), id -> makeLocationInfoComponent(id.toString())), true);

        return SimplyHotSpringsConfig.biomeReasons.size();
    }

    private static int sendKnownBiomes(CommandSourceStack source, boolean with)
    {
        source.sendSuccess(new TranslatableComponent(LANG_BIOMESLIST + (with ? "with" : "without"))
                .withStyle(with ? ChatFormatting.GREEN : ChatFormatting.DARK_RED), true);

        Set<ResourceLocation> filteredIds = SimplyHotSpringsConfig.biomeReasons.object2ObjectEntrySet().stream()
                .filter(entry -> with == entry.getValue().allowsGeneration())
                .map(entry -> entry.getKey().location()).collect(Collectors.toSet());
        if (filteredIds.isEmpty())
            source.sendSuccess(noneComponent(), true);
        else
            source.sendSuccess(ComponentUtils.formatAndSortList(filteredIds, id -> makeLocationInfoComponent(id.toString())), true);

        return filteredIds.size();
    }

    // biometypes

    private static int sendAllBiomeTypes(CommandSourceStack source)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMETYPES + "all"), true);

        if (BiomeDictionary.Type.getAll().isEmpty())
            source.sendSuccess(noneComponent(), true);
        else
            source.sendSuccess(makeMultiComponent(BiomeDictionary.Type.getAll(), type -> type.getName(), string -> makeBiomeTypeComponent(string)), true);

        return BiomeDictionary.Type.getAll().size();
    }

    private static int sendBiomesOfType(CommandSourceStack source, BiomeDictionary.Type type)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMETYPES + "biomes", type.getName()), true);

        Set<ResourceKey<Biome>> biomeIds = BiomeDictionary.getBiomes(type);
        if (biomeIds.isEmpty())
            source.sendSuccess(noneComponent(), true);
        else
            source.sendSuccess(makeMultiComponent(biomeIds, key -> key.location(), id -> makeLocationInfoComponent(id.toString())), true);

        return biomeIds.size();
    }

    // placespring

    private static int placeSpring(CommandSourceStack source, BlockPos pos)
    {
        ServerLevel level = source.getLevel();
        Random rand = new Random();

        pos = pos.offset(-8, 0, -8);

        while (pos.getY() > level.getMinBuildHeight() + 5 && level.isEmptyBlock(pos))
            pos = pos.below();

        pos = pos.below(rand.nextInt(3));

        boolean success = true;
        String reasonKey = "";
        if (pos.getY() > level.getMinBuildHeight() + 4)
        {
            if (!HotSpringsFeature.doPlace(level, rand, pos, false))
            {
                success = false;
                reasonKey = "failed";
            }
        }
        else
        {
            success = false;
            reasonKey = "too_low";
        }

        MutableComponent message = makeTeleportComponent(LANG_PLACESPRING + (success ? "placed" : "not_placed"), pos.offset(8, 0, 8));
        message.withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED);
        if (!reasonKey.isEmpty())
            message.append(new TranslatableComponent(LANG_PLACESPRING + reasonKey));
        source.sendSuccess(message, true);

        return success ? 1 : 0;
    }

    // text component stuff

    /**
     * @return a TranslatableComponent colored with ChatFormatting.AQUA
     */
    private static MutableComponent makeAquaTranslatable(String key, Object... args)
    {
        return new TranslatableComponent(key, args).withStyle(ChatFormatting.AQUA);
    }

    /**
     * @return a text component encapsulating this {@link GenerationReason}
     */
    public static MutableComponent makeHotSpringsReasonComponent(GenerationReason reason)
    {
        return new TranslatableComponent(reason.getYN()).setStyle(Style.EMPTY.applyFormat(reason.getTextFormatting())
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TranslatableComponent(LANG_LOCATIONINFO + "reason").append("\n")
                                .append(new TranslatableComponent(reason.getKey()).withStyle(reason.getTextFormatting())))));
    }

    /**
     * @return a TextComponent of toCopy that suggests itself to the chat box when you click it
     */
    private static MutableComponent makeSuggestComponent(String toCopy)
    {
        return new TextComponent(toCopy).setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)
                .withHoverEvent(clickForSuggest).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy)));
    }

    private static final HoverEvent clickForSuggest = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_LOCATIONINFO + "click"));

    /**
     * @return a TextComponent of location that runs /simplyhotsprings locationinfo [location] when clicked
     */
    private static MutableComponent makeLocationInfoComponent(String location)
    {
        return new TextComponent(location)
                .setStyle(Style.EMPTY.withHoverEvent(clickForInfo)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + LOCATIONINFO + " " + location)));
    }

    private static final HoverEvent clickForInfo = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_BIOMESLIST + "click"));

    /**
     * @return a TextComponent of name that runs /simplyhotsprings biometypes [name] when clicked
     */
    private static MutableComponent makeBiomeTypeComponent(String name)
    {
        return new TextComponent(name)
                .setStyle(Style.EMPTY.withHoverEvent(clickForList)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + BIOMETYPES + " " + name)));
    }

    private static final HoverEvent clickForList = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_BIOMETYPES + "click"));

    /**
     * @return a TranslatableComponent that teleports you to the pos when clicked
     */
    private static MutableComponent makeTeleportComponent(String key, BlockPos pos)
    {
        return new TranslatableComponent(key, pos.getX(), pos.getY(), pos.getZ())
                .setStyle(Style.EMPTY.withHoverEvent(clickForTeleport)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ())));
    }

    private static final HoverEvent clickForTeleport = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_PLACESPRING + "click"));

    /**
     * @return a TextComponent of /simplyhotsprings help [subcommand] that runs itself when clicked
     */
    private static MutableComponent makeHelpComponent(String subcommand)
    {
        String command = "/" + COMMAND + " " + HELP + " " + subcommand;
        return new TextComponent(command)
                .setStyle(Style.EMPTY.withHoverEvent(clickForHelp)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
    }

    private static final HoverEvent clickForHelp = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_HELP + "click"));

    private static MutableComponent noneComponent()
    {
        return new TranslatableComponent(LANG_COMMAND + "none");
    }

    /**
     * turns the collection of things into a list of comparable things, sorts them, turns them into text components, and then puts them all into one text component separated by
     * commas. see {@link ComponentUtils#formatAndSortList}
     * 
     * @param <T>
     *            the type of the collection
     * @param <C>
     *            the comparable thing
     * @param collection
     *            the collection of things
     * @param toComparable
     *            turns each thing into something that extends {@link Comparable} so that the things can be sorted
     * @param toTextComponent
     *            turns each comparable into an actual text component
     */
    private static <T, C extends Comparable<C>> Component makeMultiComponent(Collection<T> collection,
            Function<T, C> toComparable,
            Function<C, Component> toTextComponent)
    {
        return ComponentUtils.formatAndSortList(collection.stream().map(toComparable).collect(Collectors.toList()), toTextComponent);
    }

    /**
     * does {@link #makeMultiComponent(Collection, Function, Function)} and appends it to first
     */
    private static <T, C extends Comparable<C>> Component makeMultiComponent(MutableComponent first, Collection<T> collection,
            Function<T, C> toComparable,
            Function<C, Component> toTextComponent)
    {
        return first.append(makeMultiComponent(collection, toComparable, toTextComponent));
    }

}
