package atomicstryker.ruins.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class RuinGenerator {
	private RuinHandler ruinsHandler;
	private RuinStats stats;
	private LinkedList<RuinBoundingBox> lastTen;
	private int NumTries = 0, LastNumTries = 0;
	private final int WORLD_MAX_HEIGHT = 256;

    public RuinGenerator( RuinHandler rh ) {
		ruinsHandler = rh;
		stats = new RuinStats();
		new LinkedList<RuinIBuildable>();
		lastTen = new LinkedList<RuinBoundingBox>();
    }

    public boolean generateNormal( World world, Random random, int xBase, int j, int zBase ) {
		// here we're generating 5 chunks behind normal.
		// This should hopefully solve the "wanderer" problem with edge chunks.

    	for( int c = 0; c < ruinsHandler.triesPerChunkNormal; c++ ) {
    		if( random.nextInt( 100 ) < ruinsHandler.chanceToSpawnNormal ) {
    			// ditch the y coord, we'll be coming down from the top, checking
    			// for a suitable square to start.
    			//					int xMod = ( random.nextInt( 2 ) == 1 ? random.nextInt( 16 ) : 0 - random.nextInt( 16 ) );
    			//					int zMod = ( random.nextInt( 2 ) == 1 ? random.nextInt( 16 ) : 0 - random.nextInt( 16 ) );
    			int xMod = random.nextInt( 16 );
    			int zMod = random.nextInt( 16 );
    			int x = xBase + xMod;
    			int z = zBase + zMod;
    			createBuilding( world, random, x, z, 0, false );
    		}
    	}
        return true;
    }

    public boolean generateNether( World world, Random random, int xBase, int j, int zBase ) {
        for( int c = 0; c < ruinsHandler.triesPerChunkNether; c++ ) {
            if( random.nextInt( 100 ) < ruinsHandler.chanceToSpawnNether ) {
                // ditch the y coord, we'll be coming down from the top, checking
                // for a suitable square to start.
				int xMod = ( random.nextInt( 2 ) == 1 ? random.nextInt( 16 ) : 0 - random.nextInt( 16 ) );
				int zMod = ( random.nextInt( 2 ) == 1 ? random.nextInt( 16 ) : 0 - random.nextInt( 16 ) );
				int x = xBase + xMod;
				int z = zBase + zMod;
				createBuilding( world, random, x, z, 0, true );
            }
        }
        return true;
    }

    private void createBuilding( World world, Random random, int x, int z, int minDistance, boolean Nether ) {
		int rotate = random.nextInt( 4 );
		BiomeGenBase biome = world.getBiomeGenForCoordsBody(x, z);
		int biomeID = biome.biomeID;
        int nextMinDistance = 0;
        
		if( ruinsHandler.useGeneric( random, biomeID ) )
		{
			biomeID = RuinsMod.BIOME_NONE;
		}
		stats.biomes[biomeID]++;
		
		RuinIBuildable ruinTemplate = ruinsHandler.getTemplate( random, biomeID );
		if( ruinTemplate == null )
		{
			return;
		}

		NumTries++;

        if( minDistance != 0 ) {
			stats.SiteTries++;
            // tweak the x and z from the Min Distance, minding the bounding box
            minDistance += random.nextInt( 3 ) + ruinTemplate.getMinDistance();
            x += ( random.nextInt( 2 ) == 1 ? 0 - minDistance : minDistance );
            z += ( random.nextInt( 2 ) == 1 ? 0 - minDistance : minDistance );
        }

		if( willOverlap( ruinTemplate, x, z, rotate ) ) {
			// try again.
			int xTemp = getRandomAdjustment( random, x, minDistance );
			int zTemp = getRandomAdjustment( random, z, minDistance );
			if( willOverlap( ruinTemplate, xTemp, zTemp, rotate ) ) {
				// last chance
				xTemp = getRandomAdjustment( random, x, minDistance );
				zTemp = getRandomAdjustment( random, z, minDistance );
				if( willOverlap( ruinTemplate, xTemp, zTemp, rotate ) ) {
					stats.BoundingBoxFails++;
					return;
				}
				x = xTemp;
				z = zTemp;
			} else {
				x = xTemp;
				z = zTemp;
			}
		}

        int y = findSuitableY( world, ruinTemplate, x, z, Nether );
        if( y > 0 ) {
//            if( r.checkArea( world, x, y, z, rotate, stats ) ) {
            if( ruinTemplate.checkArea( world, x, y, z, rotate ) ) {
            	if (!ruinsHandler.disableLogging) {
    				if( minDistance != 0 ) {
    				    System.out.printf("Creating ruin %s of Biome %s as part of a site at [%d|%d|%d]\n", ruinTemplate.getName(), biome.biomeName, x, y, z);
    				} else {
    				    System.out.printf("Creating ruin %s of Biome %s at [%d|%d|%d]\n", ruinTemplate.getName(), biome.biomeName, x, y, z);
    				}
            	}
				stats.NumCreated++;

				ruinTemplate.doBuild( world, random, x, y, z, rotate );
				manageLastTen( ruinTemplate, x, z, rotate );
				nextMinDistance = ruinTemplate.getMinDistance();
				if( ruinTemplate.isUnique() ) {
					ruinsHandler.removeTemplate( ruinTemplate, biomeID );
					try {
						ruinsHandler.writeExclusions( ruinsHandler.saveFolder );
					} catch( Exception e ) {
						System.err.println( "Could not write exclusions for world: " + ruinsHandler.saveFolder );
						e.printStackTrace();
					}
				}
            } else {
                return;
            }
			if( Nether ) {
				if( random.nextInt( 100 ) < ruinsHandler.chanceForSiteNether ) {
					createBuilding( world, random, x, z, nextMinDistance, true );
				}
			} else {
				if( random.nextInt( 100 ) < ruinsHandler.chanceForSiteNormal ) {
					createBuilding( world, random, x, z, nextMinDistance, false );
				}
			}
        }
		if( NumTries > ( LastNumTries + 1000 ) ) {
			LastNumTries = NumTries;
			printStats();
		}
    }

	private void printStats() {
		if (!ruinsHandler.disableLogging) {
			int total = stats.NumCreated + stats.BadBlockFails + stats.LevelingFails + stats.CutInFails + stats.OverhangFails + stats.NoAirAboveFails + stats.BoundingBoxFails;
			System.out.println( "Current Stats:" );
			System.out.println( "    Total Tries:                 " + total );
			System.out.println( "    Number Created:              " + stats.NumCreated );
			System.out.println( "    Site Tries:                  " + stats.SiteTries );
			System.out.println( "    Within Another Bounding Box: " + stats.BoundingBoxFails );
			System.out.println( "    Bad Blocks:                  " + stats.BadBlockFails );
			System.out.println( "    No Leveling:                 " + stats.LevelingFails );
			System.out.println( "    No Cut-In:                   " + stats.CutInFails );
			
			for (int i = 0; i < stats.biomes.length; i++)
			{
				if (stats.biomes[i] != 0)
				{
					if (i != RuinsMod.BIOME_NONE)
						System.out.println(BiomeGenBase.biomeList[i].biomeName+": "+stats.biomes[i]+" Biome building attempts");
					else
						System.out.println("Any-Biome: "+stats.biomes[i]+" building attempts");
				}
			}
			
			System.out.println();
		}
	}
	
	private int getRandomAdjustment( Random random, int base, int minDistance ) {
		return random.nextInt(8) - random.nextInt(8) + ( random.nextInt( 2 ) == 1 ? 0 - minDistance : minDistance );
	}

	private void manageLastTen( RuinIBuildable r, int x, int z, int rotate ) {
		lastTen.add( r.getBoundingBox( x, z, rotate ) );
		if( lastTen.size() > 10 ) {
			lastTen.remove();
		}
	}

	private boolean willOverlap( RuinIBuildable r, int x, int z, int rotate ) {
		RuinBoundingBox current = r.getBoundingBox( x, z, rotate );
		Iterator<RuinBoundingBox> i = lastTen.descendingIterator();
		while( i.hasNext() ) {
			if( i.next().collides( current ) ) { return true; }
		}
		return false;
	}

    private int findSuitableY( World world, RuinIBuildable r, int x, int z, boolean Nether ) {
		if( Nether ) {
			// The Nether has an entirely different topography so we'll use two methods
			// in a semi-random fashion (since we're not getting the random here)
			if( ( x % 2 == 1 ) ^ ( z % 2 == 1 ) ) {
				// from the top.  Find the first air block from the ceiling
				for( int y = WORLD_MAX_HEIGHT-1; y > -1; y-- ) {
					if( world.getBlockId( x, y, z ) == 0 ) {
						// now find the first non-air block from here
						for( ; y > -1; y-- ) {
							if( ! r.isAir( world.getBlockId( x, y, z ) ) ) {
								if( r.isAcceptable( world, x, y, z ) ) { return y; }
							}
						}
					}
				}
			} else {
				// from the bottom.  find the first air block from the floor
				for( int y = 0; y < WORLD_MAX_HEIGHT; y++ ) {
					if( ! r.isAir( world.getBlockId( x, y, z ) ) ) {
						if( r.isAcceptable( world, x, y - 1, z ) ) { return y - 1; }
					}
				}
			}
			return -1;
		} else {
			for( int y = WORLD_MAX_HEIGHT-1; y > -1; y-- ) {
				if( r.isAcceptable( world, x, y, z ) ) { return y; }
				if( ! r.isAir( world.getBlockId( x, y, z ) ) ) { return -1; }
			}
		}
        return -1;
    }
}