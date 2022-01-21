package connor135246.simplyhotsprings.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import connor135246.simplyhotsprings.common.world.HotSpringsWorldGen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommandSimplyHotSprings implements ICommand
{

    public static final CommandSimplyHotSprings INSTANCE = new CommandSimplyHotSprings();

    /** base command */
    public static final String COMMAND = Reference.MODID;
    /** command options */
    public static final String HELP = "help",
            LOCATIONINFO = "locationinfo",
            BIOMESLIST = "biomeslist", ALL = "all", WITH = "with", WITHOUT = "without",
            BIOMETYPES = "biometypes",
            PLACESPRING = "placespring";

    public static final String LANG_COMMAND = Reference.MODID + ".command.";
    public static final String LANG_LOCATIONINFO = LANG_COMMAND + LOCATIONINFO + ".";
    public static final String LANG_BIOMESLIST = LANG_COMMAND + BIOMESLIST + ".";
    public static final String LANG_BIOMETYPES = LANG_COMMAND + BIOMETYPES + ".";
    public static final String LANG_PLACESPRING = LANG_COMMAND + PLACESPRING + ".";
    public static final String LANG_HELP = LANG_COMMAND + HELP + ".";

    @Override
    public int compareTo(ICommand ico)
    {
        return this.getName().compareTo(ico.getName());
    }

    @Override
    public String getName()
    {
        return COMMAND;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + COMMAND + " " + HELP;
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.<String> emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw new WrongUsageException(getUsage(null));
        else if (args[0].equals(HELP))
            sendHelp(server, sender, args);
        else if (args[0].equals(LOCATIONINFO))
            sendLocationInfo(server, sender, args);
        else if (args[0].equals(BIOMESLIST))
            sendBiomesList(server, sender, args);
        else if (args[0].equals(BIOMETYPES))
            sendBiomeTypes(server, sender, args);
        else if (args[0].equals(PLACESPRING))
            placeSpring(server, sender, args);
        else
            throw new WrongUsageException(getUsage(null));
    }

    // help

    public void sendHelp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String subcommand = "";
        int helps = 3;
        if (args.length > 1)
        {
            if (args[1].equals(LOCATIONINFO))
            {
                subcommand = LOCATIONINFO;
                helps = 7;
            }
            else if (args[1].equals(BIOMESLIST))
            {
                subcommand = BIOMESLIST;
                helps = 5;
            }
            else if (args[1].equals(BIOMETYPES))
            {
                subcommand = BIOMETYPES;
                helps = 3;
            }
            else if (args[1].equals(PLACESPRING))
            {
                subcommand = PLACESPRING;
                helps = 1;
            }
        }

        if (subcommand.isEmpty())
        {
            sender.sendMessage(new TextComponentString("--- " + getUsage(sender) + " ---")
                    .setStyle(new Style().setColor(TextFormatting.GOLD)));

            sender.sendMessage(makeHelpComponent(LOCATIONINFO));
            sender.sendMessage(makeHelpComponent(BIOMESLIST));
            sender.sendMessage(makeHelpComponent(BIOMETYPES));
            sender.sendMessage(makeHelpComponent(PLACESPRING));
        }
        else
        {
            sender.sendMessage(new TextComponentString("--- " + getUsage(sender) + " " + subcommand + " ---")
                    .setStyle(new Style().setColor(TextFormatting.GOLD)));

            for (int i = 0; i <= helps; ++i)
            {
                sender.sendMessage(new TextComponentTranslation(LANG_HELP + subcommand + "." + i)
                        .setStyle(new Style().setColor(i % 2 == 1 ? TextFormatting.GRAY : TextFormatting.WHITE)));
            }
        }

        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, helps + 1);
    }

    // locationinfo

    public void sendLocationInfo(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        World world;
        BlockPos pos;
        Biome biome = null;

        if (args.length < 4)
        {
            if (args.length > 1)
            {
                ResourceLocation id = new ResourceLocation(args[1]);
                if (ForgeRegistries.BIOMES.containsKey(id))
                {
                    world = sender.getEntityWorld();
                    pos = sender.getPosition();
                    biome = ForgeRegistries.BIOMES.getValue(id);
                }
                else if (args[1].contains(":"))
                    throw new CommandException(LANG_LOCATIONINFO + "biome_not_found", args[1]);
                else
                {
                    Entity entity = CommandBase.getEntity(server, sender, args[1]);
                    world = entity.getEntityWorld();
                    pos = entity.getPosition();
                }
            }
            else
            {
                world = sender.getEntityWorld();
                pos = sender.getPosition();
            }
        }
        else
        {
            world = sender.getEntityWorld();
            pos = CommandBase.parseBlockPos(sender, args, 1, true);
        }

        if (biome == null)
        {
            if (!world.isBlockLoaded(pos))
                throw new CommandException(LANG_LOCATIONINFO + "block_out_of_world");
            biome = world.getBiomeForCoordsBody(pos);
        }

        sender.sendMessage(makeAquaTranslatable(LANG_LOCATIONINFO + "dim_id")
                .appendSibling(makeSuggestComponent(world.provider.getDimension() + "")));

        GenerationReason reason = SimplyHotSpringsConfig.getGenerationReasonWorld(world);
        if (reason.allowsGeneration())
        {
            reason = SimplyHotSpringsConfig.biomeReasons.get(biome.getRegistryName());

            sender.sendMessage(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_name")
                    .appendSibling(makeSuggestComponent(biome.getRegistryName().toString())));

            sender.sendMessage(makeMultiComponent(makeAquaTranslatable(LANG_LOCATIONINFO + "biome_types"),
                    BiomeDictionary.getTypes(biome), type -> type.getName(), string -> makeSuggestComponent(string)));
        }

        sender.sendMessage(makeAquaTranslatable(LANG_LOCATIONINFO + "hot_springs")
                .appendSibling(makeHotSpringsReasonComponent(reason)));

        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, reason.allowsGeneration() ? 1 : 0);
    }

    // biomeslist

    public void sendBiomesList(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw new WrongUsageException(getUsage(sender) + " " + BIOMESLIST);
        else if (args[1].equals(ALL))
        {
            sender.sendMessage(makeAquaTranslatable(LANG_BIOMESLIST + "all"));

            sender.sendMessage(makeSortedList(SimplyHotSpringsConfig.biomeReasons.keySet(), id -> makeLocationInfoComponent(id.toString())));

            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, SimplyHotSpringsConfig.biomeReasons.size());
        }
        else
        {
            boolean with;
            if (args[1].equals(WITH))
                with = true;
            else if (args[1].equals(WITHOUT))
                with = false;
            else
                throw new WrongUsageException(getUsage(sender) + " " + BIOMESLIST);

            sender.sendMessage(new TextComponentTranslation(LANG_BIOMESLIST + (with ? "with" : "without"))
                    .setStyle(new Style().setColor(with ? TextFormatting.GREEN : TextFormatting.DARK_RED)));

            Set<ResourceLocation> filteredIds = SimplyHotSpringsConfig.biomeReasons.object2ObjectEntrySet().stream()
                    .filter(entry -> with == entry.getValue().allowsGeneration())
                    .map(entry -> entry.getKey()).collect(Collectors.toSet());
            sender.sendMessage(makeSortedList(filteredIds, id -> makeLocationInfoComponent(id.toString())));

            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, filteredIds.size());
        }
    }

    // biometypes

    public void sendBiomeTypes(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 1)
        {
            sender.sendMessage(makeAquaTranslatable(LANG_BIOMETYPES + "all"));

            sender.sendMessage(makeMultiComponent(BiomeDictionary.Type.getAll(), type -> type.getName(), string -> makeBiomeTypeComponent(string)));

            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, BiomeDictionary.Type.getAll().size());
        }
        else
        {
            for (BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
                if (type.getName().equals(args[1]))
                {
                    sender.sendMessage(makeAquaTranslatable(LANG_BIOMETYPES + "biomes", type.getName()));

                    Set<Biome> biomes = BiomeDictionary.getBiomes(type);
                    sender.sendMessage(makeMultiComponent(biomes, biome -> biome.getRegistryName(), id -> makeLocationInfoComponent(id.toString())));

                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, biomes.size());
                    return;
                }
            throw new CommandException(LANG_BIOMETYPES + "type_not_found", args[1]);
        }
    }

    // placespring

    public void placeSpring(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4)
            throw new WrongUsageException(getUsage(sender) + " " + PLACESPRING);

        World world = sender.getEntityWorld();
        BlockPos pos = CommandBase.parseBlockPos(sender, args, 1, true);
        Random rand = new Random();

        pos = pos.add(-8, 0, -8);

        while (pos.getY() > 5 && world.isAirBlock(pos))
            pos = pos.down();
        
        pos = pos.down(rand.nextInt(3));

        boolean success = true;
        String reasonKey = "";
        if (pos.getY() > 4)
        {
            if (!HotSpringsWorldGen.doGenerate(rand, world, pos, null))
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

        ITextComponent message = makeTeleportComponent(LANG_PLACESPRING + (success ? "placed" : "not_placed"), pos.add(8, 0, 8));
        message.getStyle().setColor(success ? TextFormatting.GREEN : TextFormatting.RED);
        if (!reasonKey.isEmpty())
            message.appendSibling(new TextComponentTranslation(LANG_PLACESPRING + reasonKey));
        sender.sendMessage(message);

        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, success ? 1 : 0);
    }

    //

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 1)
            return CommandBase.getListOfStringsMatchingLastWord(args, LOCATIONINFO, BIOMESLIST, BIOMETYPES, PLACESPRING, HELP);
        else if (args.length > 1)
        {
            if (args[0].equals(LOCATIONINFO))
            {
                if (args.length == 2)
                    return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BIOMES.getKeys());
                else if (args.length >= 2 && args.length <= 4 && !args[1].contains(":"))
                    return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
            }
            else if (args[0].equals(BIOMESLIST))
            {
                if (args.length == 2)
                    return CommandBase.getListOfStringsMatchingLastWord(args, ALL, WITH, WITHOUT);
            }
            else if (args[0].equals(BIOMETYPES))
            {
                if (args.length == 2)
                    return CommandBase.getListOfStringsMatchingLastWord(args, BiomeDictionary.Type.getAll());
            }
            else if (args[0].equals(PLACESPRING))
            {
                if (args.length >= 2 && args.length <= 4)
                    return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
            }
            else if (args[0].equals(HELP))
            {
                if (args.length == 2)
                    return CommandBase.getListOfStringsMatchingLastWord(args, LOCATIONINFO, BIOMESLIST, BIOMETYPES, PLACESPRING);
            }
        }
        return Collections.<String> emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1 && args.length == 2 && args[0].equals(LOCATIONINFO) && !args[1].contains(":");
    }

    // text component stuff

    /**
     * @return a TextComponentTranslation colored with TextFormatting.AQUA
     */
    private static ITextComponent makeAquaTranslatable(String key, Object... args)
    {
        return new TextComponentTranslation(key, args).setStyle(new Style().setColor(TextFormatting.AQUA));
    }

    /**
     * @return a text component encapsulating this {@link GenerationReason}
     */
    public static ITextComponent makeHotSpringsReasonComponent(GenerationReason reason)
    {
        return new TextComponentTranslation(reason.getYN()).setStyle(new Style().setColor(reason.getTextFormatting())
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponentTranslation(LANG_LOCATIONINFO + "reason").appendText("\n")
                                .appendSibling(new TextComponentTranslation(reason.getKey()).setStyle(new Style().setColor(reason.getTextFormatting()))))));
    }

    /**
     * @return a TextComponentString of toCopy that suggests itself to the chat box when you click it
     */
    private static ITextComponent makeSuggestComponent(String toCopy)
    {
        return new TextComponentString(toCopy).setStyle(new Style().setColor(TextFormatting.WHITE)
                .setHoverEvent(clickForSuggest).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy)));
    }

    private static final HoverEvent clickForSuggest = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(LANG_LOCATIONINFO + "click"));

    /**
     * @return a TextComponentString of location that runs /simplyhotsprings locationinfo [location] when clicked
     */
    private static ITextComponent makeLocationInfoComponent(String location)
    {
        return new TextComponentString(location)
                .setStyle(new Style().setHoverEvent(clickForInfo)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + LOCATIONINFO + " " + location)));
    }

    private static final HoverEvent clickForInfo = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(LANG_BIOMESLIST + "click"));

    /**
     * @return a TextComponentString of name that runs /simplyhotsprings biometypes [name] when clicked
     */
    private static ITextComponent makeBiomeTypeComponent(String name)
    {
        return new TextComponentString(name)
                .setStyle(new Style().setHoverEvent(clickForList)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + BIOMETYPES + " " + name)));
    }

    private static final HoverEvent clickForList = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(LANG_BIOMETYPES + "click"));

    /**
     * @return a TextComponentTranslation that teleports you to the pos when clicked
     */
    private static ITextComponent makeTeleportComponent(String key, BlockPos pos)
    {
        return new TextComponentTranslation(key, pos.getX(), pos.getY(), pos.getZ())
                .setStyle(new Style().setHoverEvent(clickForTeleport)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ())));
    }

    private static final HoverEvent clickForTeleport = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(LANG_PLACESPRING + "click"));

    /**
     * @return a TextComponentString of /simplyhotsprings help [subcommand] that runs itself when clicked
     */
    private static ITextComponent makeHelpComponent(String subcommand)
    {
        String command = "/" + COMMAND + " " + HELP + " " + subcommand;
        return new TextComponentString(command)
                .setStyle(new Style().setHoverEvent(clickForHelp)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
    }

    private static final HoverEvent clickForHelp = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(LANG_HELP + "click"));

    /**
     * turns the collection of things into a list of comparable things, sorts them, turns them into text components, and then puts them all into one text component separated by
     * commas. see {@link #makeSortedList}
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
        return makeSortedList(collection.stream().map(toComparable).collect(Collectors.toList()), toTextComponent);
    }

    /**
     * does {@link #makeMultiComponent(Collection, Function, Function)} and appends it to first
     */
    private static <T, C extends Comparable<C>> ITextComponent makeMultiComponent(ITextComponent first, Collection<T> collection,
            Function<T, C> toComparable,
            Function<C, ITextComponent> toTextComponent)
    {
        return first.appendSibling(makeMultiComponent(collection, toComparable, toTextComponent));
    }

    // --- these two methods are copy-pasted from 1.16.5 net.minecraft.util.text.TextComponentUtils ---

    public static <T extends Comparable<T>> ITextComponent makeSortedList(Collection<T> collection, Function<T, ITextComponent> toTextComponent)
    {
        if (collection.isEmpty())
            return new TextComponentString("");
        else if (collection.size() == 1)
            return toTextComponent.apply(collection.iterator().next());
        else
        {
            List<T> list = Lists.newArrayList(collection);
            list.sort(Comparable::compareTo);
            return func_240649_b_(list, toTextComponent);
        }
    }

    public static <T> ITextComponent func_240649_b_(Collection<T> collection, Function<T, ITextComponent> toTextComponent)
    {
        if (collection.isEmpty())
            return new TextComponentString("");
        else if (collection.size() == 1)
            return toTextComponent.apply(collection.iterator().next()).createCopy();
        else
        {
            ITextComponent itextcomponent = new TextComponentString("");
            boolean first = true;

            for (T t : collection)
            {
                if (!first)
                    itextcomponent.appendSibling((new TextComponentString(", ")).setStyle(new Style().setColor(TextFormatting.GRAY)));

                itextcomponent.appendSibling(toTextComponent.apply(t));
                first = false;
            }

            return itextcomponent;
        }
    }

}
