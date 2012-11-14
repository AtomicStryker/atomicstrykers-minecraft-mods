import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

public class SchToTml {
	public static void main( String[] args ) {
		if( ! ( args.length > 0 ) ) {
			System.out.println( "Usage:" );
			System.out.println( "    SchToTml <file1> <file2> <file3> <etc...>" );
			System.out.println( "    You must specify at least one filename to convert." );
			return;
		}
		for( int i = 0; i < args.length; i++ ) {
			try {
				String SchFile = args[i];
				System.out.println( "Converting file " + SchFile );
				String TmlFile = convertFileName( SchFile );
				Tag parse = Tag.readFrom( new FileInputStream( SchFile ) );
				parse.print();
				System.out.println( "Saving file " + TmlFile );
				saveAs( TmlFile, parse );
				System.out.println();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	private static void saveAs( String tmlfile, Tag t ) throws Exception {
		short height = 0, width = 0, length = 0;
		byte[] blocks = null, metadata = null;

		// check for schematic data.
		if( t.getType() != Tag.Type.TAG_Compound ) {
			throw new Exception( "Found incorrect NBT format in this file, schematic cannot be converted." );
		}

		// grab the subtags.
		Tag[] tags = (Tag[]) t.getValue();
		for( Tag st : tags ) {
			if( st.getType() == Tag.Type.TAG_Short ) {
				if( st.getName().equals( "Height" ) ) {
					height = ((Short) st.getValue()).shortValue();
				}
				if( st.getName().equals( "Width" ) ) {
					width = ((Short) st.getValue()).shortValue();
				}
				if( st.getName().equals( "Length" ) ) {
					length = ((Short) st.getValue()).shortValue();
				}
			} else if( st.getType() == Tag.Type.TAG_Byte_Array ) {
				if( st.getName().equals( "Blocks" ) ) {
					blocks = (byte[]) st.getValue();
				}
				if( st.getName().equals( "Data" ) ) {
					metadata = (byte[]) st.getValue();
				}
			}
		}
		if( height <= 0 || width <= 0 || length <= 0 ) {
			throw new Exception( "Found height, width, or length of 0, schematic cannot be converted." );
		}
		if( blocks.length <= 0 || metadata.length <= 0 ) {
			throw new Exception( "No block or metadata found, schematic cannot be converted." );
		}

		// convert the data into our own style.
		byte[][][] cBlocks = new byte[height][length][width];
		byte[][][] cMeta = new byte[height][length][width];
		int y = 0, z = 0, x = 0;
		for( int i = 0; i < blocks.length; i++ ) {
			cBlocks[y][z][x] = blocks[i];
			cMeta[y][z][x] = metadata[i];
			x++;
			if( x >= width ) {
				x = 0;
				z++;
				if( z >= length ) {
					z = 0;
					y++;
				}
			}
		}

		// parse out the block rules.
		ArrayList<BlockRule> rules = new ArrayList<BlockRule>();
		BlockRule temp;
		int curRule = 1;
		for( y = 0; y < height; y++ ) {
			for( z = 0; z < length; z++ ) {
				for( x = 0; x < width; x++ ) {
					temp = new BlockRule( cBlocks[y][z][x], cMeta[y][z][x] );
					if( ! ruleExists( rules, temp ) ) {
						temp.setRuleNum( curRule );
						rules.add( temp );
						curRule++;
					}
				}
			}
		}

		// create a line list, add in the instructions, and fill in some defaults.
		ArrayList<String> lines = new ArrayList<String>();
		lines.add( new String( "# Converted from schematic" ) );
		lines.add( new String( "# THIS TEMPLATE USES DEFAULTS!  Please edit for corrections." ) );
		lines.add( new String( "# If you need help, consult template_rules.txt" ) );
		lines.add( new String( "" ) );
		lines.add( new String( "weight=1" ) );
		lines.add( new String( "embed_into_distance=1" ) );
		lines.add( new String( "acceptable_target_blocks=1,2,3,12,13" ) );
		lines.add( new String( "dimensions=" + height + "," + width + "," + length ) );
		lines.add( new String( "allowable_overhang=0" ) );
		lines.add( new String( "max_cut_in=2" ) );
		lines.add( new String( "cut_in_buffer=1" ) );
		lines.add( new String( "max_leveling=2" ) );
		lines.add( new String( "leveling_buffer=1" ) );
		lines.add( new String( "preserve_water=0" ) );
		lines.add( new String( "preserve_lava=0" ) );
		lines.add( new String( "preserve_plants=0" ) );
		lines.add( new String( "" ) );
		lines.add( new String( "" ) );

		// add the block rules.
		Iterator<BlockRule> b = rules.iterator();
		while( b.hasNext() ) {
			lines.add( b.next().toString() );
		}
		lines.add( new String( "" ) );
		lines.add( new String( "" ) );

		// add the layers.  First we have to rotate the template.
		cBlocks = rotateTemplate( cBlocks );
		cMeta = rotateTemplate( cMeta );

		String line;
		for( y = 0; y < height; y++ ) {
			lines.add( new String( "layer" ) );
			for( z = 0; z < width; z++ ) {
				line = new String( "" );
				for( x = 0; x < length; x++ ) {
					if( x == 0 ) {
						line += "" + getRuleNum( rules, cBlocks[y][z][x], cMeta[y][z][x] );
					} else {
						line += "," + getRuleNum( rules, cBlocks[y][z][x], cMeta[y][z][x] );
					}
				}
				lines.add( line );
			}
			lines.add( new String( "endlayer" ) );
			lines.add( new String( "" ) );
		}

/*		for( y = 0; y < height; y++ ) {
			lines.add( new String( "layer" ) );
			for( z = 0; z < length; z++ ) {
				line = new String( "" );
				for( x = 0; x < width; x++ ) {
					if( x == 0 ) {
						line += "" + getRuleNum( rules, cBlocks[y][z][x], cMeta[y][z][x] );
					} else {
						line += "," + getRuleNum( rules, cBlocks[y][z][x], cMeta[y][z][x] );
					}
				}
				lines.add( line );
			}
			lines.add( new String( "endlayer" ) );
			lines.add( new String( "" ) );
		}*/

		// save it all and we're good.
		PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( tmlfile ) ) );
		Iterator<String> i = lines.iterator();
		while( i.hasNext() ) {
			pw.println( i.next() );
		}
		pw.flush();
		pw.close();
	}

	private static String convertFileName( String schfile ) {
		int i = schfile.lastIndexOf( "." );
		String base = schfile.substring( 0, i );
		return base + ".tml";
	}

	private static boolean ruleExists( ArrayList<BlockRule> list, BlockRule check ) {
		// check for air.  If so, the rule exists.
		if( check.ID == 0 ) { return true; }

		// now look for other rules.
		if( list.size() <= 0 ) { return false; }
		Iterator<BlockRule> i = list.iterator();
		while( i.hasNext() ) {
			if( i.next().matches( check ) ) {
				return true;
			}
		}
		return false;
	}

	private static int getRuleNum( ArrayList<BlockRule> list, int ID, int metadata ) {
		if( ID == 0 ) { return 0; }
		Iterator<BlockRule> i = list.iterator();
		BlockRule temp, check = new BlockRule( ID, metadata );
		while( i.hasNext() ) {
			temp = i.next();
			if( temp.matches( check ) ) {
				return temp.rulenum;
			}
		}
		// safeguard
		return 0;
	}

	private static byte[][][] rotateTemplate( byte[][][] data ) {
		int xDim = data[0][0].length;
		int zDim = data[0].length;
		byte[][][] retval = new byte[data.length][xDim][zDim];
		for( int y = 0; y < data.length; y++ ) {
			for( int x = 0; x < xDim; x++ ) {
				for( int z = 0; z < zDim; z++ ) {
					retval[y][x][z] = data[y][zDim - (z + 1)][x];
				}
			}
		}
		return retval;
	}

	private static class BlockRule {
		protected int ID, metadata, rulenum = 0;
		public BlockRule( int bid, int md ) {
			ID = bid;
			metadata = md;
		}

		public boolean matches( BlockRule b ) {
			if( ID == b.ID && metadata == b.metadata ) { return true; }
			return false;
		}

		public void setRuleNum( int i ) {
			rulenum = i;
		}

		@Override
		public String toString() {
			if( metadata != 0 ) {
				return "rule" + rulenum + "=0,100," + ID + "-" + metadata;
			}
			return "rule" + rulenum + "=0,100," + ID;
		}
	}
}