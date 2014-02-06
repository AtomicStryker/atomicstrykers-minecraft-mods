package com.sirolf2009.necromancy.core.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.block.BlockAltar;
import com.sirolf2009.necromancy.block.BlockScentBurner;
import com.sirolf2009.necromancy.block.BlockSewing;
import com.sirolf2009.necromancy.client.gui.GuiAltar;
import com.sirolf2009.necromancy.client.gui.GuiScentBurner;
import com.sirolf2009.necromancy.client.gui.GuiSewing;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.inventory.ContainerAltar;
import com.sirolf2009.necromancy.inventory.ContainerScentBurner;
import com.sirolf2009.necromancy.inventory.ContainerSewing;
import com.sirolf2009.necromancy.network.PacketDispatcher.IPacketHandler;
import com.sirolf2009.necromancy.network.PacketDispatcher.WrappedPacket;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntityScentBurner;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

import cpw.mods.fml.common.network.IGuiHandler;

public class PacketHandler implements IPacketHandler, IGuiHandler
{

    @Override
    public Object getServerGuiElement(int ID, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, int x, int y, int z)
    {
        if (ID == BlockAltar.guiID)
            return new ContainerAltar(player.inventory, (TileEntityAltar) player.worldObj.getTileEntity(x, y, z));
        if (ID == BlockSewing.guiID)
            return new ContainerSewing(player.inventory, player.worldObj.getTileEntity(x, y, z));
        if (ID == BlockScentBurner.guiID)
            return new ContainerScentBurner(player.inventory, player.worldObj.getTileEntity(x, y, z));
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == BlockAltar.guiID)
            return new GuiAltar(player.inventory, (TileEntityAltar) player.worldObj.getTileEntity(x, y, z));
        if (ID == BlockSewing.guiID)
            return new GuiSewing(player.inventory, (TileEntitySewing) player.worldObj.getTileEntity(x, y, z));
        if (ID == BlockScentBurner.guiID)
            return new GuiScentBurner(player.inventory, (TileEntityScentBurner) player.worldObj.getTileEntity(x, y, z));
        return null;
    }

    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        if (packetType == 0)
        {
            ClientProxy.mc.thePlayer.getEntityData().setBoolean("aggressive", packet.data.readInt() == 1);
        }
        else if (packetType == 1)
        { // we're making friends :D
            EntityPlayer playerEntity = (EntityPlayer) ClientProxy.mc.theWorld.getEntityByID(packet.data.readInt() & 0xFF);
            playerEntity.getEntityData().setString(playerEntity.getCommandSenderName(), "friend");
        }
        else if (packetType == 2)
        { // who needs friends anyway
            EntityPlayer playerEntity = (EntityPlayer) ClientProxy.mc.theWorld.getEntityByID(packet.data.readInt() & 0xFF);
            playerEntity.getEntityData().setString(playerEntity.getCommandSenderName(), "enemy");
        }
    }
}