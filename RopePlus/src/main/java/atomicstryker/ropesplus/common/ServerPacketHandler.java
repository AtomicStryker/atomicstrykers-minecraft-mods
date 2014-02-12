package atomicstryker.ropesplus.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.network.ForgePacketWrapper;
import atomicstryker.network.PacketDispatcher.IPacketHandler;
import atomicstryker.network.WrappedPacket;

public class ServerPacketHandler implements IPacketHandler
{

    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(int packetID, WrappedPacket packet, EntityPlayer player)
    {
        ByteBuf data = packet.data;

        if (packetID == 1) // arrow slot selection from client
        {
            Class[] decodeAs = { Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            RopesPlusCore.instance.setselectedSlot((EntityPlayer) player, (Integer) packetReadout[0]);
        }
        else if (packetID == 5) // client has reached hookshot max pull, kill the rope ent { int entID }
        {
            Class[] decodeAs = { Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            EntityPlayer p = (EntityPlayer) player;
            Entity target = p.worldObj.getEntityByID((Integer) packetReadout[0]);
            if (target != null && target instanceof EntityFreeFormRope)
            {
                target.setDead();
            }
        }
        else if (packetID == 7) // client updates server about its position on zipline { int entID, float relativeLength }
        {
            Class[] decodeAs = { Integer.class, Float.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            EntityPlayer p = (EntityPlayer) player;
            Entity target = p.worldObj.getEntityByID((Integer) packetReadout[0]);
            if (target != null && target instanceof EntityFreeFormRope)
            {
                double[] coords = ((EntityFreeFormRope) target).getCoordsAtRelativeLength((Float) packetReadout[1]);
                // System.out.println("server got pos update: "+coords[0]+", "+coords[1]+", "+coords[2]);
                p.setPositionAndUpdate(coords[0], coords[1] - 2.5D, coords[2]);
                p.fallDistance = 0;
            }
        }
        else if (packetID == 8) // client wants a sound played at player ent { String sound }
        {
            Class[] decodeAs = { String.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            EntityPlayer p = (EntityPlayer) player;
            p.worldObj.playSoundAtEntity(p, (String) packetReadout[0], 1f, 1f);
        }
    }

}
