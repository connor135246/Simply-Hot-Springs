package connor135246.simplyhotsprings.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

public class SimplyHotSpringsCommand
{

    /** base command */
    public static final String COMMAND = SimplyHotSprings.MODID;
    /** command options */
    public static final String LOCATIONINFO = "locationinfo", BIOMESLIST = "biomeslist", BIOMETYPES = "biometypes",
            ALL = "all", WITH = "with", WITHOUT = "without";

    public static final String LANG_COMMAND = "commands." + SimplyHotSprings.MODID + ".";
    public static final String LANG_LOCATIONINFO = LANG_COMMAND + LOCATIONINFO + ".";
    public static final String LANG_BIOMESLIST = LANG_COMMAND + BIOMESLIST + ".";
    public static final String LANG_BIOMETYPES = LANG_COMMAND + BIOMETYPES + ".";

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal(COMMAND).requires((source) -> {
            return source.hasPermissionLevel(2);
        }).then(Commands.literal(LOCATIONINFO).executes((context) -> {
            return sendLocationInfo(context.getSource(), new BlockPos(context.getSource().getPos()));
        }).then(Commands.argument("target", EntityArgument.entity()).executes((context) -> {
            return sendLocationInfo(context.getSource(), EntityArgument.getEntity(context, "target").getPosition());
        })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> {
            return sendLocationInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
        })).then(Commands.argument("biome", ResourceLocationArgument.resourceLocation()).suggests(SuggestionProviders.field_239574_d_).executes((context) -> {
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
                }))));
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
        return sendLocationInfo(source, RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, biomeId));
    }

    private static int sendLocationInfo(CommandSource source, RegistryKey<Biome> biomeKey)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_name")
                .appendSibling(makeSuggestComponent(biomeKey.getLocation().toString())), true);

        source.sendFeedback(makeMultiComponent(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types"),
                BiomeDictionary.getTypes(biomeKey), type -> type.getName(), string -> makeSuggestComponent(string)), true);

        GenerationReason reason = SimplyHotSpringsConfig.biomeReasons.get(biomeKey);
        source.sendFeedback(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                .appendSibling(makeHotSpringsReasonComponent(reason)), true);

        return reason.allowsGeneration() ? 1 : 0;
    }

    // biomeslist

    private static int sendAllKnownBiomes(CommandSource source)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_BIOMESLIST + "all"), true);

        source.sendFeedback(makeMultiComponent(SimplyHotSpringsConfig.biomeReasons.keySet(),
                key -> key.getLocation(), id -> makeLocationInfoComponent(id.toString())), true);

        return SimplyHotSpringsConfig.biomeReasons.size();
    }

    private static int sendKnownBiomes(CommandSource source, boolean with)
    {
        source.sendFeedback(new TranslationTextComponent(LANG_BIOMESLIST + (with ? "with" : "without"))
                .mergeStyle(with ? TextFormatting.GREEN : TextFormatting.DARK_RED), true);

        Set<ResourceLocation> filteredIds = SimplyHotSpringsConfig.biomeReasons.object2ObjectEntrySet().stream()
                .filter(entry -> with == entry.getValue().allowsGeneration())
                .map(entry -> entry.getKey().getLocation()).collect(Collectors.toSet());
        source.sendFeedback(TextComponentUtils.makeSortedList(filteredIds, id -> makeLocationInfoComponent(id.toString())), true);

        return filteredIds.size();
    }

    // biometypes

    private static int sendAllBiomeTypes(CommandSource source)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_BIOMETYPES + "all"), true);

        source.sendFeedback(makeMultiComponent(BiomeDictionary.Type.getAll(), type -> type.getName(), string -> makeBiomeTypeComponent(string)), true);

        return BiomeDictionary.Type.getAll().size();
    }

    private static int sendBiomesOfType(CommandSource source, BiomeDictionary.Type type)
    {
        source.sendFeedback(makeAquaTranslatable(LANG_BIOMETYPES + "biomes", type.getName()), true);

        Set<RegistryKey<Biome>> biomeIds = BiomeDictionary.getBiomes(type);
        source.sendFeedback(makeMultiComponent(biomeIds, key -> key.getLocation(), id -> makeLocationInfoComponent(id.toString())), true);

        return biomeIds.size();
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
     * turns the collection of things into a list of comparable things, sorts them, turns them into text components, and then puts them all into one text component separated by
     * commas. see {@link TextComponentUtils#makeSortedList}
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
    private static <T, C extends Comparable<C>> ITextComponent makeMultiComponent(Collection<T> collection,
            Function<T, C> toComparable,
            Function<C, ITextComponent> toTextComponent)
    {
        return TextComponentUtils.makeSortedList(collection.stream().map(toComparable).collect(Collectors.toList()), toTextComponent);
    }

    /**
     * does {@link #makeMultiComponent(Collection, Function, Function)} and appends it to first
     */
    private static <T, C extends Comparable<C>> ITextComponent makeMultiComponent(IFormattableTextComponent first, Collection<T> collection,
            Function<T, C> toComparable,
            Function<C, ITextComponent> toTextComponent)
    {
        return first.appendSibling(makeMultiComponent(collection, toComparable, toTextComponent));
    }

}
