package atomicstryker.necromancy.network;

import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import atomicstryker.necromancy.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;

public class TearShotPacket implements IPacket
{

    private String user;
    private boolean blood;

    public TearShotPacket()
    {
    }

    public TearShotPacket(String username, boolean blo)
    {
        user = username;
        blood = blo;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeBoolean(blood);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        blood = bytes.readBoolean();
        
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(user);
        if (player != null)
        {
            EntityTear tear = blood ? new EntityTearBlood(player.worldObj, player) : new EntityTear(player.worldObj, player);
            
            if (blood)
            {
                if (player.getHealth() > 0.5F)
                {
                    player.attackEntityFrom(DamageSource.starve, 0.5F);
                }
                else
                {
                    return;
                }
            }
            else
            {
                if (player.getFoodStats().getFoodLevel() > 3)
                {
                    player.getFoodStats().addExhaustion(3F);
                }
                else
                {
                    return;
                }
            }
            
            player.worldObj.spawnEntityInWorld(tear);
            tear.playSound("necromancy:tear", 1.0F, 1.0F / (player.getRNG().nextFloat() * 0.4F + 0.8F));
        }
    }

}
