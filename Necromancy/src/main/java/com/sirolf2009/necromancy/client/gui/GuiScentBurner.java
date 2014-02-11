package com.sirolf2009.necromancy.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.inventory.ContainerScentBurner;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;
import com.sirolf2009.necromancy.tileentity.TileEntityScentBurner;

public class GuiScentBurner extends GuiContainer
{

    public TileEntityScentBurner burner;

    public GuiScentBurner(InventoryPlayer par1InventoryPlayer, TileEntityScentBurner par2TileEntityScentBurner)
    {
        super(new ContainerScentBurner(par1InventoryPlayer, par2TileEntityScentBurner));
        burner = par2TileEntityScentBurner;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRendererObj.drawString("String", 88, 72, 1);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Necromancy.proxy.bindTexture(ReferenceNecromancy.TEXTURES_GUI_SCENTBURNER);
        int var5 = (width - xSize) / 2;
        int var6 = (height - ySize) / 2;
        drawTexturedModalRect(var5, var6, 0, 0, xSize, ySize);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        int i1;

        if (this.burner.isBurning)
        {
            i1 = 200;
            this.drawTexturedModalRect(k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
        }
    }
}
