package atomicstryker.minions.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Minion control Item class. Nothing to see here really.
 * 
 * 
 * @author AtomicStryker
 */

public class ItemMastersStaff extends Item
{
    private long lastTime;
    private final long coolDown = 100L;

    public ItemMastersStaff(int var1)
    {
        super(var1);
        this.maxStackSize = 1;
        
        this.setCreativeTab(CreativeTabs.tabCombat);
        lastTime = System.currentTimeMillis();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon("minions:masterstaff");
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack var1, World var2, EntityPlayer var3, int ticksHeld)
    {
        int ticksLeftFromMax = this.getMaxItemUseDuration(var1) - ticksHeld;
        float pointStrength = (float) ticksLeftFromMax / 20.0F;
        pointStrength = (pointStrength * pointStrength + pointStrength * 2.0F) / 3.0F;

        if (System.currentTimeMillis() > lastTime + coolDown)
        {
            lastTime = System.currentTimeMillis();
            if (pointStrength > 1.0F)
            {
                // full power!
                MinionsCore.proxy.onMastersGloveRightClickHeld(var1, var2, var3);
            }
            else
            {
                // shorter tap
                MinionsCore.proxy.onMastersGloveRightClick(var1, var2, var3);
            }
        }
    }

    @Override
    public ItemStack onEaten(ItemStack var1, World var2, EntityPlayer var3)
    {
        return var1;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack var1)
    {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack var1)
    {
        return EnumAction.block;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3)
    {
        var3.setItemInUse(var1, this.getMaxItemUseDuration(var1));
        return var1;
    }

    @Override
    public String getItemDisplayName(ItemStack itemStack)
    {
        return "§EMaster's Staff";
    }
}
