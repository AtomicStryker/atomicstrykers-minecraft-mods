package atomicstryker.petbat.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemPocketedPetBat extends Item
{

    protected ItemPocketedPetBat(int par1)
    {
        super(par1);
        maxStackSize = 1;
        setMaxDamage(28);
        setCreativeTab(CreativeTabs.tabCombat);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("petbat:pocketbat");
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if (world.isRemote)
        {
            PetBatMod.proxy.displayGui(itemStack);
        }
        
        return itemStack;
    }
    
    @Override
    public boolean getIsRepairable(ItemStack batStack, ItemStack repairStack)
    {
        return repairStack.itemID == PetBatMod.instance().TAME_ITEM_ID;
    }
    
    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    @Override
    public boolean getShareTag()
    {
        return true;
    }
    
    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return stack.stackTagCompound != null && PetBatMod.instance().getLevelFromExperience(stack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP")) > 5;
    }
    
    public static ItemStack fromBatEntity(EntityPetBat batEnt)
    {
        if (batEnt.worldObj.isRemote)
        {
            return null;
        }
        
        ItemStack batstack = new ItemStack(PetBatMod.instance().itemPocketedBat);
        writeCompoundStringToItemStack(batstack, "display", "Name", batEnt.getDisplayName());
        writeCompoundStringToItemStack(batstack, "petbatmod", "Owner", batEnt.getOwnerName());
        writeCompoundIntegerToItemStack(batstack, "petbatmod", "BatXP", batEnt.getBatExperience());
        batstack.setItemDamage(invertHealthValue(batEnt.getHealth(), batEnt.getMaxHealth()));
        return batstack;
    }
    
    public static EntityPetBat toBatEntity(EntityPlayer player, ItemStack batStack)
    {
        EntityPetBat batEnt = new EntityPetBat(player.worldObj);
        String owner = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getString("Owner") : player.username;
        String name = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("display").getString("Name") : "I was cheated";
        int xp = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP") : 0;
        if (owner.equals("")) owner = player.username;
        if (name.equals("")) name = "I was cheated";
        batEnt.setNames(owner, name);
        batEnt.setEntityHealth(invertHealthValue(batStack.getItemDamage(), 16 + (2*PetBatMod.instance().getLevelFromExperience(xp))));
        batEnt.setBatExperience(xp);
        return batEnt;
    }
    
    public static EntityPetBat toBatEntity(World world, ItemStack batStack)
    {
        EntityPetBat batEnt = new EntityPetBat(world);
        String owner = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getString("Owner") : ((EntityPlayer)world.playerEntities.get(0)).username;
        String name = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("display").getString("Name") : "I was cheated";
        int xp = batStack.stackTagCompound != null ? batStack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP") : 0;
        if (owner.equals("")) owner = ((EntityPlayer)world.playerEntities.get(0)).username;
        if (name.equals("")) name = "I was cheated";
        batEnt.setNames(owner, name);
        batEnt.setEntityHealth(invertHealthValue(batStack.getItemDamage(), 16 + (2*PetBatMod.instance().getLevelFromExperience(xp))));
        batEnt.setBatExperience(xp);
        return batEnt;
    }
    
    public static void writeBatNameToItemStack(ItemStack stack, String name)
    {
        writeCompoundStringToItemStack(stack, "display", "Name", name);
    }
    
    public static String getBatNameFromItemStack(ItemStack stack)
    {
        return stack.stackTagCompound != null ? stack.stackTagCompound.getCompoundTag("display").getString("Name") : "I was cheated";
    }
    
    /**
     * @param input value to invert
     * @param max maximum health value
     * @return inverted value
     */
    public static int invertHealthValue(int input, int max)
    {
        return Math.abs(input - max);
    }
    
    public static void writeCompoundIntegerToItemStack(ItemStack stack, String tag, String key, int data)
    {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setInteger(key, data);
    }

    public static void writeCompoundStringToItemStack(ItemStack stack, String tag, String key, String data)
    {
        checkCompoundTag(stack, tag);
        stack.stackTagCompound.getCompoundTag(tag).setString(key, data);
    }
    
    private static void checkCompoundTag(ItemStack stack, String tag)
    {
        if (stack.stackTagCompound == null)
        {
            stack.stackTagCompound = new NBTTagCompound();
        }

        if (!stack.stackTagCompound.hasKey(tag))
        {
            stack.stackTagCompound.setCompoundTag(tag, new NBTTagCompound());
        }
    }
    
}
