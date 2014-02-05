package mods.nandonalt.coralmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCoral2 extends Block {

	private final int type;

	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	public BlockCoral2(int type) {
		super(Material.rock);
		setCreativeTab(CreativeTabs.tabDecorations);
		this.type = type;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		Block currentBlock = world.getBlock(x, y, z);
		String blockName = currentBlock.getLocalizedName();
        if(blockName.startsWith("tile.Coral") && blockName.length() == 11) {
            return false;
        }
		return super.canPlaceBlockAt(world, x, y, z);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[2];
		iconBuffer[0] = iconRegister.registerIcon("coralmod:reef1");
		iconBuffer[1] = iconRegister.registerIcon("coralmod:reef2");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		final IIcon sprite;
		if(type < 0 || type > iconBuffer.length) {
			sprite = iconBuffer[0];
		} else {
			sprite = iconBuffer[type];
		}

		return sprite;
	}

}
