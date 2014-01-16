package atomicstryker.minions.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.MinionsClient;

/**
 * Minions Custom Dig Menu, allows you to choose your desired Dimensions
 * 
 * 
 * @author AtomicStryker
 */

public class GuiCustomDigMenu extends GuiScreen
{
    protected String screenTitle = "You want WHAT?!";
    
    private int xzSize = 3;
    private int ySize = 3;

    @SuppressWarnings("unchecked")
	@Override
    public void initGui()
    {
    	xzSize = MinionsClient.customSizeXZ;
    	ySize = MinionsClient.customSizeY;
    	
        this.field_146292_n.clear();
        
        this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 120, "Nevermind"));
        this.field_146292_n.add(new GuiButton(2, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 10, "Reset Dimensions"));
        
        this.field_146292_n.add(new GuiButton(3, this.field_146294_l / 10 * 6, this.field_146295_m / 4 + 35, 50, 20, "+2"));
        this.field_146292_n.add(new GuiButton(4, this.field_146294_l / 10 * 3, this.field_146295_m / 4 + 35, 50, 20, "-2"));
        
        this.field_146292_n.add(new GuiButton(5, this.field_146294_l / 10 * 8, this.field_146295_m / 4 + 35, 50, 20, "+10"));
        this.field_146292_n.add(new GuiButton(6, this.field_146294_l / 10 * 1, this.field_146295_m / 4 + 35, 50, 20, "-10"));
        
        this.field_146292_n.add(new GuiButton(7, this.field_146294_l / 10 * 6, this.field_146295_m / 4 + 75, 50, 20, "+1"));
        this.field_146292_n.add(new GuiButton(8, this.field_146294_l / 10 * 3, this.field_146295_m / 4 + 75, 50, 20, "-1"));
        
        this.field_146292_n.add(new GuiButton(9, this.field_146294_l / 10 * 8, this.field_146295_m / 4 + 75, 50, 20, "+5"));
        this.field_146292_n.add(new GuiButton(10, this.field_146294_l / 10 * 1, this.field_146295_m / 4 + 75, 50, 20, "-5"));
        
        //this.field_146292_n.add(new GuiButton(4, this.field_146294_l / 4 - 100, this.field_146295_m / 4 + 80, "Strip Mine"));

        this.field_146292_n.add(new GuiButton(1, this.field_146294_l / 2 - 100, this.field_146295_m / 4 -20, "Make it so!"));

    }

    @Override
    protected void func_146284_a(GuiButton var1)
    {
        if (var1.field_146124_l)
        {
        	int ID = var1.field_146127_k;
        	
        	if (ID == 0)
        	{
        		this.field_146297_k.func_147108_a((GuiScreen)null);
        	}
        	else if (ID == 1)
        	{
        	    MinionsClient.isSelectingMineArea = true;
        	    MinionsClient.customSizeXZ = this.xzSize;
        	    MinionsClient.customSizeY = this.ySize;
        	    MinionsClient.mineAreaShape = 2;
                this.field_146297_k.func_147108_a((GuiScreen)null);
        	}
        	else if (ID == 2)
        	{
        	    xzSize = 3;
        	    ySize = 3;
        	}
        	else if (ID == 3)
        	{
        		xzSize+=2;
        	}
        	else if (ID == 4)
        	{
        		xzSize-=2;
        	}
        	else if (ID == 5)
        	{
        		xzSize+=10;
        	}
        	else if (ID == 6)
        	{
        		xzSize-=10;
        	}
        	else if (ID == 7)
        	{
        		ySize+=1;
        	}
        	else if (ID == 8)
        	{
        		ySize-=1;
        	}
        	else if (ID == 9)
        	{
        		ySize+=5;
        	}
        	else if (ID == 10)
        	{
        		ySize-=5;
        	}
        	
        	if (xzSize < 3)
        		xzSize = 3;
        	if (ySize < 3)
        		ySize = 3;
        	if (xzSize > 71)
        		xzSize = 71;
        	if (ySize > 25)
        		ySize = 25;
        }
    }

    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
    	this.func_146276_q_();
    	this.drawCenteredString(this.field_146289_q, this.screenTitle, this.field_146294_l / 2, 40, 16777215);
    	
    	this.drawCenteredString(this.field_146289_q, "Width: "+this.xzSize, this.field_146294_l / 2, this.field_146295_m / 4 + 40, 16777215);
    	this.drawCenteredString(this.field_146289_q, "Height: "+this.ySize, this.field_146294_l / 2, this.field_146295_m / 4 + 80, 16777215);
    	
    	GL11.glPushMatrix();
    	GL11.glTranslatef((float)(this.field_146294_l / 2), 0.0F, 50.0F);
    	float var4 = 93.75F;
    	GL11.glScalef(-var4, -var4, -var4);
    	GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

    	GL11.glPopMatrix();
    	super.drawScreen(var1, var2, var3);
    }
}
