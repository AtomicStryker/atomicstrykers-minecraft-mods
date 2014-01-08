package atomicstryker.battletowers.common.network;

import io.netty.buffer.ByteBuf;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.network.NetworkHelper.IPacket;

public class LoginPacket implements IPacket
{
    
    @Override
    public void readBytes(ByteBuf bytes)
    {
        AS_BattleTowersCore.towerDestroyerEnabled = bytes.readByte();
        System.out.println("BTCLIENT packet read, AS_BattleTowersCore.towerDestroyerEnabled = "+AS_BattleTowersCore.towerDestroyerEnabled);
    }

    @Override
    public void writeBytes(ByteBuf bytes)
    {
        bytes.writeByte(AS_BattleTowersCore.towerDestroyerEnabled);
        System.out.println("BTSERVER packet sent, AS_BattleTowersCore.towerDestroyerEnabled = "+AS_BattleTowersCore.towerDestroyerEnabled);
    }
    
}