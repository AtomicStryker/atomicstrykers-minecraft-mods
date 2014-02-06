package com.sirolf2009.necromancy.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGeneric extends Item
{

    public ItemGeneric()
    {
        super();
        IIcons = new IIcon[names.length];
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (stack.getItemDamage() == 0)
            if (player.inventory.consumeInventoryItem(Items.glass_bottle))
            {
                stack.stackSize--;
                if (!player.inventory.addItemStackToInventory(new ItemStack(ItemNecromancy.genericItems, 1, 2)))
                {
                    player.entityDropItem(new ItemStack(ItemNecromancy.genericItems, 1, 2), 0f);
                }
            }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    public boolean isFull3D()
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Returns true if this item should be rotated by 180 degrees around the Y axis when being held in an entities
     * hands.
     */
    public boolean shouldRotateAroundWhenRendering()
    {
        return true;
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
        if (par1ItemStack.getItemDamage() > names.length)
        {
            par1ItemStack.setItemDamage(2);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack)
    {
        return new StringBuilder().append("").append(names[par1ItemStack.getItemDamageForDisplay()]).toString();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int var4 = 0; var4 < names.length; var4++)
        {
            par3List.add(new ItemStack(par1, 1, var4));
        }
    }

    public static ItemStack getItemStackFromName(String name)
    {
        for (int i = 0; i < names.length; i++)
            if (names[i].equalsIgnoreCase(name))
                return new ItemStack(ItemNecromancy.genericItems, 1, i);
        return null;
    }

    public static ItemStack getItemStackFromName(String name, int amount)
    {
        for (int i = 0; i < names.length; i++)
            if (names[i].equalsIgnoreCase(name))
                return new ItemStack(ItemNecromancy.genericItems, amount, i);
        return null;
    }

    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
        for (int index = 0; index < names.length; index++)
        {
            String path = names[index].replace(" ", "").toLowerCase();
            IIcons[index] = IIconRegister.registerIcon("necromancy:" + path.toLowerCase());
        }
        tearBlood = IIconRegister.registerIcon("necromancy:BloodTear");
        tearNormal = IIconRegister.registerIcon("necromancy:Tear");
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        return IIcons[par1];
    }

    private IIcon[] IIcons;
    public static String names[] = { "Bone Needle", "Soul in a Jar", "Jar of Blood", "Brain on a Stick" };
    public static IIcon tearNormal;
    public static IIcon tearBlood;
}
