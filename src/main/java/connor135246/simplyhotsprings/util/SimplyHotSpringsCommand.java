package connor135246.simplyhotsprings.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class SimplyHotSpringsCommand
{

    /** base command */
    public static final String COMMAND = SimplyHotSprings.MODID;
    /** command options */
    public static final String HELP = "help",
            LOCATIONINFO = "locationinfo",
            BIOMESLIST = "biomeslist", ALL = "all", WITH = "with", WITHOUT = "without",
            BIOMETAGS = "biometags";

    public static final String LANG_COMMAND = "commands." + SimplyHotSprings.MODID + ".";
    public static final String LANG_LOCATIONINFO = LANG_COMMAND + LOCATIONINFO + ".";
    public static final String LANG_BIOMESLIST = LANG_COMMAND + BIOMESLIST + ".";
    public static final String LANG_BIOMETAGS = LANG_COMMAND + BIOMETAGS + ".";
    public static final String LANG_HELP = LANG_COMMAND + HELP + ".";
    public static final String NONE = LANG_COMMAND + "none";
    public static final String PLACE = "place";

    public static final SuggestionProvider<CommandSourceStack> ALL_MODIFIERS = SuggestionProviders
            .register(new ResourceLocation(SimplyHotSprings.MODID, "all_modifiers"), (context, builder) -> {
                return SharedSuggestionProvider.suggestResource(SimplyHotSpringsConfig.biomeModifierReasons.columnKeySet(), builder);
            });

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
        })).then(Commands.literal(BIOMETAGS).executes((context) -> {
            return sendHelpForSubcommand(context.getSource(), BIOMETAGS, 3);
        })).then(Commands.literal(PLACE).executes((context) -> {
            return sendHelpForSubcommand(context.getSource(), PLACE, 1);
        }))).then(Commands.literal(LOCATIONINFO).executes((context) -> {
            return sendLocationInfo(context.getSource(), getSourcePos(context.getSource()));
        }).then(Commands.argument("target", EntityArgument.entity()).executes((context) -> {
            return sendLocationInfo(context.getSource(), EntityArgument.getEntity(context, "target").blockPosition());
        })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
            return sendLocationInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
        })).then(Commands.argument("biome", ResourceKeyArgument.key(Registry.BIOME_REGISTRY)).executes((context) -> {
            return sendLocationInfo(context.getSource(), getResourceKey(context, "biome", Registry.BIOME_REGISTRY, ERROR_BIOME_INVALID));
        }))).then(Commands.literal(BIOMESLIST).then(withPageArg(Commands.literal(ALL), (context, page) -> {
            return sendAllKnownBiomes(context.getSource(), page);
        })).then(Commands.literal(WITH).then(withPageArg(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(ALL_MODIFIERS), (context, page) -> {
            return sendKnownBiomesForModifer(context.getSource(), context.getArgument("modifier", ResourceLocation.class), true, page);
        }))).then(Commands.literal(WITHOUT).then(withPageArg(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(ALL_MODIFIERS), (context, page) -> {
            return sendKnownBiomesForModifer(context.getSource(), context.getArgument("modifier", ResourceLocation.class), false, page);
        })))).then(withPageArg(Commands.literal(BIOMETAGS), (context, page) -> {
            return sendAllBiomeTags(context.getSource(), page);
        }).then(withPageArg(Commands.argument("biome_tag", TagKeyArgument.tagKeyArgument(Registry.BIOME_REGISTRY)), (context, page) -> {
            return sendBiomesOfTag(context.getSource(), TagKeyArgument.get(context, "biome_tag", Registry.BIOME_REGISTRY, ERROR_BIOME_TAG_INVALID), page);
        }))));
    }

    /**
     * From {@link ResourceKeyArgument#getRegistryType}, casts the ResourceKey to its registry.
     */
    public static <T> ResourceKey<T> getResourceKey(CommandContext<CommandSourceStack> context, String name, ResourceKey<Registry<T>> registry,
            DynamicCommandExceptionType exception) throws CommandSyntaxException
    {
        ResourceKey<?> result = context.getArgument(name, ResourceKey.class);
        Optional<ResourceKey<T>> optional = result.cast(registry);
        return optional.orElseThrow(() -> {
            return exception.create(result.location());
        });
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
            CommandContextIntFunction command)
    {
        return argument.executes((context) -> command.applyAsInt(context, 0))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes((context) -> command.applyAsInt(context, IntegerArgumentType.getInteger(context, "page") - 1)));
    }

    /**
     * Just used as a {@link ToIntBiFunction} that throws a {@link CommandSyntaxException}.
     */
    @FunctionalInterface
    private static interface CommandContextIntFunction
    {
        public int applyAsInt(CommandContext<CommandSourceStack> context, int value) throws CommandSyntaxException;
    }

    // help

    private static int sendHelp(CommandSourceStack source)
    {
        source.sendSuccess(Component.literal("--- " + "/" + COMMAND + " " + HELP + " ---").withStyle(ChatFormatting.GOLD), false);

        source.sendSuccess(makeHelpComponent(LOCATIONINFO), false);
        source.sendSuccess(makeHelpComponent(BIOMESLIST), false);
        source.sendSuccess(makeHelpComponent(BIOMETAGS), false);
        source.sendSuccess(Component.literal("/place feature simplyhotsprings:hot_springs_default").setStyle(Style.EMPTY.withHoverEvent(clickForHelp)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + HELP + " place"))), false);

        return 4;
    }

    private static int sendHelpForSubcommand(CommandSourceStack source, String subcommand, int helps)
    {
        source.sendSuccess(Component.literal("--- " + "/" + COMMAND + " " + HELP + " " + subcommand + " ---").withStyle(ChatFormatting.GOLD), false);

        for (int i = 0; i <= helps; ++i)
            source.sendSuccess(Component.translatable(LANG_HELP + subcommand + "." + i)
                    .withStyle(i % 2 == 1 ? ChatFormatting.GRAY : ChatFormatting.WHITE), false);

        return helps + 1;

    }

    // locationinfo

    private static int sendLocationInfo(CommandSourceStack source, BlockPos pos)
    {
        return sendLocationInfo(source, source.getLevel().getBiome(pos));
    }

    private static int sendLocationInfo(CommandSourceStack source, ResourceKey<Biome> biomeKey) throws CommandSyntaxException
    {
        Registry<Biome> biomeReg = source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        return sendLocationInfo(source, biomeReg.getHolder(biomeKey).orElseThrow(() -> ERROR_BIOME_INVALID.create(biomeKey)));
    }

    private static int sendLocationInfo(CommandSourceStack source, Holder<Biome> biome)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_name")
                .append(makeSuggestComponent(source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome.value()).toString())), false);

        List<TagKey<Biome>> tags = biome.getTagKeys().collect(Collectors.toList());
        if (tags.isEmpty())
            source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_tags").append(noneComponent().withStyle(ChatFormatting.WHITE)), false);
        else
            source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_tags")
                    .append(makeMultiComponent(tags, sortTag(), tag -> makeSuggestComponent("#" + tag.location()))), false);

        if (SimplyHotSpringsConfig.biomeModifierReasons.isEmpty())
        {
            source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                    .append(noneHotSpringsReasonComponent(GenerationReason.NO_HOT_SPRINGS)), false);
            return 0;
        }
        else
        {
            Map<ResourceLocation, GenerationReason> map = SimplyHotSpringsConfig.biomeModifierReasons.row(biome);
            if (map.isEmpty())
                source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                        .append(noneHotSpringsReasonComponent(GenerationReason.UNKNOWN_BIOME)), false);
            else
                source.sendSuccess(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                        .append(makeMultiComponent(map.keySet(), ResourceLocation::compareNamespaced,
                                id -> textHotSpringsReasonComponent(id.toString(), map.containsKey(id) ? map.get(id) : GenerationReason.UNKNOWN_MODIFIER))),
                        false);

            return Ints.saturatedCast(map.values().stream().filter(reason -> reason.allowsGeneration()).count());
        }
    }

    // biomeslist

    private static int sendAllKnownBiomes(CommandSourceStack source, int page)
    {
        if (SimplyHotSpringsConfig.biomeModifierReasons.rowKeySet().isEmpty())
        {
            source.sendFailure(Component.translatable(GenerationReason.NO_HOT_SPRINGS.getKey()));
            return 0;
        }

        source.sendSuccess(makeAquaTranslatable(LANG_BIOMESLIST + "all"), false);

        Set<ResourceLocation> biomes = SimplyHotSpringsConfig.biomeModifierReasons.rowKeySet().stream()
                .map(biome -> biome.unwrapKey().get().location()).collect(Collectors.toSet());
        sendPaginatedComponents(source, biomes, ResourceLocation::compareNamespaced, id -> makeLocationInfoComponent(id.toString()),
                page, "/" + COMMAND + " " + BIOMESLIST + " " + ALL);

        return biomes.size();
    }

    private static int sendKnownBiomesForModifer(CommandSourceStack source, ResourceLocation modifier, boolean with, int page) throws CommandSyntaxException
    {
        if (!SimplyHotSpringsConfig.biomeModifierReasons.columnKeySet().contains(modifier))
        {
            source.sendFailure(Component.translatable(LANG_BIOMESLIST + "modifier_not_found", modifier));
            return 0;
        }

        source.sendSuccess(Component.translatable(LANG_BIOMESLIST + (with ? "with" : "without"), modifier)
                .withStyle(with ? ChatFormatting.GREEN : ChatFormatting.DARK_RED), false);

        Set<ResourceLocation> filteredIds = SimplyHotSpringsConfig.biomeModifierReasons.column(modifier).entrySet().stream()
                .filter(entry -> with == entry.getValue().allowsGeneration())
                .map(entry -> entry.getKey().unwrapKey().get().location()).collect(Collectors.toSet());
        sendPaginatedComponents(source, filteredIds, ResourceLocation::compareNamespaced, id -> makeLocationInfoComponent(id.toString()),
                page, "/" + COMMAND + " " + BIOMESLIST + " " + (with ? WITH : WITHOUT) + " " + modifier);

        return filteredIds.size();
    }

    // biometags

    private static int sendAllBiomeTags(CommandSourceStack source, int page)
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMETAGS + "all"), false);

        List<TagKey<Biome>> tags = source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getTagNames().collect(Collectors.toList());
        sendPaginatedComponents(source, tags, sortTag(), tag -> makeBiomeTagComponent("#" + tag.location()),
                page, "/" + COMMAND + " " + BIOMETAGS);

        return tags.size();
    }

    private static int sendBiomesOfTag(CommandSourceStack source, TagKey<Biome> tagKey, int page) throws CommandSyntaxException
    {
        source.sendSuccess(makeAquaTranslatable(LANG_BIOMETAGS + "biomes", "#" + tagKey.location()), false);

        Set<ResourceLocation> biomes = source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getTag(tagKey)
                .orElseThrow(() -> ERROR_BIOME_TAG_INVALID.create("#" + tagKey.location()))
                .stream().map(biome -> biome.unwrapKey().get().location()).collect(Collectors.toSet());
        sendPaginatedComponents(source, biomes, ResourceLocation::compareNamespaced,
                id -> makeLocationInfoComponent(id.toString()), page, "/" + COMMAND + " " + BIOMETAGS + " #" + tagKey.location());

        return biomes.size();
    }

    // text component stuff

    /**
     * @return a TranslatableComponent colored with ChatFormatting.AQUA
     */
    private static MutableComponent makeAquaTranslatable(String key, Object... args)
    {
        return Component.translatable(key, args).withStyle(ChatFormatting.AQUA);
    }

    /**
     * @return the component with a hover text encapsulating this {@link GenerationReason}
     */
    private static MutableComponent makeHotSpringsReasonComponent(MutableComponent component, GenerationReason reason)
    {
        return component.setStyle(Style.EMPTY.applyFormat(reason.getTextFormatting())
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(reason.getYN()).withStyle(reason.getTextFormatting()).append(": \n")
                                .append(Component.translatable(reason.getKey()).withStyle(reason.getTextFormatting())))));
    }

    /**
     * @return a text component of name with a hover text encapsulating this {@link GenerationReason}
     */
    private static MutableComponent textHotSpringsReasonComponent(String name, GenerationReason reason)
    {
        return makeHotSpringsReasonComponent(Component.literal(name), reason);
    }

    /**
     * @return a none component with a hover text encapsulating this {@link GenerationReason}
     */
    private static MutableComponent noneHotSpringsReasonComponent(GenerationReason reason)
    {
        return makeHotSpringsReasonComponent(noneComponent(), reason);
    }

    /**
     * @return a TextComponent of toCopy that suggests itself to the chat box when you click it
     */
    private static MutableComponent makeSuggestComponent(String toCopy)
    {
        return Component.literal(toCopy).setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)
                .withHoverEvent(clickForSuggest).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy)));
    }

    private static final HoverEvent clickForSuggest = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LANG_LOCATIONINFO + "click"));

    /**
     * @return a TextComponent of location that runs /simplyhotsprings locationinfo [location] when clicked
     */
    private static MutableComponent makeLocationInfoComponent(String location)
    {
        return Component.literal(location)
                .setStyle(Style.EMPTY.withHoverEvent(clickForInfo)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + LOCATIONINFO + " " + location)));
    }

    private static final HoverEvent clickForInfo = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LANG_BIOMESLIST + "click"));

    /**
     * @return a TextComponent of name that runs /simplyhotsprings biometags [name] when clicked
     */
    private static MutableComponent makeBiomeTagComponent(String name)
    {
        return Component.literal(name)
                .setStyle(Style.EMPTY.withHoverEvent(clickForList)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + BIOMETAGS + " " + name)));
    }

    private static final HoverEvent clickForList = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LANG_BIOMETAGS + "click"));

    /**
     * @return a TextComponent of /simplyhotsprings help [subcommand] that runs itself when clicked
     */
    private static MutableComponent makeHelpComponent(String subcommand)
    {
        String command = "/" + COMMAND + " " + HELP + " " + subcommand;
        return Component.literal(command)
                .setStyle(Style.EMPTY.withHoverEvent(clickForHelp)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
    }

    private static final HoverEvent clickForHelp = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LANG_HELP + "click"));

    private static MutableComponent makePageComponent(int current, int max, String pageCommand)
    {
        return Component.translatable(LANG_COMMAND + "page_header",
                Component.literal("<--").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY).withHoverEvent(clickForPrevious)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand + " " + Math.max(1, current - 1 < 1 ? max : current - 1)))),
                Component.literal("-->").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY).withHoverEvent(clickForNext)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand + " " + (current + 1 > max ? 1 : current + 1)))),
                current, max).withStyle(ChatFormatting.GRAY);
    }

    private static final HoverEvent clickForNext = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LANG_COMMAND + "next_page"));
    private static final HoverEvent clickForPrevious = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LANG_COMMAND + "previous_page"));

    private static MutableComponent noneComponent()
    {
        return Component.translatable(LANG_COMMAND + "none");
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
            source.sendSuccess(Component.literal(" ").append(noneComponent()), false);
        else
        {
            List<T> list = new ArrayList<T>(collection);
            list.sort(comparator);

            for (int i = page * itemsPerPage; i < list.size() && i < (page + 1) * itemsPerPage; i++)
                source.sendSuccess(Component.literal(" ").append(toTextComponent.apply(list.get(i))), false);
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

    private static Comparator<TagKey<Biome>> sortTag()
    {
        return (tag1, tag2) -> tag1.location().compareNamespaced(tag2.location());
    }

    private static final DynamicCommandExceptionType ERROR_BIOME_INVALID = new DynamicCommandExceptionType((args) -> {
        return Component.translatable(LANG_LOCATIONINFO + "biome_not_found", args);
    });

    private static final DynamicCommandExceptionType ERROR_BIOME_TAG_INVALID = new DynamicCommandExceptionType((args) -> {
        return Component.translatable(LANG_BIOMETAGS + "tag_not_found", args);
    });

}
