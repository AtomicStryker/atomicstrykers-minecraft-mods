package atomicstryker.petbat.client;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.BatNamePacket;

public class GuiPetBatRename extends GuiScreen
{
    private final String screenTitle;
    private final ItemStack petBatItemStack;
    private GuiTextField textfield;

    private final int xp;
    private final int xpToNext;
    private final int level;
    private final double maxHealth;
    private final double health;
    private final int attackStrength;
    private final String levelTitle;
    private final String levelDesc;

    public GuiPetBatRename(ItemStack stack)
    {
        petBatItemStack = stack;
        screenTitle = StatCollector.translateToLocal("translation.PetBat:gui_title");

        xp = stack.getTagCompound() != null ? stack.getTagCompound().getCompoundTag("petbatmod").getInteger("BatXP") : 0;
        xpToNext = PetBatMod.instance().getMissingExperienceToNextLevel(xp);
        level = PetBatMod.instance().getLevelFromExperience(xp);
        maxHealth = 16d + (level * 2);
        health = stack.getTagCompound() != null ? stack.getTagCompound().getCompoundTag("petbatmod").getFloat("health") : 0;
        attackStrength = 1 + level;
        levelTitle = PetBatMod.instance().getLevelTitle(level);
        levelDesc = PetBatMod.instance().getLevelDescription(level);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        textfield = new GuiTextField(0, fontRendererObj, this.width / 2 - 75, 60, 150, 20);
        textfield.setTextColor(-1);
        textfield.setMaxStringLength(30);
        textfield.setFocused(true);
        textfield.setText(ItemPocketedPetBat.getBatNameFromItemStack(petBatItemStack));
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        if (textfield.textboxKeyTyped(par1, par2))
        {
            if (!textfield.getText().equals(""))
            {
                PetBatMod.instance().networkHelper.sendPacketToServer(new BatNamePacket(Minecraft.getMinecraft().thePlayer.getCommandSenderName(),
                        textfield.getText()));
            }
        }
        else
        {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        this.textfield.mouseClicked(par1, par2, par3);
    }

    @Override
    public void updateScreen()
    {
        textfield.updateCursorCounter();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();

        int x = this.width / 2;
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, x, 40, 0x0000AA);

        int y = 100;
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:level")
                + EnumChatFormatting.RESET + level + " " + levelTitle), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(
                fontRendererObj,
                (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:experience") + EnumChatFormatting.RESET + xp + (xpToNext == -1
                        ? "" : StatCollector.translateToLocal("translation.PetBat:missing_xp") + xpToNext)), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:health")
                + EnumChatFormatting.RESET + health + " / " + maxHealth), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(fontRendererObj, (EnumChatFormatting.BOLD + StatCollector.translateToLocal("translation.PetBat:attack_power")
                + EnumChatFormatting.RESET + attackStrength), x, y, 0xFFFFFF);

        y += 30;
        drawCenteredString(fontRendererObj, EnumChatFormatting.ITALIC + levelDesc, x, y, 0xC82536);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2), 0.0F, 50.0F);
        float var4 = 93.75F;
        GL11.glScalef(-var4, -var4, -var4);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glPopMatrix();
        textfield.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }

}
