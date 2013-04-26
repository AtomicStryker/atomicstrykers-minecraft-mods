package ic2.advancedmachines.common;

import ic2.advancedmachines.client.AdvancedMachinesClient;
import ic2.api.Items;
import ic2.api.energy.event.EnergyTileUnloadEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class BlockAdvancedMachines extends BlockContainer
{
    
    private final int idWrench;
    private final int idEWrench;
    private Icon[][] iconBuffer;

    public BlockAdvancedMachines(int var1)
    {
        super(var1, Material.iron);
        this.setHardness(2.0F);
        this.setStepSound(soundMetalFootstep);
        
        idWrench = Items.getItem("wrench").itemID;
        idEWrench = Items.getItem("electricWrench").itemID;
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
        int facing = (te instanceof TileEntityBlock) ? ((int) (((TileEntityBlock)te).getFacing())) : 0;
        
        if(isActive(world, x, y, z))
            return iconBuffer[blockMeta][AdvancedMachinesClient.sideAndFacingToSpriteOffset[blockSide][facing]+6];
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

    /**
     * Get the block's damage value (for use with pick block).
     */
    @Override
    public int getDamageValue(World world, int x, int y, int z)
    {
        return world.getBlockMetadata(x, y, z); // advanced machine item meta exactly equals the block meta
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

    private TileEntityAdvancedMachine getBlockEntity(int var1)
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
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, player, stack);
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        if (entityPlayer.isSneaking())
        {
            return false;   
        }
        
        if (entityPlayer.getCurrentEquippedItem() != null
        && (entityPlayer.getCurrentEquippedItem().itemID == idWrench || entityPlayer.getCurrentEquippedItem().itemID == idEWrench))
        {            
            TileEntityAdvancedMachine team = (TileEntityAdvancedMachine)world.getBlockTileEntity(x, y, z);
            if (team != null)
            {
                //EnergyNet.getForWorld(world).removeTileEntity(team);
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(team));
                team.invalidate();
                team.setActive(false);
                return true;
            }
        }
        else
        {
            entityPlayer.openGui(AdvancedMachines.instance, 0, world, x, y, z);
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
