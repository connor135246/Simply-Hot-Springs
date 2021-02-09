package connor135246.simplyhotsprings.util;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class CommandSimplyHotSprings implements ICommand
{

    public static final CommandSimplyHotSprings INSTANCE = new CommandSimplyHotSprings();

    public static final String LOCATIONINFO = "locationinfo";

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
        return "/" + Reference.MODID + " " + LOCATIONINFO + " [player] OR /" + Reference.MODID + " " + LOCATIONINFO + " [<x> <y> <z>]";
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

            if (args.length < 4)
            {
                EntityPlayerMP player = args.length > 1 ? CommandBase.getPlayer(server, sender, args[1]) : CommandBase.getCommandSenderAsPlayer(sender);
                world = player.world;
                pos = player.getPosition();
            }
            else
            {
                world = sender.getEntityWorld();
                pos = CommandBase.parseBlockPos(sender, args, 1, true);
            }

            if (world.isBlockLoaded(pos))
            {
                Biome biome = world.getBiomeForCoordsBody(pos);

                sender.sendMessage(makeHoverAndClickComponent(TextFormatting.AQUA + "Dimension ID: " + TextFormatting.WHITE,
                        world.provider.getDimension() + ""));

                sender.sendMessage(makeHoverAndClickComponent(TextFormatting.AQUA + "Biome Name: " + TextFormatting.WHITE,
                        biome.getRegistryName().toString()));

                sender.sendMessage(makeHoverAndClickComponent(TextFormatting.AQUA + "Biome Types: " + TextFormatting.WHITE,
                        CommandBase.joinNiceString(BiomeDictionary.getTypes(biome).toArray())));

                String reason = SimplyHotSpringsConfig.WorldGen.generateReason(world, pos);
                TextComponentString canGenerate = new TextComponentString(TextFormatting.AQUA + "Hot Springs: "
                        + (SimplyHotSpringsConfig.WorldGen.generateReason(world, pos).startsWith(TextFormatting.GREEN + "") ? TextFormatting.GREEN + "Yes"
                                : TextFormatting.DARK_RED + "No"));
                canGenerate.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponentString("Reason:\n" + reason)));
                sender.sendMessage(canGenerate);

                return;
            }
            else
                throw new CommandException("Cannot check block outside of world", new Object[0]);
        }
        throw new WrongUsageException(getUsage(null), new Object[0]);
    }

    private static final HoverEvent clickMe = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to put in chat box"));

    private ITextComponent makeHoverAndClickComponent(String in, String toCopy)
    {
        TextComponentString component = new TextComponentString(in + toCopy);
        component.getStyle().setHoverEvent(clickMe);
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy));
        return component;
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
            return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        else if (args.length >= 2 && args.length <= 4)
            return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
        else
            return Collections.<String> emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1;
    }

}
