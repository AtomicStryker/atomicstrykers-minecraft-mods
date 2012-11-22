package atomicstryker.powerconverters.common;

import ic2.api.Items;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.fuels.IronEngineFuel;

public class ItemJetpackFueller extends Item
{
	public ItemJetpackFueller(int i)
	{
		super(i);
		setItemName("jetpackFueller");
		setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof ITankContainer)
		{
		    ITankContainer tank = ((ITankContainer)te);
		    for (ILiquidTank liquidtank : tank.getTanks(ForgeDirection.getOrientation(side)))
		    {
		        for (Object fuelO : IronEngineFuel.fuels)
		        {
		            if (liquidtank.getLiquid().itemID == ((IronEngineFuel)fuelO).liquid.itemID)
		            {
		                for(int i = 0; i < entityplayer.inventory.getSizeInventory(); i++)
		                {
		                    ItemStack s = entityplayer.inventory.getStackInSlot(i);
		                    if(s != null && s.itemID == Items.getItem("jetpack").getItem().shiftedIndex)
		                    {
		                        int fuelToUse = s.getItemDamage() / PowerConverterCore.jetpackFuelRefilledPerFuelUnit;
		                        LiquidStack fuelDrained = tank.drain(0, fuelToUse, true);
		                        int jetpackFuel = s.getMaxDamage() - fuelDrained.amount * PowerConverterCore.jetpackFuelRefilledPerFuelUnit - (s.getMaxDamage() - s.getItemDamage());
		                        ItemStack newjet = Items.getItem("jetpack").copy();
		                        newjet.setItemDamage(jetpackFuel);
		                        entityplayer.inventory.setInventorySlotContents(i, newjet);
		                        return false;
		                    }
		                }
		            }
		        }
		    }
		}
		return false;
	}
	
	@Override
	public String getTextureFile()
	{
		return PowerConverterCore.itemTexture;
	}

}
