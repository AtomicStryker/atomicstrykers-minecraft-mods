package atomicstryker.minions.client.render;

import org.lwjgl.opengl.GL11;

/**
 * Stores data about a line that can be rendered
 * 
 * @author lahwran
 * @author yetanotherx
 * 
 */
public class LineInfo {

    public float lineWidth;
    public float red;
    public float green;
    public float blue;
    public float alpha;
    public int depthfunc;

    public LineInfo(float lineWidth, float r, float g, float b, float a, int depthfunc) {
        this.lineWidth = lineWidth;
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
        this.depthfunc = depthfunc;
    }

    public LineInfo(float lineWidth, float r, float g, float b) {
        this(lineWidth, r, g, b, 1.0f, GL11.GL_LEQUAL);
    }

    public LineInfo(LineInfo orig) {
        this.lineWidth = orig.lineWidth;
        this.red = orig.red;
        this.green = orig.green;
        this.blue = orig.blue;
        this.alpha = orig.alpha;
        this.depthfunc = orig.depthfunc;
    }

    public void prepareRender() {
        GL11.glLineWidth(lineWidth);
        GL11.glDepthFunc(depthfunc);
    }

    public void prepareColor() {
        GL11.glColor4f(red, green, blue, alpha);
    }
}
