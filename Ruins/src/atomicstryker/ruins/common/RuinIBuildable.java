package atomicstryker.ruins.common;

import java.util.Random;

import net.minecraft.world.World;

public interface RuinIBuildable {
	public String getName();
	public int getWeight();
	public int getMinDistance();
	public boolean isUnique();
	public boolean isAir( int blockID );
	public boolean preserveBlock( int blockID );
	public boolean isAcceptable( World world, int x, int y, int z );
	public boolean checkArea( World world, int xBase, int y, int zBase, int rotate );
//	public boolean checkArea( World world, int xBase, int y, int zBase, int rotate, RuinStats stats );
	public RuinBoundingBox getBoundingBox( int x, int z, int rotate );
	public void doBuild( World world, Random random, int xBase, int y, int zBase, int rotate );
}