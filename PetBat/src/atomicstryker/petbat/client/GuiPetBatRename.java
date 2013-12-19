package atomicstryker.petbat.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiPetBatRename extends GuiScreen
{
    private final String screenTitle = "Human-Bat Interface";
    private final ItemStack petBatItemStack;
    private GuiTextField textfield;
    
    private final int xp;
    private final int xpToNext;
    private final int level;
    private final int maxHealth;
    private final double health;
    private final int attackStrength;
    private final String levelTitle;
    private final String levelDesc;
    
    public GuiPetBatRename(ItemStack stack)
    {
        petBatItemStack = stack;
        
        xp = stack.stackTagCompound != null ? stack.stackTagCompound.getCompoundTag("petbatmod").getInteger("BatXP") : 0;
        xpToNext = PetBatMod.instance().getMissingExperienceToNextLevel(xp);
        level = PetBatMod.instance().getLevelFromExperience(xp);
        maxHealth = 16 + (level * 2);
        health = ItemPocketedPetBat.invertHealthValue(stack.getItemDamage(), maxHealth);
        attackStrength = 1 + level;
        levelTitle = PetBatMod.instance().getLevelTitle(level);
        levelDesc = PetBatMod.instance().getLevelDescription(level);
    }
    
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        textfield = new GuiTextField(fontRenderer, this.width / 2 - 75, 60, 150, 20);
        textfield.setTextColor(-1);
        textfield.setMaxStringLength(30);
        textfield.setFocused(true);
        textfield.setText(ItemPocketedPetBat.getBatNameFromItemStack(petBatItemStack));
    }
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
    
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2)
    {
        if (textfield.textboxKeyTyped(par1, par2))
        {
            if (!textfield.getText().equals(""))
            {
                Object[] toSend = {textfield.getText()};
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("PetBat", 1, toSend));   
            }
        }
        else
        {
            super.keyTyped(par1, par2);
        }
    }
    
    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);
        this.textfield.mouseClicked(par1, par2, par3);
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen()
    {
        textfield.updateCursorCounter();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        
        int x = this.width / 2;        
        this.drawCenteredString(this.fontRenderer, this.screenTitle, x, 40, 0x0000AA);
        
        int y = 100;
        drawCenteredString(fontRenderer, (EnumChatFormatting.BOLD+"Level "+EnumChatFormatting.RESET+this.level+" "+this.levelTitle), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(fontRenderer, (EnumChatFormatting.BOLD+"Experience: "+EnumChatFormatting.RESET+this.xp+(xpToNext == -1 ? "" : ", missing for next level: "+xpToNext)), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(fontRenderer, (EnumChatFormatting.BOLD+"Health: "+EnumChatFormatting.RESET+this.health+" / "+this.maxHealth), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(fontRenderer, (EnumChatFormatting.BOLD+"Attack Power: "+EnumChatFormatting.RESET+this.attackStrength), x, y, 0xFFFFFF);
        
        y += 30;
        drawCenteredString(fontRenderer, EnumChatFormatting.ITALIC+levelDesc, x, y, 0xC82536);
        
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
        float var4 = 93.75F;
        GL11.glScalef(-var4, -var4, -var4);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glPopMatrix();
        textfield.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }
    
}
