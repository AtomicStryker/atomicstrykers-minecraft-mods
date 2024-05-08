package atomicstryker.multimine.common;

import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ISidedProxy {
    void commonSetup();

    void handlePartialBlockPacket(final PartialBlockPacket packet, final IPayloadContext context);

    void handlePartialBlockRemovalPacket(PartialBlockRemovalPacket payload, IPayloadContext context);
}
