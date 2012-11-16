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
    public void onPlayerStoppedUsing(ItemStack usedItemStack, World world, EntityPlayer player, int heldTicks)
    {
        // get vanilla bow
        ItemStack vanillaBow = RopesPlusBowController.getVanillaBowForPlayer(player);
	    
    	int arrowSlot = RopesPlusCore.instance.selectedSlot(player);
    	ItemStack[] mainInv = player.inventory.mainInventory;
        if (mainInv[arrowSlot] != null && player.inventory.hasItem(mainInv[arrowSlot].itemID))
        {            
            int ticksLeftToCharge = this.getMaxItemUseDuration(usedItemStack) - heldTicks;
            float bowChargeRatio = (float)ticksLeftToCharge / 20.0F;
            bowChargeRatio = (bowChargeRatio * bowChargeRatio + bowChargeRatio * 2.0F) / 3.0F;

            if ((double)bowChargeRatio < 0.1D)
            {
				mainInv[player.inventory.currentItem] = RopesPlusBowController.getVanillaBowForPlayer(player);
                return;
            }

            if (bowChargeRatio > 1.0F)
            {
                bowChargeRatio = 1.0F;
            }
            
    		EntityArrow303 entityarrow303 = null;
    		Item arrowCandidate = player.inventory.getStackInSlot(arrowSlot).getItem();
    		if (arrowCandidate != null && arrowCandidate instanceof ItemArrow303)
    		{
    			entityarrow303 = ((ItemArrow303)arrowCandidate).arrow;
    		}
    		if(entityarrow303 == null)
    		{
    			mainInv[player.inventory.currentItem] = RopesPlusBowController.getVanillaBowForPlayer(player);
    			return;
    		}
            
    		EntityArrow303 newArrow = entityarrow303.newArrow(world, player, bowChargeRatio*2);

            if (bowChargeRatio == 1.0F)
            {
                newArrow.setIsCritical(true);
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

            world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + bowChargeRatio * 0.5F);

            player.inventory.consumeInventoryItem(mainInv[arrowSlot].itemID);
            player.inventory.inventoryChanged = true;

            if (!world.isRemote)
            {
                world.spawnEntityInWorld(newArrow);
            }
        }
        
        // put vanilla bow back in hands, do damage etc
        mainInv[player.inventory.currentItem] = vanillaBow;
		if (vanillaBow != null)
		{
			mainInv[player.inventory.currentItem].damageItem(1, player);
		}
		else
		{
		    player.sendChatToPlayer("Do not cheat yourself a RopesPlusBow! Use the vanilla bow!");
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
