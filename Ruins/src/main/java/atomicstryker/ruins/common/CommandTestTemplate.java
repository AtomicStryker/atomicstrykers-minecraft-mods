package atomicstryker.ruins.common;

import java.io.File;
import java.io.PrintWriter;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class CommandTestTemplate extends CommandBase
{

    public static RuinTemplate parsedRuin;
    private int lastFinalY;

    @Override
    public String getName()
    {
        return "testruin";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "/testruin TEMPLATENAME [X Y Z ROTATION] manually spawns the target Ruin of the templateparser folder, [] optional";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(ICommandSender sender, String[] args)
    {
        EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
        int xpos, ypos, zpos;
        xpos = sender.getPosition().getX();
        ypos = sender.getPosition().getY();
        zpos = sender.getPosition().getZ();
        if (player != null && args.length < 4)
        {
            if (args.length < 1)
            {
                if (parsedRuin != null)
                {
                    execBuild(sender.getEntityWorld(), RuinsMod.DIR_NORTH, xpos, ypos-1, zpos);
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
                tryBuild(sender, args, xpos, ypos-1, zpos);
            }
        }
        else if (args.length >= 4)
        {
        	try
        	{
                if (args[2].equals("_"))
                {
                    tryBuild(sender, args, (int)func_175769_b(xpos, args[1], -30000000, 30000000, false), -1, 
                    		(int)func_175769_b(zpos, args[3], -30000000, 30000000, false));
                }
                else
                {
                    tryBuild(sender, args, (int)func_175769_b(xpos, args[1], -30000000, 30000000, false), 
                    		(int)func_175769_b(ypos, args[2], -30000000, 30000000, false)-1, 
                    		(int)func_175769_b(zpos, args[3], -30000000, 30000000, false));
                }
        	}
        	catch (NumberInvalidException e)
        	{
        		sender.addChatMessage(new ChatComponentText("Invalid coordinates specified"));
			}
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
                int rotation = (args.length > 4) ? Integer.parseInt(args[4]) : RuinsMod.DIR_NORTH;
                
                if (parsedRuin != null)
                {
                    if (y < 0)
                    {
                        for (y = RuinGenerator.WORLD_MAX_HEIGHT - 1; y > 7; y--)
                        {
                            final Block b = sender.getEntityWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
                            if (parsedRuin.isIgnoredBlock(b, sender.getEntityWorld(), x, y, z))
                            {
                                continue;
                            }
                            
                            if (parsedRuin.isAcceptableSurface(b))
                            {
                                break;
                            }
                            sender.addChatMessage(new ChatComponentText("Could not find acceptable Y coordinate"));
                            return;
                        }
                    }
                    
                    if (MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(sender.getEntityWorld(), parsedRuin, x, y, z, rotation, true, true)))
                    {
                        sender.addChatMessage(new ChatComponentText("EventRuinTemplateSpawn returned as cancelled, not building that."));
                    }
                    else
                    {
                        execBuild(sender.getEntityWorld(), rotation, x, y, z);
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

    private void execBuild(World world, int rotation, int x, int y, int z)
    {
        lastFinalY = parsedRuin.doBuild(world, world.rand, x, y, z, rotation);
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof ICommand)
        {
            return ((ICommand) o).getName().compareTo(getName());
        }
        return 0;
    }
}
