package atomicstryker.ic2.advancedmachines;

import ic2.api.item.Items;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import atomicstryker.ic2.advancedmachines.client.AdvancedMachinesClient;

public class BlockAdvancedMachines extends BlockContainer
{

    private Icon[][] iconBuffer;

    public BlockAdvancedMachines(int var1)
    {
        super(var1, Material.iron);
        this.setHardness(2.0F);
        this.setStepSound(soundMetalFootstep);
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconBuffer = new Icon[3][12]; // 3 machines, 6 sides each, in ON and OFF states

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
    }

    @Override
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int blockSide)
    {
        int blockMeta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getBlockTileEntity(x, y, z);
        int facing = (te instanceof TileEntityBlock) ? ((int) (((TileEntityBlock) te).getFacing())) : 0;

        if (isActive(world, x, y, z))
            return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing] + 6];
        else
            return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing]];
    }

    @Override
    public Icon getIcon(int blockSide, int blockMeta)
    {
        return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][3]];
    }

    @Override
    public TileEntity createNewTileEntity(World world)
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
        default:
            return null;
        }
    }

    @Override
    public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int meta, int fortune)
    {
        ArrayList<ItemStack> dropList = super.getBlockDropped(world, x, y, z, meta, fortune);
        TileEntity te = world.getBlockTileEntity(x, y, z);
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
    public void breakBlock(World world, int x, int y, int z, int blockID, int blockMeta)
    {
        super.breakBlock(world, x, y, z, blockID, blockMeta);
        boolean var5 = true;
        for (Iterator<ItemStack> iter = this.getBlockDropped(world, x, y, z, world.getBlockMetadata(x, y, z), 0).iterator(); iter.hasNext(); var5 = false)
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
    public int idDropped(int var1, Random var2, int var3)
    {
        return Items.getItem("advancedMachine").itemID;
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

    public int getGui(World var1, int var2, int var3, int var4, EntityPlayer var5)
    {
        switch (var1.getBlockMetadata(var2, var3, var4))
        {
        case 0:
            return ModAdvancedMachines.instance.guiIdRotary;
        case 1:
            return ModAdvancedMachines.instance.guiIdSingularity;
        case 2:
            return ModAdvancedMachines.instance.guiIdCentrifuge;
        default:
            return 0;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, player, stack);
        int heading = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntityBlock te = (TileEntityBlock) world.getBlockTileEntity(x, y, z);
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
        return ((TileEntityBlock) iba.getBlockTileEntity(x, y, z)).getActive();
    }

    @Override
    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        if (axis == ForgeDirection.UNKNOWN)
        {
            return false;
        }
        TileEntity tileEntity = worldObj.getBlockTileEntity(x, y, z);

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
            TileEntity te = world.getBlockTileEntity(i, j, k);
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
        TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
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
