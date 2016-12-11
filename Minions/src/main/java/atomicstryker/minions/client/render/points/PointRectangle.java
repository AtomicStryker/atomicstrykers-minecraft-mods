package atomicstryker.minions.client.render.points;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.shapes.Render3DBox;
import atomicstryker.minions.common.util.Vector2;
import net.minecraft.util.math.Vec3d;

/**
 * Stores data about a prism surrounding two
 * blocks in the world. Used to store info
 * about the selector blocks for polys. Keeps 
 * track of color, x/y/z values, and rendering.
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class PointRectangle {

    protected Vector2 point;
    protected LineColor color = LineColor.POLYPOINT;

    public PointRectangle(Vector2 point) {
        this.point = point;
    }

    public PointRectangle(int x, int z) {
        this.point = new Vector2(x, z);
    }

    public void render(int min, int max, Vec3d cameraPos) {
        float off = 0.03f;
        Vector2 minVec = new Vector2(off, off);
        Vector2 maxVec = new Vector2(off + 1, off + 1);
        new Render3DBox(color, point.subtract(minVec).toVector3(min - off), point.add(maxVec).toVector3(max + 1 + off)).render(cameraPos);
    }

    public Vector2 getPoint() {
        return point;
    }

    public void setPoint(Vector2 point) {
        this.point = point;
    }

    public LineColor getColor() {
        return color;
    }

    public void setColor(LineColor color) {
        this.color = color;
    }
}
