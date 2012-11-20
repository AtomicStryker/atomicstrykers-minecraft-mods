package atomicstryker.ruins.common;

import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.FileReader;


import net.minecraft.src.World;

public class RuinTemplate implements RuinIBuildable {
	private String name;
    private int[] targets;
    private int height = 0, width = 0, length = 0, overhang = 0, weight = 1, embed = 1;
	private int leveling = 0, lbuffer = 0, cutIn = 0, cbuffer = 0, w_off = 0, l_off = 0;
    private boolean preserveWater = false, preserveLava = false, preservePlants = false,
					unique = false;
    private ArrayList<RuinTemplateRule> rules = new ArrayList<RuinTemplateRule>();
    private ArrayList<RuinTemplateLayer> layers = new ArrayList<RuinTemplateLayer>();

    public RuinTemplate( String filename ) throws Exception {
        // load in the given file as a template
        try {
            ArrayList<String> lines = new ArrayList<String>();
            BufferedReader br = new BufferedReader( new FileReader( filename ) );
            String read = br.readLine();
            while( read != null ) {
                lines.add( read );
                read = br.readLine();
            }
            parseFile( lines );
			name = filename;
        } catch ( Exception e ) {
            System.err.println( "Failed loading template: " + filename );
            e.printStackTrace();
        }
    }

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}

	public int getMinDistance() {
		return ( width > length ? width : length ) + ( cbuffer > lbuffer ? cbuffer : lbuffer );
	}

	public boolean isUnique() {
		return unique;
	}

	public boolean isAir( int blockID ) {
		if( blockID == 0 ) { return true; }
		// treat snow and most plants as air too
		if( blockID == 30 ) { return true; }
		if( blockID == 31 ) { return true; }
		if( blockID == 32 ) { return true; }
		if( blockID == 37 ) { return true; }
		if( blockID == 38 ) { return true; }
		if( blockID == 39 ) { return true; }
		if( blockID == 40 ) { return true; }
		if( blockID == 59 ) { return true; }
		if( blockID == 78 ) { return true; }
		if( blockID == 106 ) { return true; }
		if( blockID == 111 ) { return true; }
		return preserveBlock( blockID );
	}

	public boolean preserveBlock( int blockID ) {
		if( preserveWater ) {
			if( blockID == 8 ) { return true; }
			if( blockID == 9 ) { return true; }
			if( blockID == 79 ) { return true; }
		}
		if( preserveLava ) {
			if( blockID == 10 ) { return true; }
			if( blockID == 11 ) { return true; }
		}
		if( preservePlants ) {
			if( blockID == 17 ) { return true; }
			if( blockID == 18 ) { return true; }
			if( blockID == 81 ) { return true; }
			if( blockID == 83 ) { return true; }
			if( blockID == 86 ) { return true; }
			if( blockID == 100 ) { return true; }
//			if( blockID == 103 ) { return true; }
			if( blockID == 99) { return true; }
			if( blockID == 104) { return true; }
			if( blockID == 105) { return true; }
			if( blockID == 106) { return true; }
			if( blockID == 111) { return true; }
		}
		return false;
	}

    public boolean isAcceptable( World world, int x, int y, int z ) {
        // checks if the square is acceptable for a ruin to be built upon
        for( int i = 0; i < targets.length; i++ ) {
            if( world.getBlockId( x, y, z ) == targets[i] ) { return true; }
        }
        return false;
    }

    public boolean checkArea( World world, int xBase, int y, int zBase, int rotate ) {
//    public boolean checkArea( World world, int xBase, int y, int zBase, int rotate, RuinStats stats ) {
        // setup some variable defaults (north/south)
		int x = xBase + w_off;
        int z = zBase + l_off;
		int xDim = width;
		int zDim = length;

		// how are we oriented?
		if( rotate == RuinsMod.DIR_EAST || rotate == RuinsMod.DIR_WEST ) {
			// reorient for east/west rotation
			x = xBase + l_off;
			z = zBase + w_off;
			xDim = length;
			zDim = width;
		}

        // ensure we're not building on anything we don't like
		
		//kolt666: Changed if( ! ( isAir( world.getBlockId( x + x1, y, z = z1 )... to if( ! ( isAir( world.getBlockId( x + x1, y, z + z1 )
        for( int x1 = 0; x1 < xDim; x1++ ) {
            for( int z1 = 0; z1 < zDim; z1++ ) {
				if( ! ( isAir( world.getBlockId( x + x1, y, z + z1 ) ) || isAcceptable( world, x + x1, y, z + z1 ) ) ) {
					// stats.BadBlockFails++;
					return false;
				}
            }
        }

		// now let's check the area around us based on the template options
		if( leveling > 0 ) {
			// using site leveling rather than overhang.  Here we're really just checking
			// for air at one greater than the maximum leveling distance, since we're
			// just going to fill in everything else with the source block.
			for( int x1 = ( 0 - lbuffer ); x1 < ( xDim + lbuffer * 2 ); x1++ ) {
				for( int z1 = ( 0 - lbuffer ); z1 < ( zDim + lbuffer * 2 ); z1++ ) {
					if( isAir( world.getBlockId( x + x1, y - ( leveling + 1 ), z + z1 ) ) ) {
						// stats.LevelingFails++;
						return false;
					}
				}
			}
		} else {
			// if there's more than overhang air blocks under the ruins, ditch out.
			int aircount = 0;
			for( int x1 = 0; x1 < xDim; x1++ ) {
				for( int z1 = 0; z1 < zDim; z1++ ) {
					if( world.getBlockId( x + x1, y - 1, z + z1 ) == 0 ) { aircount++; }
					if( aircount > overhang ) {
						// stats.OverhangFails++;
						return false;
					}
				}
			}
		}

        // now check for vertical clearance.
		if( cutIn > 0 ) {
			// since we're using cut in, we're only concerned with what's at the max cut in
			// height.  Everything else will be cleared out before we build.
			int cutHeight = cutIn + 1;
			for( int x1 = ( 0 - cbuffer ); x1 < ( xDim + 2 * cbuffer ); x1++ ) {
				for( int z1 = ( 0 - cbuffer ); z1 < ( zDim + 2 * cbuffer ); z1++ ) {
					if( ! isAir( world.getBlockId( x + x1, y + cutHeight, z + z1 ) ) ) {
						// stats.CutInFails++;
						return false;
					}
				}
			}
		} else {
			// ensure we have air above the site.
			int mod = height - embed;
			for( int y1 = 1; y1 < mod; y1++ ) {
				for( int x1 = 0; x1 < xDim; x1++ ) {
					for( int z1 = 0; z1 < zDim; z1++ ) {
						if( ! isAir( world.getBlockId( x + x1, y + y1, z + z1 ) ) ) {
							// stats.NoAirAboveFails++;
							return false;
						}
					}
				}
			}
		}

        // looks like a good spot!
        return true;
    }

	public RuinBoundingBox getBoundingBox( int x, int z, int rotate ) {
		int add = ( cbuffer > lbuffer ? cbuffer : lbuffer );
		int xMin = 0, xMax = 0, zMin = 0, zMax = 0;
		if( ( rotate == RuinsMod.DIR_EAST ) || ( rotate == RuinsMod.DIR_WEST ) ) {
			xMin = x + l_off - add;
			xMax = xMin + length + add;
			zMin = z + w_off - add;
			zMax = zMin + width + add;
		} else {
			xMin = x + w_off - add;
			xMax = xMin + width + add;
			zMin = z + l_off - add;
			zMax = zMin + length + add;
		}
		return new RuinBoundingBox( xMin, xMax, zMin, zMax );
	}

    public void doBuild( World world, Random random, int xBase, int y, int zBase, int rotate ) {
        // we need to shift the base coordinates and take care of any rotations before
		// we can begin creating the layers.
		int x, z, xDim, zDim;
		boolean eastwest;
        RuinTemplateLayer curlayer;
		RuinTemplateRule curRule;

		// initialize all these variables
		ArrayList<RuinRuleProcess> laterun = new ArrayList<RuinRuleProcess>();
		ArrayList<RuinRuleProcess> lastrun = new ArrayList<RuinRuleProcess>();
        Iterator<RuinTemplateLayer> i = layers.iterator();
        int y_off = 1 - embed;
        int rulenum = 0;
		if( ( rotate == RuinsMod.DIR_EAST ) || ( rotate == RuinsMod.DIR_WEST ) ) {
			eastwest = true;
			x = xBase + l_off;
			xDim = length;
			z = zBase + w_off;
			zDim = width;
		} else {
			eastwest = false;
			x = xBase + w_off;
			xDim = width;
			z = zBase + l_off;
			zDim = length;
		}

		// do any site cut-in and leveling needed
		if( cutIn > 0 ) {
			cutInSite( world, xBase, y, zBase, eastwest );
		}
		if( leveling > 0 ) {
			levelSite( world, world.getBlockId( xBase, y, zBase ), xBase, y, zBase, eastwest );
		}

		// the main loop
        while( i.hasNext() ) {
            curlayer = i.next();
			for( int x1 = 0; x1 < xDim; x1++ ) {
				for( int z1 = 0; z1 < zDim; z1++ ) {
					switch( rotate ) {
					case RuinsMod.DIR_EAST:
						// rulenum = curlayer.getRuleAt( z1, length - ( x1 + 1 ) );
						rulenum = curlayer.getRuleAt( z1, xDim - ( x1 + 1 ) );
						break;
					case RuinsMod.DIR_SOUTH:
						rulenum = curlayer.getRuleAt( xDim - ( x1 + 1 ), zDim - ( z1 + 1 ) );
						break;
					case RuinsMod.DIR_WEST:
						// rulenum = curlayer.getRuleAt( width - ( z1 + 1 ), x1 );
						rulenum = curlayer.getRuleAt( zDim - ( z1 + 1 ), x1 );
						break;
					default:
						rulenum = curlayer.getRuleAt( x1, z1 );
						break;
					}

					curRule = rules.get( rulenum );
					if( curRule.runLater() ) {
						laterun.add( new RuinRuleProcess( curRule, x + x1, y + y_off, z + z1, rotate ) );
					} else if( curRule.runLast() ) {
						lastrun.add( new RuinRuleProcess( curRule, x + x1, y + y_off, z + z1, rotate ) );
					} else {
						curRule.doBlock( world, random, x + x1, y + y_off, z + z1, rotate );
					}
				}
			}

			// we're done with this layer
            y_off++;
        }

		// get the late runs and finish up
		doLateRuns( world, random, laterun, lastrun );
		world.markBlockRangeForRenderUpdate( xBase, y + 1 - embed, zBase, xBase + xDim, y + ( 1 - embed ) + height, zBase + zDim );
	}

	private void doLateRuns( World world, Random random, ArrayList<RuinRuleProcess> laterun, ArrayList<RuinRuleProcess> lastrun ) {
		Iterator<RuinRuleProcess> rp = laterun.iterator();
		while( rp.hasNext() ) {
			rp.next().doBlock( world, random );
		}

		rp = lastrun.iterator();
		while( rp.hasNext() ) {
			rp.next().doBlock( world, random );
		}
	}

	private void cutInSite( World world, int xBase, int y, int zBase, boolean eastwest ) {
		// remove blocks in and around the site as needed, up to the maximum
		// height. setup some variable defaults (north/south)
		int x = xBase + w_off - cbuffer;
        int z = zBase + l_off - cbuffer;
		int xDim = width + 2 * cbuffer;
		int zDim = length + 2 * cbuffer;
		int yDim = ( cutIn > height ) ? cutIn : height;

		// how are we oriented?
		if( eastwest ) {
			// reorient for east/west rotation
			x = xBase + l_off - cbuffer;
			z = zBase + w_off - cbuffer;
			xDim = length + 2 * cbuffer;
			zDim = width + 2 * cbuffer;
		}

		// this takes care of the embed code, since the y we get passed should never
		// be offset.
		y += 1;

		for( int y1 = 0; y1 < yDim; y1++ ) {
			for( int x1 = 0; x1 < xDim; x1++ ) {
				for( int z1 = 0; z1 < zDim; z1++ ) {
					if( ! preserveBlock( world.getBlockId( x + x1, y + y1, z + z1 ) ) ) {
						world.setBlockWithNotify( x + x1, y + y1, z + z1, 0 );
					}
				}
			}
		}
	}

	private void levelSite( World world, int fillBlockID, int xBase, int y, int zBase, boolean eastwest ) {
		// Add blocks around the build site to level it in as needed.
		// setup some variable defaults (north/south)
		int x = xBase + w_off - lbuffer;
        int z = zBase + l_off - lbuffer;
		int xDim = width + 2 * lbuffer;
		int zDim = length + 2 * lbuffer;
		// adding one here for the for loop.  should catch all.
		int yDim = 0 - ( leveling + 1 );

		// how are we oriented?
		if( eastwest ) {
			// reorient for east/west rotation
			x = xBase + l_off - lbuffer;
			z = zBase + w_off - lbuffer;
			xDim = length + 2 * lbuffer;
			zDim = width + 2 * lbuffer;
		}

		for( int y1 = 0; y1 > yDim; y1-- ) {
			for( int x1 = 0; x1 < xDim; x1++ ) {
				for( int z1 = 0; z1 < zDim; z1++ ) {
					if( isAir( world.getBlockId( x + x1, y + y1, z + z1 ) ) ) {
						world.setBlockWithNotify( x + x1, y + y1, z + z1, fillBlockID );
					}
				}
			}
		}
	}

    private void parseFile( ArrayList<String> lines ) throws Exception {
        // first get the variables.
        parseVariables( lines );

        // the first rule added will always be the air block rule.
        //rules.add( new RuinTemplateRule( "0,100,0" ) );
		rules.add( new RuinRuleAir( this, "" ) );

        // now get the rest of the data
        Iterator<String> i = lines.iterator();
        String line;
        while( i.hasNext() ) {
            line = i.next();
            if( ! line.startsWith( "#" ) ) {
                if( line.startsWith( "layer" ) ) {
                    // add in data until we reach the end of the layer
                    ArrayList<String> layerlines = new ArrayList<String>();
                    line = i.next();
                    while( ! line.startsWith( "endlayer" ) ) {
                        if( line.charAt( 0 ) != '#' ) {
                            layerlines.add( line );
                        }
                        line = i.next();
                    }
                    layers.add( new RuinTemplateLayer( layerlines, width, length ) );
                } else if( line.startsWith( "rule" ) ) {
                    String[] parts = line.split( "=" );
                    rules.add( new RuinTemplateRule( this, parts[1] ) );
                }
            }
        }
    }

    private void parseVariables( ArrayList<String> variables ) throws Exception {
        Iterator<String> i = variables.iterator();
        String line;
        while( i.hasNext() ) {
            line = i.next();
            if( ! line.startsWith( "#" ) ) {
                if( line.startsWith( "acceptable_target_blocks" ) ) {
                    String[] check = line.split( "=" );
                    check = check[1].split( "," );
                    if( check.length < 1 ) {
                        throw new Exception( "No targets specified!" );
                    }
                    targets = new int[check.length];
                    for( int x = 0; x < check.length; x++ ) {
                        targets[x] = Integer.parseInt( check[x] );
                    }
                }
                if( line.startsWith( "dimensions" ) ) {
                    String[] check = line.split( "=" );
                    check = check[1].split( "," );
                    height = Integer.parseInt( check[0] );
                    width = Integer.parseInt( check[1] );
                    length = Integer.parseInt( check[2] );
                }
                if( line.startsWith( "weight" ) ) {
                    String[] check = line.split( "=" );
                    weight = Integer.parseInt( check[1] );
                }
                if( line.startsWith( "unique" ) ) {
                    String[] check = line.split( "=" );
					if( Integer.parseInt( check[1] ) == 1 ) {
						unique = true;
					}
                }
                if( line.startsWith( "embed_into_distance" ) ) {
                    String[] check = line.split( "=" );
                    embed = Integer.parseInt( check[1] );
                }
                if( line.startsWith( "allowable_overhang" ) ) {
                    String[] check = line.split( "=" );
                    overhang = Integer.parseInt( check[1] );
                }
                if( line.startsWith( "max_cut_in" ) ) {
                    String[] check = line.split( "=" );
                    cutIn = Integer.parseInt( check[1] );
                }
                if( line.startsWith( "cut_in_buffer" ) ) {
                    String[] check = line.split( "=" );
                    cbuffer = Integer.parseInt( check[1] );
					if( cbuffer > 5 ) { cbuffer = 5; }
                }
                if( line.startsWith( "max_leveling" ) ) {
                    String[] check = line.split( "=" );
                    leveling = Integer.parseInt( check[1] );
                }
                if( line.startsWith( "leveling_buffer" ) ) {
                    String[] check = line.split( "=" );
                    lbuffer = Integer.parseInt( check[1] );
					if( lbuffer > 5 ) { lbuffer = 5; }
                }
                if( line.startsWith( "preserve_water" ) ) {
                    String[] check = line.split( "=" );
					if( Integer.parseInt( check[1] ) == 1 ) {
						preserveWater = true;
					}
                }
                if( line.startsWith( "preserve_lava" ) ) {
                    String[] check = line.split( "=" );
					if( Integer.parseInt( check[1] ) == 1 ) {
						preserveLava = true;
					}
                }
                if( line.startsWith( "preserve_plants" ) ) {
                    String[] check = line.split( "=" );
					if( Integer.parseInt( check[1] ) == 1 ) {
						preservePlants = true;
					}
                }
            }
        }
        if( width % 2 == 1 ) {
            w_off = 0 - ( width - 1 ) / 2;
        } else {
            w_off = 0 - width / 2;
        }
        if( length % 2 == 1 ) {
            l_off = 0 - ( length - 1 ) / 2;
        } else {
            l_off = 0 - length / 2;
        }
    }
}