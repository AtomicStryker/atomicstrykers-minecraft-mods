package atomicstryker.battletowers.common;

import java.util.*;
import java.lang.*;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.TileEntityMobSpawner;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenerator;

public class AS_WorldGenTower extends WorldGenerator
{
    WorldGenHandler maker;
	
    private int floor;
	private int floorIterator;
    private boolean topFloor;
	
    private int candidates[][] = {
        {
            4, -5
        }, {
            4, 0
        }, {
            4, 5,
        }, {
            0, -5
        }, {
            0, 0
        }, {
            0, 5,
        }, {
            -4, -5
        }, {
            -4, 0,
        }, {
            -4, 5
        }
    };
	
	private int candidatecount = candidates.length;
	final int maxHoleDepthInBase = 22;
	private towerTypes towerChosen;

    public AS_WorldGenTower(WorldGenHandler creator)
    {
    	maker = creator;
    }
	
    public boolean generate(World world, Random random, int ix, int jy, int kz)
    {
		int centerblockY = jy;
		
		int countWater = 0;
		int countSand = 0;
		int countSnow = 0;
		int countFoliage = 0;
		int countElse = 0;
		
		for (int ccounter = 0; ccounter < candidatecount; ccounter++)
		{
			int pair[] = candidates[ccounter];
			int checkBlockY = GetSurfaceBlockHeight(world, ix+pair[0], kz+pair[1]);
			
			int ID = world.getBlockId(ix+pair[0], checkBlockY, kz+pair[1]);
			
			if (world.getBlockId(ix+pair[0], checkBlockY+1, kz+pair[1]) == Block.snow.blockID || ID == Block.ice.blockID)
			{
				countSnow++;
			}
			else if (ID == Block.sand.blockID || ID == Block.sandStone.blockID)
			{
				countSand++;
			}
			else if (ID == Block.waterStill.blockID)
			{
				countWater++;
			}
			else if (ID == Block.leaves.blockID || ID == Block.waterlily.blockID || ID == Block.wood.blockID)
			{
				countFoliage++;
			}
			else
				countElse++;
			
			if (Math.abs(checkBlockY - centerblockY) > maxHoleDepthInBase)
			{
				//System.err.println("Tower Gen abort: Uneven Surface, diff value: "+Math.abs(checkBlockY - centerblockY));
				return false;
			}
			
			for (int ycounter2 = 1; ycounter2 <= 3; ycounter2++)
			{
				ID = world.getBlockId(ix+pair[0], (checkBlockY+ycounter2), kz+pair[1]);
				if (IsBannedBlockID(ID))
				{
					//System.err.println("Tower Gen abort: Surface banned Block of ID: "+ID+" at height: "+ycounter2);
					return false;
				}
			}
			
			for (int ycounter = 1; ycounter <= 5; ycounter++)
			{
				ID = world.getBlockId(ix+pair[0], checkBlockY - ycounter, kz+pair[1]);
				
				if (ID == 0 || IsBannedBlockID(ID))
				{
					//System.err.println("Tower Gen abort: Depth check - Banned Block or hole, Depth: "+ycounter+" ID: "+ID);
					return false;
				}
			}
		}
		
		//System.err.println("Snow: "+countSnow+" Sand: "+countSand+" Water: "+countWater+" else: "+countElse);
		
		int[] nums = {countWater, countSnow, countSand, countFoliage, countElse};
		Arrays.sort(nums);
		int result = nums[nums.length-1];
		
		//System.err.println("Picked max value of "+result);
		
		if(countSand == result)
		{
			towerChosen = towerTypes.SandStone;
		}
		else if(countSnow == result)
		{
			towerChosen = towerTypes.Ice;
		}
		else if(countWater == result)
		{
			towerChosen = towerTypes.CobbleStoneMossy;
		}
		else if(countFoliage == result)
		{
			towerChosen = towerTypes.CobbleStoneMossy;
		}
		else // standard is cobblestone, really rare should be nether
		{
			if(random.nextInt(10) == 0)
			{
				towerChosen = towerTypes.Netherrack;
			}
			else
			{
				towerChosen = (random.nextInt(5) == 0) ? towerTypes.SmoothStone : towerTypes.CobbleStone;
			}
		}
		
		int towerWallBlockID = towerChosen.GetWallBlockID();
		int towerLightBlockID = towerChosen.GetLightBlockID();
		int towerFloorBlockID = towerChosen.GetFloorBlockID();
		
        floor = 1;
        topFloor = false;
		int builderHeight = jy - 6;
        for(; builderHeight < 120; builderHeight += 7) // builderHeight jumps floors
        {
            if(builderHeight + 7 >= 120)
            {
                topFloor = true;
            }
			
            for(floorIterator = 0; floorIterator < 7; floorIterator++) // build each floor height block till next floor
            {
                if(floor == 1 && floorIterator < 4) // initial floor
                {
                    floorIterator = 4;
                }
                for(int xIterator = -7; xIterator < 7; xIterator++) // do each X
                {
                    for(int zIterator = -7; zIterator < 7; zIterator++) // do each Z
                    {
                        int iCurrent = xIterator + ix;
                        int jCurrent = floorIterator + builderHeight;
                        int zCurrent = zIterator + kz;
						
                        if(zIterator == -7) // last row, 14
                        {
                            if(xIterator > -5 && xIterator < 4) // rear outer wall
                            {
                                BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            }
                            continue;
                        }
                        if(zIterator == -6 || zIterator == -5) // rows 12 and 13
                        {
                            if(xIterator == -5 || xIterator == 4) // outer wall parts
                            {
                                BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                                continue;
                            }
                            if(zIterator == -6) // row 13 extra
                            {
                                if(xIterator == (floorIterator + 1) % 7 - 3) // stairwell!!
                                {
                                    world.setBlock(iCurrent, jCurrent, zCurrent, towerChosen.GetStairBlockID());
                                    if(floorIterator == 5)
                                    {
                                        world.setBlock(iCurrent - 7, jCurrent, zCurrent, towerFloorBlockID);
                                    }
                                    if(floorIterator == 6 && topFloor) // top ledge part
                                    {
                                        BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                                    }
                                    continue;
                                }
                                if(xIterator < 4 && xIterator > -5) // tower insides
                                {
                                    world.setBlock(iCurrent, jCurrent, zCurrent, 0);
                                }
                                continue;
                            }
                            if(zIterator != -5 || xIterator <= -5 || xIterator >= 5) // outside tower
                            {
                                continue;
                            }
                            if(floorIterator != 0 && floorIterator != 6 || xIterator != -4 && xIterator != 3)
                            {
                                if(floorIterator == 5 && (xIterator == 3 || xIterator == -4))
                                {
                                    BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                                }
								else
                                {
                                    BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID); // under stairwell
                                }
                            }
							else
                            {
                                world.setBlock(iCurrent, jCurrent, zCurrent, 0); // stairwell space
                            }
                            continue;
                        }
                        if(zIterator == -4 || zIterator == -3 || zIterator == 2 || zIterator == 3) // rows 11, 10, 5, 4
                        {
                            if(xIterator == -6 || xIterator == 5) // outer wall parts
                            {
                                BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                                continue;
                            }
                            if(xIterator <= -6 || xIterator >= 5) // outside tower
                            {
                                continue;
                            }
                            if(floorIterator == 5)
                            {
                                BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                                continue;
                            }
                            if(world.getBlockId(iCurrent, jCurrent, zCurrent) != Block.chest.blockID) // tower inside space
                            {
                                world.setBlock(iCurrent, jCurrent, zCurrent, 0);
                            }
                            continue;
                        }
                        if(zIterator > -3 && zIterator < 2) // rows 10 to 5 
                        {
                            if(xIterator == -7 || xIterator == 6)
                            {
                                if(floorIterator < 0 || floorIterator > 3 || xIterator != -7 && xIterator != 6 || zIterator != -1 && zIterator != 0) // wall, short of window
                                {
                                    BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                                }
								else
                                {
                                    world.setBlock(iCurrent, jCurrent, zCurrent, 0);
                                }
                                continue;
                            }
                            if(xIterator <= -7 || xIterator >= 6)
                            {
                                continue;
                            }
                            if(floorIterator == 5)
                            {
                                BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                            }
							else
                            {
                                world.setBlock(iCurrent, jCurrent, zCurrent, 0);
                            }
                            continue;
                        }
                        if(zIterator == 4) // row 3
                        {
                            if(xIterator == -5 || xIterator == 4)
                            {
                                BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                                continue;
                            }
                            if(xIterator <= -5 || xIterator >= 4)
                            {
                                continue;
                            }
                            if(floorIterator == 5)
                            {
                                BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                            }
							else
                            {
                                world.setBlock(iCurrent, jCurrent, zCurrent, 0);
                            }
                            continue;
                        }
                        if(zIterator == 5) // row 2
                        {
                            if(xIterator == -4 || xIterator == -3 || xIterator == 2 || xIterator == 3)
                            {
                                BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                                continue;
                            }
                            if(xIterator <= -3 || xIterator >= 2)
                            {
                                continue;
                            }
                            if(floorIterator == 5)
                            {
                                BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                            }
							else
                            {
                                BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            }
                            continue;
                        }
                        if(zIterator != 6 || xIterator <= -3 || xIterator >= 2)
                        {
                            continue;
                        }
                        if(floorIterator < 0 || floorIterator > 3 || xIterator != -1 && xIterator != 0)
                        {
                            BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                        }
						else
                        {
                            BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                        }
                    }

                }
            }

            if(floor == 2)
            {
                world.setBlock(ix + 3, builderHeight, kz - 5, towerWallBlockID);
                world.setBlock(ix + 3, builderHeight - 1, kz - 5, towerWallBlockID);
            }
            if(topFloor)
            {
                double d = ix;
                double d1 = builderHeight + 6;
                double d2 = (double)kz + 0.5D;
                AS_EntityGolem entitygolem = new AS_EntityGolem(world, towerChosen.ordinal());
                entitygolem.setLocationAndAngles(d, d1, d2, world.rand.nextFloat() * 360F, 0.0F);
                entitygolem.setPosition(d, d1, d2);
                world.spawnEntityInWorld(entitygolem);
            }
			else
            {
                world.setBlockWithNotify(ix + 2, builderHeight + 6, kz + 2, Block.mobSpawner.blockID);
                TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world.getBlockTileEntity(ix + 2, builderHeight + 6, kz + 2);
                tileentitymobspawner.setMobID(getMobType(random));
                world.setBlockWithNotify(ix - 3, builderHeight + 6, kz + 2, Block.mobSpawner.blockID);
                TileEntityMobSpawner tileentitymobspawner1 = (TileEntityMobSpawner)world.getBlockTileEntity(ix - 3, builderHeight + 6, kz + 2);
                tileentitymobspawner1.setMobID(getMobType(random));
            }
            // chest petal
            world.setBlock(ix, builderHeight + 6, kz + 3, towerFloorBlockID);
            world.setBlock(ix - 1, builderHeight + 6, kz + 3, towerFloorBlockID);
            
            if(builderHeight + 56 >= 120 && floor == 1)
            {
                floor = 2;
            }
            // chest
            TowerStageItemManager floorChestManager = maker.getTowerStageManagerForFloor(floor, random);
            
            for(int chestlength = 0; chestlength < 2; chestlength++)
            {
                world.setBlockAndMetadata(ix - chestlength, builderHeight + 7, kz + 3, Block.chest.blockID, 2);
                TileEntityChest tileentitychest = new TileEntityChest();
                world.setBlockTileEntity(ix - chestlength, builderHeight + 7, kz + 3, tileentitychest);
                for(int j5 = 0; j5 < 3; j5++)
                {
                    ItemStack itemstack = floorChestManager.getStageItem(random);
                    if(itemstack != null)
                    {
                        tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), itemstack);
                    }
                }
            }
			
			// Light Sources, move all non opaque lightsources upwards
			if (towerLightBlockID != 0 && Block.blocksList[towerLightBlockID].isOpaqueCube())
			{
				builderHeight+=2;
			}
			
            world.setBlock(ix + 3, builderHeight, kz - 6, towerLightBlockID);
            world.setBlock(ix - 4, builderHeight, kz - 6, towerLightBlockID);
            world.setBlock(ix + 1, builderHeight, kz - 4, towerLightBlockID);
            world.setBlock(ix - 2, builderHeight, kz - 4, towerLightBlockID);
			
			if (towerLightBlockID != 0 && Block.blocksList[towerLightBlockID].isOpaqueCube())
			{
				builderHeight-=2;
			}
			
            for(int l3 = 0; l3 < (floor * 4 + towerChosen.ordinal()) - 8 && !topFloor; l3++) // random hole poker
            {
                int k4 = 5 - random.nextInt(12);
                int k5 = builderHeight + 5;
                int j6 = 5 - random.nextInt(10);
                if(j6 < -2 && k4 < 4 && k4 > -5 && k4 != 1 && k4 != -2)
                {
                    continue;
                }
                k4 += ix;
                j6 += kz;
                if(world.getBlockId(k4, k5, j6) == towerFloorBlockID && world.getBlockId(k4, k5 + 1, j6) != Block.mobSpawner.blockID)
                {
                    world.setBlock(k4, k5, j6, 0);
                }
            }

            floor++;
        }

        return true;
    }
	
	private void BuildFloorPiece(World world, int i, int j, int k, int towerFloorBlockID)
	{
		world.setBlock(i, j, k, towerFloorBlockID);
		
		if (towerChosen.GetFloorBlockMetaData() != 0)
		{
			world.setBlockMetadataWithNotify(i, j, k, towerChosen.GetFloorBlockMetaData());
		}
	}
	
	private void BuildWallPiece(World world, int i, int j, int k, int towerWallBlockID)
	{
		world.setBlock(i, j, k, towerWallBlockID);
		if(floor == 1 && floorIterator == 4)
		{
			FillTowerBaseToGround(world, i, j, k, towerWallBlockID);
		}
	}
	
	private void FillTowerBaseToGround(World world, int i, int j, int k, int blocktype)
	{
		int x = j-1;
		do
		{
			world.setBlock(i, x, k, blocktype);
			x--;
		}
		while(x>0 && !IsBuildableBlockID(world.getBlockId(i, x, k)));
	}
	
	private int GetSurfaceBlockHeight(World world, int x, int z)
	{
		int h = 50;
		
		do
		{
			h++;
		}
		while (world.getBlockId(x, h, z) != 0 && !IsFoliageBlockID(world.getBlockId(x, h, z)));
		
		return h-1;
	}
	
	private boolean IsFoliageBlockID(int ID)
	{
		return (ID == Block.snow.blockID
			|| ID == Block.tallGrass.blockID
			|| ID == Block.deadBush.blockID
			|| ID == Block.wood.blockID
			|| ID == Block.leaves.blockID);
	}
	
	private boolean IsBuildableBlockID(int ID)
	{
		return (ID == Block.stone.blockID
			|| ID == Block.grass.blockID
			|| ID == Block.sand.blockID
			|| ID == Block.sandStone.blockID
			|| ID == Block.gravel.blockID
			|| ID == Block.dirt.blockID);
	}
	
	private boolean IsBannedBlockID(int ID)
	{
		return (ID == Block.plantYellow.blockID
			|| ID == Block.plantRed.blockID
			|| ID == Block.mushroomBrown.blockID
			|| ID == Block.mushroomRed.blockID
			|| ID == Block.cactus.blockID
			|| ID == Block.pumpkin.blockID
			|| ID == Block.lavaMoving.blockID			
			|| ID == Block.lavaStill.blockID);
	}
	
	public enum towerTypes
	{
		CobbleStone(Block.cobblestone.blockID, Block.torchWood.blockID, Block.stoneDoubleSlab.blockID, 0, Block.stairCompactCobblestone.blockID),
		CobbleStoneMossy(Block.cobblestoneMossy.blockID, Block.torchWood.blockID, Block.stoneDoubleSlab.blockID, 0, Block.stairCompactCobblestone.blockID),
		SandStone(Block.sandStone.blockID, Block.torchWood.blockID, Block.stoneDoubleSlab.blockID, 1, Block.stairCompactCobblestone.blockID),
		Ice(Block.ice.blockID, 0 /*Block.glowStone.blockID*/, Block.blockClay.blockID, 2, Block.stairCompactPlanks.blockID), // since when does glowstone melt ice
		SmoothStone(Block.stone.blockID, Block.torchWood.blockID, Block.stoneDoubleSlab.blockID, 3, Block.stairCompactCobblestone.blockID),
		Netherrack(Block.netherrack.blockID, Block.glowStone.blockID, Block.slowSand.blockID, 0, Block.stairCompactCobblestone.blockID),
		Jungle(Block.cobblestoneMossy.blockID, Block.web.blockID, Block.dirt.blockID, 0, Block.stairCompactPlanks.blockID);
		
		// meta data slabs: a[] = { "stone", "sand", "wood", "cobble" };
		
		private int wallBlockID;
		private int lightBlockID;
		private int floorBlockID;
		private int floorBlockMetaData;
		private int stairBlockID;
		
		towerTypes(int a, int b, int c, int d, int e)
		{
			this.wallBlockID = a;
			this.lightBlockID = b;
			this.floorBlockID = c;
			this.floorBlockMetaData = d;
			this.stairBlockID = e;
		}
		
		int GetWallBlockID()
		{
			return wallBlockID;
		}
		
		int GetLightBlockID()
		{
			return lightBlockID;
		}
		
		int GetFloorBlockID()
		{
			return floorBlockID;
		}
		
		int GetFloorBlockMetaData()
		{
			return floorBlockMetaData;
		}
		
		int GetStairBlockID()
		{
			return stairBlockID;
		}
	}

    private String getMobType(Random random)
    {
        switch (random.nextInt(4))
		{
			case 0:
			{
				if(ModLoader.isModLoaded("mod_prefixskeletons"))
				{
					return "PrefixSkeleton";
				}
				else
				{
					return "Skeleton";
				}
			}
			case 1:
			{
				if(ModLoader.isModLoaded("mod_MoreCreepsAndWeirdos"))
				{
					return "Mummy";
				}
				else
				{
					return "Zombie";
				}
			}
			case 2:
			{
				return "Zombie";
			}
			case 3:
			{
				return "Spider";
			}
			case 4:
			{
				return "";
			}
		}
		
		return "";
    }
}
