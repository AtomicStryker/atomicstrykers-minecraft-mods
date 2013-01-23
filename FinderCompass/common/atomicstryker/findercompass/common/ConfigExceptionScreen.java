package atomicstryker.findercompass.common;

import net.minecraft.client.gui.GuiErrorScreen;

public class ConfigExceptionScreen extends GuiErrorScreen
{
    private final String upper;
    private final String lower;
    
    public ConfigExceptionScreen(String lineA, String lineB)
    {
        super();
        upper = lineA;
        lower = lineB;
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawGradientRect(0, 0, this.width, this.height, -12574688, -11530224);
        this.drawCenteredString(this.fontRenderer, upper, this.width / 2, 90, 16777215);
        this.drawCenteredString(this.fontRenderer, lower, this.width / 2, 110, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
