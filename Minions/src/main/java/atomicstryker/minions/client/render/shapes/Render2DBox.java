package atomicstryker.minions.client.render.shapes;

import java.util.List;

import net.minecraft.client.renderer.VertexBuffer;
import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.render.LineColor;
import atomicstryker.minions.client.render.LineInfo;
import atomicstryker.minions.client.render.points.PointRectangle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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
        VertexBuffer vb = Tessellator.getInstance().getBuffer();
        for (LineInfo tempColor : color.getColors()) {
            tempColor.prepareRender();

            vb.begin(GL11.GL_LINES, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
            tempColor.prepareColor();

            points.stream().filter(point -> point != null).forEach(point -> {
                vb.pos(point.getPoint().getX() + 0.5, min + off, point.getPoint().getY() + 0.5).endVertex();
                vb.pos(point.getPoint().getX() + 0.5, max + 1 + off, point.getPoint().getY() + 0.5).endVertex();
            });
            Tessellator.getInstance().draw();
        }
    }
}
