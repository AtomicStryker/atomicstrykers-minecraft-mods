package atomicstryker.findercompass.client.mixin;

import atomicstryker.findercompass.client.mixinimpl.FinderCompassItemRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements IResourceManagerReloadListener {

    private FinderCompassItemRenderer finderCompassItemRenderer = new FinderCompassItemRenderer((ItemRenderer) (Object) this);

    @Overwrite
    private void renderModel(IBakedModel model, ItemStack stack) {
        finderCompassItemRenderer.renderModel(model, stack);
    }
}
