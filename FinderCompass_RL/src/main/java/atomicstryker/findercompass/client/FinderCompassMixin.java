package atomicstryker.findercompass.client;

import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class FinderCompassMixin implements InitializationListener {

    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("findercompass.client.json");
    }
}