package atomicstryker.minions.client.render.shapes;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.client.render.points.PointRectangle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.List;

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

            Tessellator.getInstance().getBuffer().begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
            tempColor.prepareColor();
            for (PointRectangle point : points) {
                if (point != null) {
                	Tessellator.getInstance().getBuffer().pos(point.getPoint().getX() + 0.5, height, point.getPoint().getY() + 0.5).endVertex();
                }
            }
            Tessellator.getInstance().draw();
        }
    }
}
