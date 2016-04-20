package atomicstryker.minions.client.render.points;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.shapes.Render3DBox;
import atomicstryker.minions.common.util.Vector3;

/**
 * Stores data about a cube surrounding a
 * block in the world. Used to store info
 * about the selector Blocks. Keeps track
 * of color, x/y/z values, and rendering.
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class PointCube {

    protected Vector3 point;
    protected LineColor color = LineColor.CUBOIDPOINT1;

    public PointCube(Vector3 point) {
        this.point = point;
    }

    public PointCube(int x, int y, int z) {
        this.point = new Vector3(x, y, z);
    }
    
    public PointCube(double x, double y, double z) {
        this.point = new Vector3(x, y, z);
    }

    public void render() {
        double off = 0.03f;
        Vector3 minVec = new Vector3(off, off, off);
        Vector3 maxVec = new Vector3(off + 1, off + 1, off + 1);

        new Render3DBox(color, point.subtract(minVec), point.add(maxVec)).render();
    }

    public Vector3 getPoint() {
        return point;
    }

    public void setPoint(Vector3 point) {
        this.point = point;
    }

    public LineColor getColor() {
        return color;
    }

    public void setColor(LineColor color) {
        this.color = color;
    }
}
