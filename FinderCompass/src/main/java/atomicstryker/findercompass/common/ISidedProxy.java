package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;

import java.nio.file.Path;

public interface ISidedProxy {
    void commonSetup();

    void onReceivedSearchPacket(FeatureSearchPacket packet);

    void onReceivedHandshakePacket(HandshakePacket handShakePacket);

    Path getMcFolder();
}
