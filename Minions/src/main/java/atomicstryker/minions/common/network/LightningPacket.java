package atomicstryker.minions.common.network;

import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.codechicken.ChickenLightningBolt;
import atomicstryker.minions.common.codechicken.Vector3;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class LightningPacket implements IPacket
{

    private String user;
    private double sx, sy, sz, ex, ey, ez;
    private LightningPacket cache;

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
        cache = this;
        
        if (user.equals("server"))
        {
            FMLClientHandler.instance().getClient().addScheduledTask(new ScheduledCodeClient());
        }
        else
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new ScheduledCodeServer());
        }
    }
    
    class ScheduledCodeClient implements Runnable
    {

        @Override
        public void run()
        {
            Vector3 start = new Vector3(sx, sy, sz);
            Vector3 end = new Vector3(ex, ey, ez);
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            long randomizer = player.world.rand.nextLong();
            spawnLightningBolt(player.world, player, start, end, randomizer);
        }
    }
    
    class ScheduledCodeServer implements Runnable
    {

        @Override
        public void run()
        {
            Vector3 start = new Vector3(sx, sy, sz);
            Vector3 end = new Vector3(ex, ey, ez);
            EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(user);
            if (player != null)
            {                
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    long randomizer = player.world.rand.nextLong();
                    
                    cache.user = "server";
                    MinionsCore.instance.networkHelper.sendPacketToAllAroundPoint(cache, new TargetPoint(player.dimension, sx, sy, sz, 32D));
                    
                    spawnLightningBolt(player.world, player, start, end, randomizer);
                    
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
