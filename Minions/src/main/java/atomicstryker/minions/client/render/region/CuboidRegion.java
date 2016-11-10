package atomicstryker.minions.client.render.region;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.points.PointCube;
import atomicstryker.minions.client.render.shapes.Render3DBox;
import atomicstryker.minions.client.render.shapes.Render3DGrid;
import atomicstryker.minions.common.util.Vector3;
import atomicstryker.minions.common.util.Vector3m;

/**
 * Main controller for a cuboid-type region
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class CuboidRegion extends BaseRegion {

    protected PointCube firstPoint;
    protected PointCube secondPoint;

    public CuboidRegion() {
    }

    @Override
    public void render() {
        if (firstPoint != null && secondPoint != null) {
            firstPoint.render();
            secondPoint.render();

            Vector3[] bounds = this.calcBounds();
            new Render3DBox(LineColor.CUBOIDBOX, bounds[0], bounds[1]).render();
            new Render3DGrid(LineColor.CUBOIDGRID, bounds[0], bounds[1]).render();

        } else if (firstPoint != null) {
            firstPoint.render();
        } else if (secondPoint != null) {
            secondPoint.render();
        }
    }

    @Override
    public void setCuboidPoint(int id, int x, int y, int z) {
        if (id == 0) {
            firstPoint = new PointCube(x, y, z);
            firstPoint.setColor(LineColor.CUBOIDPOINT1);
        } else if (id == 1) {
            secondPoint = new PointCube(x, y, z);
            secondPoint.setColor(LineColor.CUBOIDPOINT2);
        }
    }
    
    public void wipePointCubes()
    {
    	firstPoint = secondPoint = null;
    }

    protected Vector3m[] calcBounds() {
        float off = 0.02f;
        float off1 = 1 + off;

        Vector3m[] out = new Vector3m[2];
        out[0] = new Vector3m(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        out[1] = new Vector3m(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        for (PointCube point : new PointCube[]{firstPoint, secondPoint}) {
            if (point.getPoint().getX() + off1 > out[1].getX()) {
                out[1].setX(point.getPoint().getX() + off1);
            }

            if (point.getPoint().getX() - off < out[0].getX()) {
                out[0].setX(point.getPoint().getX() - off);
            }

            if (point.getPoint().getY() + off1 > out[1].getY()) {
                out[1].setY(point.getPoint().getY() + off1);
            }

            if (point.getPoint().getY() - off < out[0].getY()) {
                out[0].setY(point.getPoint().getY() - off);
            }

            if (point.getPoint().getZ() + off1 > out[1].getZ()) {
                out[1].setZ(point.getPoint().getZ() + off1);
            }

            if (point.getPoint().getZ() - off < out[0].getZ()) {
                out[0].setZ(point.getPoint().getZ() - off);
            }
        }

        return out;
    }

    @Override
    public RegionType getType() {
        return RegionType.CUBOID;
    }
}
