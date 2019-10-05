package atomicstryker.infernalmobs.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SaveEventHandler {

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof Chunk)) {
            return;
        }
        Chunk chunk = (Chunk) event.getChunk();
        Entity newEnt;
        for (int i = 0; i < chunk.getEntityLists().length; i++) {
            for (Object o : chunk.getEntityLists()[i]) {
                newEnt = (Entity) o;
                if (newEnt instanceof LivingEntity) {
                    /*
                     * an EntityLiving was just dumped to a save file and
                     * removed from the world
                     */
                    if (InfernalMobsCore.getIsRareEntity((LivingEntity) newEnt)) {
                        InfernalMobsCore.removeEntFromElites((LivingEntity) newEnt);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof Chunk)) {
            return;
        }
        Chunk chunk = (Chunk) event.getChunk();
        Entity newEnt;
        for (int i = 0; i < chunk.getEntityLists().length; i++) {
            for (Object o : chunk.getEntityLists()[i]) {
                newEnt = (Entity) o;
                if (newEnt instanceof LivingEntity) {
                    String savedMods = newEnt.getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
                    if (!savedMods.equals("")) {
                        InfernalMobsCore.instance().addEntityModifiersByString((LivingEntity) newEnt, savedMods);
                    }
                }
            }
        }
    }

}
