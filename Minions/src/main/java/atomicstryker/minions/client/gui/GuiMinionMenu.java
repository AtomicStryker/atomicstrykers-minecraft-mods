package atomicstryker.minions.client.gui;

import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.HaxPacket;
import atomicstryker.minions.common.network.UnsummonPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * Minion Menu, provides access to evil deeds and later minion commands
 * 
 * 
 * @author AtomicStryker
 */

public class GuiMinionMenu extends GuiScreen
{
    protected String screenTitle = "The Darkness listens...";
    
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Nevermind"));
        
        if (MinionsClient.hasMinionsSMPOverride)
        {
        	this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 0, "Unsummon Minions"));
        	this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 40, "Dig Mineshaft"));
        	this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 4 + 80, "Strip Mine"));
        	
        	this.buttonList.add(new GuiButton(4, this.width / 4 *3, this.height / 4 + 40, 100, 20, "Dig..."));
        }
		else if (MinionsCore.instance.evilDeedXPCost == -1)
		{
			this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 0, "Deeds Disabled by config!"));
		}
		else if (MinionsCore.instance.evilDoings == null || MinionsCore.instance.evilDoings.size() == 0)
		{
		    this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 0, "Missing Deed config file!"));
		}
        else if (mc.thePlayer.experienceLevel >= MinionsCore.instance.evilDeedXPCost)
        {
        	this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 0, "Commit to Evil"));
        }
    }

    @Override
    public void updateScreen()
    {
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
        		if (MinionsClient.hasMinionsSMPOverride)
        		{
        			MinionsCore.instance.networkHelper.sendPacketToServer(new UnsummonPacket(mc.thePlayer.getName()));
        			mc.displayGuiScreen(null);
        		}
        		else
        		{
        			this.mc.displayGuiScreen(new GuiDeedMenu());
        		}
        	}
        	else if (ID == 2)
        	{
            	MinionsClient.isSelectingMineArea = !MinionsClient.isSelectingMineArea;
                this.mc.displayGuiScreen(null);
                MinionsClient.mineAreaShape = 0;
        	}
        	else if (ID == 3)
        	{
        	    MinionsClient.isSelectingMineArea = !MinionsClient.isSelectingMineArea;
        	    MinionsClient.mineAreaShape = 1;
                this.mc.displayGuiScreen(null);
        	}
        	else if (ID == 4)
        	{
                this.mc.displayGuiScreen(new GuiCustomDigMenu());
        	}
        }
    }
    
    private int cheatCount = 0;

    @Override
    protected void keyTyped(char var1, int var2)
    {
    	if (var1 == 'i' && cheatCount == 0) cheatCount++;
    	if (var1 == 'l' && cheatCount == 1) cheatCount++;
    	if (var1 == 'u' && cheatCount == 2) cheatCount++;
    	if (var1 == 'v' && cheatCount == 3) cheatCount++;
    	if (var1 == 'e' && cheatCount == 4) cheatCount++;
    	if (var1 == 'v' && cheatCount == 5) cheatCount++;
    	if (var1 == 'i' && cheatCount == 6) cheatCount++;
    	if (var1 == 'l' && cheatCount == 7)
        {
    	    MinionsCore.instance.networkHelper.sendPacketToServer(new HaxPacket(mc.thePlayer.getName())); // cheater!!!!
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
    	this.drawDefaultBackground();
    	this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 40, 16777215);
    	/*
    	GL11.glPushMatrix();
    	GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
    	float var4 = 93.75F;
    	GL11.glScalef(-var4, -var4, -var4);
    	GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
    	GL11.glPopMatrix();
    	*/
    	super.drawScreen(var1, var2, var3);
    }
}
