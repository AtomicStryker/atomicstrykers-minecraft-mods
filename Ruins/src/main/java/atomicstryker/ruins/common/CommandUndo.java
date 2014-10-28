package atomicstryker.ruins.common;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommandUndo extends CommandBase
{
    
    private static Block[][][] blockArray;
    private static int[][][] metaArray;
    private static int xBase, yBase, zBase;
    
    public CommandUndo()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onSpawningRuin(EventRuinTemplateSpawn event)
    {
        if (event.testingRuin)
        {
            RuinData data = event.template.getRuinData(event.x, event.y, event.z, event.rotation);
            xBase = data.xMin;
            yBase = data.yMin;
            zBase = data.zMin;
            blockArray = new Block[data.xMax - data.xMin + 1][data.yMax - data.yMin + 1][data.zMax - data.zMin + 1];
            metaArray = new int[data.xMax - data.xMin + 1][data.yMax - data.yMin + 1][data.zMax - data.zMin + 1];
            for (int x = 0; x < blockArray.length; x++)
            {
                for (int y = 0; y < blockArray[0].length; y++)
                {
                    for (int z = 0; z < blockArray[0][0].length; z++)
                    {
                        blockArray[x][y][z] = event.world.getBlock(xBase+x, yBase+y, zBase+z);
                        metaArray[x][y][z] = event.world.getBlockMetadata(xBase+x, yBase+y, zBase+z);
                    }
                }
            }
        }
    }
    
    @Override
    public String getCommandName()
    {
        return "undoruin";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "/undoruin restores the site of the last testruin command";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        World w = sender.getEntityWorld();
        if (w != null)
        {
            if (blockArray != null)
            {
                for (int x = 0; x < blockArray.length; x++)
                {
                    for (int y = 0; y < blockArray[0].length; y++)
                    {
                        for (int z = 0; z < blockArray[0][0].length; z++)
                        {
                            w.setBlock(xBase+x, yBase+y, zBase+z, blockArray[x][y][z], metaArray[x][y][z], 2);
                        }
                    }
                }
                
                // kill off the resulting entityItems instances
                for (Entity e : (List<Entity>) w.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(xBase - 1, yBase - 1,
                        zBase - 1, xBase + blockArray.length + 1, yBase + blockArray[0].length + 1, zBase + blockArray[0][0].length + 1)))
                {
                    e.setDead();
                }
                
                // reset cache
                blockArray = null;
            }
            else
            {
                sender.addChatMessage(new ChatComponentText("There is nothing cached to be undone..."));
            }
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("Command can only be run ingame..."));
        }
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
