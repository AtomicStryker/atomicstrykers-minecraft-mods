package ic2.advancedmachines.client;

import ic2.advancedmachines.common.ContainerRotaryMacerator;
import ic2.advancedmachines.common.TileEntityRotaryMacerator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class GuiRotaryMacerator extends GuiContainer
{
    public TileEntityRotaryMacerator tileentity;
    private static ResourceLocation tex = new ResourceLocation("advancedmachines", "/textures/gui/GUIRotary.png");

    public GuiRotaryMacerator(InventoryPlayer var1, TileEntityRotaryMacerator var2)
    {
        super(new ContainerRotaryMacerator(var1, var2));
        this.tileentity = var2;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(StatCollector.translateToLocal("item.advancedmachines:rotaryMacerator.name"), 49, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString("Speed:", 10, 36, 4210752);
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
        this.drawTexturedModalRect(var5 + 75, var6 + 34, 176, 14, var7 + 1, 16);
    }
}
