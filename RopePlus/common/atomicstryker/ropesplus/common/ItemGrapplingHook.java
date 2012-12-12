package atomicstryker.ropesplus.common;

import java.util.Map;
import java.util.Random;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.ropesplus.client.RopesPlusClient;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class ItemGrapplingHook extends Item
{

    public ItemGrapplingHook(int i)
    {
        super(i);
        maxStackSize = 1;
        setTextureFile("/atomicstryker/ropesplus/client/ropesPlusItems.png");
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
        if (!world.isRemote && RopesPlusCore.getGrapplingHookMap().get(entityplayer) != null)
        {
        	//System.out.println("recalling serverside hook!");
        	RopesPlusCore.getGrapplingHookMap().get(entityplayer).recallHook(entityplayer);
        	PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_Ropes", 3, null), (Player)entityplayer);
        	RopesPlusCore.getGrapplingHookMap().remove(entityplayer);
        }
		else
        {
            world.playSoundAtEntity(entityplayer, "random.hurt", 1.0F, 1.0F / (itemRand.nextFloat() * 0.1F + 0.95F));
            if(!world.isRemote)
            {
            	//System.out.println("spawning serverside hook!");
            	EntityGrapplingHook newhook = new EntityGrapplingHook(world, entityplayer);
                world.spawnEntityInWorld(newhook);
                RopesPlusCore.getGrapplingHookMap().put(entityplayer, newhook);
                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_Ropes", 2, null), (Player)entityplayer);
            }
            entityplayer.swingItem();
        }
        return itemstack;
    }
}
