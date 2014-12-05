package atomicstryker.infernalmobs.common;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SaveEventHandler
{

    @SuppressWarnings("rawtypes")
	@SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event)
    {
        Chunk chunk = event.getChunk();
        Entity newEnt;
        for (int i = 0; i < chunk.getEntityLists().length; i++)
        {
        	Iterator iter = chunk.getEntityLists()[i].iterator();
        	while (iter.hasNext())
        	{
        		newEnt = (Entity) iter.next();
                if (newEnt instanceof EntityLivingBase)
                {
                    /*
                     * an EntityLiving was just dumped to a save file and
                     * removed from the world
                     */
                    if (InfernalMobsCore.getIsRareEntity((EntityLivingBase) newEnt))
                    {
                        InfernalMobsCore.removeEntFromElites((EntityLivingBase) newEnt);
                    }
                }
        	}
        }
    }

    @SuppressWarnings("rawtypes")
	@SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event)
    {
        Chunk chunk = event.getChunk();
        Entity newEnt;
        for (int i = 0; i < chunk.getEntityLists().length; i++)
        {
        	Iterator iter = chunk.getEntityLists()[i].iterator();
        	while (iter.hasNext())
        	{
        		newEnt = (Entity) iter.next();
                if (newEnt instanceof EntityLivingBase)
                {
                    String savedMods = newEnt.getEntityData().getString(InfernalMobsCore.instance().getNBTTag());
                    if (!savedMods.equals(""))
                    {
                        InfernalMobsCore.instance().addEntityModifiersByString((EntityLivingBase) newEnt, savedMods);
                    }
                }
            }
        }
    }

}
