package atomicstryker.infernalmobs.common;

import net.minecraft.src.Chunk;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;

public class SaveEventHandler
{
	@ForgeSubscribe
    public void onChunkLoad(ChunkEvent.Load event)
    {
		Chunk chunk = event.getChunk();
        for (int i = 0; i < chunk.entityLists.length; i++)
        {
            for (int j = 0; j < chunk.entityLists[i].size(); j++)
            {
                Entity newEnt = (Entity) chunk.entityLists[i].get(j);
                
                if (newEnt instanceof EntityLiving)
                {
                    /* an EntityLiving was just loaded from a save file and spawned into the world */
                    String savedMods = newEnt.getEntityData().getString(InfernalMobsCore.getNBTTag());
                    if (!savedMods.equals(""))
                    {
                        InfernalMobsCore.addEntityModifiersByString((EntityLiving) newEnt, savedMods);
                    }
                }
            }
        }
    }

	@ForgeSubscribe
    public void onChunkUnload(ChunkEvent.Unload event)
    {
		Chunk chunk = event.getChunk();
        for (int i = 0; i < chunk.entityLists.length; i++)
        {
            for (int j = 0; j < chunk.entityLists[i].size(); j++)
            {
                Entity newEnt = (Entity) chunk.entityLists[i].get(j);
                
                if (newEnt instanceof EntityLiving)
                {
                    /* an EntityLiving was just dumped to a save file and removed from the world */
                    if (InfernalMobsCore.getIsRareEntity((EntityLiving) newEnt))
                    {
                        InfernalMobsCore.removeEntFromElites((EntityLiving) newEnt);
                    }
                }
            }
        }
    }
}
