package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;

import java.io.File;

public interface ISidedProxy {
    void commonSetup();

    void onReceivedSearchPacket(FeatureSearchPacket packet);

    void onReceivedHandshakePacket(HandshakePacket handShakePacket);

    File getMcFolder();
}
