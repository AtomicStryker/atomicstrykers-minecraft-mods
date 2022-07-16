package atomicstryker.infernalmobs.common;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SaveEventHandler {

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level) {
            Level level = ((Level) event.getLevel());
            InfernalMobsCore.clearAllElitesOfLevel(level);
        }
    }

    // DOES NOT WORK IN 1.17
//    @SubscribeEvent
//    public void onChunkLoad(ChunkEvent.Load event) {
//        if (event.getWorld() == null || event.getWorld().isClientSide()) {
//            return;
//        }
//        if (event.getChunk() instanceof LevelChunk) {
//            LevelChunk chunk = (LevelChunk) event.getChunk();
//            BlockPos leftFrontPos = chunk.getPos().getWorldPosition();
//            BlockPos rightBackPos = new BlockPos(chunk.getPos().getMaxBlockX(), chunk.getHeight(), chunk.getPos().getMaxBlockZ());
//
//            for (LivingEntity livingEntitesInLoadedChunk : event.getWorld().getEntitiesOfClass(LivingEntity.class, new AABB(leftFrontPos, rightBackPos))) {
//                String savedMods = livingEntitesInLoadedChunk.getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
//                if (!savedMods.isEmpty() && !savedMods.equals(InfernalMobsCore.instance().getNBTMarkerForNonInfernalEntities())) {
//                    InfernalMobsCore.instance().addEntityModifiersByString(livingEntitesInLoadedChunk, savedMods);
//                }
//            }
//        }
//    }

}
