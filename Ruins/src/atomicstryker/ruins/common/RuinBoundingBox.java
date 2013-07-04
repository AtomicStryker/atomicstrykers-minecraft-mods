package atomicstryker.ruins.common;

public class RuinBoundingBox {
	protected int xMin, xMax, zMin, zMax;

	public RuinBoundingBox( int xmin, int xmax, int zmin, int zmax ) {
		xMin = xmin;
		xMax = xmax;
		zMin = zmin;
		zMax = zmax;
	}

	public boolean collides( RuinBoundingBox check ) {
		if( ( check.xMin >= xMin ) && ( check.xMin <= xMax ) ) { return true; }
		if( ( check.xMax >= xMin ) && ( check.xMax <= xMax ) ) { return true; }
		if( ( check.zMin >= zMin ) && ( check.zMin <= zMax ) ) { return true; }
		if( ( check.zMax >= zMin ) && ( check.zMax <= zMax ) ) { return true; }
		return false;
	}
}