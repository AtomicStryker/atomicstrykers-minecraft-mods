package atomicstryker.magicyarn.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

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
        field_146292_n.clear();
        field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + 120, "Cancel"));
        
        if (playerList.length > 4)
        {
            field_146292_n.add(new GuiButton(1, this.field_146294_l / 10 * 8, this.field_146295_m / 4 + 75, 50, 20, "->"));
        }
        
        int targetListSize = field_146292_n.size()+BUTTONS_WANTED;
        int i = 0;
        
        EntityPlayer c = FMLClientHandler.instance().getClient().thePlayer;
        EntityPlayer p;
        for (int x = nextStartIndex; x < playerList.length && field_146292_n.size() != targetListSize ; x++)
        {
            if (nextStartIndex >= playerList.length)
            {
                nextStartIndex = 0;
                x = 0;
            }
            
            p = (EntityPlayer) playerList[x];
            if (!p.func_146103_bH().getName().equals(c.func_146103_bH().getName()))
            {
                field_146292_n.add(new GuiButton(x+BUTTON_TO_PLAYER_ID_OFFSET, this.field_146294_l / 2 - 100, this.field_146295_m / 4 + i++*40, p.func_146103_bH().getName()));
            }
            nextStartIndex = x;
        }
    }
    
    @Override
    protected void func_146284_a(GuiButton button)
    {
        if (button.field_146124_l)
        {
            if (button.field_146127_k == 0)
            {
                field_146297_k.func_147108_a((GuiScreen)null);
            }
            else if (button.field_146127_k == 1)
            {
                nextStartIndex += BUTTONS_WANTED;
                generateButtons();
            }
            else
            {
                EntityPlayer target = (EntityPlayer) playerList[button.field_146127_k-BUTTON_TO_PLAYER_ID_OFFSET];
                MagicYarnClient.instance.tryPathToPlayer(target);
                field_146297_k.thePlayer.func_145747_a(new ChatComponentText("Trying to path to "+target.func_146103_bH().getName()));
                field_146297_k.func_147108_a((GuiScreen)null);
            }
        }
    }
    
    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
        this.func_146276_q_();
        this.drawCenteredString(this.field_146289_q, this.screenTitle, this.field_146294_l / 2, 40, 16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.field_146294_l / 2), 0.0F, 50.0F);
        float scale = 93.75F;
        GL11.glScalef(-scale, -scale, -scale);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

        GL11.glPopMatrix();
        super.drawScreen(var1, var2, var3);
    }

}
