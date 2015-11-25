package atomicstryker.ropesplus.common;

import atomicstryker.ropesplus.common.network.HookshotPacket;
import atomicstryker.ropesplus.common.network.ZiplinePacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockZipLineAnchor extends BlockContainer
{

    public BlockZipLineAnchor()
    {
        super(Material.vine);
        float f = 0.1F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityPlayer, EnumFacing side, float xOffset, float yOffset, float zOffset)
    {
        if (!world.isRemote)
        {
            TileEntityZipLineAnchor teAnchor = (TileEntityZipLineAnchor) world.getTileEntity(pos);
            if (teAnchor.getHasZipLine())
            {
                RopesPlusCore.instance.networkHelper.sendPacketToPlayer(new ZiplinePacket("server", teAnchor.getZipLineEntity().getEntityId(), 0f),
                        (EntityPlayerMP) entityPlayer);
                entityPlayer.worldObj.playSoundAtEntity(entityPlayer, "ropesplus:zipline", 1.0F,
                        1.0F / (entityPlayer.getRNG().nextFloat() * 0.1F + 0.95F));
                return true;
            }
            else
            {
                for (Object o : world.loadedEntityList)
                {
                    if (o instanceof EntityFreeFormRope)
                    {
                        EntityFreeFormRope rope = (EntityFreeFormRope) o;
                        if (rope.getShooter() != null && rope.getShooter().equals(entityPlayer))
                        {
                            if (rope.getEndY() > pos.getY())
                            {
                                entityPlayer.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("translation.ropesplus:ZiplineFailC")));
                                break;
                            }
                            else
                            {
                                int targetX = MathHelper.floor_double(rope.getEndX());
                                int targetY = MathHelper.floor_double(rope.getEndY());
                                int targetZ = MathHelper.floor_double(rope.getEndZ());
                                if (world.getBlockState(new BlockPos(targetX, targetY, targetZ)).getBlock().isNormalCube())
                                {
                                    teAnchor.setTargetCoordinates(targetX, targetY, targetZ);
                                    if (!entityPlayer.capabilities.isCreativeMode)
                                    {
                                        entityPlayer.inventory.consumeInventoryItem(RopesPlusCore.instance.itemHookShot);
                                    }

                                    RopesPlusCore.instance.networkHelper.sendPacketToPlayer(new HookshotPacket(-1, 0, 0, 0), (EntityPlayerMP) entityPlayer);

                                    rope.setDead();
                                    entityPlayer.worldObj.playSoundAtEntity(entityPlayer, "ropesplus:ropetension", 1.0F, 1.0F / (entityPlayer.getRNG()
                                            .nextFloat() * 0.1F + 0.95F));
                                    return true;
                                }
                                else
                                {
                                    final String s =
                                            String.format("%s [%d|%d|%d] %s", StatCollector.translateToLocal("translation.ropesplus:ZiplineFailA"),
                                                    targetX, targetY, targetZ, StatCollector.translateToLocal("translation.ropesplus:ZiplineFailB"));
                                    entityPlayer.addChatComponentMessage(new ChatComponentText(s));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return super.onBlockActivated(world, pos, state, entityPlayer, side, xOffset, yOffset, zOffset);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityZipLineAnchor();
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
    {
        super.onNeighborBlockChange(world, pos, state, block);
        if (!world.getBlockState(pos.add(0, 1, 0)).getBlock().isOpaqueCube())
        {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockState(pos, Blocks.air.getDefaultState());
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos)
    {
        return world.getBlockState(pos.add(0, 1, 0)).getBlock().isOpaqueCube();
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean isFullCube()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

}
