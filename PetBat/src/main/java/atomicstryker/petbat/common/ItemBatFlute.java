package atomicstryker.petbat.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemBatFlute extends Item
{

    protected ItemBatFlute()
    {
        super();
        maxStackSize = 1;
        setMaxDamage(0);
    }
    
    @Override
    public boolean getShareTag()
    {
        return true;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        EntityPetBat bat;
        if (itemStack.getTagCompound() != null)
        {
            String batname = itemStack.getTagCompound().getString("batName");
            for (int i = 0; i < world.loadedEntityList.size(); i++)
            {
                if (world.loadedEntityList.get(i) instanceof EntityPetBat)
                {
                    bat = (EntityPetBat) world.loadedEntityList.get(i);
                    if (bat.getName().equals(batname))
                    {
                        bat.recallToOwner();
                        itemStack.stackSize = 0;
                    }
                }
            }
            return itemStack;
        }
        return null;
    }
    

    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        String batname = itemStack.getTagCompound() != null ? (": " + itemStack.getTagCompound().getString("batName")) : ": unassigned";
        return EnumChatFormatting.GOLD + super.getItemStackDisplayName(itemStack) + batname;
    }
    
}
