package atomicstryker.magicyarn.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.FMLClientHandler;

import org.lwjgl.opengl.GL11;

public class GuiNavigateToPlayer extends GuiScreen
{
    
    private final String screenTitle = "Try navigating to player:";
    
    private final int BUTTON_TO_PLAYER_ID_OFFSET = 2;
    private final int BUTTONS_WANTED = 3;
    
    private Object[] playerList;
    
    /**
     * To only keep x players per screen, remember the index we at
     */
    private int nextStartIndex;
    
    @Override
    public void initGui()
    {
        nextStartIndex = 0;
        playerList = FMLClientHandler.instance().getClient().theWorld.playerEntities.toArray();
        generateButtons();
    }
    
    @SuppressWarnings("unchecked")
    private void generateButtons()
    {
        buttonList.clear();
        buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Cancel"));
        
        if (playerList.length > 4)
        {
            buttonList.add(new GuiButton(1, this.width / 10 * 8, this.height / 4 + 75, 50, 20, "->"));
        }
        
        int targetListSize = buttonList.size()+BUTTONS_WANTED;
        int i = 0;
        
        EntityPlayer c = FMLClientHandler.instance().getClient().thePlayer;
        EntityPlayer p;
        for (int x = nextStartIndex; x < playerList.length && buttonList.size() != targetListSize ; x++)
        {
            if (nextStartIndex >= playerList.length)
            {
                nextStartIndex = 0;
                x = 0;
            }
            
            p = (EntityPlayer) playerList[x];
            if (!p.getGameProfile().getName().equals(c.getGameProfile().getName()))
            {
                buttonList.add(new GuiButton(x+BUTTON_TO_PLAYER_ID_OFFSET, this.width / 2 - 100, this.height / 4 + i++*40, p.getGameProfile().getName()));
            }
            nextStartIndex = x;
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                mc.displayGuiScreen((GuiScreen)null);
            }
            else if (button.id == 1)
            {
                nextStartIndex += BUTTONS_WANTED;
                generateButtons();
            }
            else
            {
                EntityPlayer target = (EntityPlayer) playerList[button.id-BUTTON_TO_PLAYER_ID_OFFSET];
                MagicYarnClient.instance.tryPathToPlayer(target);
                mc.thePlayer.addChatMessage(new ChatComponentText("Trying to path to "+target.getGameProfile().getName()));
                mc.displayGuiScreen((GuiScreen)null);
            }
        }
    }
    
    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 40, 16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
        float scale = 93.75F;
        GL11.glScalef(-scale, -scale, -scale);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

        GL11.glPopMatrix();
        super.drawScreen(var1, var2, var3);
    }

}
