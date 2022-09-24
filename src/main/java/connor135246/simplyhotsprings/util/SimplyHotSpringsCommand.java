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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
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

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal(COMMAND).requires((source) -> {
            return source.hasPermissionLevel(2);
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
            return sendLocationInfo(context.getSource(), EntityArgument.getEntity(context, "target").getPosition());
        })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
            return sendLocationInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
        })).then(Commands.argument("biome", ResourceLocationArgument.resourceLocation()).suggests(SuggestionProviders.field_239574_d_).executes((context) -> {
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
    private static BlockPos getSourcePos(CommandSource source) throws CommandSyntaxException
    {
        BlockPos pos = new BlockPos(source.getPos());
        if (!source.getWorld().isBlockLoaded(pos))
            throw BlockPosArgument.POS_UNLOADED.create();
        else if (!ServerWorld.isValid(pos))
            throw BlockPosArgument.POS_OUT_OF_WORLD.create();
        else
            return pos;
    }

    /**
     * Shortcut for adding an optional page argument to the given node
     */
    private static ArgumentBuilder<CommandSource, ?> withPageArg(ArgumentBuilder<CommandSource, ?> argument,
            ToIntBiFunction<CommandContext<CommandSource>, Integer> command)
    {
        return argument.executes((context) -> command.applyAsInt(context, 0))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes((context) -> command.applyAsInt(context, IntegerArgumentType.getInteger(context, "page") - 1)));
    }

    // help

    private static int sendHelp(CommandSource source)
    {
        source.sendFeedback(new StringTextComponent("--- " + "/" + COMMAND + " " + HELP + " ---").mergeStyle(TextFormatting.GOLD), true);

        source.sendFeedback(makeHelpComponent(LOCATIONINFO), true);
        source.sendFeedback(makeHelpComponent(BIOMESLIST), true);
        source.sendFeedback(makeHelpComponent(BIOMETYPES), true);
        source.sendFeedback(makeHelpComponent(PLACESPRING), true);

        return 4;
    }

    private static int sendHelpForSubcommand(CommandSource source, String subcommand, int helps)
    {
        source.sendFeedback(new StringTextComponent("--- " + "/" + COMMAND + " " + HELP + " " + subcommand + " ---").mergeStyle(TextFormatting.GOLD), true);

        for (int i = 0; i <= helps; ++i)
            source.sendFeedback(new TranslationTextComponent(LANG_HELP + subcommand + "." + i)
                    .mergeStyle(i % 2 == 1 ? TextFormatting.GRAY : TextFormatting.WHITE), true);

        return helps + 1;

    }

    // locationinfo

    private static int sendLocationInfo(CommandSource source, BlockPos pos)
    {
        Optional<RegistryKey<Biome>> optionalBiomeKey = source.getWorld().func_242406_i(pos);

        if (optionalBiomeKey.isPresent())
            return sendLocationInfo(source, optionalBiomeKey.get());
        else
        {
            source.sendErrorMessage(new TranslationTextComponent(LANG_LOCATIONINFO + "no_biome_key"));
            return 0;
        }
    }

    private static int sendLocationInfo(CommandSource source, ResourceLocation biomeId)
    {
        if (source.func_241861_q().getRegistry(Registry.BIOME_KEY).getOptional(biomeId).isPresent())
            return sendLocationInfo(source, RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biomeId));
        else
        {
            source.sendErrorMessage(new TranslationTextComponent(LANG_LOCATIONINFO + "biome_not_found", biomeId));
            return 0;
        }
    }

    private static int sendLocationInfo(CommandSource source, RegistryKey<Biome> biomeKey)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_name")
                .appendSibling(makeSuggestComponent(biomeKey.getLocation().toString())), true);

        if (!BiomeDictionary.hasAnyType(biomeKey))
            source.sendFeedback(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types")
                    .appendSibling(noneComponent().mergeStyle(TextFormatting.WHITE)), true);
        else
            source.sendFeedback(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types")
                    .appendSibling(makeMultiComponent(BiomeDictionary.getTypes(biomeKey), sortType(), type -> makeSuggestComponent(type.getName()))), true);

        GenerationReason reason = SimplyHotSpringsConfig.biomeReasons.get(biomeKey);
        source.sendFeedback(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                .appendSibling(makeHotSpringsReasonComponent(reason)), true);

        return reason.allowsGeneration() ? 1 : 0;
    }

    // biomeslist

    private static int sendAllKnownBiomes(CommandSource source, int page)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_BIOMESLIST + "all"), true);

        sendPaginatedComponents(source, SimplyHotSpringsConfig.biomeReasons.keySet(), sortKey(), key -> makeLocationInfoComponent(key.getLocation().toString()),
                page, "/" + COMMAND + " " + BIOMESLIST + " " + ALL);

        return SimplyHotSpringsConfig.biomeReasons.size();
    }

    private static int sendKnownBiomes(CommandSource source, boolean with, int page)
    {
        source.sendFeedback(new TranslationTextComponent(LANG_BIOMESLIST + (with ? "with" : "without"))
                .mergeStyle(with ? TextFormatting.GREEN : TextFormatting.DARK_RED), true);

        Set<ResourceLocation> filteredIds = SimplyHotSpringsConfig.biomeReasons.object2ObjectEntrySet().stream()
                .filter(entry -> with == entry.getValue().allowsGeneration())
                .map(entry -> entry.getKey().getLocation()).collect(Collectors.toSet());
        sendPaginatedComponents(source, filteredIds, ResourceLocation::compareNamespaced, id -> makeLocationInfoComponent(id.toString()),
                page, "/" + COMMAND + " " + BIOMESLIST + " " + (with ? WITH : WITHOUT));

        return filteredIds.size();
    }

    // biometypes

    private static int sendAllBiomeTypes(CommandSource source, int page)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_BIOMETYPES + "all"), true);

        sendPaginatedComponents(source, BiomeDictionary.Type.getAll(), sortType(), type -> makeBiomeTypeComponent(type.getName()),
                page, "/" + COMMAND + " " + BIOMETYPES);

        return BiomeDictionary.Type.getAll().size();
    }

    private static int sendBiomesOfType(CommandSource source, BiomeDictionary.Type type, int page)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_BIOMETYPES + "biomes", type.getName()), true);

        Set<RegistryKey<Biome>> biomeIds = BiomeDictionary.getBiomes(type);
        sendPaginatedComponents(source, biomeIds, sortKey(), key -> makeLocationInfoComponent(key.getLocation().toString()),
                page, "/" + COMMAND + " " + BIOMETYPES + " " + type);

        return biomeIds.size();
    }

    // placespring

    private static int placeSpring(CommandSource source, BlockPos pos)
    {
        ServerWorld world = source.getWorld();
        Random rand = new Random();

        pos = pos.add(-8, 0, -8);

        while (pos.getY() > 5 && world.isAirBlock(pos))
            pos = pos.down();

        pos = pos.down(rand.nextInt(3));

        boolean success = true;
        String reasonKey = "";
        if (pos.getY() > 4)
        {
            if (!HotSpringsFeature.doGenerate(world, rand, pos, false))
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

        IFormattableTextComponent message = makeTeleportComponent(LANG_PLACESPRING + (success ? "placed" : "not_placed"), pos.add(8, 0, 8));
        message.mergeStyle(success ? TextFormatting.GREEN : TextFormatting.RED);
        if (!reasonKey.isEmpty())
            message.appendSibling(new TranslationTextComponent(LANG_PLACESPRING + reasonKey));
        source.sendFeedback(message, true);

        return success ? 1 : 0;
    }

    // text component stuff

    /**
     * @return a TranslationTextComponent colored with TextFormatting.AQUA
     */
    private static IFormattableTextComponent makeAquaTranslatable(String key, Object... args)
    {
        return new TranslationTextComponent(key, args).mergeStyle(TextFormatting.AQUA);
    }

    /**
     * @return a text component encapsulating this {@link GenerationReason}
     */
    public static IFormattableTextComponent makeHotSpringsReasonComponent(GenerationReason reason)
    {
        return new TranslationTextComponent(reason.getYN()).mergeStyle(Style.EMPTY.setFormatting(reason.getTextFormatting())
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TranslationTextComponent(LANG_LOCATIONINFO + "reason").appendString("\n")
                                .appendSibling(new TranslationTextComponent(reason.getKey()).mergeStyle(reason.getTextFormatting())))));
    }

    /**
     * @return a StringTextComponent of toCopy that suggests itself to the chat box when you click it
     */
    private static IFormattableTextComponent makeSuggestComponent(String toCopy)
    {
        return new StringTextComponent(toCopy).mergeStyle(Style.EMPTY.setFormatting(TextFormatting.WHITE)
                .setHoverEvent(clickForSuggest).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy)));
    }

    private static final HoverEvent clickForSuggest = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_LOCATIONINFO + "click"));

    /**
     * @return a StringTextComponent of location that runs /simplyhotsprings locationinfo [location] when clicked
     */
    private static IFormattableTextComponent makeLocationInfoComponent(String location)
    {
        return new StringTextComponent(location)
                .mergeStyle(Style.EMPTY.setHoverEvent(clickForInfo)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + LOCATIONINFO + " " + location)));
    }

    private static final HoverEvent clickForInfo = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_BIOMESLIST + "click"));

    /**
     * @return a StringTextComponent of name that runs /simplyhotsprings biometypes [name] when clicked
     */
    private static IFormattableTextComponent makeBiomeTypeComponent(String name)
    {
        return new StringTextComponent(name)
                .mergeStyle(Style.EMPTY.setHoverEvent(clickForList)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + BIOMETYPES + " " + name)));
    }

    private static final HoverEvent clickForList = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_BIOMETYPES + "click"));

    /**
     * @return a TranslationTextComponent that teleports you to the pos when clicked
     */
    private static IFormattableTextComponent makeTeleportComponent(String key, BlockPos pos)
    {
        return new TranslationTextComponent(key, pos.getX(), pos.getY(), pos.getZ())
                .setStyle(Style.EMPTY.setHoverEvent(clickForTeleport)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ())));
    }

    private static final HoverEvent clickForTeleport = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_PLACESPRING + "click"));

    /**
     * @return a StringTextComponent of /simplyhotsprings help [subcommand] that runs itself when clicked
     */
    private static IFormattableTextComponent makeHelpComponent(String subcommand)
    {
        String command = "/" + COMMAND + " " + HELP + " " + subcommand;
        return new StringTextComponent(command)
                .mergeStyle(Style.EMPTY.setHoverEvent(clickForHelp)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
    }

    private static final HoverEvent clickForHelp = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_HELP + "click"));

    private static IFormattableTextComponent makePageComponent(int current, int max, String pageCommand)
    {
        return new TranslationTextComponent(LANG_COMMAND + "page_header",
                new StringTextComponent("<--").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY).setHoverEvent(clickForPrevious)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand + " " + Math.max(1, current - 1 < 1 ? max : current - 1)))),
                new StringTextComponent("-->").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY).setHoverEvent(clickForNext)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand + " " + (current + 1 > max ? 1 : current + 1)))),
                current, max).mergeStyle(TextFormatting.GRAY);
    }

    private static final HoverEvent clickForNext = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_COMMAND + "next_page"));
    private static final HoverEvent clickForPrevious = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_COMMAND + "previous_page"));

    private static IFormattableTextComponent noneComponent()
    {
        return new TranslationTextComponent(LANG_COMMAND + "none");
    }

    /**
     * sorts the collection, gets the items that will appear on the given page, turns them into components, and sends them.
     */
    private static <T> void sendPaginatedComponents(CommandSource source,
            Collection<T> collection,
            @Nullable Comparator<T> comparator,
            Function<T, ITextComponent> toTextComponent,
            int page,
            String pageCommand)
    {
        // recent chat is 10 lines, and there's a title and a page header
        int itemsPerPage = 8;

        int maxPage = collection.size() / itemsPerPage + (collection.size() % itemsPerPage == 0 ? 0 : 1) - 1;
        if (page > maxPage)
            page = maxPage;

        source.sendFeedback(makePageComponent(page + 1, maxPage + 1, pageCommand), true);

        if (collection.isEmpty())
            source.sendFeedback(new StringTextComponent(" ").appendSibling(noneComponent()), true);
        else
        {
            List<T> list = new ArrayList<T>(collection);
            list.sort(comparator);

            for (int i = page * itemsPerPage; i < list.size() && i < (page + 1) * itemsPerPage; i++)
                source.sendFeedback(new StringTextComponent(" ").appendSibling(toTextComponent.apply(list.get(i))), true);
        }
    }

    /**
     * sorts the collection, turns them into text components, and then puts them all into one text component separated by commas. <br>
     * it's {@link TextComponentUtils#makeSortedList}, but you can specify the comparator.
     */
    private static <T> ITextComponent makeMultiComponent(Collection<T> collection,
            @Nullable Comparator<T> comparator,
            Function<T, ITextComponent> toTextComponent)
    {
        List<T> list = new ArrayList<T>(collection);
        list.sort(comparator);
        return TextComponentUtils.func_240649_b_(list, toTextComponent);
    }

    private static Comparator<BiomeDictionary.Type> sortType()
    {
        return (type1, type2) -> type1.getName().compareTo(type2.getName());
    }

    private static <T> Comparator<RegistryKey<T>> sortKey()
    {
        return (key1, key2) -> key1.getLocation().compareNamespaced(key2.getLocation());
    }

}
