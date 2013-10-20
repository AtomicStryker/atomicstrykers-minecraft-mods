package atomicstryker.infernalmobs.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;

public class SaveEventHandler
{

	@ForgeSubscribe
    public void onChunkUnload(ChunkEvent.Unload event)
    {
		Chunk chunk = event.getChunk();
        for (int i = 0; i < chunk.entityLists.length; i++)
        {
            for (int j = 0; j < chunk.entityLists[i].size(); j++)
            {
                Entity newEnt = (Entity) chunk.entityLists[i].get(j);
                
                if (newEnt instanceof EntityLivingBase)
                {
                    /* an EntityLiving was just dumped to a save file and removed from the world */
                    if (InfernalMobsCore.getIsRareEntity((EntityLivingBase) newEnt))
                    {
                        InfernalMobsCore.removeEntFromElites((EntityLivingBase) newEnt);
                    }
                }
            }
        }
    }
	
}
