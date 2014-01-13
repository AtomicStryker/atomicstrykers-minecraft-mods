package atomicstryker.petbat.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.ForgePacketWrapper;
import atomicstryker.petbat.common.network.PacketDispatcher;

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
        health = stack.stackTagCompound.getCompoundTag("petbatmod").getFloat("health");
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
        textfield = new GuiTextField(field_146289_q, this.field_146294_l / 2 - 75, 60, 150, 20);
        textfield.func_146193_g(-1);
        textfield.func_146203_f(30);
        textfield.func_146195_b(true);
        textfield.func_146180_a(ItemPocketedPetBat.getBatNameFromItemStack(petBatItemStack));
    }
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void func_146281_b()
    {
        super.func_146281_b();
        Keyboard.enableRepeatEvents(false);
    }
    
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2)
    {
        if (textfield.func_146201_a(par1, par2))
        {
            if (!textfield.func_146179_b().equals(""))
            {
                Object[] toSend = {textfield.func_146179_b()};
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
        this.textfield.func_146192_a(par1, par2, par3);
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen()
    {
        textfield.func_146178_a();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.func_146276_q_();
        
        int x = this.field_146294_l / 2;        
        this.drawCenteredString(this.field_146289_q, this.screenTitle, x, 40, 0x0000AA);
        
        int y = 100;
        drawCenteredString(field_146289_q, (EnumChatFormatting.BOLD+"Level "+EnumChatFormatting.RESET+this.level+" "+this.levelTitle), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(field_146289_q, (EnumChatFormatting.BOLD+"Experience: "+EnumChatFormatting.RESET+this.xp+(xpToNext == -1 ? "" : ", missing for next level: "+xpToNext)), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(field_146289_q, (EnumChatFormatting.BOLD+"Health: "+EnumChatFormatting.RESET+this.health+" / "+this.maxHealth), x, y, 0xFFFFFF);
        y += 12;
        drawCenteredString(field_146289_q, (EnumChatFormatting.BOLD+"Attack Power: "+EnumChatFormatting.RESET+this.attackStrength), x, y, 0xFFFFFF);
        
        y += 30;
        drawCenteredString(field_146289_q, EnumChatFormatting.ITALIC+levelDesc, x, y, 0xC82536);
        
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.field_146294_l / 2), 0.0F, 50.0F);
        float var4 = 93.75F;
        GL11.glScalef(-var4, -var4, -var4);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glPopMatrix();
        textfield.func_146194_f();
        super.drawScreen(par1, par2, par3);
    }
    
}
