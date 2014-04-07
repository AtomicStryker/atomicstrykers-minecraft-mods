package atomicstryker.ruins.common;

import java.io.File;
import java.io.PrintWriter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

public class CommandTestTemplate extends CommandBase
{

    private EntityPlayer player;

    public static RuinIBuildable parsedRuin;

    @Override
    public String getCommandName()
    {
        return "testruin";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "/testruin TEMPLATENAME manually spawns the target Ruin of the templateparser folder";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        player = sender.getEntityWorld().getPlayerEntityByName(sender.getCommandSenderName());
        if (player != null)
        {
            if (args.length < 1)
            {
                if (parsedRuin != null)
                {
                    execBuild(RuinsMod.DIR_NORTH);
                }
                else
                {
                    player.addChatMessage(new ChatComponentText("You need to use the command with the target template name, eg. /parseruin beach/LightHouse"));
                    player = null;
                }
            }
            else
            {
                String target = args[0];
                if (!target.contains("/"))
                {
                    target = "templateparser/" + target;
                }
                
                File file = new File(RuinsMod.getMinecraftBaseDir(), "mods/resources/ruins/" + target + ".tml");
                if (file.exists() && file.canWrite())
                {
                    try
                    {                        
                        parsedRuin = new RuinTemplate(new PrintWriter(System.out, true), file.getCanonicalPath(), file.getName(), true);
                        execBuild((args.length > 1) ? Integer.parseInt(args[1]) : RuinsMod.DIR_NORTH);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    sender.addChatMessage(new ChatComponentText("Could not open/write file "+file));
                }
            }
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("Command is only available for ingame player entities."));
        }
    }
    
    private void execBuild(int rotation)
    {
        parsedRuin.doBuild(player.worldObj, player.getRNG(), MathHelper.floor_double(player.posX + .5),
                MathHelper.floor_double(player.posY - .5), MathHelper.floor_double(player.posZ + .5), rotation);
        parsedRuin = null;
    }
    
    @Override
    public int compareTo(Object o)
    {
        if (o instanceof ICommand)
        {
            return ((ICommand)o).getCommandName().compareTo(getCommandName());
        }
        return 0;
    }
}
