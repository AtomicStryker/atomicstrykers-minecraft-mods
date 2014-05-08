package atomicstryker.ropesplus.common;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import atomicstryker.ropesplus.client.RopesPlusClient;
import atomicstryker.ropesplus.common.network.GrapplingHookPacket;

public class ItemGrapplingHook extends Item
{

    public ItemGrapplingHook()
    {
        super();
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabTools);
    }
    
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("ropesplus:itemGrapplingHook");
    }

    @Override
    public boolean isFull3D()
    {
        return true;
    }

    @Override
    public boolean shouldRotateAroundWhenRendering()
    {
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        if(world.isRemote && RopesPlusClient.grapplingHookOut)
        {
        	//System.out.println("client swings, has hook out!");
            entityplayer.swingItem();
        }
        if (!world.isRemote && RopesPlusCore.instance.getGrapplingHookMap().get(entityplayer) != null)
        {
        	//System.out.println("recalling serverside hook!");
        	RopesPlusCore.instance.getGrapplingHookMap().get(entityplayer).recallHook(entityplayer);
        	RopesPlusCore.instance.networkHelper.sendPacketToPlayer(new GrapplingHookPacket(false), (EntityPlayerMP) entityplayer);
        	RopesPlusCore.instance.getGrapplingHookMap().remove(entityplayer);
        }
		else
        {
            world.playSoundAtEntity(entityplayer, "random.hurt", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
            if(!world.isRemote)
            {
            	//System.out.println("spawning serverside hook!");
            	EntityGrapplingHook newhook = new EntityGrapplingHook(world, entityplayer);
                world.spawnEntityInWorld(newhook);
                RopesPlusCore.instance.getGrapplingHookMap().put(entityplayer, newhook);
                RopesPlusCore.instance.networkHelper.sendPacketToPlayer(new GrapplingHookPacket(true), (EntityPlayerMP) entityplayer);
            }
            entityplayer.swingItem();
        }
        return itemstack;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.GOLD+super.getItemStackDisplayName(itemStack);
    }
}
