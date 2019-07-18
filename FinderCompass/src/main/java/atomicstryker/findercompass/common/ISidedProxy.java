package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;

public interface ISidedProxy {
    void onReceivedSearchPacket(FeatureSearchPacket packet);
    void onReceivedHandshakePacket(HandshakePacket handShakePacket);
}
