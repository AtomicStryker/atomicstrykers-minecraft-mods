package atomicstryker.minions.client.gui;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.client.MinionsClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

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

    @Override
    public void initGui()
    {
    	xzSize = MinionsClient.customSizeXZ;
    	ySize = MinionsClient.customSizeY;
    	
        this.buttonList.clear();
        
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Nevermind"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 10, "Reset Dimensions"));
        
        this.buttonList.add(new GuiButton(3, this.width / 10 * 6, this.height / 4 + 35, 50, 20, "+2"));
        this.buttonList.add(new GuiButton(4, this.width / 10 * 3, this.height / 4 + 35, 50, 20, "-2"));
        
        this.buttonList.add(new GuiButton(5, this.width / 10 * 8, this.height / 4 + 35, 50, 20, "+10"));
        this.buttonList.add(new GuiButton(6, this.width / 10 * 1, this.height / 4 + 35, 50, 20, "-10"));
        
        this.buttonList.add(new GuiButton(7, this.width / 10 * 6, this.height / 4 + 75, 50, 20, "+1"));
        this.buttonList.add(new GuiButton(8, this.width / 10 * 3, this.height / 4 + 75, 50, 20, "-1"));
        
        this.buttonList.add(new GuiButton(9, this.width / 10 * 8, this.height / 4 + 75, 50, 20, "+5"));
        this.buttonList.add(new GuiButton(10, this.width / 10 * 1, this.height / 4 + 75, 50, 20, "-5"));
        
        //this.buttonList.add(new GuiButton(4, this.width / 4 - 100, this.height / 4 + 80, "Strip Mine"));

        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 -20, "Make it so!"));

    }

    @Override
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
        	int ID = var1.id;
        	
        	if (ID == 0)
        	{
        		this.mc.displayGuiScreen(null);
        	}
        	else if (ID == 1)
        	{
        	    MinionsClient.isSelectingMineArea = true;
        	    MinionsClient.customSizeXZ = this.xzSize;
        	    MinionsClient.customSizeY = this.ySize;
        	    MinionsClient.mineAreaShape = 2;
                this.mc.displayGuiScreen(null);
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
    	this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 40, 16777215);
    	
        this.drawCenteredString(this.fontRenderer, "Width: " + this.xzSize, this.width / 2, this.height / 4 + 40, 16777215);
        this.drawCenteredString(this.fontRenderer, "Height: " + this.ySize, this.width / 2, this.height / 4 + 80, 16777215);
    	
    	GL11.glPushMatrix();
    	GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
    	float var4 = 93.75F;
    	GL11.glScalef(-var4, -var4, -var4);
    	GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

    	GL11.glPopMatrix();
    	super.drawScreen(var1, var2, var3);
    }
}
