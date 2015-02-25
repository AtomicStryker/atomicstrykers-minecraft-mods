package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class LightningPacket implements IPacket
{

    private String user;
    private double sx, sy, sz, ex, ey, ez;

    public LightningPacket()
    {
    }

    public LightningPacket(String username, double a, double b, double c, double d, double e, double f)
    {
        user = username;
        sx = a;
        sy = b;
        sz = c;
        ex = d;
        ey = e;
        ez = f;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeDouble(sx);
        bytes.writeDouble(sy);
        bytes.writeDouble(sz);
        bytes.writeDouble(ex);
        bytes.writeDouble(ey);
        bytes.writeDouble(ez);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        sx = bytes.readDouble();
        sy = bytes.readDouble();
        sz = bytes.readDouble();
        ex = bytes.readDouble();
        ey = bytes.readDouble();
        ez = bytes.readDouble();
        
        Vector3 start = new Vector3(sx, sy, sz);
        Vector3 end = new Vector3(ex, ey, ez);
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            MinionsClient.spawnLightningBolt(start, end);
        }
        else
        {
            EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
            if (player != null)
            {                
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    long randomizer = player.worldObj.rand.nextLong();
                    
                    MinionsCore.instance.networkHelper.sendPacketToAllAroundPoint(this, new TargetPoint(player.dimension, sx, sy, sz, 32D));
                    
                    spawnLightningBolt(player.worldObj, player, start, end, randomizer);
                    
                    MinionsCore.instance.exhaustPlayerSmall(player);                
                }
            }
        }
    }
    
    private void spawnLightningBolt(World world, EntityLivingBase shooter, Vector3 startvec, Vector3 endvec, long randomizer)
    {
        for (int i = 3; i != 0; i--)
        {
            ChickenLightningBolt bolt = new ChickenLightningBolt(world, startvec, endvec, randomizer);
            bolt.defaultFractal();
            bolt.finalizeBolt();
            bolt.setWrapper(shooter);
            ChickenLightningBolt.offerBolt(bolt);   
        }
    }

}
