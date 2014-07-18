package atomicstryker.ic2.advancedmachines.client;

import ic2.core.block.machine.ContainerOreWashing;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltiphelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class GuiRotaryOreWasher extends GuiContainer
{

    private static ResourceLocation tex = new ResourceLocation("advancedmachines", "textures/gui/GUIOreWashingPlant.png");

    private ContainerOreWashing container;

    public GuiRotaryOreWasher(ContainerOreWashing containerOreWashing, TileEntityOreWashing te)
    {
        super(containerOreWashing);
        container = containerOreWashing;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRendererObj.drawString(StatCollector.translateToLocal("item.advancedmachines:rotaryOreWasher.name"), 8, 6, 4210752);

        FluidStack fluidstack = ((TileEntityOreWashing) this.container.base).getFluidStackfromTank();
        if (fluidstack != null)
        {
            String tooltip = fluidstack.getFluid().getName() + ": " + fluidstack.amount + StatCollector.translateToLocal("ic2.generic.text.mb");
            GuiTooltiphelper.drawAreaTooltip(par1 - this.guiLeft, par2 - this.guiTop, tooltip, 63, 23, 76, 71);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(tex);
        int xOffset = (this.width - this.xSize) / 2;
        int yOffset = (this.height - this.ySize) / 2;
        drawTexturedModalRect(xOffset, yOffset, 0, 0, this.xSize, this.ySize);

        int chargeLevel = (int) (14.0F * ((TileEntityOreWashing) this.container.base).getChargeLevel());
        int progress = (int) (20.0F * ((TileEntityOreWashing) this.container.base).getProgress());

        if (chargeLevel > 0)
        {
            drawTexturedModalRect(xOffset + 9, yOffset + 57 - chargeLevel, 176, 14 - chargeLevel, 14, chargeLevel);
        }
        if (progress > 0)
        {
            drawTexturedModalRect(xOffset + 102, yOffset + 38, 176, 117, progress + 1, 20);
        }
        TileEntityOreWashing te = (TileEntityOreWashing) this.container.base;

        if (te.getTankAmount() > 0)
        {
            IIcon fluidIcon = te.getFluidTank().getFluid().getFluid().getIcon();

            if (fluidIcon != null)
            {
                drawTexturedModalRect(xOffset + 60, yOffset + 20, 176, 15, 20, 55);

                this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                int liquidHeight = te.gaugeLiquidScaled(47);
                DrawUtil.drawRepeated(fluidIcon, xOffset + 64, yOffset + 24 + 47 - liquidHeight, 12.0D, liquidHeight, this.zLevel);

                this.mc.renderEngine.bindTexture(tex);
                drawTexturedModalRect(xOffset + 64, yOffset + 24, 176, 70, 12, 47);
            }
        }
    }
}
