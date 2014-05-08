package atomicstryker.ropesplus.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.network.HookshotPacket;
import atomicstryker.ropesplus.common.network.ZiplinePacket;

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
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("ropesplus:grhkanchor");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float xOffset, float yOffset, float zOffset)
    {
        TileEntityZipLineAnchor teAnchor = (TileEntityZipLineAnchor) world.getTileEntity(x, y, z);

        if (teAnchor.getHasZipLine() && !entityPlayer.worldObj.isRemote)
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
                        if (rope.getEndY() > y)
                        {
                            entityPlayer.addChatMessage(new ChatComponentText("Newton says you can't Zipline upwards, sorry..."));
                            break;
                        }
                        else
                        {
                            int targetX = MathHelper.floor_double(rope.getEndX());
                            int targetY = MathHelper.floor_double(rope.getEndY());
                            int targetZ = MathHelper.floor_double(rope.getEndZ());
                            if (world.getBlock(targetX, targetY, targetZ).isOpaqueCube())
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
                                entityPlayer.addChatComponentMessage(new ChatComponentText("Zipline target Block [" + targetX + "|" + targetY + "|"
                                        + targetZ + "] not opaque!"));
                                break;
                            }
                        }
                    }
                }
            }
        }

        return super.onBlockActivated(world, x, y, z, entityPlayer, side, xOffset, yOffset, zOffset);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityZipLineAnchor();
    }

    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, Block l)
    {
        super.onNeighborBlockChange(world, i, j, k, l);
        if (!world.getBlock(i, j + 1, k).isOpaqueCube())
        {
            dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
            world.setBlock(i, j, k, Blocks.air, 0, 3);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        return world.getBlock(i, j + 1, k).isOpaqueCube();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return 1;
    }

}
