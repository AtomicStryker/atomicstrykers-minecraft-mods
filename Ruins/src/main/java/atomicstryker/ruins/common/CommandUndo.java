package atomicstryker.ruins.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommandUndo extends CommandBase
{
    
    private static ArrayList<TemplateArea> savedLocations = new ArrayList<TemplateArea>();
    private static RuinTemplate runningTemplateSpawn;
    
    private class TemplateArea
    {
        Block[][][] blockArray;
        int[][][] metaArray;
        int xBase, yBase, zBase;
    }
    
    public CommandUndo()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onSpawningRuin(EventRuinTemplateSpawn event)
    {
        if (event.testingRuin || runningTemplateSpawn != null)
        {
            if (event.isPrePhase)
            {
                // firing the first template event, adjacents may or may not come
                if (runningTemplateSpawn == null)
                {
                    runningTemplateSpawn = event.template;
                    // flush the last locations
                    savedLocations.clear();
                }
                else
                {
                    System.out.println("Ruins undo command caught adjacent template, saving it too..");
                }
                
                RuinData data = event.template.getRuinData(event.x, event.y, event.z, event.rotation);
                TemplateArea ta = new TemplateArea();
                ta.xBase = data.xMin;
                ta.yBase = data.yMin;
                ta.zBase = data.zMin;
                ta.blockArray = new Block[data.xMax - data.xMin + 1][data.yMax - data.yMin + 1][data.zMax - data.zMin + 1];
                ta.metaArray = new int[data.xMax - data.xMin + 1][data.yMax - data.yMin + 1][data.zMax - data.zMin + 1];
                IBlockState bstate;
                for (int x = 0; x < ta.blockArray.length; x++)
                {
                    for (int y = 0; y < ta.blockArray[0].length; y++)
                    {
                        for (int z = 0; z < ta.blockArray[0][0].length; z++)
                        {
                        	bstate = event.world.getBlockState(new BlockPos(ta.xBase+x, ta.yBase+y, ta.zBase+z));
                            ta.blockArray[x][y][z] = bstate.getBlock();
                            ta.metaArray[x][y][z] = ta.blockArray[x][y][z].getMetaFromState(bstate);
                        }
                    }
                }
                savedLocations.add(ta);
                
                if (savedLocations.size() > 100)
                {
                    // safety overflow valve in case something goes wrong
                    savedLocations.clear();
                    runningTemplateSpawn = null;
                }
            }
            else if (runningTemplateSpawn == event.template)
            {
                // finished spawning all adjacents, post event of initial template firing
                runningTemplateSpawn = null;
                // since this is null the savedLocations will be cleared then the next spawn occurs
            }
        }
    }
    
    @Override
    public String getName()
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
    public void execute(ICommandSender sender, String[] args)
    {
        World w = sender.getEntityWorld();
        if (w != null)
        {
            if (savedLocations.isEmpty())
            {
                sender.addChatMessage(new ChatComponentText("There is nothing cached to be undone..."));
            }
            else
            {
                for (TemplateArea ta : savedLocations)
                {
                    for (int x = 0; x < ta.blockArray.length; x++)
                    {
                        for (int y = 0; y < ta.blockArray[0].length; y++)
                        {
                            for (int z = 0; z < ta.blockArray[0][0].length; z++)
                            {
                                w.setBlockState(new BlockPos(ta.xBase+x, ta.yBase+y, ta.zBase+z), ta.blockArray[x][y][z].getStateFromMeta(ta.metaArray[x][y][z]), 2);
                            }
                        }
                    }
                    
                    // kill off the resulting entityItems instances
                    for (Entity e : (List<Entity>) w.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.fromBounds(ta.xBase - 1, ta.yBase - 1,
                            ta.zBase - 1, ta.xBase + ta.blockArray.length + 1, ta.yBase + ta.blockArray[0].length + 1, ta.zBase + ta.blockArray[0][0].length + 1)))
                    {
                        e.setDead();
                    }
                }
                sender.addChatMessage(new ChatComponentText("Cleared away "+savedLocations.size()+" template sites."));
                savedLocations.clear();
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
            return ((ICommand) o).getName().compareTo(getName());
        }
        return 0;
    }
}
