package atomicstryker.minions.client.render;

import org.lwjgl.opengl.GL11;

/**
 * Stores color data for each type of line.
 * 
 * Each line has a normal line, and a hidden line.
 * The normal line has an alpha value of 0.8f, and
 * the hidden line has an alpha value of 0.2f. They
 * both have a thickness of 3.0f.
 * 
 * @author yetanotherx
 * @author lahwran
 */
public enum LineColor {

    CUBOIDGRID(0.8F, 0.2F, 0.2F),
    CUBOIDBOX(0.8F, 0.3F, 0.3F),
    CUBOIDPOINT1(0.2F, 0.8F, 0.2F), //33CC33
    CUBOIDPOINT2(0.2F, 0.2F, 0.8F), //3333CC
    POLYGRID(0.8F, 0.2F, 0.2F),
    POLYBOX(0.8F, 0.3F, 0.3F),
    POLYPOINT(0.2F, 0.8F, 0.8F), //33CCCC
    ELLIPSOIDGRID(0.8F, 0.3F, 0.3F),
    ELLIPSOIDCENTER(0.8F, 0.8F, 0.2F), //CCCC33
    CYLINDERGRID(0.8F, 0.2F, 0.2F),
    CYLINDERBOX(0.8F, 0.3F, 0.3F),
    CYLINDERCENTER(0.8F, 0.2F, 0.8F); //CC33CC
    protected LineInfo normal;
    protected LineInfo hidden;

    LineColor(float r, float g, float b) {
        normal = new LineInfo(3.0f, r, g, b, 0.8f, GL11.GL_LESS);
        hidden = new LineInfo(3.0f, r, g, b, 0.2f, GL11.GL_GEQUAL);
    }
    
    LineColor(String hex) {
        setColor(hex);
    }

    public LineInfo getHidden() {
        return hidden;
    }

    public LineInfo getNormal() {
        return normal;
    }

    public LineInfo[] getColors() {
        return new LineInfo[]{hidden, normal};
    }

    public void setColor(String hex) {
        Integer r = Integer.parseInt(hex.substring(1, 3), 16);
        Integer g = Integer.parseInt(hex.substring(3, 5), 16);
        Integer b = Integer.parseInt(hex.substring(5, 7), 16);
        
        float rF = r.floatValue() / 256.0F;
        float gF = g.floatValue() / 256.0F;
        float bF = b.floatValue() / 256.0F;
        
        normal = new LineInfo(3.0f, rF, gF, bF, 0.8f, GL11.GL_LESS);
        hidden = new LineInfo(3.0f, rF, gF, bF, 0.2f, GL11.GL_GEQUAL);
    }
}
