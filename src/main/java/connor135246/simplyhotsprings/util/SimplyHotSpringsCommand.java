package connor135246.simplyhotsprings.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
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
            return sendLocationInfo(context.getSource(), getSourcePos(context.getSource()));
        }).then(Commands.argument("target", EntityArgument.entity()).executes((context) -> {
            return sendLocationInfo(context.getSource(), EntityArgument.getEntity(context, "target").blockPosition());
        })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
            return sendLocationInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
        })).then(Commands.argument("biome", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_BIOMES).executes((context) -> {
            return sendLocationInfo(context.getSource(), context.getArgument("biome", ResourceLocation.class));
        }))).then(Commands.literal(BIOMESLIST)
                .then(withPageArg(Commands.literal(ALL), (context, page) -> {
                    return sendAllKnownBiomes(context.getSource(), page);
                })).then(withPageArg(Commands.literal(WITH), (context, page) -> {
                    return sendKnownBiomes(context.getSource(), true, page);
                })).then(withPageArg(Commands.literal(WITHOUT), (context, page) -> {
                    return sendKnownBiomes(context.getSource(), false, page);
                }))).then(withPageArg(Commands.literal(BIOMETYPES), (context, page) -> {
                    return sendAllBiomeTypes(context.getSource(), page);
                }).then(withPageArg(Commands.argument("biome_type", BiomeTypeArgument.biomeTypeArgument()), (context, page) -> {
                    return sendBiomesOfType(context.getSource(), context.getArgument("biome_type", BiomeDictionary.Type.class), page);
                }))).then(Commands.literal(PLACESPRING).executes((context) -> {
                    return placeSpring(context.getSource(), getSourcePos(context.getSource()));
                }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
                    return placeSpring(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
                }))));
    }

    /**
     * From {@link BlockPosArgument#getLoadedBlockPos}, ensures the command position is valid.
     */
    @SuppressWarnings("deprecation")
    private static BlockPos getSourcePos(CommandSourceStack source) throws CommandSyntaxException
    {
        BlockPos pos = new BlockPos(source.getPosition());
        if (!source.getUnsidedLevel().hasChunkAt(pos))
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        else if (!source.getUnsidedLevel().isInWorldBounds(pos))
            throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
        else
            return pos;
    }

    /**
     * Shortcut for adding an optional page argument to the given node
     */
    private static ArgumentBuilder<CommandSourceStack, ?> withPageArg(ArgumentBuilder<CommandSourceStack, ?> argument,
            ToIntBiFunction<CommandContext<CommandSourceStack>, Integer> command)
    {
        return argument.executes((context) -> command.applyAsInt(context, 0))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes((context) -> command.applyAsInt(context, IntegerArgumentType.getInteger(context, "page") - 1)));
    }

    // help

    private static int sendHelp(CommandSourceStack source)
    {
        source.sendSuccess(new TextComponent("--- " + "/" + COMMAND + " " + HELP + " ---").withStyle(ChatFormatting.GOLD), false);

        source.sendSuccess(makeHelpComponent(LOCATIONINFO), false);
        source.sendSuccess(makeHelpComponent(BIOMESLIST), false);
        source.sendSuccess(makeHelpComponent(BIOMETYPES), false);
        source.sendSuccess(makeHelpComponent(PLACESPRING), false);

        return 4;
    }

    private static int sendHelpForSubcommand(CommandSourceStack source, String subcommand, int helps)
    {
        source.sendSuccess(new TextComponent("--- " + "/" + COMMAND + " " + HELP + " " + subcommand + " ---").withStyle(ChatFormatting.GOLD), false);

        for (int i = 0; i <= helps; ++i)
            source.sendSuccess(new TranslatableComponent(LANG_HELP + subcommand + "." + i)
                    .withStyle(i % 2 == 1 ? ChatFormatting.GRAY : ChatFormatting.WHITE), false);

        return helps + 1;

    }

    // locationinfo

    private static int sendLocationInfo(CommandSourceStack source, BlockPos pos)
    {
        Optional<ResourceKey<Biome>> optionalBiomeKey = source.getLevel().getBiomeName(pos);

        if (optionalBiomeKey.isPresent())
            return sendLocationInfo(source, optionalBiomeKey.get());
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
                .append(makeSuggestComponent(biomeKey.location().toString())), false);

        if (!BiomeDictionary.hasAnyType(biomeKey))
            source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types")
                    .append(noneComponent().withStyle(ChatFormatting.WHITE)), false);
        else
            source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types")
                    .append(makeMultiComponent(BiomeDictionary.getTypes(biomeKey), sortType(), type -> makeSuggestComponent(type.getName()))), false);

        GenerationReason reason = SimplyHotSpringsConfig.biomeReasons.get(biomeKey);
        source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                .append(makeHotSpringsReasonComponent(reason)), false);

        return reason.allowsGeneration() ? 1 : 0;
    }

    // biomeslist

    private static int sendAllKnownBiomes(CommandSourceStack source, int page)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMESLIST + "all"), false);

        sendPaginatedComponents(source, SimplyHotSpringsConfig.biomeReasons.keySet(), sortKey(), key -> makeLocationInfoComponent(key.location().toString()),
                page, "/" + COMMAND + " " + BIOMESLIST + " " + ALL);

        return SimplyHotSpringsConfig.biomeReasons.size();
    }

    private static int sendKnownBiomes(CommandSourceStack source, boolean with, int page)
    {
        source.sendSuccess(new TranslatableComponent(LANG_BIOMESLIST + (with ? "with" : "without"))
                .withStyle(with ? ChatFormatting.GREEN : ChatFormatting.DARK_RED), false);

        Set<ResourceLocation> filteredIds = SimplyHotSpringsConfig.biomeReasons.object2ObjectEntrySet().stream()
                .filter(entry -> with == entry.getValue().allowsGeneration())
                .map(entry -> entry.getKey().location()).collect(Collectors.toSet());
        sendPaginatedComponents(source, filteredIds, ResourceLocation::compareNamespaced, id -> makeLocationInfoComponent(id.toString()),
                page, "/" + COMMAND + " " + BIOMESLIST + " " + (with ? WITH : WITHOUT));

        return filteredIds.size();
    }

    // biometypes

    private static int sendAllBiomeTypes(CommandSourceStack source, int page)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMETYPES + "all"), false);

        sendPaginatedComponents(source, BiomeDictionary.Type.getAll(), sortType(), type -> makeBiomeTypeComponent(type.getName()),
                page, "/" + COMMAND + " " + BIOMETYPES);

        return BiomeDictionary.Type.getAll().size();
    }

    private static int sendBiomesOfType(CommandSourceStack source, BiomeDictionary.Type type, int page)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMETYPES + "biomes", type.getName()), false);

        Set<ResourceKey<Biome>> biomeIds = BiomeDictionary.getBiomes(type);
        sendPaginatedComponents(source, biomeIds, sortKey(), key -> makeLocationInfoComponent(key.location().toString()),
                page, "/" + COMMAND + " " + BIOMETYPES + " " + type);

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

    private static MutableComponent makePageComponent(int current, int max, String pageCommand)
    {
        return new TranslatableComponent(LANG_COMMAND + "page_header",
                new TextComponent("<--").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY).withHoverEvent(clickForPrevious)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand + " " + Math.max(1, current - 1 < 1 ? max : current - 1)))),
                new TextComponent("-->").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY).withHoverEvent(clickForNext)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand + " " + (current + 1 > max ? 1 : current + 1)))),
                current, max).withStyle(ChatFormatting.GRAY);
    }

    private static final HoverEvent clickForNext = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_COMMAND + "next_page"));
    private static final HoverEvent clickForPrevious = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(LANG_COMMAND + "previous_page"));

    private static MutableComponent noneComponent()
    {
        return new TranslatableComponent(LANG_COMMAND + "none");
    }

    /**
     * sorts the collection, gets the items that will appear on the given page, turns them into components, and sends them.
     */
    private static <T> void sendPaginatedComponents(CommandSourceStack source,
            Collection<T> collection,
            @Nullable Comparator<T> comparator,
            Function<T, Component> toTextComponent,
            int page,
            String pageCommand)
    {
        // recent chat is 10 lines, and there's a title and a page header
        int itemsPerPage = 8;

        int maxPage = collection.size() / itemsPerPage + (collection.size() % itemsPerPage == 0 ? 0 : 1) - 1;
        if (page > maxPage)
            page = maxPage;

        source.sendSuccess(makePageComponent(page + 1, maxPage + 1, pageCommand), false);

        if (collection.isEmpty())
            source.sendSuccess(new TextComponent(" ").append(noneComponent()), false);
        else
        {
            List<T> list = new ArrayList<T>(collection);
            list.sort(comparator);

            for (int i = page * itemsPerPage; i < list.size() && i < (page + 1) * itemsPerPage; i++)
                source.sendSuccess(new TextComponent(" ").append(toTextComponent.apply(list.get(i))), false);
        }
    }

    /**
     * sorts the collection, turns them into text components, and then puts them all into one text component separated by commas. <br>
     * it's {@link ComponentUtils#formatAndSortList}, but you can specify the comparator.
     */
    private static <T> Component makeMultiComponent(Collection<T> collection,
            @Nullable Comparator<T> comparator,
            Function<T, Component> toTextComponent)
    {
        List<T> list = new ArrayList<T>(collection);
        list.sort(comparator);
        return ComponentUtils.formatList(list, toTextComponent);
    }

    private static Comparator<BiomeDictionary.Type> sortType()
    {
        return (type1, type2) -> type1.getName().compareTo(type2.getName());
    }

    private static <T> Comparator<ResourceKey<T>> sortKey()
    {
        return (key1, key2) -> key1.location().compareNamespaced(key2.location());
    }

}
