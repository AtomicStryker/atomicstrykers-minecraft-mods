package com.sirolf2009.necromancy.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.sirolf2009.necromancy.Necromancy;

public class ItemBodyPart extends Item
{

    public ItemBodyPart()
    {
        super();
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancyBodyParts);
        addParts("Cow", "Torso", "Head", "Arm", "Legs");
        addParts("Creeper", "Torso", "Legs");
        addParts("Enderman", "Head", "Torso", "Arm", "Legs");
        addParts("Pig", "Head", "Torso", "Arm", "Legs");
        addParts("Pigzombie", "Head", "Torso", "Arm", "Legs");
        addParts("Skeleton", "Torso", "Arm", "Legs");
        // addParts("Small Slime", "Head", "Torso", "Arm", "Legs");
        addParts("Spider", "Head", "Torso", "Legs");
        addParts("Zombie", "Torso", "Arm", "Legs");
        addParts("Chicken", "Head", "Torso", "Arm", "Legs");
        addParts("Mooshroom", "Head", "Torso", "Arm", "Legs");
        addParts("Villager", "Head", "Torso", "Arm", "Legs");
        addParts("Witch", "Head", "Torso", "Arm", "Legs");
        addParts("Squid", "Head", "Torso", "Legs");
        addParts("Cave_Spider", "Head", "Torso", "Legs");
        addParts("Sheep", "Head", "Torso", "Arm", "Legs");
        textures = new IIcon[necroEntities.size()];
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack)
    {
        return necroEntities.get(par1ItemStack.getItemDamage());
    }

    public void addParts(String entity, String... parts)
    {
        for (String part : parts)
        {
            necroEntities.add(entity + " " + part);
        }
    }

    public static ItemStack getItemStackFromName(String name, int amount)
    {
        Iterator<String> itr = necroEntities.iterator();
        int i = 0;
        while (itr.hasNext())
        {
            String part = itr.next();
            if (part.equals(name))
                return new ItemStack(ItemNecromancy.bodyparts, amount, i);
            i++;
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i < necroEntities.size(); i++)
        {
            par3List.add(new ItemStack(par1, 1, i));
        }
    }

    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
        String name, path;
        for (int index = 0; index < necroEntities.size(); index++)
        {
            name = necroEntities.get(index);
            path = name.replaceAll(" ", "/");
            path = path.replaceAll("_", " ");
            textures[index] = IIconRegister.registerIcon("necromancy:bodyparts/" + path);
        }
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        return textures[par1];
    }

    public static List<String> necroEntities = new ArrayList<String>();
    public static IIcon[] textures;
}
