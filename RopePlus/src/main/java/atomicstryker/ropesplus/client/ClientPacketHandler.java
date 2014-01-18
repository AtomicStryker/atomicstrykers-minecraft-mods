package atomicstryker.ropesplus.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.Settings_RopePlus;
import atomicstryker.ropesplus.common.network.ForgePacketWrapper;
import atomicstryker.ropesplus.common.network.PacketDispatcher.IPacketHandler;
import atomicstryker.ropesplus.common.network.PacketDispatcher.WrappedPacket;

public class ClientPacketHandler implements IPacketHandler
{

	@SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(int packetID, WrappedPacket packet, EntityPlayer player)
    {
        ByteBuf data = packet.data;
        
        //System.out.println("client got packet of id "+packetID);
        
        if (packetID == 2) // server tells client he has a grappling hook out
        {
        	RopesPlusClient.grapplingHookOut = true;
        }
        else if (packetID == 3) // server tells client he has a grappling hook out no longer
        {
        	RopesPlusClient.grapplingHookOut = false;
        }
        else if (packetID == 4) // server tells client to accept hookshot drag ownership { entID hookshot }
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class, Integer.class };
            Object[] readOut = ForgePacketWrapper.readPacketData(data, decodeAs);
            RopesPlusCore.proxy.setHasClientRopeOut(true);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
            RopesPlusCore.proxy.setShouldHookShotPull(0f);
            RopesPlusClient.onAffixedToHookShotRope((Integer) readOut[0]);
            ((EntityPlayer)player).worldObj.spawnParticle("largeexplode", ((Integer)readOut[1])+0.5D, (Integer)readOut[2], ((Integer)readOut[3])+0.5D, 1.0D, 0.0D, 0.0D);
        }
        else if (packetID == 5) // server tells client hookshot is now pulling
        {
            RopesPlusCore.proxy.setShouldHookShotPull(-1f);
        }
        else if (packetID == 6) // server tells client hookshot is gone now
        {
            RopesPlusCore.proxy.setHasClientRopeOut(false);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
            RopesPlusCore.proxy.setShouldHookShotPull(0f);
            
            EntityPlayer p = (EntityPlayer) player;
            for (Object o : p.worldObj.loadedEntityList)
            {
                if (o instanceof EntityFreeFormRope)
                {
                    EntityFreeFormRope rope = (EntityFreeFormRope) o;
                    if (rope.getShooter() != null && rope.getShooter().equals(p))
                    {
                        rope.setDead();
                        break;
                    }
                }
            }
        }
        else if (packetID == 7) // server tells client to slide down zipline { entID ropeEnt }
        {
            Class[] decodeAs = { Integer.class };
            Object[] readOut = ForgePacketWrapper.readPacketData(data, decodeAs);
            RopesPlusClient.onUsedZipLine((Integer) readOut[0]);
        }
        else if (packetID == 8) // server tells client bow hook setting to override client setting { boolean bowHookDisabled }
        {
            Class[] decodeAs = { Boolean.class };
            Object[] readOut = ForgePacketWrapper.readPacketData(data, decodeAs);
            Settings_RopePlus.disableBowHook = (Boolean) readOut[0];
        }
	}

}
