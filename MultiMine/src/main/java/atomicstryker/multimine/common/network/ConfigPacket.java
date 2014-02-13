package atomicstryker.multimine.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;

public class ConfigPacket implements IPacket
{

    private String excludedBlocks, excludedItems;

    public ConfigPacket()
    {
    }

    public ConfigPacket(String bdata, String idata)
    {
        excludedBlocks = bdata;
        excludedItems = idata;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, excludedBlocks);
        ByteBufUtils.writeUTF8String(bytes, excludedItems);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        excludedBlocks = ByteBufUtils.readUTF8String(bytes);
        excludedItems = ByteBufUtils.readUTF8String(bytes);
        MultiMineClient.instance().onServerSentExcludedBlocksList(excludedBlocks);
        MultiMineClient.instance().onServerSentExcludedItemsList(excludedItems);
    }

}
