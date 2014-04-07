package atomicstryker.ic2.advancedmachines.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import atomicstryker.ic2.advancedmachines.ContainerAdvancedMacerator;
import atomicstryker.ic2.advancedmachines.ContainerAdvancedMachine;
import atomicstryker.ic2.advancedmachines.IProxy;
import atomicstryker.ic2.advancedmachines.TileEntityAdvancedCompressor;
import atomicstryker.ic2.advancedmachines.TileEntityAdvancedExtractor;
import atomicstryker.ic2.advancedmachines.TileEntityAdvancedMacerator;
import atomicstryker.ic2.advancedmachines.TileEntityAdvancedRecycler;

public class AdvancedMachinesClient implements IProxy
{
    public static int[][] sideAndFacingToSpriteOffset;

    @Override
    public void load()
    {        
        try
        {
            sideAndFacingToSpriteOffset = (int[][])Class.forName("ic2.core.block.BlockMultiID").getField("sideAndFacingToSpriteOffset").get(null);
        }
        catch (Exception e)
        {
            sideAndFacingToSpriteOffset = new int[][]{
                    {
                        3, 2, 0, 0, 0, 0
                    }, {
                        2, 3, 1, 1, 1, 1
                    }, {
                        1, 1, 3, 2, 5, 4
                    }, {
                        0, 0, 2, 3, 4, 5
                    }, {
                        4, 5, 4, 5, 3, 2
                    }, {
                        5, 4, 5, 4, 2, 3
                    }
            };
        }
    }

    @Override
    public Object getGuiElementForClient(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null)
        {
            if (te instanceof TileEntityAdvancedMacerator)
            {
                return new GuiRotaryMacerator(new ContainerAdvancedMacerator(player, (TileEntityAdvancedMacerator) te), (TileEntityAdvancedMacerator) te);
            }
            else if (te instanceof TileEntityAdvancedExtractor)
            {
                return new GuiCentrifugeExtractor(new ContainerAdvancedMachine(player, (TileEntityAdvancedExtractor) te), (TileEntityAdvancedExtractor) te);
            }
            else if (te instanceof TileEntityAdvancedCompressor)
            {
                return new GuiSingularityCompressor(new ContainerAdvancedMachine(player, (TileEntityAdvancedCompressor) te), (TileEntityAdvancedCompressor) te);
            }
            else if (te instanceof TileEntityAdvancedRecycler)
            {
                return new GuiCombinedRecycler(new ContainerAdvancedMachine(player, (TileEntityAdvancedRecycler) te), (TileEntityAdvancedRecycler) te);
            }
        }

        return null;
    }

}
