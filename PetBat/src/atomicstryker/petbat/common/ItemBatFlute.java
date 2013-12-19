package atomicstryker.petbat.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemBatFlute extends Item
{

    protected ItemBatFlute(int par1)
    {
        super(par1);
        maxStackSize = 1;
        setMaxDamage(0);
        setCreativeTab(CreativeTabs.tabCombat);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("petbat:batflute");
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
        String batname = itemStack.stackTagCompound.getString("batName");
        for (int i = 0; i < world.loadedEntityList.size(); i++)
        {
            if (world.loadedEntityList.get(i) instanceof EntityPetBat)
            {
                bat = (EntityPetBat) world.loadedEntityList.get(i);
                if (bat.getDisplayName().equals(batname))
                {
                    bat.recallToOwner();
                    itemStack.stackSize = 0;
                }
            }
        }
        return itemStack;
    }
    
    @Override
    public String getItemDisplayName(ItemStack itemStack)
    {
        String batname = itemStack.stackTagCompound != null ? itemStack.stackTagCompound.getString("batName") : "";
        return EnumChatFormatting.GOLD+"Bat Flute: "+batname;
    }
    
}
