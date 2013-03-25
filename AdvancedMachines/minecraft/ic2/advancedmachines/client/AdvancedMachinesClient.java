package ic2.advancedmachines.client;

import ic2.advancedmachines.common.IProxy;
import ic2.advancedmachines.common.TileEntityCentrifugeExtractor;
import ic2.advancedmachines.common.TileEntityRotaryMacerator;
import ic2.advancedmachines.common.TileEntitySingularityCompressor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

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
        TileEntity te = world.getBlockTileEntity(x, y, z);

        if (te != null)
        {
            if (te instanceof TileEntityRotaryMacerator)
            {
                return new GuiRotaryMacerator(player.inventory, (TileEntityRotaryMacerator) te);
            }
            else if (te instanceof TileEntityCentrifugeExtractor)
            {
                return new GuiCentrifugeExtractor(player.inventory, (TileEntityCentrifugeExtractor) te);
            }
            else if (te instanceof TileEntitySingularityCompressor)
            {
                return new GuiSingularityCompressor(player.inventory, (TileEntitySingularityCompressor) te);
            }
        }

        return null;
    }

}
