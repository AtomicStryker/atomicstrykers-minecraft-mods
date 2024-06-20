package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.file.Path;

public interface ISidedProxy {
    void commonSetup();

    Path getMcFolder();

    void handleFeatureSearch(final FeatureSearchPacket packet, final IPayloadContext context);
}
