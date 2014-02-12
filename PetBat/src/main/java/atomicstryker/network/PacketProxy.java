package atomicstryker.network;


public interface PacketProxy
{
    public String getSenderName();
    
    public void onPacketData(int packetType, WrappedPacket packet, String sender);
}
