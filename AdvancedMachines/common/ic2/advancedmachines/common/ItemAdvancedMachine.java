package ic2.advancedmachines.common;

import java.util.*;

import net.minecraft.src.*;
import ic2.api.*;

public class ItemAdvancedMachine extends ItemBlock
{
    public ItemAdvancedMachine(int var1)
    {
        super(var1);
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public int getMetadata(int var1)
    {
        return var1;
    }

    @Override
    public String getItemNameIS(ItemStack var1)
    {
        int var2 = var1.getItemDamage();
        switch (var2)
        {
            case 0:
                return "blockRotaryMacerator";
            case 1:
                return "blockSingularityCompressor";
            case 2:
                return "blockCentrifugeExtractor";
            default:
                return null;
        }
    }
    
    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
    }
}
