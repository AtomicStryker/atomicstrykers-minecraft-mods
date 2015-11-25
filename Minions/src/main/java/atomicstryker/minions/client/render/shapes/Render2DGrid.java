package atomicstryker.minions.client.render.shapes;

import java.util.List;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.client.render.points.PointRectangle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * Draws the grid for a polygon region
 * 
 * @author yetanotherx
 * @author lahwran
 */
public class Render2DGrid {
    
    protected LineColor color;
    protected List<PointRectangle> points;
    protected int min;
    protected int max;

    public Render2DGrid(LineColor color, List<PointRectangle> points, int min, int max) {
        this.color = color;
        this.points = points;
        this.min = min;
        this.max = max;
    }
    
    public void render() {
        double off = 0.03;
        for (double height = min; height <= max + 1; height++) {
            drawPoly(height + off);
        }
    }

    protected  void drawPoly(double height) {
        for (LineInfo tempColor : color.getColors()) {
            tempColor.prepareRender();

            Tessellator.getInstance().getWorldRenderer().func_181668_a(GL11.GL_LINE_LOOP, DefaultVertexFormats.field_181703_c);
            tempColor.prepareColor();
            for (PointRectangle point : points) {
                if (point != null) {
                	Tessellator.getInstance().getWorldRenderer().func_181662_b(point.getPoint().getX() + 0.5, height, point.getPoint().getY() + 0.5).func_181675_d();
                }
            }
            Tessellator.getInstance().draw();
        }
    }
}
