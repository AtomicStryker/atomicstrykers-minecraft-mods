package mods.nandonalt.coralmod.client;

import mods.nandonalt.coralmod.CoralMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

public class GuiCoralReef extends GuiScreen {

	private static final String[] SIZES = new String[] {"Small", "Normal", "Big"};

	/**
	 * Gets the description for button with specified index
	 */
	private String getDesc(int index) {
		final String desc;
		if(index >= 0 && index < CoralMod.DESCRIPTIONS.length) {
			desc = CoralMod.DESCRIPTIONS[index];
		} else {
			desc = "Unimplemented";
		}
		return desc + ": " + getState(index);
	}

	private String getField(int index) {
		final String field;
		if(index >= 0 && index < CoralMod.getAllowedFields().size()) {
			field = CoralMod.getAllowedFields().get(index);
		} else {
			field = "enable"; // default
		}
		return field;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
    @Override
	public void initGui() {
		int i = 0;

		for(i = 0; i < CoralMod.getAllowedFields().size(); i++) {
			buttonList.add(new GuiButton(i, width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), getDesc(i)));
		}

		if(inGame()) {
			buttonList.add(new GuiButton(i, width / 2 - 155 + 8 % 2 * 160 + 80, height / 6 + 24 * (8 >> 1), "Back to Game"));
		} else {
			buttonList.add(new GuiButton(i, width / 2 - 100, height / 6 + 24 * (8 >> 1), "Done"));
		}
	}

	/**
	 * Gets the current state of the button's setting
	 */
	private String getState(int index) {
		String field = getField(index);
		String state = CoralMod.getValue(field);
		try {
			int size = Integer.parseInt(state);
			if(size >= 0 && size < SIZES.length) {
				return SIZES[size];
			} else {
				System.err.println(field + " has an unsupported value");
				return "???";
			}
		} catch (NumberFormatException nfe) {
			Boolean bool = Boolean.parseBoolean(state);
			if(bool) {
				return "ON";
			} else {
				return "OFF";
			}
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char character, int key) {
		if (key == Keyboard.KEY_ESCAPE) {
			CoralMod.instance.updateSettings();
		}
		super.keyTyped(character, key);
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id < CoralMod.getAllowedFields().size()) {
			CoralMod.toggle(getField(par1GuiButton.id));
			par1GuiButton.displayString = getDesc(par1GuiButton.id);
		} else {
			if(inGame()) {
				mc.displayGuiScreen((GuiScreen)null);
				mc.setIngameFocus();
			} else {
				this.mc.displayGuiScreen(new GuiOptions(null, this.mc.gameSettings));
			}
			CoralMod.instance.updateSettings();
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		String status = "(Enabled)";

		if(!inGame()) {
			status = "(Options)";
		} else if(!mc.isSingleplayer()) {
			status = "(Disabled)";
		}

		drawCenteredString(fontRendererObj, "CoralReef Mod " + status, width / 2, 20, 16777215);

		super.drawScreen(par1, par2, par3);
	}

	private boolean inGame() {
		return mc.theWorld != null;
	}

}
