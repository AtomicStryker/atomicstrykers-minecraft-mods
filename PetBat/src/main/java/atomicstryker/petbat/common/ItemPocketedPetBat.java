package atomicstryker.petbat.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemPocketedPetBat extends Item
{

    protected ItemPocketedPetBat()
    {
        super();
        maxStackSize = 1;
        setMaxDamage(28);
        setCreativeTab(CreativeTabs.tabCombat);
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
        return false;
    }
    
    @Override
    public boolean getShareTag()
    {
        return true;
    }
    
    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return stack.getTagCompound() != null && PetBatMod.instance().getLevelFromExperience(stack.getTagCompound().getCompoundTag("petbatmod").getInteger("BatXP")) > 5;
    }
    
    public static ItemStack fromBatEntity(EntityPetBat batEnt)
    {
        if (batEnt.worldObj.isRemote)
        {
            return null;
        }
        
        ItemStack batstack = new ItemStack(PetBatMod.instance().itemPocketedBat);
        writeCompoundStringToItemStack(batstack, "display", "Name", batEnt.getDisplayName().getFormattedText());
        writeCompoundStringToItemStack(batstack, "petbatmod", "Owner", batEnt.getOwnerName());
        writeCompoundIntegerToItemStack(batstack, "petbatmod", "BatXP", batEnt.getBatExperience());
        writeCompoundFloatToItemStack(batstack, "petbatmod", "health", batEnt.getHealth());
        batstack.getTagCompound().getCompoundTag("petbatmod").setFloat("health", batEnt.getHealth());
        batstack.setItemDamage((int) invertHealthValue(batEnt.getHealth(), batEnt.getMaxHealth()));
        return batstack;
    }
    
    public static EntityPetBat toBatEntity(World world, ItemStack batStack, EntityPlayer player)
    {
        EntityPetBat batEnt = new EntityPetBat(world);
        String owner = batStack.getTagCompound() != null ? batStack.getTagCompound().getCompoundTag("petbatmod").getString("Owner") : player.getCommandSenderName();
        String name = batStack.getTagCompound() != null ? batStack.getTagCompound().getCompoundTag("display").getString("Name") : "Battus Genericus";
        int xp = batStack.getTagCompound() != null ? batStack.getTagCompound().getCompoundTag("petbatmod").getInteger("BatXP") : 0;
        if (owner.equals("")) owner = player.getCommandSenderName();
        if (name.equals("")) name = "Battus Genericus";
        batEnt.setNames(owner, name);
        batEnt.setOwnerEntity(player);
        batEnt.setBatExperience(xp);
        batEnt.setHealth(batStack.getTagCompound() != null ? batStack.getTagCompound().getCompoundTag("petbatmod").getFloat("health") : batEnt.getMaxHealth());
        return batEnt;
    }
    
    public static void writeBatNameToItemStack(ItemStack stack, String name)
    {
        writeCompoundStringToItemStack(stack, "display", "Name", EnumChatFormatting.DARK_PURPLE + name);
    }
    
    public static String getBatNameFromItemStack(ItemStack stack)
    {
        return (stack.getTagCompound() != null ? stack.getTagCompound().getCompoundTag("display").getString("Name") : "Battus Genericus");
    }
    
    /**
     * @param input value to invert
     * @param max maximum health value
     * @return inverted value
     */
    public static double invertHealthValue(double input, double max)
    {
        return Math.abs(input - max);
    }
    
    public static void writeCompoundIntegerToItemStack(ItemStack stack, String tag, String key, int data)
    {
        checkCompoundTag(stack, tag);
        stack.getTagCompound().getCompoundTag(tag).setInteger(key, data);
    }
    
    public static void writeCompoundFloatToItemStack(ItemStack stack, String tag, String key, float data)
    {
        checkCompoundTag(stack, tag);
        stack.getTagCompound().getCompoundTag(tag).setFloat(key, data);
    }

    public static void writeCompoundStringToItemStack(ItemStack stack, String tag, String key, String data)
    {
        checkCompoundTag(stack, tag);
        stack.getTagCompound().getCompoundTag(tag).setString(key, data);
    }
    
    private static void checkCompoundTag(ItemStack stack, String tag)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }

        if (!stack.getTagCompound().hasKey(tag))
        {
            stack.getTagCompound().setTag(tag, new NBTTagCompound());
        }
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.DARK_PURPLE+super.getItemStackDisplayName(itemStack);
    }
    
}
