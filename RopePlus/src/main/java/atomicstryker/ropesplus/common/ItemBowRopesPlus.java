package atomicstryker.ropesplus.common;

import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBowRopesPlus extends ItemBow
{
    //private int heldTicksBuffer;
    
	public ItemBowRopesPlus()
	{
		super();
		this.setCreativeTab(null);
	}
    
	@Override
    public void onPlayerStoppedUsing(ItemStack usedItemStack, World world, EntityPlayer player, int heldTicks)
    {
	    //heldTicksBuffer = heldTicks;
        // get vanilla bow
        final ItemStack vanillaBow = RopesPlusBowController.getVanillaBowForPlayer(player);
        final ItemStack[] mainInv = player.inventory.mainInventory;
        
    	int arrowSlot = RopesPlusCore.instance.selectedSlot(player);
    	if (arrowSlot != -1)
    	{
            if (mainInv[arrowSlot] != null && player.inventory.hasItemStack(mainInv[arrowSlot]))
            {            
                int ticksLeftToCharge = this.getMaxItemUseDuration(usedItemStack) - heldTicks;
                float bowChargeRatio = (float)ticksLeftToCharge / 20.0F;
                bowChargeRatio = (bowChargeRatio * bowChargeRatio + bowChargeRatio * 2.0F) / 3.0F;

                if ((double)bowChargeRatio < 0.1D)
                {
                    mainInv[player.inventory.currentItem] = vanillaBow;
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
                    mainInv[player.inventory.currentItem] = vanillaBow;
                    return;
                }
                
                EntityProjectileBase newArrow = entityarrow303.newArrow(world, player, bowChargeRatio*2);
                EntityProjectileBase template = RopesPlusCore.instance.getArrowTemplate(newArrow);
                if (template != null)
                {
                    newArrow.damage = template.damage;
                }

                if (bowChargeRatio == 1.0F)
                {
                    newArrow.setIsCritical(true);
                }

                int damageEnchantPower = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, vanillaBow);
                if (damageEnchantPower > 0)
                {
                    newArrow.damage = (int) Math.rint(newArrow.damage + (double)damageEnchantPower * 0.5D + 0.5D);
                }
                
                if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, vanillaBow) > 0)
                {
                    newArrow.setFire(100);
                }

                world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + bowChargeRatio * 0.5F);
                
                if (!player.capabilities.isCreativeMode)
                {
                    player.inventory.consumeInventoryItem(arrowCandidate);
                    player.inventory.inventoryChanged = true;
                }
                
                if (!world.isRemote)
                {
                    world.spawnEntityInWorld(newArrow);
                }
            }
    	}
    	
        if (vanillaBow != null)
        {
            vanillaBow.damageItem(1, player);
        }
        else if (!world.isRemote)
        {
            player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocal("translation.ropesplus:BowCacheFail")));
        }
        
        // put vanilla bow back in hands, do damage etc
        mainInv[player.inventory.currentItem] = vanillaBow;
    }
	
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }
	
	/*
	 * TODO see if this needs fixing
	// these two enable the icon change to render an arrow on the drawn bow
	@Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
	
	@Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        int remainingUseDur = stack.getMaxItemUseDuration() - heldTicksBuffer;

        if (remainingUseDur >= 18)
        {
            return Items.bow.getItemIconForUseDuration(2);
        }

        if (remainingUseDur > 13)
        {
            return Items.bow.getItemIconForUseDuration(1);
        }

        if (remainingUseDur > 0)
        {
            return Items.bow.getItemIconForUseDuration(0);
        }
	    
        return super.getIcon(stack, pass);
    }
    */
	
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.RED+super.getItemStackDisplayName(itemStack);
    }
}
