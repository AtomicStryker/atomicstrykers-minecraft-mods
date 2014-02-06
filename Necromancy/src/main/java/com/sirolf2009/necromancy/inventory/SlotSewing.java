package com.sirolf2009.necromancy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

import cpw.mods.fml.common.FMLCommonHandler;

public class SlotSewing extends SlotCrafting
{

    public SlotSewing(EntityPlayer par1EntityPlayer, IInventory par2IInventory, IInventory par3IInventory, TileEntity sewing, ContainerSewing containerSewing, int par4, int par5, int par6)
    {
        super(par1EntityPlayer, par3IInventory, par3IInventory, par4, par5, par6);
        thePlayer = par1EntityPlayer;
        craftMatrix = par2IInventory;
        this.sewing = (TileEntitySewing) sewing;
        this.containerSewing = containerSewing;
    }

    @Override
    protected void onCrafting(ItemStack par1ItemStack)
    {
        super.onCrafting(par1ItemStack);
        par1ItemStack.onCrafting(thePlayer.worldObj, thePlayer, amountCrafted);
        amountCrafted = 0;
        sewing.decrStackSize(0, 1);
        sewing.decrStackSize(1, 1);
        containerSewing.onCraftMatrixChanged(craftMatrix);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
    {
        FMLCommonHandler.instance().firePlayerCraftingEvent(par1EntityPlayer, par2ItemStack, craftMatrix);
        this.onCrafting(par2ItemStack);

        for (int var3 = 0; var3 < craftMatrix.getSizeInventory(); ++var3)
        {
            ItemStack var4 = craftMatrix.getStackInSlot(var3);

            if (var4 != null)
            {
                craftMatrix.decrStackSize(var3, 1);

                if (var4.getItem().hasContainerItem(par2ItemStack))
                {
                    ItemStack var5 = var4.getItem().getContainerItem(var4);

                    if (var5.isItemStackDamageable() && var5.getItemDamage() > var5.getMaxDamage())
                    {
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(thePlayer, var5));
                        var5 = null;
                    }

                    if (var5 != null
                            && (!var4.getItem().doesContainerItemLeaveCraftingGrid(var4) || !thePlayer.inventory.addItemStackToInventory(var5)))
                    {
                        if (craftMatrix.getStackInSlot(var3) == null)
                        {
                            craftMatrix.setInventorySlotContents(var3, var5);
                        }
                        else
                        {
                            thePlayer.entityDropItem(var5, 0f);
                        }
                    }
                }
            }
        }
    }

    private final IInventory craftMatrix;
    private EntityPlayer thePlayer;
    private int amountCrafted;
    private TileEntitySewing sewing;
    private ContainerSewing containerSewing;
}
