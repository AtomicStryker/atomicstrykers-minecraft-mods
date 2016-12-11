package atomicstryker.minions.client.gui;

import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.common.EvilDeed;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.EvilDeedPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * Evil Deed selection menu
 * 
 * 
 * @author AtomicStryker
 */

public class GuiDeedMenu extends GuiScreen
{
    private String screenTitle;
    private ArrayList<EvilDeed> deedButtons;

    @SuppressWarnings("unchecked")
    @Override
    public void initGui()
    {
        screenTitle = I18n.format("minionsgui.chooseYourPart");
        buttonList.clear();

        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 120, I18n.format("minionsgui.chickenout")));

        ArrayList<EvilDeed> copy = (ArrayList<EvilDeed>) MinionsCore.instance.evilDoings.clone();
        deedButtons = new ArrayList<EvilDeed>();
        Random rand = new Random();
        while (deedButtons.size() < 3)
        {
            int i = rand.nextInt(copy.size() - 1);
            deedButtons.add(copy.get(i));
            copy.remove(i);
        }

        buttonList.clear();

        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 120, I18n.format("minionsgui.nevermind")));

        for (int x = 0; x < 3; x++)
        {
            EvilDeed deed = deedButtons.get(x);
            buttonList.add(new GuiButton(x + 1, width / 2 - 100, height / 4 + x * 40, I18n.format(deed.getButtonText())));
        }
    }

    @Override
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
            mc.displayGuiScreen(null);
            if (var1.id != 0)
            {
                EvilDeed deed = deedButtons.get(var1.id - 1);
                MinionsCore.instance.networkHelper.sendPacketToServer(new EvilDeedPacket(mc.player.getName(), deed.getSoundFile(), deed.getSoundLength()));
            }
        }
    }

    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, screenTitle, width / 2, 40, 16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (width / 2), 0.0F, 50.0F);
        GL11.glScalef(-93.75F, -93.75F, -93.75F);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

        GL11.glPopMatrix();
        super.drawScreen(var1, var2, var3);
    }
}
