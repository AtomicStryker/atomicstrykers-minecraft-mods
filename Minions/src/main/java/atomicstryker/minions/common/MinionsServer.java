package atomicstryker.minions.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.ForgePacketWrapper;
import atomicstryker.minions.common.network.PacketDispatcher;
import atomicstryker.minions.common.network.PacketDispatcher.WrappedPacket;

public class MinionsServer
{

    public static void onPacketData(int pt, WrappedPacket packet, EntityPlayer p)
    {
        PacketType packetType = PacketType.byID(pt);
        EntityPlayer player = (EntityPlayer)p;
        ByteBuf data = packet.data;
        
        MinionsCore.instance.debugPrint("Server received packet, ID "+packetType+", from player "+player.getCommandSenderName());
        
        switch (packetType)
        {
            case HASMINIONS:
            {
                Object[] toSend = {MinionsCore.instance.hasPlayerMinions(player) ? 1 : 0, MinionsCore.instance.hasAllMinions(player) ? 1 : 0};
                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), p);
                break;
            }
        
            case EVILDEEDDONE:
            {
                if (player != null && player.experienceLevel >= MinionsCore.instance.evilDeedXPCost)
                {
                    player.addExperienceLevel(-MinionsCore.instance.evilDeedXPCost);
                    MinionsCore.instance.onMasterAddedEvil(player);
                }
                break;
            }
            
            case CMDPICKUPENT:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int targetID = (Integer) packetReadout[2];
                
                Entity target = player.worldObj.getEntityByID(targetID);
                if (target instanceof EntityAnimal || target instanceof EntityPlayer)
                {
                    MinionsCore.instance.orderMinionToPickupEntity(player, (EntityLivingBase) target);
                }
                break;
            }
            
            case CMDDROPALL:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int targetID = (Integer) packetReadout[2];
                
                Entity target = player.worldObj.getEntityByID(targetID);
                if (target instanceof EntityMinion)
                {
                    MinionsCore.instance.orderMinionToDrop(player, (EntityMinion) target);
                }
                break;
            }
            
            case CMDMINIONSPAWN:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    
                    if (MinionsCore.instance.spawnMinionsForPlayer(player, x, y, z))
                    {
                        MinionsCore.instance.exhaustPlayerBig(player);
                    }
                    
                    Object[] toSend = {MinionsCore.instance.hasPlayerMinions(player) ? 1 : 0, MinionsCore.instance.hasAllMinions(player) ? 1 : 0};
                    PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), p);
                }
                break;
            }
            
            case CMDCHOPTREES:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    MinionsCore.instance.orderMinionsToChopTrees(player, x, y, z);
                    MinionsCore.instance.exhaustPlayerBig(player);
                }
                break;
            }
            
            case CMDSTAIRWELL:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    MinionsCore.instance.orderMinionsToDigStairWell(player, x, y, z);
                    MinionsCore.instance.exhaustPlayerBig(player);
                }
                break;
            }
            
            case CMDSTRIPMINE:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    MinionsCore.instance.orderMinionsToDigStripMineShaft(player, x, y, z);
                    MinionsCore.instance.exhaustPlayerBig(player);
                }
                break;
            }
            
            case CMDASSIGNCHEST:
            {
                Class<?>[] decodeAs = {String.class, Boolean.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                boolean sneaking = (Boolean) packetReadout[1];
                int x = (Integer) packetReadout[2];
                int y = (Integer) packetReadout[3];
                int z = (Integer) packetReadout[4];
                MinionsCore.instance.orderMinionsToChestBlock(player, sneaking, x, y, z);
                break;
            }
            
            case CMDMOVETO:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int x = (Integer) packetReadout[1];
                int y = (Integer) packetReadout[2];
                int z = (Integer) packetReadout[3];
                MinionsCore.instance.orderMinionsToMoveTo(player, x, y, z);
                break;
            }
            
            case CMDMINEOREVEIN:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int x = (Integer) packetReadout[1];
                int y = (Integer) packetReadout[2];
                int z = (Integer) packetReadout[3];
                MinionsCore.instance.orderMinionsToMineOre(player, x, y, z);
                break;
            }
            
            case CMDFOLLOW:
            {
                MinionsCore.instance.orderMinionsToFollow(player);
                break;
            }
            
            case REQUESTXPSETTING:
            {
                Object[] toSend = {MinionsCore.instance.evilDeedXPCost};
                //manager.addToSendQueue(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), 13, toSend));
                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend), p);
                break;
            }
            
            case CMDUNSUMMON:
            {
                if (player != null)
                {
                    MinionsCore.instance.unSummonPlayersMinions(player);
                }
                break;
            }
            
            case CMDCUSTOMDIG:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (player != null && MinionsCore.instance.hasPlayerWillPower(player))
                {
                    MinionsCore.instance.orderMinionsToDigCustomSpace(player, (Integer)packetReadout[1], (Integer)packetReadout[2], (Integer)packetReadout[3], (Integer)packetReadout[4], (Integer)packetReadout[5]);
                    MinionsCore.instance.exhaustPlayerBig(player);
                }
                break;
            }
            
            case LIGHTNINGBOLT:
            {
                Class<?>[] decodeAs = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                Vector3 start = new Vector3((Double)packetReadout[0], (Double)packetReadout[1], (Double)packetReadout[2]);
                Vector3 end = new Vector3((Double)packetReadout[3], (Double)packetReadout[4], (Double)packetReadout[5]);
                
                EntityPlayer caster = (EntityPlayer) p;
                
                if (MinionsCore.instance.hasPlayerWillPower(caster))
                {
                    long randomizer = caster.worldObj.rand.nextLong();
                    
                    // (startx, starty, startz, endx, endy, endz, randomlong)
                    Object[] toSend = { start.x, start.y, start.z, end.x, end.y, end.z, randomizer };
                    WrappedPacket pcket = ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.LIGHTNINGBOLT.ordinal(), toSend);
                    PacketDispatcher.sendToAllNear(caster.posX, caster.posY, caster.posZ, 50D, caster.worldObj.provider.dimensionId, pcket);
                    
                    spawnLightningBolt(caster.worldObj, caster, start, end, randomizer);
                    
                    MinionsCore.instance.exhaustPlayerSmall(caster);                
                }
                break;
            }
            
            case SOUNDTOALL:
            {
                Class<?>[] decodeAs = {Integer.class, String.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                Entity ent = player.worldObj.getEntityByID((Integer) packetReadout[0]);
                if (ent != null)
                {
                    MinionsCore.proxy.sendSoundToClients(ent, (String)packetReadout[1]);
                }
                break;
            }
            
            case HAX:
            {
                player.addExperience(200);
                break;
            }
		default:
			break;
        }
    }
    
    private static void spawnLightningBolt(World world, EntityLivingBase shooter, Vector3 startvec, Vector3 endvec, long randomizer)
    {
        for (int i = 3; i != 0; i--)
        {
            ChickenLightningBolt bolt = new ChickenLightningBolt(world, startvec, endvec, randomizer);
            bolt.defaultFractal();
            bolt.finalizeBolt();
            bolt.setWrapper(shooter);
            ChickenLightningBolt.boltlist.add(bolt);   
        }
    }
}
