package atomicstryker.petbat.client;

import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.BatNamePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

public class GuiPetBatRename extends Screen {
    private final ItemStack petBatItemStack;
    private final int xp;
    private final int xpToNext;
    private final int level;
    private final double maxHealth;
    private final double health;
    private final int attackStrength;
    private final String levelTitle;
    private final String levelDesc;
    private TextFieldWidget textfield;

    public GuiPetBatRename(ItemStack stack) {
        super(new TranslationTextComponent("translation.PetBat:gui_title"));

        petBatItemStack = stack;
        xp = stack.getTag() != null ? stack.getOrCreateChildTag("petbatmod").getInt("BatXP") : 0;
        xpToNext = PetBatMod.instance().getMissingExperienceToNextLevel(xp);
        level = PetBatMod.instance().getLevelFromExperience(xp);
        maxHealth = 16d + (level * 2);
        health = stack.getTag() != null ? stack.getOrCreateChildTag("petbatmod").getFloat("health") : 0;
        attackStrength = 1 + level;
        levelTitle = PetBatMod.instance().getLevelTitle(level);
        levelDesc = PetBatMod.instance().getLevelDescription(level);
    }

    @Override
    public void init() {
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        textfield = new TextFieldWidget(this.minecraft.fontRenderer, this.width / 2 - 75, 60, 150, 20, "Name");
        textfield.setTextColor(-1);
        textfield.setMaxStringLength(30);
        setFocused(textfield);
        textfield.setText(petBatItemStack.getDisplayName().getUnformattedComponentText());
    }

    @Override
    public void removed() {
        super.removed();
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        PetBatMod.LOGGER.debug("rename screen key pressed: {}", p_keyPressed_1_);
        if (!textfield.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            PetBatMod.LOGGER.debug("texfield if logic, textfield text: {}", textfield.getText());
            // escape, close screen
            if (p_keyPressed_1_ == 256) {
                onClose();
                return true;
            }
            // enter was pressed!
            if (p_keyPressed_1_ == 257) {
                if (!textfield.getText().equals("")) {
                    PetBatMod.instance().networkHelper.sendPacketToServer(new BatNamePacket(Minecraft.getInstance().player.getName().getString(),
                            textfield.getText()));
                }
                onClose();
                return true;
            }
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean mouseClicked(double par1, double par2, int par3) {
        if (super.mouseClicked(par1, par2, par3)) {
            return true;
        }
        return this.textfield.mouseClicked(par1, par2, par3);
    }

    @Override
    public void tick() {
        textfield.tick();
    }

    @Override
    public void render(int par1, int par2, float par3) {
        this.renderBackground();

        int x = this.width / 2;
        this.drawCenteredString(this.font, this.title.getFormattedText(), x, 40, 0x0000AA);

        int y = 100;
        drawCenteredString(font, (TextFormatting.BOLD + I18n.format("translation.PetBat:level")
                + TextFormatting.RESET + " " + level + " " + levelTitle), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(
                font,
                (TextFormatting.BOLD + I18n.format("translation.PetBat:experience") + TextFormatting.RESET + " " + xp + (xpToNext == -1
                        ? "" : I18n.format("translation.PetBat:missing_xp") + " " + xpToNext)), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(font, (TextFormatting.BOLD + I18n.format("translation.PetBat:health")
                + TextFormatting.RESET + " " + health + " / " + maxHealth), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(font, (TextFormatting.BOLD + I18n.format("translation.PetBat:attack_power")
                + TextFormatting.RESET + " " + attackStrength), x, y, 0xFFFFFF);

        y += 30;
        drawCenteredString(font, TextFormatting.ITALIC + levelDesc, x, y, 0xC82536);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2), 0.0F, 50.0F);
        float var4 = 93.75F;
        GL11.glScalef(-var4, -var4, -var4);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glPopMatrix();
        textfield.render(par1, par2, par3);
        super.render(par1, par2, par3);
    }

}
