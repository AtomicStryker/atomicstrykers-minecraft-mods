package ic2.advancedmachines.client;

import ic2.advancedmachines.common.AdvancedMachines;
import ic2.advancedmachines.common.ContainerSingularityCompressor;
import ic2.advancedmachines.common.TileEntitySingularityCompressor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiSingularityCompressor extends GuiContainer
{
    public TileEntitySingularityCompressor tileentity;
    private static ResourceLocation tex = new ResourceLocation("advancedmachines", "/textures/gui/GUISingularity.png");

    public GuiSingularityCompressor(InventoryPlayer var1, TileEntitySingularityCompressor var2)
    {
        super(new ContainerSingularityCompressor(var1, var2));
        this.tileentity = var2;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(AdvancedMachines.advCompName, 32, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString("Pressure:", 6, 36, 4210752);
        this.fontRenderer.drawString(this.tileentity.printFormattedData(), 10, 44, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
    {
        this.mc.renderEngine.func_110577_a(tex);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
        int var7;
        if (this.tileentity.energy > 0)
        {
            var7 = this.tileentity.gaugeFuelScaled(14);
            this.drawTexturedModalRect(var5 + 56, var6 + 36 + 14 - var7, 176, 14 - var7, 14, var7);
        }

        var7 = this.tileentity.gaugeProgressScaled(24);
        this.drawTexturedModalRect(var5 + 79, var6 + 34, 176, 14, var7 + 1, 16);
    }
}
