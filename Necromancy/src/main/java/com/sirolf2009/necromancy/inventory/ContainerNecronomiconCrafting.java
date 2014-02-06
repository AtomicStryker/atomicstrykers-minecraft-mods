package com.sirolf2009.necromancy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.world.World;

public class ContainerNecronomiconCrafting extends ContainerWorkbench
{

    public ContainerNecronomiconCrafting(InventoryPlayer inventoryPlayer, World world, int x, int y, int z)
    {
        super(inventoryPlayer, world, x, y, z);
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return true;
    }

}