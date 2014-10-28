package atomicstryker.ruins.common;

import java.io.File;
import java.io.PrintWriter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;

public class CommandTestTemplate extends CommandBase
{

    private EntityPlayer player;
    public static RuinTemplate parsedRuin;
    private int lastFinalY;

    @Override
    public String getCommandName()
    {
        return "testruin";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "/testruin TEMPLATENAME [ROTATION X Y Z] manually spawns the target Ruin of the templateparser folder, [] optional";
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
                    execBuild(RuinsMod.DIR_NORTH, MathHelper.floor_double(player.posX + .5), MathHelper.floor_double(player.posY - .5),
                            MathHelper.floor_double(player.posZ + .5));
                    parsedRuin = null;
                }
                else
                {
                    player.addChatMessage(new ChatComponentText(
                            "You need to use the command with the target template name, eg. /parseruin beach/LightHouse"));
                    player = null;
                }
            }
            else
            {
                tryBuild(sender, args, MathHelper.floor_double(player.posX + .5), MathHelper.floor_double(player.posY - .5), MathHelper.floor_double(player.posZ + .5));
            }
        }
        else if (args.length > 4)
        {
            tryBuild(sender, args, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("Command is only available for ingame player entities, or with coordinates specified"));
        }
    }
    
    private void tryBuild(ICommandSender sender, String[] args, int x, int y, int z)
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
                int rotation = (args.length > 1) ? Integer.parseInt(args[1]) : RuinsMod.DIR_NORTH;
                
                if (parsedRuin != null)
                {
                    if (MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(sender.getEntityWorld(), parsedRuin, x, y, z, rotation, true, true)))
                    {
                        sender.addChatMessage(new ChatComponentText("EventRuinTemplateSpawn returned as cancelled, not building that."));
                    }
                    else
                    {
                        execBuild(rotation, x, y, z);
                        MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(sender.getEntityWorld(), parsedRuin, x, lastFinalY, z, rotation, true, false));
                        parsedRuin = null;
                    }
                }
                else
                {
                    sender.addChatMessage(new ChatComponentText("Could not parse Ruin of file " + file));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("Could not open/write file " + file));
        }
    }

    private void execBuild(int rotation, int x, int y, int z)
    {
        lastFinalY = parsedRuin.doBuild(player.worldObj, player.getRNG(), x, y, z, rotation);
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof ICommand)
        {
            return ((ICommand) o).getCommandName().compareTo(getCommandName());
        }
        return 0;
    }
}
