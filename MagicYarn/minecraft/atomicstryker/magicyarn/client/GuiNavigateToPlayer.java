package atomicstryker.magicyarn.client;

import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class GuiNavigateToPlayer extends GuiScreen
{
    
    private final String screenTitle = "Try navigating to player:";
    private Object[] playerList;
    
    @Override
    public void initGui()
    {
        controlList.clear();
        controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Cancel"));
        
        playerList = FMLClientHandler.instance().getClient().theWorld.playerEntities.toArray();
        EntityPlayer c = FMLClientHandler.instance().getClient().thePlayer;
        EntityPlayer p;
        for (int x = 0; x < playerList.length ; x++)
        {
            p = (EntityPlayer) playerList[x];
            if (!p.username.equals(c.username))
            {
                controlList.add(new GuiButton(x+1, this.width / 2 - 100, this.height / 4 + x*40, p.username));
            }
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
            else
            {
                MagicYarnClient.instance.tryPathToPlayer((EntityPlayer) playerList[button.id]);
            }
        }
    }
    
    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 40, 16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
        float scale = 93.75F;
        GL11.glScalef(-scale, -scale, -scale);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

        GL11.glPopMatrix();
        super.drawScreen(var1, var2, var3);
    }

}
