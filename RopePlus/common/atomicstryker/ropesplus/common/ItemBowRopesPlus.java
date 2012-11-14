package atomicstryker.ropesplus.common;

import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import net.minecraft.src.Enchantment;
import net.minecraft.src.EnchantmentHelper;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumAction;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBow;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class ItemBowRopesPlus extends ItemBow
{
	public ItemBowRopesPlus(int i)
	{
		super(i);
	}

    /**
     * called when the player releases the use item button.
     */
	@Override
    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int heldTicks)
    {
        // get vanilla bow
        ItemStack vanillaBow = RopesPlusBowController.getVanillaBowForPlayer(par3EntityPlayer);
	    
    	int arrowSlot = RopesPlusCore.instance.selectedSlot(par3EntityPlayer); 	
        if (par3EntityPlayer.inventory.mainInventory[arrowSlot] != null && par3EntityPlayer.inventory.hasItem(par3EntityPlayer.inventory.mainInventory[arrowSlot].itemID))
        {            
            int ticksLeftToCharge = this.getMaxItemUseDuration(par1ItemStack) - heldTicks;
            float bowChargeRatio = (float)ticksLeftToCharge / 20.0F;
            bowChargeRatio = (bowChargeRatio * bowChargeRatio + bowChargeRatio * 2.0F) / 3.0F;

            if ((double)bowChargeRatio < 0.1D)
            {
				par3EntityPlayer.inventory.mainInventory[par3EntityPlayer.inventory.currentItem] = RopesPlusBowController.getVanillaBowForPlayer(par3EntityPlayer);
                return;
            }

            if (bowChargeRatio > 1.0F)
            {
                bowChargeRatio = 1.0F;
            }
            
    		EntityArrow303 entityarrow303 = null;
    		Item arrowCandidate = par3EntityPlayer.inventory.getStackInSlot(arrowSlot).getItem();
    		if (arrowCandidate != null && arrowCandidate instanceof ItemArrow303)
    		{
    			entityarrow303 = ((ItemArrow303)arrowCandidate).arrow;
    		}
    		if(entityarrow303 == null)
    		{
    			par3EntityPlayer.inventory.mainInventory[par3EntityPlayer.inventory.currentItem] = RopesPlusBowController.getVanillaBowForPlayer(par3EntityPlayer);
    			return;
    		}
            
    		EntityArrow303 newArrow = entityarrow303.newArrow(par2World, par3EntityPlayer);

            if (bowChargeRatio == 1.0F)
            {
                newArrow.arrowCritical = true;
            }

            int damageEnchantPower = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, vanillaBow);
            if (damageEnchantPower > 0)
            {
                newArrow.dmg = (int) Math.rint(newArrow.dmg + (double)damageEnchantPower * 0.5D + 0.5D);
            }
            
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, vanillaBow) > 0)
            {
                newArrow.setFire(100);
            }

            par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + bowChargeRatio * 0.5F);

            par3EntityPlayer.inventory.consumeInventoryItem(par3EntityPlayer.inventory.mainInventory[arrowSlot].itemID);
            par3EntityPlayer.inventory.inventoryChanged = true;

            if (!par2World.isRemote)
            {
                par2World.spawnEntityInWorld(newArrow);
            }
        }
        
        // put vanilla bow back in hands, do damage etc
        par3EntityPlayer.inventory.mainInventory[par3EntityPlayer.inventory.currentItem] = vanillaBow;
		if (vanillaBow != null)
		{
			par3EntityPlayer.inventory.mainInventory[par3EntityPlayer.inventory.currentItem].damageItem(1, par3EntityPlayer);
		}
		else
		{
		    par3EntityPlayer.sendChatToPlayer("Do not cheat yourself a RopesPlusBow! Use the vanilla bow!");
		}
    }
    
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }
}
