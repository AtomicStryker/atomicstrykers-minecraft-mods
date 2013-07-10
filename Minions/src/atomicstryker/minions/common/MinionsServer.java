package atomicstryker.minions.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.entity.EntityMinion;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class MinionsServer
{

    public static void onPacketData(INetworkManager mgr, Packet250CustomPayload packet, Player p)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        PacketType packetType = PacketType.byID(ForgePacketWrapper.readPacketID(data));
        EntityPlayer player = (EntityPlayer)p;
        
        // System.out.println("Server received packet, ID "+packetType+", from player "+player.username);
        
        switch (packetType)
        {
            case EVILDEEDDONE:
            {
                if (player != null && player.experienceLevel >= MinionsCore.evilDeedXPCost)
                {
                    player.addExperienceLevel(-MinionsCore.evilDeedXPCost);
                    MinionsCore.onMasterAddedEvil(player);
                }
                break;
            }
            
            case CMDPICKUPENT:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int targetID = (Integer) packetReadout[2];
                
                Entity target = MinionsCore.findEntityByID(player.worldObj, targetID);
                if (target instanceof EntityAnimal || target instanceof EntityPlayer)
                {
                    MinionsCore.orderMinionToPickupEntity(player, (EntityLivingBase) target);
                }
                break;
            }
            
            case CMDDROPALL:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int targetID = (Integer) packetReadout[2];
                
                Entity target = MinionsCore.findEntityByID(player.worldObj, targetID);
                if (target instanceof EntityMinion)
                {
                    MinionsCore.orderMinionToDrop(player, (EntityMinion) target);
                }
                break;
            }
            
            case CMDMINIONSPAWN:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (MinionsCore.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    
                    if (MinionsCore.spawnMinionsForPlayer(player, x, y, z))
                    {
                        MinionsCore.exhaustPlayerBig(player);
                    }
                    
                    Object[] toSend = {MinionsCore.hasPlayerMinions(player) ? 1 : 0, MinionsCore.hasAllMinions(player) ? 1 : 0};
                    PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend), p);
                }
                break;
            }
            
            case CMDCHOPTREES:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                if (MinionsCore.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    MinionsCore.orderMinionsToChopTrees(player, x, y, z);
                    MinionsCore.exhaustPlayerBig(player);
                }
                break;
            }
            
            case CMDSTAIRWELL:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (MinionsCore.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    MinionsCore.orderMinionsToDigStairWell(player, x, y, z);
                    MinionsCore.exhaustPlayerBig(player);
                }
                break;
            }
            
            case CMDSTRIPMINE:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (MinionsCore.hasPlayerWillPower(player))
                {
                    int x = (Integer) packetReadout[1];
                    int y = (Integer) packetReadout[2];
                    int z = (Integer) packetReadout[3];
                    MinionsCore.orderMinionsToDigStripMineShaft(player, x, y, z);
                    MinionsCore.exhaustPlayerBig(player);
                }
                break;
            }
            
            case CMDASSIGNCHEST:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int x = (Integer) packetReadout[1];
                int y = (Integer) packetReadout[2];
                int z = (Integer) packetReadout[3];
                MinionsCore.orderMinionsToChestBlock(player, x, y, z);
                break;
            }
            
            case CMDMOVETO:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int x = (Integer) packetReadout[1];
                int y = (Integer) packetReadout[2];
                int z = (Integer) packetReadout[3];
                MinionsCore.orderMinionsToMoveTo(player, x, y, z);
                break;
            }
            
            case CMDMINEOREVEIN:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                int x = (Integer) packetReadout[1];
                int y = (Integer) packetReadout[2];
                int z = (Integer) packetReadout[3];
                MinionsCore.orderMinionsToMineOre(player, x, y, z);
                break;
            }
            
            case CMDFOLLOW:
            {
                MinionsCore.orderMinionsToFollow(player);
                break;
            }
            
            case REQUESTXPSETTING:
            {
                Object[] toSend = {MinionsCore.evilDeedXPCost};
                //manager.addToSendQueue(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), 13, toSend));
                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend), p);
                break;
            }
            
            case CMDUNSUMMON:
            {
                if (player != null)
                {
                    MinionsCore.unSummonPlayersMinions(player);
                }
                break;
            }
            
            case CMDCUSTOMDIG:
            {
                Class<?>[] decodeAs = {String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                
                if (player != null && MinionsCore.hasPlayerWillPower(player))
                {
                    MinionsCore.orderMinionsToDigCustomSpace(player, (Integer)packetReadout[1], (Integer)packetReadout[2], (Integer)packetReadout[3], (Integer)packetReadout[4], (Integer)packetReadout[5]);
                    MinionsCore.exhaustPlayerBig(player);
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
                
                if (MinionsCore.hasPlayerWillPower(caster))
                {
                    long randomizer = caster.worldObj.rand.nextLong();
                    
                    // (startx, starty, startz, endx, endy, endz, randomlong)
                    Object[] toSend = { start.x, start.y, start.z, end.x, end.y, end.z, randomizer };
                    Packet pcket = ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.LIGHTNINGBOLT.ordinal(), toSend);
                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendToAllNear(caster.posX, caster.posY, caster.posZ, 50D, caster.worldObj.provider.dimensionId, pcket);
                    
                    spawnLightningBolt(caster.worldObj, caster, start, end, randomizer);
                    
                    MinionsCore.exhaustPlayerSmall(caster);                
                }
                break;
            }
            
            case SOUNDTOALL:
            {
                Class<?>[] decodeAs = {Integer.class, String.class};
                Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
                Entity ent = MinionsCore.findEntityByID(player.worldObj, (Integer) packetReadout[0]);
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
