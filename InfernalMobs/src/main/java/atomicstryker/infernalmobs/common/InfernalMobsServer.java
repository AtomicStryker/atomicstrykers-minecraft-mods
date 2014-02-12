package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;

public class InfernalMobsServer implements ISidedProxy
{
    
    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsServer;
    
    public InfernalMobsServer()
    {
        rareMobsServer = new ConcurrentHashMap<EntityLivingBase, MobModifier>();
    }
    
    @Override
    public void preInit()
    {
        // NOOP
    }

    @Override
    public void load()
    {
        // NOOP
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs()
    {
        return rareMobsServer;
    }

    @Override
    public void onHealthPacket(String stringData, int entID, float health, float maxhealth)
    {
        EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(stringData);
        if (p != null)
        {
            EntityLivingBase e = (EntityLivingBase) p.worldObj.getEntityByID(entID);
            if (e != null)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                if (mod != null)
                {
                    health = e.getHealth();
                    maxhealth = e.getMaxHealth();
                    InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new HealthPacket(stringData, entID, health, maxhealth), p);
                }
            }
        }
    }

    @Override
    public void onKnockBackPacket(float xv, float zv)
    {
        // NOOP
    }

    @Override
    public void onMobModsPacket(String stringData, int entID)
    {
        EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(stringData);
        if (p != null)
        {
            EntityLivingBase e = (EntityLivingBase) p.worldObj.getEntityByID(entID);
            if (e != null)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                if (mod != null)
                {
                    stringData = mod.getLinkedModNameUntranslated();
                    InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new MobModsPacket(stringData, entID), p);
                    InfernalMobsCore.instance().sendHealthPacket(e, mod.getActualHealth(e));
                }
            }
        }
    }

    @Override
    public void onVelocityPacket(float xv, float yv, float zv)
    {
        // NOOP
    }

}
