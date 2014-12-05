package atomicstryker.ruins.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommandParseTemplate extends CommandBase
{

    private EntityPlayer player;
    private String templateName;

    public CommandParseTemplate()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBroken(BreakEvent event)
    {
        if (event.getPlayer() == player)
        {
            new World2TemplateParser(player, event.pos.getX(), event.pos.getY(), event.pos.getZ(), templateName).start();
            player = null;
            event.setCanceled(true);
        }
    }

    @Override
    public String getName()
    {
        return "parseruin";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "/parseruin TEMPLATENAME sets the Ruins World2Template parser to wait for the next block you break, which will be considered part of the template baseplate";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(ICommandSender sender, String[] args)
    {
        player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
        if (player != null)
        {
            if (args.length != 1)
            {
                player.addChatMessage(new ChatComponentText("You need to use the command with the target template name, eg. /parseruin funhouse"));
                player = null;
            }
            else
            {
                templateName = args[0];
                player.addChatMessage(new ChatComponentText("Template parser ready to create " + templateName
                        + ". Break any block of the baseplate now."));
            }
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("Command only available for ingame player entities."));
        }
    }
    
    @Override
    public int compareTo(Object o)
    {
        if (o instanceof ICommand)
        {
            return ((ICommand)o).getName().compareTo(getName());
        }
        return 0;
    }

}
