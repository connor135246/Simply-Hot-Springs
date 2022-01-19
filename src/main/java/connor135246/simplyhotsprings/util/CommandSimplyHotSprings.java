package connor135246.simplyhotsprings.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraftforge.common.DimensionManager;

public class CommandSimplyHotSprings implements ICommand
{

    public static final CommandSimplyHotSprings INSTANCE = new CommandSimplyHotSprings();

    public static final String LOCATIONINFO = "locationinfo";

    protected static final String LANG_COMMAND = Reference.MODID + ".command.";

    @Override
    public int compareTo(ICommand ico)
    {
        return this.getName().compareTo(ico.getName());
    }

    @Override
    public String getName()
    {
        return Reference.MODID;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + Reference.MODID + " " + LOCATIONINFO + " [player]"
                + " OR /" + Reference.MODID + " " + LOCATIONINFO + " <x> <y> <z>"
                + " OR /" + Reference.MODID + " " + LOCATIONINFO + " <biome> [<dimension_id>]";
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.<String> emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0 && args[0].equals(LOCATIONINFO))
        {
            World world;
            BlockPos pos;
            Biome biome = null;

            if (args.length < 4)
            {
                if (args.length > 1)
                {
                    ResourceLocation id = new ResourceLocation(args[1]);
                    if (Biome.REGISTRY.containsKey(id))
                    {
                        pos = sender.getPosition();
                        biome = Biome.REGISTRY.getObject(id);

                        if (args.length > 2)
                        {
                            int dimId = CommandBase.parseInt(args[2]);
                            if (!DimensionManager.isDimensionRegistered(dimId))
                                throw new CommandException(LANG_COMMAND + "dim_not_found", dimId);
                            world = DimensionManager.getWorld(dimId);
                            if (world == null)
                                throw new CommandException(LANG_COMMAND + "dim_not_created", DimensionManager.getProviderType(dimId).getName(), dimId);
                        }
                        else
                            world = sender.getEntityWorld();
                    }
                    else if (args[1].contains(":"))
                        throw new CommandException(LANG_COMMAND + "biome_not_found", args[1]);
                    else
                    {
                        EntityPlayerMP player = CommandBase.getPlayer(server, sender, args[1]);
                        world = player.world;
                        pos = player.getPosition();
                    }
                }
                else
                {
                    EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);
                    world = player.world;
                    pos = player.getPosition();
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
                    throw new CommandException(LANG_COMMAND + "block_out_of_world");
                biome = world.getBiomeForCoordsBody(pos);
            }

            sender.sendMessage(makeHoverAndClickComponent(LANG_COMMAND + "dim_id",
                    world.provider.getDimension() + ""));

            sender.sendMessage(makeHoverAndClickComponent(LANG_COMMAND + "biome_name",
                    biome.getRegistryName().toString()));

            sender.sendMessage(makeHoverAndClickComponent(LANG_COMMAND + "biome_types",
                    CommandBase.joinNiceString(BiomeDictionary.getTypes(biome).toArray())));

            GenerationReason reason = SimplyHotSpringsConfig.WorldGen.getGenerationReason(world, biome);
            ITextComponent hotSpringMessage = new TextComponentTranslation(LANG_COMMAND + "hot_springs")
                    .setStyle(new Style().setColor(TextFormatting.AQUA).setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new TextComponentTranslation(LANG_COMMAND + "reason").appendText("\n")
                                            .appendSibling(new TextComponentTranslation(reason.getKey())
                                                    .setStyle(new Style().setColor(reason.getTextFormatting()))))))
                    .appendSibling(new TextComponentTranslation(reason.getYN())
                            .setStyle(new Style().setColor(reason.getTextFormatting())));
            sender.sendMessage(hotSpringMessage);
        }
        else
            throw new WrongUsageException(getUsage(null));
    }

    private static final HoverEvent clickMe = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(LANG_COMMAND + "click"));

    private ITextComponent makeHoverAndClickComponent(String key, String toCopy)
    {
        return new TextComponentTranslation(key)
                .setStyle(new Style().setColor(TextFormatting.AQUA).setHoverEvent(clickMe)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy)))
                .appendSibling(new TextComponentString(toCopy).setStyle(new Style().setColor(TextFormatting.WHITE)));
    }

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
            return CommandBase.getListOfStringsMatchingLastWord(args, new String[] { LOCATIONINFO });
        else if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args,
                    ArrayUtils.addAll(server.getOnlinePlayerNames(), Biome.REGISTRY.getKeys().stream()
                            .<ArrayList<String>> collect(ArrayList<String>::new, (list, id) -> list.add(id.toString()), (left, right) -> left.addAll(right))
                            .toArray(new String[0])));
        else if (args.length == 5 || (args.length == 3 && !(args[1].startsWith("~") || StringUtils.isNumeric(args[1]))))
            return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(DimensionManager.getIDs()));
        else if (args.length >= 2 && args.length <= 4)
            return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
        else
            return Collections.<String> emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1 && args.length == 2 && !args[1].contains(":");
    }

}
