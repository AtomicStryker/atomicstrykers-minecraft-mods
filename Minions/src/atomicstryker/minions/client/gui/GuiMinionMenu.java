package atomicstryker.minions.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.PacketType;
import atomicstryker.minions.common.network.ForgePacketWrapper;
import atomicstryker.minions.common.network.PacketDispatcher;

/**
 * Minion Menu, provides access to evil deeds and later minion commands
 * 
 * 
 * @author AtomicStryker
 */

public class GuiMinionMenu extends GuiScreen
{
    protected String screenTitle = "The Darkness listens...";
    
    @SuppressWarnings("unchecked")
	@Override
    public void initGui()
    {
        this.field_146292_n.clear();
        
        this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 120, "Nevermind"));
        
        if (MinionsClient.hasMinionsSMPOverride)
        {
        	this.field_146292_n.add(new GuiButton(1, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 0, "Unsummon Minions"));
        	this.field_146292_n.add(new GuiButton(2, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 40, "Dig Mineshaft"));
        	this.field_146292_n.add(new GuiButton(3, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 80, "Strip Mine"));
        	
        	this.field_146292_n.add(new GuiButton(4, this.field_146294_l / 4 *3, this.field_146295_m / 4 + 40, 100, 20, "Dig..."));
        }
		else if (MinionsCore.instance.evilDeedXPCost == -1)
		{
			this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 0, "Deeds Disabled by config!"));
		}
		else if (MinionsCore.instance.evilDoings == null || MinionsCore.instance.evilDoings.size() == 0)
		{
		    this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 0, "Missing Deed config file!"));
		}
        else if (field_146297_k.thePlayer.experienceLevel >= MinionsCore.instance.evilDeedXPCost)
        {
        	this.field_146292_n.add(new GuiButton(1, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 0, "Commit to Evil"));
        }
    }

    @Override
    public void updateScreen()
    {
    }

    @Override
    protected void func_146284_a(GuiButton var1)
    {
        if (var1.field_146124_l)
        {
            int ID = var1.field_146127_k;
        	
        	if (ID == 0)
        	{
        		this.field_146297_k.func_147108_a(null);
        	}
        	else if (ID == 1)
        	{
        		if (MinionsClient.hasMinionsSMPOverride)
        		{
        			Object[] toSend = {field_146297_k.thePlayer.func_146103_bH().getName()};
        			PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.CMDUNSUMMON.ordinal(), toSend)); // minion unsummon command to server
        			this.field_146297_k.func_147108_a((GuiScreen)null);
        		}
        		else
        		{
        			this.field_146297_k.func_147108_a(new GuiDeedMenu());
        		}
        	}
        	else if (ID == 2)
        	{
            	MinionsClient.isSelectingMineArea = !MinionsClient.isSelectingMineArea;
                this.field_146297_k.func_147108_a((GuiScreen)null);
                MinionsClient.mineAreaShape = 0;
        	}
        	else if (ID == 3)
        	{
        	    MinionsClient.isSelectingMineArea = !MinionsClient.isSelectingMineArea;
        	    MinionsClient.mineAreaShape = 1;
                this.field_146297_k.func_147108_a((GuiScreen)null);
        	}
        	else if (ID == 4)
        	{
                this.field_146297_k.func_147108_a(new GuiCustomDigMenu());
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
            PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HAX.ordinal(), null)); // cheater!!!!
            this.field_146297_k.func_147108_a((GuiScreen) null);
        }
    }

    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
    	this.func_146276_q_();
    	this.drawCenteredString(this.field_146289_q, this.screenTitle, this.field_146294_l / 2, 40, 16777215);
    	/*
    	GL11.glPushMatrix();
    	GL11.glTranslatef((float)(this.field_146294_l / 2), 0.0F, 50.0F);
    	float var4 = 93.75F;
    	GL11.glScalef(-var4, -var4, -var4);
    	GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
    	GL11.glPopMatrix();
    	*/
    	super.drawScreen(var1, var2, var3);
    }
}
