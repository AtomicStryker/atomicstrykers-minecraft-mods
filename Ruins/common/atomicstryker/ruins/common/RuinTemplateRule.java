package atomicstryker.ruins.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;

public class RuinTemplateRule {
    private int[] blockIDs, blockMDs;
	private String[] blockStrings;
    private int chance = 100, condition = 0;
	private RuinIBuildable owner;

    public RuinTemplateRule( RuinIBuildable r, String rule ) throws Exception {
		owner = r;
        String[] items = rule.split( "," );
        int numblocks = items.length - 2;
        if( numblocks < 1 ) { throw new Exception( "No blockIDs specified for rule!" ); }
        condition = Integer.parseInt( items[0] );
        chance = Integer.parseInt( items[1] );
        blockIDs = new int[numblocks];
        blockMDs = new int[numblocks];
		blockStrings = new String[numblocks];
		String[] data;
        for (int i = 0; i < numblocks; i++)
        {
            data = items[i + 2].split("-");
            if (data.length > 1) // has '-' in it, like "50-5" or "planks-3"
            {
                if (isNumber(data[0])) // 50-5
                {
                    blockIDs[i] = Integer.parseInt(data[0]);
                    blockMDs[i] = Integer.parseInt(data[1]);
                    blockStrings[i] = "";
                }
                else // planks-3
                {
                    blockIDs[i] = tryFindingBlockOfName(data[0]);
                    blockMDs[i] = Integer.parseInt(data[1]);
                    blockStrings[i] = items[i + 2];
                }
            }
            else if (!isNumber(data[0])) // MobSpawners and the like
            {
                blockIDs[i] = tryFindingBlockOfName(data[0]);
                blockMDs[i] = 0;
                blockStrings[i] = items[i + 2];
            }
            else // does not have metadata specified, aka "50"
            {
                blockIDs[i] = Integer.parseInt(items[i + 2]);
                blockMDs[i] = 0;
                blockStrings[i] = "";
            }
        }
    }
    
    private int tryFindingBlockOfName(String blockName)
    {
        for (Block b : Block.blocksList)
        {
            if (b != null && b.getBlockName() != null && b.getBlockName().equals(blockName))
            {
                return b.blockID;
            }
        }
        
        return -1;
    }

    private boolean isNumber(String s)
    {
        if (s == null || s.equals(""))
        {
            return false;
        }
        try
        {
            int n = Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public void doBlock( World world, Random random, int x, int y, int z, int rotate ) {
        // check to see if we can create this block
        if( random.nextInt( 100 ) < chance ) {
            // we're cleared, pass it off to the correct conditional.
            switch( condition ) {
            case 0:
                doNormalBlock( world, random, x, y, z, rotate );
                break;
            case 1:
                doAboveBlock( world, random, x, y, z, rotate );
                break;
            case 2:
                doAdjacentBlock( world, random, x, y, z, rotate );
                break;
			case 3:
				doUnderBlock( world, random, x, y, z, rotate );
            }
        }
    }

	public boolean runLater() {
		if( condition == 2 ) { return true; }
		return false;
	}

	public boolean runLast() {
		if( condition == 3 ) { return true; }
		return false;
	}

	public boolean canReplace( int blockID, int targetBlock ) {
		if( owner.preserveBlock( targetBlock ) && blockID == 0 ) { return false; }
		return true;
	}

    private void doNormalBlock( World world, Random random, int x, int y, int z, int rotate ) {
        int blocknum = getBlockNum( random );
        handleBlockSpawning( world, random, x, y, z, blocknum, rotate, blockStrings[blocknum] );
    }

    private void doAboveBlock( World world, Random random, int x, int y, int z, int rotate ) {
        if( owner.isAir( world.getBlockId( x, y - 1, z ) ) ) { return; }
        int blocknum = getBlockNum( random );
        handleBlockSpawning( world, random, x, y, z, blocknum, rotate, blockStrings[blocknum] );
    }

    private void doAdjacentBlock( World world, Random random, int x, int y, int z, int rotate ) {
        if( ( owner.isAir( world.getBlockId( x + 1, y, z ) ) ) &&
            ( owner.isAir( world.getBlockId( x, y, z + 1 ) ) ) &&
            ( owner.isAir( world.getBlockId( x, y, z - 1 ) ) ) &&
            ( owner.isAir( world.getBlockId( x - 1, y, z ) ) ) ) { return; }
        int blocknum = getBlockNum( random );
        handleBlockSpawning( world, random, x, y, z, blocknum, rotate, blockStrings[blocknum] );
    }

    private void doUnderBlock( World world, Random random, int x, int y, int z, int rotate ) {
        if( owner.isAir( world.getBlockId( x, y + 1, z ) ) ) { return; }
		int blocknum = getBlockNum( random );
		handleBlockSpawning( world, random, x, y, z, blocknum, rotate, blockStrings[blocknum] );
    }
    
	private void handleBlockSpawning(World world, Random random, int x, int y, int z, int blocknum, int rotate, String blockString)
    {
	    int blockID = blockIDs[blocknum];
        if( blockID == -1 )
        {
            doSpecialBlock( world, random, x, y, z, blockString );
        }
        else
        {
            placeBlock( world, blocknum, x, y, z, rotate );
        }
    }

    private void placeBlock( World world, int blocknum, int x, int y, int z, int rotate ) {
		if( canReplace( blockIDs[blocknum], world.getBlockId( x, y, z ) ) ) {
			if( rotate != RuinsMod.DIR_NORTH ) {
				int metadata = rotateMetadata( blockIDs[blocknum], blockMDs[blocknum], rotate );
				world.setBlock( x, y, z, blockIDs[blocknum] );
				world.setBlockMetadata( x, y, z, metadata );
			} else {
				world.setBlock( x, y, z, blockIDs[blocknum] );
				world.setBlockMetadata( x, y, z, blockMDs[blocknum] );
			}
		}
	}

    public void doSpecialBlock( World world, Random random, int x, int y, int z, String dataString )
    {
        
        if (dataString.equals("preserveBlock"))
        {
            // NOOP
        }
        else if (dataString.startsWith("MobSpawner:"))
        {
            addCustomSpawner( world, x, y, z, dataString.split(":")[1] );
        }
        else if (dataString.equals("UprightMobSpawn"))
        {
            addUprightMobSpawn( world, random, x, y, z );
        }
        else if (dataString.equals("EasyMobSpawn"))
        {
            addEasyMobSpawn( world, random, x, y, z );
        }
        else if (dataString.equals("MediumMobSpawn"))
        {
            addMediumMobSpawn( world, random, x, y, z );
        }
        else if (dataString.equals("HardMobSpawn"))
        {
            addHardMobSpawn( world, random, x, y, z );
        }
        else if (dataString.equals("EasyChest"))
        {
            addEasyChest( world, random, x, y, z, random.nextInt( 3 ) + 3 );
        }
        else if (dataString.equals("MediumChest"))
        {
            addMediumChest( world, random, x, y, z, random.nextInt( 4 ) + 3 );
        }
        else if (dataString.equals("HardChest"))
        {
            addHardChest( world, random, x, y, z, random.nextInt( 5 ) + 3 );
        }
        else
        {
            System.err.println("Ruins Mod could not determine what to spawn for ["+dataString+"] in Ruin template: "+owner.getName());
        }
    }

    private int getBlockNum( Random random ) {
		return random.nextInt( blockIDs.length );
    }
    
    private static void addCustomSpawner( World world, int x, int y, int z, String id )
    {
        world.setBlockWithNotify( x, y, z, Block.mobSpawner.blockID );
        TileEntityMobSpawner mobspawner = (TileEntityMobSpawner) world.getBlockTileEntity( x, y, z );
        if (mobspawner != null)
        {
            mobspawner.setMobID( id );
        }
    }

    private static void addEasyMobSpawn( World world, Random random, int x, int y, int z ) {
        switch( random.nextInt( 2 ) ) {
        case 0:
            addCustomSpawner( world, x, y, z, "Skeleton" );
            break;
        default:
            addCustomSpawner( world, x, y, z, "Zombie" );
            break;
        }
    }

    private static void addMediumMobSpawn( World world, Random random, int x, int y, int z ) {
        switch( random.nextInt( 4 ) ) {
        case 0:
            addCustomSpawner( world, x, y, z, "Spider" );
            break;
        case 1:
            addCustomSpawner( world, x, y, z, "Skeleton" );
            break;
        case 2:
            addCustomSpawner( world, x, y, z, "CaveSpider" );
            break;
        default:
            addCustomSpawner( world, x, y, z, "Zombie" );
            break;
        }
    }

    private static void addHardMobSpawn( World world, Random random, int x, int y, int z ) {
        switch( random.nextInt( 4 ) ) {
        case 0:
            addCustomSpawner( world, x, y, z, "Creeper" );
            break;
        case 1:
            addCustomSpawner( world, x, y, z, "CaveSpider" );
            break;
        case 2:
            addCustomSpawner( world, x, y, z, "Skeleton" );
            break;
        default:
            addCustomSpawner( world, x, y, z, "Blaze" );
            break;
        }
    }

    private static void addUprightMobSpawn( World world, Random random, int x, int y, int z ) {
        switch( random.nextInt( 3 ) ) {
        case 0:
            addCustomSpawner( world, x, y, z, "Creeper" );
            break;
        case 1:
            addCustomSpawner( world, x, y, z, "Skeleton" );
            break;
        default:
            addCustomSpawner( world, x, y, z, "Zombie" );
            break;
        }
    }

    private static void addEasyChest( World world, Random random, int x, int y, int z, int items ) {
        world.setBlockWithNotify( x, y, z, Block.chest.blockID );
        TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity( x, y, z );
        if (chest != null)
        {
            ItemStack stack = null;
            for( int i = 0; i < items; i++ ) {
                stack = getNormalStack( random );
                if( stack != null ) {
                    chest.setInventorySlotContents( random.nextInt( chest.getSizeInventory() ), stack );
                }
            }
        }
    }

    private static void addMediumChest( World world, Random random, int x, int y, int z, int items ) {
        world.setBlockWithNotify( x, y, z, Block.chest.blockID );
        TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity( x, y, z );
        if (chest != null)
        {
            ItemStack stack = null;
            for( int i = 0; i < items; i++ ) {
                if( random.nextInt( 20 ) < 19 ) {
                    stack = getNormalStack( random );
                } else {
                    stack = getLootStack( random );
                }
                if( stack != null ) {
                    chest.setInventorySlotContents( random.nextInt( chest.getSizeInventory() ), stack );
                }
            }
        }
    }

    private static void addHardChest( World world, Random random, int x, int y, int z, int items ) {
        world.setBlockWithNotify( x, y, z, Block.chest.blockID );
        TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity( x, y, z );
        if (chest != null)
        {
            ItemStack stack = null;
            for( int i = 0; i < items; i++ ) {
                if( random.nextInt( 10 ) < 9 ) {
                    stack = getNormalStack( random );
                } else {
                    stack = getLootStack( random );
                }
                if( stack != null ) {
                    chest.setInventorySlotContents( random.nextInt( chest.getSizeInventory() ), stack );
                }
            }
        }
    }

    private static ItemStack getNormalStack( Random random ) {
        int rand = random.nextInt( 25 );
        switch( rand ) {
        case 0: case 1:
            return null;
        case 2: case 3:
            return new ItemStack( Item.bread );
        case 4: case 5:
            return new ItemStack( Item.wheat, random.nextInt( 8 ) + 8 );
        case 6:
            return new ItemStack( Item.hoeSteel );
        case 7:
            return new ItemStack( Item.shovelSteel );
        case 8: case 9:
            return new ItemStack( Item.silk, random.nextInt( 3 ) + 1 );
        case 10: case 11: case 12:
            return new ItemStack( Item.seeds, random.nextInt( 8 ) + 8 );
        case 13: case 14: case 15:
            return new ItemStack( Item.bowlEmpty, random.nextInt( 2 ) + 1 );
        case 16:
            return new ItemStack( Item.bucketEmpty );
        case 17:
            return new ItemStack( Item.appleRed );
        case 18: case 19:
            return new ItemStack( Item.bone, random.nextInt( 4 ) + 1 );
        case 20: case 21:
            return new ItemStack( Item.egg, random.nextInt( 2 ) + 1 );
        case 22:
            return new ItemStack( Item.coal, random.nextInt( 5 ) + 3 );
        case 23:
            return new ItemStack( Item.ingotIron, random.nextInt( 5 ) + 3 );
        default:
            return getLootStack( random );
        }
    }

    private static ItemStack getLootStack( Random random ) {
        int rand = random.nextInt( 25 );
        switch( rand ) {
        case 0: case 1: case 2: case 3:
            return null;
        case 4: case 5:
            return new ItemStack( Item.bootsLeather );
        case 6: case 7:
            return new ItemStack( Item.legsLeather );
        case 8: case 9:
            return new ItemStack( Item.flintAndSteel );
        case 10: case 11:
            return new ItemStack( Item.axeSteel );
        case 12:
            return new ItemStack( Item.swordSteel );
        case 13:
            return new ItemStack( Item.pickaxeSteel );
        case 14: case 15:
            return new ItemStack( Item.helmetSteel );
        case 16:
            return new ItemStack( Item.plateChain );
        case 17: case 18:
            return new ItemStack( Item.book, random.nextInt( 3 ) + 1 );
        case 19:
            return new ItemStack( Item.compass );
        case 20:
            return new ItemStack( Item.pocketSundial );
        case 21:
            return new ItemStack( Item.redstone, random.nextInt( 12 ) + 12 );
        case 22:
            return new ItemStack( Item.appleGold );
        case 23:
            return new ItemStack( Item.bowlSoup, random.nextInt( 2 ) + 1 );
        default:
            return new ItemStack( Item.diamond, random.nextInt( 4 ) );
        }
    }

	private static int rotateMetadata( int blockID, int metadata, int dir ) {
		// remember that, in this mod, NORTH is the default direction.
		// this method is unused if the direction is NORTH
		int tempdata = 0;
		switch( blockID ) {
		case 27: case 28: case 66:
			// minecart tracks
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				// flat tracks
				if( metadata == 0 ) { return 1; }
				if( metadata == 1 ) { return 0; }
				// ascending tracks
				if( metadata == 2 ) { return 5; }
				if( metadata == 3 ) { return 4; }
				if( metadata == 4 ) { return 2; }
				if( metadata == 5 ) { return 3; }
				// curves
				if( metadata == 6 ) { return 7; }
				if( metadata == 7 ) { return 8; }
				if( metadata == 8 ) { return 9; }
				if( metadata == 9 ) { return 6; }
			case RuinsMod.DIR_SOUTH:
				// flat tracks
				if( metadata == 0 ) { return 0; }
				if( metadata == 1 ) { return 1; }
				// ascending tracks
				if( metadata == 2 ) { return 3; }
				if( metadata == 3 ) { return 2; }
				if( metadata == 4 ) { return 5; }
				if( metadata == 5 ) { return 4; }
				// curves
				if( metadata == 6 ) { return 8; }
				if( metadata == 7 ) { return 9; }
				if( metadata == 8 ) { return 6; }
				if( metadata == 9 ) { return 7; }
			case RuinsMod.DIR_WEST:
				// flat tracks
				if( metadata == 0 ) { return 1; }
				if( metadata == 1 ) { return 0; }
				// ascending tracks
				if( metadata == 2 ) { return 4; }
				if( metadata == 3 ) { return 5; }
				if( metadata == 4 ) { return 3; }
				if( metadata == 5 ) { return 2; }
				// curves
				if( metadata == 6 ) { return 9; }
				if( metadata == 7 ) { return 6; }
				if( metadata == 8 ) { return 7; }
				if( metadata == 9 ) { return 8; }
			}
			break;
		case 64: case 71: case 96:
			// doors
			if( metadata - 8 >= 0 ) {
				// the top half of the door
				tempdata += 8;
				metadata -= 8;
			}
			if( metadata - 4 >= 0 ) {
				// the door has swung counterclockwise around its hinge
				tempdata += 4;
				metadata -= 4;
			}
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 0 ) { return 1 + tempdata; }
				if( metadata == 1 ) { return 2 + tempdata; }
				if( metadata == 2 ) { return 3 + tempdata; }
				if( metadata == 3 ) { return 0 + tempdata; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 0 ) { return 2 + tempdata; }
				if( metadata == 1 ) { return 3 + tempdata; }
				if( metadata == 2 ) { return 0 + tempdata; }
				if( metadata == 3 ) { return 1 + tempdata; }
			case RuinsMod.DIR_WEST:
				if( metadata == 0 ) { return 3 + tempdata; }
				if( metadata == 1 ) { return 0 + tempdata; }
				if( metadata == 2 ) { return 1 + tempdata; }
				if( metadata == 3 ) { return 2 + tempdata; }
			}
			break;
		case 50: case 69: case 75: case 76: case 77:
			// torches, button, lever
			// check to see if this is a switch or a button and is flagged as thrown
			tempdata = 0;
			if( blockID == 69 || blockID == 77 ) {
				if( metadata - 8 > 0 ) {
					tempdata += 8;
					metadata -= 8;
				}
				// now see if it's a floor switch
				if( blockID == 69 && ( metadata == 5 || metadata == 6 ) ) {
					// we'll leave this as-is
					return metadata + tempdata;
				}
			} else {
				// torches on the floor.
				if( metadata == 5 ) {
					return metadata;
				}
			}
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 1 ) { return 3 + tempdata; }
				if( metadata == 2 ) { return 4 + tempdata; }
				if( metadata == 3 ) { return 2 + tempdata; }
				if( metadata == 4 ) { return 1 + tempdata; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 1 ) { return 2 + tempdata; }
				if( metadata == 2 ) { return 1 + tempdata; }
				if( metadata == 3 ) { return 4 + tempdata; }
				if( metadata == 4 ) { return 3 + tempdata; }
			case RuinsMod.DIR_WEST:
				if( metadata == 1 ) { return 4 + tempdata; }
				if( metadata == 2 ) { return 3 + tempdata; }
				if( metadata == 3 ) { return 1 + tempdata; }
				if( metadata == 4 ) { return 2 + tempdata; }
			}
			break;
		case 53: case 67: case 108: case 109: case 112:
		{
			// compact stairs
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 0 ) { return 2; }
				if( metadata == 1 ) { return 3; }
				if( metadata == 2 ) { return 1; }
				if( metadata == 3 ) { return 0; }
				if( metadata == 4 ) { return 6; }
				if( metadata == 5 ) { return 7; }
				if( metadata == 6 ) { return 5; }
				if( metadata == 7 ) { return 4; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 0 ) { return 1; }
				if( metadata == 1 ) { return 0; }
				if( metadata == 2 ) { return 3; }
				if( metadata == 3 ) { return 2; }
				if( metadata == 4 ) { return 5; }
				if( metadata == 5 ) { return 4; }
				if( metadata == 6 ) { return 7; }
				if( metadata == 7 ) { return 6; }
			case RuinsMod.DIR_WEST:
				if( metadata == 0 ) { return 3; }
				if( metadata == 1 ) { return 2; }
				if( metadata == 2 ) { return 0; }
				if( metadata == 3 ) { return 1; }
				if( metadata == 4 ) { return 7; }
				if( metadata == 5 ) { return 6; }
				if( metadata == 6 ) { return 4; }
				if( metadata == 7 ) { return 5; }
			}
			break;
		}
		case 65: case 23: case 61: case 62: case 68:
			// Ladders, Wall Signs, Furnaces and Dispensers
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 2 ) { return 5; }
				if( metadata == 3 ) { return 4; }
				if( metadata == 4 ) { return 2; }
				if( metadata == 5 ) { return 3; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 2 ) { return 3; }
				if( metadata == 3 ) { return 2; }
				if( metadata == 4 ) { return 5; }
				if( metadata == 5 ) { return 4; }
			case RuinsMod.DIR_WEST:
				if( metadata == 2 ) { return 4; }
				if( metadata == 3 ) { return 5; }
				if( metadata == 4 ) { return 3; }
				if( metadata == 5 ) { return 2; }
			}
			break;
		case 106:
		/* meta readout
		N: 8
		E: 1
		S: 2
		W: 4
		*/
			// Vines
			switch( dir ) {
			case RuinsMod.DIR_EAST: // turn one right
				if( metadata == 8 ) { return 1; }
				if( metadata == 1 ) { return 2; }
				if( metadata == 2 ) { return 4; }
				if( metadata == 4 ) { return 8; }
			case RuinsMod.DIR_SOUTH: // run 2 right
				if( metadata == 8 ) { return 2; }
				if( metadata == 1 ) { return 4; }
				if( metadata == 2 ) { return 8; }
				if( metadata == 4 ) { return 1; }
			case RuinsMod.DIR_WEST: // turn 1 left
				if( metadata == 8 ) { return 4; }
				if( metadata == 1 ) { return 8; }
				if( metadata == 2 ) { return 1; }
				if( metadata == 4 ) { return 2; }
			}
			break;
		case 86: case 91: case 93: case 94:
			// Pumpkins and Jack-O-Lanterns, Redstone Repeaters
			if( blockID == 93 || blockID == 94 ) {
				// check for the delay tick for repeaters
				if( metadata - 4 >= 0 ) {
					if( metadata - 8 >= 0 ) {
						if( metadata - 12 >= 0 ) {
							// four tick delay
							tempdata += 12;
							metadata -= 12;
						} else {
							// three tick delay
							tempdata += 8;
							metadata -= 8;
						}
					} else {
						// two tick delay
						tempdata += 4;
						metadata -= 4;
					}
				}
			}
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 0 ) { return 1 + tempdata; }
				if( metadata == 1 ) { return 2 + tempdata; }
				if( metadata == 2 ) { return 3 + tempdata; }
				if( metadata == 3 ) { return 0 + tempdata; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 0 ) { return 2 + tempdata; }
				if( metadata == 1 ) { return 3 + tempdata; }
				if( metadata == 2 ) { return 0 + tempdata; }
				if( metadata == 3 ) { return 1 + tempdata; }
			case RuinsMod.DIR_WEST:
				if( metadata == 0 ) { return 3 + tempdata; }
				if( metadata == 1 ) { return 0 + tempdata; }
				if( metadata == 2 ) { return 1 + tempdata; }
				if( metadata == 3 ) { return 2 + tempdata; }
			}
			break;
		case 26:
			// Beds
			if( metadata - 8 >= 0 ) {
				// this is the foot of the bed block.
				tempdata += 8;
				metadata -= 8;
			}
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 0 ) { return 1 + tempdata; }
				if( metadata == 1 ) { return 2 + tempdata; }
				if( metadata == 2 ) { return 3 + tempdata; }
				if( metadata == 3 ) { return 0 + tempdata; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 0 ) { return 2 + tempdata; }
				if( metadata == 1 ) { return 3 + tempdata; }
				if( metadata == 2 ) { return 0 + tempdata; }
				if( metadata == 3 ) { return 1 + tempdata; }
			case RuinsMod.DIR_WEST:
				if( metadata == 0 ) { return 3 + tempdata; }
				if( metadata == 1 ) { return 0 + tempdata; }
				if( metadata == 2 ) { return 1 + tempdata; }
				if( metadata == 3 ) { return 2 + tempdata; }
			}
			break;
		case 63:
			// sign posts
			switch( dir ) {
			case RuinsMod.DIR_EAST:
				if( metadata == 0 ) { return 4; }
				if( metadata == 1 ) { return 5; }
				if( metadata == 2 ) { return 6; }
				if( metadata == 3 ) { return 7; }
				if( metadata == 4 ) { return 8; }
				if( metadata == 5 ) { return 9; }
				if( metadata == 6 ) { return 10; }
				if( metadata == 7 ) { return 11; }
				if( metadata == 8 ) { return 12; }
				if( metadata == 9 ) { return 13; }
				if( metadata == 10 ) { return 14; }
				if( metadata == 11 ) { return 15; }
				if( metadata == 12 ) { return 0; }
				if( metadata == 13 ) { return 1; }
				if( metadata == 14 ) { return 2; }
				if( metadata == 15 ) { return 3; }
			case RuinsMod.DIR_SOUTH:
				if( metadata == 0 ) { return 8; }
				if( metadata == 1 ) { return 9; }
				if( metadata == 2 ) { return 10; }
				if( metadata == 3 ) { return 11; }
				if( metadata == 4 ) { return 12; }
				if( metadata == 5 ) { return 13; }
				if( metadata == 6 ) { return 14; }
				if( metadata == 7 ) { return 15; }
				if( metadata == 8 ) { return 0; }
				if( metadata == 9 ) { return 1; }
				if( metadata == 10 ) { return 2; }
				if( metadata == 11 ) { return 3; }
				if( metadata == 12 ) { return 4; }
				if( metadata == 13 ) { return 5; }
				if( metadata == 14 ) { return 6; }
				if( metadata == 15 ) { return 7; }
			case RuinsMod.DIR_WEST:
				if( metadata == 0 ) { return 12; }
				if( metadata == 1 ) { return 13; }
				if( metadata == 2 ) { return 14; }
				if( metadata == 3 ) { return 15; }
				if( metadata == 4 ) { return 0; }
				if( metadata == 5 ) { return 1; }
				if( metadata == 6 ) { return 2; }
				if( metadata == 7 ) { return 3; }
				if( metadata == 8 ) { return 4; }
				if( metadata == 9 ) { return 5; }
				if( metadata == 10 ) { return 6; }
				if( metadata == 11 ) { return 7; }
				if( metadata == 12 ) { return 8; }
				if( metadata == 13 ) { return 9; }
				if( metadata == 14 ) { return 10; }
				if( metadata == 15 ) { return 11; }
			}
		}
		// we should never get here, but users can be silly sometimes.
		return metadata + tempdata;
	}
}