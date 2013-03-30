package atomicstryker.ropesplus.common;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;


public class BlockZipLineAnchor extends BlockContainer
{
    public BlockZipLineAnchor(int blockIndex)
    {
        super(blockIndex, Material.vine);
        float f = 0.1F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
    }
    
    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("ropesplus:grhkanchor");
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float xOffset, float yOffset, float zOffset)
    {
        TileEntityZipLineAnchor teAnchor = (TileEntityZipLineAnchor) world.getBlockTileEntity(x, y, z);
        
        if (teAnchor.getHasZipLine() && !world.isRemote)
        {
            Object[] toSend = { teAnchor.getZipLineEntity().entityId };
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_Ropes", 7, toSend), (Player) entityPlayer);
            world.playSoundAtEntity(entityPlayer, "zipline", 1.0F, 1.0F / (entityPlayer.getRNG().nextFloat() * 0.1F + 0.95F));
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
                            entityPlayer.sendChatToPlayer("Newton says you can't Zipline upwards, sorry...");
                            break;
                        }
                        else
                        {
                            int targetX = MathHelper.floor_double(rope.getEndX());
                            int targetY = MathHelper.floor_double(rope.getEndY());
                            int targetZ = MathHelper.floor_double(rope.getEndZ());
                            if (world.isBlockOpaqueCube(targetX, targetY, targetZ))
                            {
                                teAnchor.setTargetCoordinates(targetX, targetY, targetZ);
                                entityPlayer.inventory.consumeInventoryItem(RopesPlusCore.itemHookShot.itemID);
                                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_Ropes", 6, null), (Player) entityPlayer);
                                rope.setDead();
                                world.playSoundAtEntity(entityPlayer, "ropetension", 1.0F, 1.0F / (entityPlayer.getRNG().nextFloat() * 0.1F + 0.95F));
                                return true;
                            }
                            else
                            {
                                entityPlayer.sendChatToPlayer("Zipline target Block ["+targetX+"|"+targetY+"|"+targetZ+"] not opaque!");
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
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityZipLineAnchor();
    }

    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, int l)
    {
        super.onNeighborBlockChange(world, i, j, k, l);
        if(!world.isBlockOpaqueCube(i, j + 1, k))
        {
            dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
            world.setBlock(i, j, k, 0, 0, 3);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        return world.isBlockOpaqueCube(i, j + 1, k);
    }
	
    @Override
    public void breakBlock(World world, int par2, int par3, int par4, int par5, int par6)
    {
        super.breakBlock(world, par2, par3, par4, par5, par6);
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
