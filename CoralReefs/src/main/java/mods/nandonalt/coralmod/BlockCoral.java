package mods.nandonalt.coralmod;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCoral extends Block {

	private final int type;
	private static boolean setTab = false;
	private boolean stacked = false;
	private int bubblesCooldown = -1;

	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	public static final String[] types = new String[] {
		"coral1", "coral2", "coral3",
		"coral4", "coral5", "coral6"
	};

	public BlockCoral(int type) {
		super(Material.water);
		this.type = type;

		if(type == 1) {
			float f = 0.375F;
			setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
		} else if(type == 6) {
			float f = 0.5F;
			setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
		}

		if(!setTab) {
			setCreativeTab(CreativeTabs.tabDecorations);
			setTab = true;
		}

		setTickRandomly(true);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if(CoralMod.getGrow()) {
			super.updateTick(world, x, y, z, random);
			int metadata = world.getBlockMetadata(x, y, z);
			if(metadata == 1 || metadata == 4) {
				int offset = 1;
				while(world.getBlock(x, y - offset, z) == this)
					offset++;

				int rand = random.nextInt(100);
				if(rand == 0 && CoralMod.checkWater(world, x, y + 1, z)
				&& CoralMod.checkWater(world, x, y + 2, z) && offset < 4) {
					world.setBlock(x, y + 1, z, this, metadata, 3);
				}
			}
		}

	}

	@Override
	public int damageDropped(int metadata) {
		return metadata;
	}

	@Override
	public int quantityDropped(Random random) {
		return 1;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		Block currentBlock = world.getBlock(x, y, z);

		boolean isWaterBlock = CoralMod.checkWater(world, x, y, z);

		if(currentBlock != Blocks.air && !isWaterBlock) {
			return false;
		}

		if(isWaterBlock && !CoralMod.checkWater(world, x, y + 1, z)) {
			return false;
		}

		if (world.getBlock(x, y - 1, z) == CoralMod.Coral1 && world.getBlockMetadata(x, y - 1, z) == 1) {
			if(this == CoralMod.Coral1 && type == 1) {
				stacked = true;
				return true;
			}
		}

		return canBlockStay(world, x, y, z);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return type;
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		Block belowBlockId = world.getBlock(x, y - 1, z);
		int belowBlockMeta = world.getBlockMetadata(x, y - 1, z);
		int currentBlockMeta = world.getBlockMetadata(x, y, z);

		if (currentBlockMeta == 1) {
			if ((belowBlockId == CoralMod.Coral1 && belowBlockMeta == 1)
			|| belowBlockId == CoralMod.Coral2 || belowBlockId == CoralMod.Coral3) {
				return true;
			}

		}

		if ((currentBlockMeta == 4) && (((belowBlockId == CoralMod.Coral1) && (belowBlockMeta == 4))
		|| (belowBlockId == CoralMod.Coral2) || (belowBlockId == CoralMod.Coral3))) {
			return true;
		}

		if (CoralMod.checkWater(world, x, y + 1, z, false) && ((belowBlockId == CoralMod.Coral2) || (belowBlockId == CoralMod.Coral3))) {
			return true;
		}

		return (CoralMod.checkWater(world, x, y + 1, z, true) && ((belowBlockId == CoralMod.Coral2) || (belowBlockId == CoralMod.Coral3)));
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlockID) {
		if(!canBlockStay(world, x, y, z)) {
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockToAir(x, y, z);
		}

	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		int metadata = world.getBlockMetadata(x, y, z);
		if(!(entity instanceof EntityWaterMob) && metadata == 4) {
			entity.attackEntityFrom(DamageSource.cactus, 2);
		}

	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if(stacked && world.getBlock(x, y - 1, z) == CoralMod.Coral1) {
			int meta = world.getBlockMetadata(x, y, z);
			if(meta != 1) {
				dropBlockAsItem(world, x, y, z, meta, 0);
				world.setBlockToAir(x, y, z);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[types.length];

		for (int i = 0; i < types.length; i++) {
			iconBuffer[i] = iconRegister.registerIcon("coralmod:" + types[i]);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		final IIcon sprite;
		if(metadata < 0 || metadata >= iconBuffer.length) {
			sprite = iconBuffer[0];
		} else {
			sprite = iconBuffer[metadata];
		}

		return sprite;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(CoralMod.Coral1, 1, 0));
		par3List.add(new ItemStack(CoralMod.Coral1, 1, 1));
		par3List.add(new ItemStack(CoralMod.Coral1, 1, 2));
		par3List.add(new ItemStack(CoralMod.Coral4, 1, 3));
		par3List.add(new ItemStack(CoralMod.Coral5, 1, 4));
		par3List.add(new ItemStack(CoralMod.Coral5, 1, 5));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
	    if(CoralMod.getBubble() && world.getBlock(x, y + 1, z).getMaterial() == Material.water) {
	        Block block = world.getBlock(x, y + 1, z);
            String blockName = block.getLocalizedName();
            if(blockName.startsWith("tile.Coral") && blockName.length() == 11) {
                spawnBubbles(world, x, y, z);
            } else if(blockName.equals("tile.water")) {
                spawnBubbles(world, x, y, z);
            } else {
                return;
            }
	    }
	}

	private void spawnBubbles(World world, int x, int y, int z) {
		Random rand = world.rand;
		double d1 = 0.0625D;

		bubblesCooldown++;

		if(bubblesCooldown >= 3) {
			bubblesCooldown = 0;
		}

		if(bubblesCooldown != 0) {
			return;
		}

		for(int i = 0; i < 6; i++) {
			double d2 = (double)((float)x + rand.nextFloat());
			double d3 = (double)((float)y + rand.nextFloat());
			double d4 = (double)((float)z + rand.nextFloat());
			if(i == 0 && !world.getBlock(x, y + 1, z).isOpaqueCube()) {
				d3 = (double)(y + 1) + d1;
			}

			if(i == 1 && !world.getBlock(x, y - 1, z).isOpaqueCube()) {
				d3 = (double)(y + 0) - d1;
			}

			if(i == 2 && !world.getBlock(x, y, z + 1).isOpaqueCube()) {
				d4 = (double)(z + 1) + d1;
			}

			if(i == 3 && !world.getBlock(x, y, z - 1).isOpaqueCube()) {
				d4 = (double)(z + 0) - d1;
			}

			if(i == 4 && !world.getBlock(x + 1, y, z).isOpaqueCube()) {
				d2 = (double)(x + 1) + d1;
			}

			if(i == 5 && !world.getBlock(x - 1, y, z).isOpaqueCube()) {
				d2 = (double)(x + 0) - d1;
			}

			if(d2 < (double)x || d2 > (double)(x + 1) || d3 < 0.0D || d3 > (double)(y + 1) || d4 < (double)z || d4 > (double)(z + 1)) {
				world.spawnParticle("bubble", d2, d3, d4, 0.0D, 0.0D, 0.0D);
			}
		}
	}

}
