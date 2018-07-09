package atomicstryker.ruins.common;

import java.io.File;
import java.io.PrintWriter;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

class CommandTestTemplate extends CommandBase
{

    public static RuinTemplate parsedRuin;

    @Override
    public String getName()
    {
        return "testruin";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return "/testruin TEMPLATENAME [X Y Z ROTATION] manually spawns the target Ruin of the templateparser folder, [] optional";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
        boolean is_player = player != null;
        int xpos, ypos, zpos;
        xpos = sender.getPosition().getX();
        ypos = sender.getPosition().getY();
        zpos = sender.getPosition().getZ();
        if (is_player && args.length < 4)
        {
            if (args.length < 1)
            {
                if (parsedRuin != null)
                {
                    parsedRuin.doBuild(sender.getEntityWorld(), sender.getEntityWorld().rand, xpos, ypos, zpos, RuinsMod.DIR_NORTH, is_player);
                    parsedRuin = null;
                }
                else
                {
                    player.sendMessage(new TextComponentTranslation("You need to use the command with the target template name, eg. /parseruin beach/LightHouse"));
                }
            }
            else
            {
                tryBuild(sender, args, xpos, ypos, zpos, is_player);
            }
        }
        else if (args.length >= 4)
        {
            try
            {
                if (args[2].equals("_"))
                {
                    tryBuild(sender, args, (int) parseDouble(xpos, args[1], -30000000, 30000000, false), -1, (int) parseDouble(zpos, args[3], -30000000, 30000000, false), is_player);
                }
                else
                {
                    tryBuild(sender, args, (int) parseDouble(xpos, args[1], -30000000, 30000000, false), (int) parseDouble(ypos, args[2], -30000000, 30000000, false),
                            (int) parseDouble(zpos, args[3], -30000000, 30000000, false), is_player);
                }
            }
            catch (NumberInvalidException e)
            {
                sender.sendMessage(new TextComponentTranslation("Invalid coordinates specified"));
            }
        }
        else
        {
            sender.sendMessage(new TextComponentTranslation("Command is only available for ingame player entities, or with coordinates specified"));
        }
    }

    private void tryBuild(ICommandSender sender, String[] args, int x, int y, int z, boolean is_player)
    {
        String target = args[0];
        if (!target.contains("/"))
        {
            target = "templateparser/" + target;
        }

        File file = new File(RuinsMod.getMinecraftBaseDir(), RuinsMod.TEMPLATE_PATH_MC_EXTRACTED + target + ".tml");
        if (file.exists() && file.canWrite())
        {
            try
            {
                parsedRuin = new RuinTemplate(new PrintWriter(System.out, true), file.getCanonicalPath(), file.getName(), is_player);
                int rotation = (args.length > 4) ? Integer.parseInt(args[4]) : RuinsMod.DIR_NORTH;

                if (parsedRuin != null)
                {
                    if (y < 0)
                    {
                        for (y = RuinGenerator.WORLD_MAX_HEIGHT - 1; y > 7; y--)
                        {
                            BlockPos pos = new BlockPos(x, y, z);
                            final Block b = sender.getEntityWorld().getBlockState(pos).getBlock();
                            if (parsedRuin.isIgnoredBlock(b, sender.getEntityWorld(), pos))
                            {
                                continue;
                            }

                            if (parsedRuin.isAcceptableSurface(b))
                            {
                                break;
                            }
                            sender.sendMessage(new TextComponentTranslation("Could not find acceptable Y coordinate"));
                            return;
                        }
                        ++y;
                    }

                    if (parsedRuin.doBuild(sender.getEntityWorld(), sender.getEntityWorld().rand, x, y, z, rotation, is_player) >= 0)
                    {
                        parsedRuin = null;
                    }
                    else
                    {
                        sender.sendMessage(new TextComponentTranslation("EventRuinTemplateSpawn returned as cancelled, not building that."));
                    }
                }
                else
                {
                    sender.sendMessage(new TextComponentTranslation("Could not parse Ruin of file " + file));
                }
            }
            catch (RuinTemplate.RequiredModNotActiveException | RuinTemplate.ProhibitedModActiveException e)
            {
                sender.sendMessage(new TextComponentTranslation(e.getMessage()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            sender.sendMessage(new TextComponentTranslation("Could not open/write file " + file));
        }
    }

}
