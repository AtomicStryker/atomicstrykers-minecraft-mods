package atomicstryker.infernalmobs.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.PacketDispatcher;

public class EntityTracker
{
    
    @ForgeSubscribe
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityMob)
        {
            askServerForMobMods(event.entity);
        }
    }
    
    private void askServerForMobMods(Entity ent)
    {
        // question: Packet ID 1, from client, { entID }
        Object[] input = { ent.entityId };
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_IM", 1, input));
    }
    
}
