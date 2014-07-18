package atomicstryker.ic2.advancedmachines;

import ic2.api.item.IC2Items;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import atomicstryker.ic2.advancedmachines.client.AdvancedMachinesClient;

public class BlockAdvancedMachines extends BlockContainer
{

    private IIcon[][] iconBuffer;

    public BlockAdvancedMachines()
    {
        super(Material.iron);
        this.setHardness(2.0F);
        this.setStepSound(soundTypeAnvil);
    }

    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        iconBuffer = new IIcon[5][12]; // 5 machines, 6 sides each, in ON and OFF states

        // meta 0, macerator
        // first the 6 sides in OFF state
        iconBuffer[0][0] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[0][1] = par1IconRegister.registerIcon("advancedmachines:topMaceratorOFF"); // top
        iconBuffer[0][2] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // north
        iconBuffer[0][3] = par1IconRegister.registerIcon("advancedmachines:frontMaceratorOFF"); // east
        iconBuffer[0][4] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // south
        iconBuffer[0][5] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // west
        // then the 6 sides in ON state
        iconBuffer[0][6] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[0][7] = par1IconRegister.registerIcon("advancedmachines:topMaceratorON"); // top
        iconBuffer[0][8] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // north
        iconBuffer[0][9] = par1IconRegister.registerIcon("advancedmachines:frontMaceratorON"); // east
        iconBuffer[0][10] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // south
        iconBuffer[0][11] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // west

        // meta 1, compressor
        // first the 6 sides in OFF state
        iconBuffer[1][0] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[1][1] = par1IconRegister.registerIcon("advancedmachines:topCompressorOFF"); // top
        iconBuffer[1][2] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // north
        iconBuffer[1][3] = par1IconRegister.registerIcon("advancedmachines:frontCompressorOFF"); // east
        iconBuffer[1][4] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // south
        iconBuffer[1][5] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // west
        // then the 6 sides in ON state
        iconBuffer[1][6] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[1][7] = par1IconRegister.registerIcon("advancedmachines:topCompressorON"); // top
        iconBuffer[1][8] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // north
        iconBuffer[1][9] = par1IconRegister.registerIcon("advancedmachines:frontCompressorON"); // east
        iconBuffer[1][10] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // south
        iconBuffer[1][11] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // west

        // meta 2, extractor
        // first the 6 sides in OFF state
        iconBuffer[2][0] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[2][1] = par1IconRegister.registerIcon("advancedmachines:topExtractorOFF"); // top
        iconBuffer[2][2] = par1IconRegister.registerIcon("advancedmachines:sideExtractorOFF"); // north
        iconBuffer[2][3] = par1IconRegister.registerIcon("advancedmachines:frontExtractorOFF"); // east
        iconBuffer[2][4] = par1IconRegister.registerIcon("advancedmachines:sideExtractorOFF"); // south
        iconBuffer[2][5] = par1IconRegister.registerIcon("advancedmachines:sideExtractorOFF"); // west
        // then the 6 sides in ON state
        iconBuffer[2][6] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[2][7] = par1IconRegister.registerIcon("advancedmachines:topExtractorON"); // top
        iconBuffer[2][8] = par1IconRegister.registerIcon("advancedmachines:sideExtractorON"); // north
        iconBuffer[2][9] = par1IconRegister.registerIcon("advancedmachines:frontExtractorON"); // east
        iconBuffer[2][10] = par1IconRegister.registerIcon("advancedmachines:sideExtractorON"); // south
        iconBuffer[2][11] = par1IconRegister.registerIcon("advancedmachines:sideExtractorON"); // west
        
        // meta 3, recycler
        // first the 6 sides in OFF state
        iconBuffer[3][0] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[3][1] = par1IconRegister.registerIcon("advancedmachines:topMaceratorOFF"); // top
        iconBuffer[3][2] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // north
        iconBuffer[3][3] = par1IconRegister.registerIcon("advancedmachines:frontRecyclerOFF"); // east
        iconBuffer[3][4] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // south
        iconBuffer[3][5] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // west
        // then the 6 sides in ON state
        iconBuffer[3][6] = par1IconRegister.registerIcon("advancedmachines:bottom"); // bottom
        iconBuffer[3][7] = par1IconRegister.registerIcon("advancedmachines:topMaceratorON"); // top
        iconBuffer[3][8] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // north
        iconBuffer[3][9] = par1IconRegister.registerIcon("advancedmachines:frontRecyclerON"); // east
        iconBuffer[3][10] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // south
        iconBuffer[3][11] = par1IconRegister.registerIcon("advancedmachines:sideplate"); // west
        
        // meta 4, orewasher
        // first the 6 sides in OFF state
        iconBuffer[4][0] = par1IconRegister.registerIcon("advancedmachines:bottom32"); // bottom
        iconBuffer[4][1] = par1IconRegister.registerIcon("advancedmachines:topOreWasher"); // top
        iconBuffer[4][2] = par1IconRegister.registerIcon("advancedmachines:sideOreWasherOFF"); // north
        iconBuffer[4][3] = par1IconRegister.registerIcon("advancedmachines:frontOreWasherOFF"); // east
        iconBuffer[4][4] = par1IconRegister.registerIcon("advancedmachines:sideOreWasherOFF"); // south
        iconBuffer[4][5] = par1IconRegister.registerIcon("advancedmachines:back32"); // west
        // then the 6 sides in ON state
        iconBuffer[4][6] = par1IconRegister.registerIcon("advancedmachines:bottom32"); // bottom
        iconBuffer[4][7] = par1IconRegister.registerIcon("advancedmachines:topOreWasher"); // top
        iconBuffer[4][8] = par1IconRegister.registerIcon("advancedmachines:sideOreWasherOFF"); // north
        iconBuffer[4][9] = par1IconRegister.registerIcon("advancedmachines:frontOreWasherOFF"); // east
        iconBuffer[4][10] = par1IconRegister.registerIcon("advancedmachines:sideOreWasherOFF"); // south
        iconBuffer[4][11] = par1IconRegister.registerIcon("advancedmachines:back32"); // west
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int blockSide)
    {
        int blockMeta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        int facing = (te instanceof TileEntityBlock) ? ((int) (((TileEntityBlock) te).getFacing())) : 0;

        if (isActive(world, x, y, z))
            return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing] + 6];
        else
            return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing]];
    }

    @Override
    public IIcon getIcon(int blockSide, int blockMeta)
    {
        return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][3]];
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int meta)
    {
        switch (meta)
        {
        case 0:
            return new TileEntityAdvancedMacerator();
        case 1:
            return new TileEntityAdvancedCompressor();
        case 2:
            return new TileEntityAdvancedExtractor();
        case 3:
            return new TileEntityAdvancedRecycler();
        case 4:
            return new TileEntityAdvancedOreWasher();
        default:
            return null;
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> dropList = super.getDrops(world, x, y, z, metadata, fortune);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IInventory)
        {
            IInventory iinv = (IInventory) te;
            for (int index = 0; index < iinv.getSizeInventory(); ++index)
            {
                ItemStack itemstack = iinv.getStackInSlot(index);
                if (itemstack != null)
                {
                    dropList.add(itemstack);
                    iinv.setInventorySlotContents(index, (ItemStack) null);
                }
            }
        }

        return dropList;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block blockID, int blockMeta)
    {
        super.breakBlock(world, x, y, z, blockID, blockMeta);
        boolean var5 = true;
        for (Iterator<ItemStack> iter = getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0).iterator(); iter.hasNext(); var5 = false)
        {
            ItemStack var7 = (ItemStack) iter.next();
            if (!var5)
            {
                if (var7 == null)
                {
                    return;
                }

                double var8 = 0.7D;
                double var10 = (double) world.rand.nextFloat() * var8 + (1.0D - var8) * 0.5D;
                double var12 = (double) world.rand.nextFloat() * var8 + (1.0D - var8) * 0.5D;
                double var14 = (double) world.rand.nextFloat() * var8 + (1.0D - var8) * 0.5D;
                EntityItem var16 = new EntityItem(world, (double) x + var10, (double) y + var12, (double) z + var14, var7);
                var16.delayBeforeCanPickup = 10;
                world.spawnEntityInWorld(var16);
                return;
            }
        }
    }
    
    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return IC2Items.getItem("advancedMachine").getItem();
    }

    /**
     * Get the block's damage value (for use with pick block).
     */
    @Override
    public int getDamageValue(World world, int x, int y, int z)
    {
        return world.getBlockMetadata(x, y, z); // advanced machine item meta
                                                // exactly equals the block meta
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, player, stack);
        int heading = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntityBlock te = (TileEntityBlock) world.getTileEntity(x, y, z);
        switch (heading)
        {
        case 0:
            te.setFacing((short) 2);
            break;
        case 1:
            te.setFacing((short) 5);
            break;
        case 2:
            te.setFacing((short) 3);
            break;
        case 3:
            te.setFacing((short) 4);
            break;
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        if (!entityPlayer.isSneaking())
        {
            entityPlayer.openGui(ModAdvancedMachines.instance, 0, world, x, y, z);
            return true;
        }

        return false;
    }

    private boolean isActive(IBlockAccess iba, int x, int y, int z)
    {
        return ((TileEntityBlock) iba.getTileEntity(x, y, z)).getActive();
    }

    @Override
    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        if (axis == ForgeDirection.UNKNOWN)
        {
            return false;
        }
        TileEntity tileEntity = worldObj.getTileEntity(x, y, z);

        if ((tileEntity instanceof IWrenchable))
        {
            IWrenchable te = (IWrenchable) tileEntity;

            int newFacing = ForgeDirection.getOrientation(te.getFacing()).getRotation(axis).ordinal();

            if (te.wrenchCanSetFacing(null, newFacing))
            {
                te.setFacing((short) newFacing);
            }
        }

        return false;
    }

    @Override
    public void randomDisplayTick(World world, int i, int j, int k, Random random)
    {
        if (!IC2.platform.isRendering())
        {
            return;
        }
        int meta = world.getBlockMetadata(i, j, k);

        if ((meta == 1) && (isActive(world, i, j, k)))
        {
            TileEntity te = world.getTileEntity(i, j, k);
            int facing = (te instanceof TileEntityBlock) ? ((TileEntityBlock) te).getFacing() : 0;

            float f = i + 0.5F;
            float f1 = j + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float f2 = k + 0.5F;
            float f3 = 0.52F;
            float f4 = random.nextFloat() * 0.6F - 0.3F;

            switch (facing)
            {
            case 4:
                world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                break;
            case 5:
                world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                break;
            case 2:
                world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                break;
            case 3:
                world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
            }

        }
        if ((meta == 3) && (isActive(world, i, j, k)))
        {
            float f = i + 1.0F;
            float f1 = j + 1.0F;
            float f2 = k + 1.0F;
            for (int z = 0; z < 4; z++)
            {
                float fmod = -0.2F - random.nextFloat() * 0.6F;
                float f1mod = -0.1F + random.nextFloat() * 0.2F;
                float f2mod = -0.2F - random.nextFloat() * 0.6F;
                world.spawnParticle("smoke", f + fmod, f1 + f1mod, f2 + f2mod, 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    @Override
    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World par1World, int par2, int par3, int par4, int par5)
    {
        TileEntity te = par1World.getTileEntity(par2, par3, par4);
        if (te != null)
        {
            if ((te instanceof TileEntityStandardMachine))
            {
                TileEntityStandardMachine tem = (TileEntityStandardMachine) te;
                return (int) Math.floor(tem.getProgress() * 15.0F);
            }
        }

        return 0;
    }

}
