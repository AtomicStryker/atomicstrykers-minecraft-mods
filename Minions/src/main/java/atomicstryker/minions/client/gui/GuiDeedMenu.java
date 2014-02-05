package atomicstryker.minions.client.gui;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.common.EvilDeed;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.PacketType;
import atomicstryker.minions.common.network.ForgePacketWrapper;
import atomicstryker.minions.common.network.PacketDispatcher;

/**
 * Evil Deed selection menu
 * 
 * 
 * @author AtomicStryker
 */

public class GuiDeedMenu extends GuiScreen
{
    protected String screenTitle = "Choose your part!";
       
    private boolean fadeOutfadeInUnderWay = false;
    private int incrementingInt;
    private int fadeState = 1;
    private int actionCalled = 0;
    private long timeStayBlack = 1000L;
    private long timeFadeStart = 0L;
    
    private ArrayList<EvilDeed> deedButtons;


    @SuppressWarnings("unchecked")
	@Override
    public void initGui()
    {
        this.buttonList.clear();
        
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Chicken out"));
        
    	ArrayList<EvilDeed> copy = (ArrayList<EvilDeed>) MinionsCore.instance.evilDoings.clone();
    	deedButtons = new ArrayList<EvilDeed>();
    	Random rand = new Random();
    	while (deedButtons.size() < 3)
    	{
    		int i = rand.nextInt(copy.size()-1);
    		deedButtons.add(copy.get(i));
    		copy.remove(i);
    	}
    	
    	this.buttonList.clear();
    	
    	this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Nevermind"));
    	
    	for (int x = 0; x < 3; x++)
    	{
    		EvilDeed deed = (EvilDeed) deedButtons.get(x);
    		this.buttonList.add(new GuiButton(x+1, this.width / 2 - 100, this.height / 4 + x*40, deed.getButtonText()));
    	}
    }

    @Override
    public void updateScreen()
    {        
        if (fadeOutfadeInUnderWay)
        {
        	if (fadeState == 0 && timeFadeStart != 0L && System.currentTimeMillis() - timeFadeStart > timeStayBlack)
        	{
        		//System.out.println("Time passed, unfading now!");
        		fadeState = 2;
        		timeFadeStart = 0L;
        	}

        	else if (fadeState == 1)
        	{
        		incrementingInt+=5;

        		if (incrementingInt >= 180)
        		{
        			//System.out.println("Full Fade reached, waiting for time!");
        			incrementingInt = 180;
        			timeFadeStart = System.currentTimeMillis();
        			fadeState = 0;
        			
        			mc.thePlayer.worldObj.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, ((EvilDeed)this.deedButtons.get(actionCalled-1)).getSoundFile(), 1.0F, 1.0F, false);
        			timeStayBlack = ((EvilDeed)this.deedButtons.get(actionCalled-1)).getSoundLength() * 1000L;
        		}
        	}
        	else if (fadeState == 2)
        	{
        		incrementingInt-=5;

        		if (incrementingInt <= 20)
        		{
        			incrementingInt = 20;
        			fadeState = 1;
        			fadeOutfadeInUnderWay = false;
        			timeFadeStart = 0L;

        			Object[] toSend = {mc.thePlayer.getGameProfile().getName()};
        			PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.EVILDEEDDONE.ordinal(), toSend)); // evildeed call

        			this.mc.displayGuiScreen((GuiScreen)null);
        			//System.out.println("Unfade finished, destroyed menu!");
        		}
        	}
        }
    }

    @Override
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
        	int ID = var1.id;
        	actionCalled = ID;
        	
        	if (ID == 0)
        	{
        		this.mc.displayGuiScreen((GuiScreen)null);
        	}
        	else
        	{
            	//System.out.println("Started Fade out!");
            	fadeState = 1;
            	fadeOutfadeInUnderWay = true;
        	}
        }
    }

    @Override
    protected void keyTyped(char var1, int var2)
    {
    	
    }

    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
    	this.drawDefaultBackground();
    	this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 40, 16777215);
    	GL11.glPushMatrix();
    	GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
    	float var4 = 93.75F;
    	GL11.glScalef(-var4, -var4, -var4);
    	GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

    	GL11.glPopMatrix();
    	super.drawScreen(var1, var2, var3);
    	
    	if (fadeOutfadeInUnderWay)
    	{
    		if (fadeState == 0)
    		{
    			Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, -16777216);
    			return;
    		}
    		
    		double d = (double)incrementingInt / 200;
    		d = 1.0D - d;
    		if(d < 0.0D) { d = 0.0D; }
    		if(d > 1.0D) { d = 1.0D; }
    		d *= d;
    		int j4 = (int)(255D * d);
    		int fadeIn = 0;
    		if(j4 < 255)
    		{
    			fadeIn = (j4 << 24);
    		}
    		Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, 0 - fadeIn);
    	}
    }
}
