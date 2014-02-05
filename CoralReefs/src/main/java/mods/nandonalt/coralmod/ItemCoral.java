package mods.nandonalt.coralmod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCoral extends ItemBlock {

	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	private static final String[] types = BlockCoral.types;

	public ItemCoral(Block b) {
		super(b);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int itemDamage) {
		return itemDamage;
	}

	// Forces item textures to be used
	@Override
	public int getSpriteNumber() {
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[types.length];

		for (int i = 0; i < types.length; i++) {
			iconBuffer[i] = iconRegister.registerIcon("coralmod:" + types[i]);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damageValue) {
		final IIcon sprite;
		if(damageValue < 0 || damageValue > iconBuffer.length) {
			sprite = iconBuffer[0];
		} else {
			sprite = iconBuffer[damageValue];
		}

		return sprite;
	}

}
