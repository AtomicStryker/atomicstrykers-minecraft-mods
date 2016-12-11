package atomicstryker.minions.common.jobmanager;

import atomicstryker.minions.common.entity.EntityMinion;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeHooks;

/**
 * Blocktask for mining a single Block, then replacing it with another
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_ReplaceBlock extends BlockTask_MineBlock
{
	public final Block blockToPlace;
	public final int metaToPlace;
	
    public BlockTask_ReplaceBlock(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz, Block blockOrdered, int metaOrdered)
    {
    	super(boss, input, ix, iy, iz);
    	blockToPlace = blockOrdered;
    	metaToPlace = metaOrdered;
    }
    
    @SuppressWarnings("deprecation")
    public void onFinishedTask()
    {
    	super.onFinishedTask();
    	
    	int event = ForgeHooks.onBlockBreakEvent(worker.world, worker.world.getWorldInfo().getGameType(), (EntityPlayerMP) worker.master, new BlockPos(posX, posY, posZ));
        if (event != -1)
        {
            worker.world.setBlockState(new BlockPos(posX,  posY,  posZ),  blockToPlace.getStateFromMeta(metaToPlace));
        }
    }
}