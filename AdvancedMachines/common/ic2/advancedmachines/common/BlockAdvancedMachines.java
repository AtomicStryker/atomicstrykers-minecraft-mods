package ic2.advancedmachines.common;

import net.minecraft.src.*;
import ic2.advancedmachines.client.AdvancedMachinesClient;
import ic2.api.EnergyNet;
import ic2.api.Items;

import java.util.*;

public class BlockAdvancedMachines extends BlockContainer
{
    public int[][] sprites;
    private final int idWrench;
    private final int idEWrench;

    public BlockAdvancedMachines(int var1)
    {
        super(var1, Material.iron);
        this.setHardness(2.0F);
        this.setStepSound(soundMetalFootstep);
        this.sprites = new int[][] {{86, 20, 86, 19, 86, 21, 86, 19}, {86, 26, 86, 27, 86, 26, 86, 28}, {86, 86, 24, 22, 86, 86, 25, 23}};
        this.blockIndexInTexture = this.sprites[0][0];
        
        idWrench = Items.getItem("wrench").itemID;
        idEWrench = Items.getItem("electricWrench").itemID;
    }

    @Override
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int blockSide)
    {
        int blockMeta = world.getBlockMetadata(x, y, z);

        TileEntity te = world.getBlockTileEntity(x, y, z);
        int facing = (te instanceof TileEntityBlock) ? ((int) (((TileEntityBlock)te).getFacing())) : 0;
        if(isActive(world, x, y, z))
        	return blockMeta + (AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing] + 6) * 16;
        else
        	return blockMeta + AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing] * 16;
    }

    @Override
    public int getBlockTextureFromSideAndMetadata(int blockSide, int metaData)
    {
        return metaData + AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][3] * 16;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return null;
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return getBlockEntity(meta);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        super.onBlockAdded(world, x, y, z);
        //TileEntityAdvancedMachine te = this.getBlockEntity(world.getBlockMetadata(x, y, z));
        //world.setBlockTileEntity(x, y, z, te);
    }

    @Override
    public ArrayList getBlockDropped(World var1, int var2, int var3, int var4, int var5, int var6)
    {
        ArrayList var7 = super.getBlockDropped(var1, var2, var3, var4, var5, var6);
        TileEntity var8 = var1.getBlockTileEntity(var2, var3, var4);
        if (var8 instanceof IInventory)
        {
            IInventory var9 = (IInventory)var8;

            for (int var10 = 0; var10 < var9.getSizeInventory(); ++var10)
            {
                ItemStack var11 = var9.getStackInSlot(var10);
                if (var11 != null)
                {
                    var7.add(var11);
                    var9.setInventorySlotContents(var10, (ItemStack)null);
                }
            }
        }

        return var7;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        boolean var5 = true;
        for (Iterator iter = this.getBlockDropped(world, x, y, z, world.getBlockMetadata(x, y, z), 0).iterator(); iter.hasNext(); var5 = false)
        {
            ItemStack var7 = (ItemStack)iter.next();
            if (!var5)
            {
                if (var7 == null)
                {
                    return;
                }

                double var8 = 0.7D;
                double var10 = (double)world.rand.nextFloat() * var8 + (1.0D - var8) * 0.5D;
                double var12 = (double)world.rand.nextFloat() * var8 + (1.0D - var8) * 0.5D;
                double var14 = (double)world.rand.nextFloat() * var8 + (1.0D - var8) * 0.5D;
                EntityItem var16 = new EntityItem(world, (double)x + var10, (double)y + var12, (double)z + var14, var7);
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

    @Override
    public int damageDropped(int var1)
    {
        return 12;
    }

    @Override
    public String getTextureFile()
    {
        return "/ic2/advancedmachines/client/sprites/block_advmachine.png";
    }

    public int getGui(World var1, int var2, int var3, int var4, EntityPlayer var5)
    {
        switch (var1.getBlockMetadata(var2, var3, var4))
        {
            case 0:
                return AdvancedMachines.guiIdRotary;
            case 1:
                return AdvancedMachines.guiIdSingularity;
            case 2:
                return AdvancedMachines.guiIdCentrifuge;
            default:
                return 0;
        }
    }

    public TileEntityAdvancedMachine getBlockEntity(int var1)
    {
        switch (var1)
        {
            case 0:
                return new TileEntityRotaryMacerator();
            case 1:
                return new TileEntitySingularityCompressor();
            case 2:
                return new TileEntityCentrifugeExtractor();
            default:
                return null;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player)
    {
        super.onBlockPlacedBy(world, x, y, z, player);
        int heading = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntityAdvancedMachine te = (TileEntityAdvancedMachine)world.getBlockTileEntity(x, y, z);
        switch (heading)
        {
        case 0:
            te.setFacing((short)2);
            break;
        case 1:
            te.setFacing((short)5);
            break;
        case 2:
            te.setFacing((short)3);
            break;
        case 3:
            te.setFacing((short)4);
            break;
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        if (par5EntityPlayer.getCurrentEquippedItem() != null
        && (par5EntityPlayer.getCurrentEquippedItem().itemID == idWrench || par5EntityPlayer.getCurrentEquippedItem().itemID == idEWrench))
        {
            if (par5EntityPlayer.isSneaking())
            {
                return false;   
            }
            
            TileEntityAdvancedMachine team = (TileEntityAdvancedMachine)world.getBlockTileEntity(x, y, z);
            if (team != null)
            {
                EnergyNet.getForWorld(world).removeTileEntity(team);
                team.invalidate();
                team.setActive(false);
                return true;
            }
        }
        else
        {
            par5EntityPlayer.openGui(AdvancedMachines.instance, 0, world, x, y, z);
            return true;
        }
        return false;
    }

    public static boolean isActive(IBlockAccess var0, int var1, int var2, int var3)
    {
        return ((TileEntityAdvancedMachine)var0.getBlockTileEntity(var1, var2, var3)).getActive();
    }

    public static int getFacing(IBlockAccess var0, int var1, int var2, int var3)
    {
        return ((TileEntityAdvancedMachine)var0.getBlockTileEntity(var1, var2, var3)).getFacing();
    }

    public static float getWrenchRate(IBlockAccess var0, int var1, int var2, int var3)
    {
        return ((TileEntityAdvancedMachine)var0.getBlockTileEntity(var1, var2, var3)).getWrenchDropRate();
    }

    @Override
    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5)
    {
        int var6 = var1.getBlockMetadata(var2, var3, var4);
        if ((var6 == 0 || var6 == 1) && isActive(var1, var2, var3, var4))
        {
            float var7 = (float)var2 + 1.0F;
            float var8 = (float)var3 + 1.0F;
            float var9 = (float)var4 + 1.0F;

            for (int var10 = 0; var10 < 4; ++var10)
            {
                float var11 = -0.2F - var5.nextFloat() * 0.6F;
                float var12 = -0.1F + var5.nextFloat() * 0.2F;
                float var13 = -0.2F - var5.nextFloat() * 0.6F;
                var1.spawnParticle("smoke", (double)(var7 + var11), (double)(var8 + var12), (double)(var9 + var13), 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
