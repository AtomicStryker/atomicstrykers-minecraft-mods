package atomicstryker.findercompass.common.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.client.FinderCompassLogic;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;

public class HandshakePacket implements IPacket
{

    private String userName;
    private byte[] configByteArray;

    public HandshakePacket()
    {
    }
    
    public HandshakePacket(String n)
    {
        userName = n;
    }

    @Override
    public void writeBytes(ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            bytes.writeShort(userName.length());
            for (char c : userName.toCharArray()) bytes.writeChar(c);
        }
        else
        {
            File config = FinderCompassMod.instance.compassConfig;
            configByteArray = new byte[(int)config.length()];
            try
            {
                FileInputStream fis = new FileInputStream(config);
                fis.read(configByteArray);
                fis.close();
                bytes.writeShort(configByteArray.length);
                bytes.writeBytes(configByteArray);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void readBytes(ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            FinderCompassLogic.serverHasFinderCompass = true;
            short len = bytes.readShort();
            configByteArray = new byte[len];
            bytes.readBytes(configByteArray);
            FinderCompassClientTicker.instance.inputOverrideConfig(new DataInputStream(new ByteArrayInputStream(configByteArray)));
        }
        else
        {
            short len = bytes.readShort();
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
            userName = String.valueOf(chars);

            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(userName);
            File config = FinderCompassMod.instance.compassConfig;
            if (p != null && config != null && config.exists())
            {
                FinderCompassMod.instance.networkHelper.sendPacketToPlayer(this, p);
            }
        }
    }

}
