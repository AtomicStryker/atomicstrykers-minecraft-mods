package atomicstryker.dynamiclights.client;

import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class DynamicLightsMixin implements InitializationListener {

    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("dynamiclights.client.json");
    }
}
