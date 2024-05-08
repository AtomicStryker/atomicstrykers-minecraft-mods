package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.File;

public interface ISidedProxy {
    void commonSetup();

    File getMcFolder();

    void handleFeatureSearch(final FeatureSearchPacket packet, final IPayloadContext context);
}
