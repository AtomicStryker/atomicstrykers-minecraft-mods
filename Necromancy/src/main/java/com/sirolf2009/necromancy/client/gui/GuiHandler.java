package com.sirolf2009.necromancy.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.inventory.ContainerAltar;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityAltar)
            return new ContainerAltar(player.inventory, (TileEntityAltar) tileEntity);
        else
            return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityAltar)
            return new GuiAltar(player.inventory, (TileEntityAltar) tileEntity);
        if (tileEntity instanceof TileEntitySewing)
            return new GuiSewing(player.inventory, (TileEntitySewing) tileEntity);
        else
            return null;
    }
}
