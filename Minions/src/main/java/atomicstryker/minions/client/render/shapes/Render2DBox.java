package atomicstryker.minions.client.render.shapes;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.client.render.points.PointRectangle;

/**
 * Draws the top and bottom rings of
 * a polygon region
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class Render2DBox {

    protected LineColor color;
    protected List<PointRectangle> points;
    protected int min;
    protected int max;

    public Render2DBox(LineColor color, List<PointRectangle> points, int min, int max) {
        this.color = color;
        this.points = points;
        this.min = min;
        this.max = max;
    }

    public void render() {
        double off = 0.03;
        for (LineInfo tempColor : color.getColors()) {
            tempColor.prepareRender();

            Tessellator.getInstance().getWorldRenderer().startDrawing(GL11.GL_LINES);
            tempColor.prepareColor();

            for (PointRectangle point : points) {
                if (point != null) {
                	Tessellator.getInstance().getWorldRenderer().addVertex(point.getPoint().getX() + 0.5, min + off, point.getPoint().getY() + 0.5);
                	Tessellator.getInstance().getWorldRenderer().addVertex(point.getPoint().getX() + 0.5, max + 1 + off, point.getPoint().getY() + 0.5);
                }
            }
            Tessellator.getInstance().draw();
        }
    }
}
