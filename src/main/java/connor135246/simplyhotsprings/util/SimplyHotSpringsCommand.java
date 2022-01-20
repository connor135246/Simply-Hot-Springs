package connor135246.simplyhotsprings.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

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
    public static final String COMMAND = "simplyhotsprings";
    /** command options */
    public static final String LOCATIONINFO = "locationinfo", BIOMESLIST = "biomeslist", ALL = "all", WITH = "with", WITHOUT = "without";

    protected static final String LANG_COMMAND = "commands." + SimplyHotSprings.MODID + ".";
    protected static final String LANG_LOCATIONINFO = LANG_COMMAND + "locationinfo.";
    protected static final String LANG_BIOMESLIST = LANG_COMMAND + "biomeslist.";

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
                    return listAllBiomes(context.getSource());
                })).then(Commands.literal(WITH).executes((context) -> {
                    return listBiomes(context.getSource(), true);
                })).then(Commands.literal(WITHOUT).executes((context) -> {
                    return listBiomes(context.getSource(), false);
                }))));
    }

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
        source.sendFeedback(makeSuggestComponent(LANG_LOCATIONINFO + "biome_name",
                biomeKey.getLocation().toString()), true);

        source.sendFeedback(makeSuggestComponent(LANG_LOCATIONINFO + "biome_types",
                joinNiceString(BiomeDictionary.getTypes(biomeKey), BiomeDictionary.Type::getName)), true);

        GenerationReason reason = SimplyHotSpringsConfig.biomeReasons.get(biomeKey);
        source.sendFeedback(new TranslationTextComponent(LANG_LOCATIONINFO + "hot_springs")
                .mergeStyle(Style.EMPTY.applyFormatting(TextFormatting.AQUA)
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TranslationTextComponent(LANG_LOCATIONINFO + "reason").appendString("\n")
                                        .appendSibling(new TranslationTextComponent(reason.getKey())
                                                .mergeStyle(reason.getTextFormatting())))))
                .appendSibling(new TranslationTextComponent(reason.getYN())
                        .mergeStyle(reason.getTextFormatting())),
                true);

        return 1;
    }

    private static final HoverEvent clickForSuggest = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_LOCATIONINFO + "click"));

    private static ITextComponent makeSuggestComponent(String key, String suggest)
    {
        return new TranslationTextComponent(key)
                .mergeStyle(Style.EMPTY.applyFormatting(TextFormatting.AQUA)
                        .setHoverEvent(clickForSuggest)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest)))
                .appendSibling(new StringTextComponent(suggest)
                        .mergeStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE)));
    }

    private static int listAllBiomes(CommandSource source)
    {
        source.sendFeedback(new TranslationTextComponent(LANG_BIOMESLIST + "all").mergeStyle(TextFormatting.AQUA), true);
        source.sendFeedback(TextComponentUtils.makeSortedList(SimplyHotSpringsConfig.biomeReasons.keySet().stream()
                .collect(ArrayList<ResourceLocation>::new, (list, key) -> list.add(key.getLocation()), (left, right) -> left.addAll(right)),
                id -> makeLocationInfoComponent(id.toString())), true);
        return 1;
    }

    private static int listBiomes(CommandSource source, boolean with)
    {
        source.sendFeedback(new TranslationTextComponent(LANG_BIOMESLIST + (with ? "with" : "without"))
                .mergeStyle(with ? TextFormatting.GREEN : TextFormatting.DARK_RED), true);
        source.sendFeedback(TextComponentUtils.makeSortedList(SimplyHotSpringsConfig.biomeReasons.object2ObjectEntrySet().stream()
                .filter(entry -> with == entry.getValue().allowsGeneration())
                .collect(ArrayList<ResourceLocation>::new, (list, entry) -> list.add(entry.getKey().getLocation()), (left, right) -> left.addAll(right)),
                id -> makeLocationInfoComponent(id.toString())), true);
        return 1;
    }

    private static final HoverEvent clickForInfo = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(LANG_BIOMESLIST + "click"));

    private static ITextComponent makeLocationInfoComponent(String location)
    {
        return new StringTextComponent(location)
                .mergeStyle(Style.EMPTY.setHoverEvent(clickForInfo)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + LOCATIONINFO + " " + location)));
    }

    /**
     * {@link net.minecraft.util.text.TextComponentUtils#func_240649_b_} but with Strings instead of ITextComponents
     */
    public static <T> String joinNiceString(Collection<T> list, Function<T, String> toString)
    {
        if (list.isEmpty())
            return "";
        else if (list.size() == 1)
            return toString.apply(list.iterator().next());
        else
        {
            StringBuilder builder = new StringBuilder();
            boolean first = true;

            for (T t : list)
            {
                if (!first)
                    builder.append(", ");

                builder.append(toString.apply(t));
                first = false;
            }

            return builder.toString();
        }
    }

}
