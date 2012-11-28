package atomicstryker.minefactoryreloaded.common.blocks;

import java.util.List;

import atomicstryker.minefactoryreloaded.client.MineFactoryClient;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.core.Util;


import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockRail;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class BlockRailPassengerPickup extends BlockRail
{
	public BlockRailPassengerPickup(int blockId, int textureIndex)
	{
		super(blockId, textureIndex, true);
		setBlockName("passengerPickupRail");
		setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if(world.isRemote || !(entity instanceof EntityMinecart))
		{
			return;
		}
		EntityMinecart minecart = (EntityMinecart)entity;
		if(minecart.minecartType != 0 || minecart.riddenByEntity != null)
		{
			return;
		}
		
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
				x - Util.getInt(MineFactoryReloadedCore.passengerRailSearchMaxHorizontal),
				y - Util.getInt(MineFactoryReloadedCore.passengerRailSearchMaxVertical),
				z - Util.getInt(MineFactoryReloadedCore.passengerRailSearchMaxHorizontal),
				x + Util.getInt(MineFactoryReloadedCore.passengerRailSearchMaxHorizontal) + 1,
				y + Util.getInt(MineFactoryReloadedCore.passengerRailSearchMaxVertical) + 1,
				z + Util.getInt(MineFactoryReloadedCore.passengerRailSearchMaxHorizontal) + 1);
		
		@SuppressWarnings("rawtypes")
		List entities = world.getEntitiesWithinAABB(EntityPlayer.class, bb);
		
		for(Object o : entities)
		{
			if(!(o instanceof EntityPlayer))
			{
				continue;
			}
			((EntityPlayer)o).mountEntity(minecart);
			return;
		}
	}

	@Override
	public String getTextureFile()
	{
        return MineFactoryReloadedCore.terrainTexture;
	}
}
