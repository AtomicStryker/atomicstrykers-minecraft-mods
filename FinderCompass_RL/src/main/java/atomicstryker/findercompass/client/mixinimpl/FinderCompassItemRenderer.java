package atomicstryker.findercompass.client.mixinimpl;

import atomicstryker.findercompass.client.CompassRenderHook;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FinderCompassItemRenderer {

    private static final Logger LOGGER = LogManager.getLogger();

    private ItemRenderer itemRenderer;

    private Method renderModelMethod;

    public FinderCompassItemRenderer(ItemRenderer original) {
        itemRenderer = original;

        LOGGER.info("looking for a void method with these 3 param classes: {}, {}, {}", IBakedModel.class.getSimpleName(), int.class.getSimpleName(), ItemStack.class.getSimpleName());

        for (Method m : itemRenderer.getClass().getDeclaredMethods()) {
            LOGGER.debug("reflection looking at method {}, params {}, returns {}", m, m.getParameterTypes(), m.getReturnType());
            Class<?>[] paramClasses = m.getParameterTypes();
            if (paramClasses.length == 3
                    && paramClasses[0].equals(IBakedModel.class)
                    && paramClasses[1].equals(int.class)
                    && paramClasses[2].equals(ItemStack.class)) {
                renderModelMethod = m;
                renderModelMethod.setAccessible(true);
                LOGGER.info("identified renderModel method: {}", renderModelMethod);
                break;
            }
        }
        if (renderModelMethod == null) {
            throw new UnsupportedOperationException("reflect into renderModel failed");
        }
    }

    public void renderModel(IBakedModel model, ItemStack stack) {
        // itemRenderer.renderModel(model, -1, stack);
        try {
            renderModelMethod.invoke(itemRenderer, model, -1, stack);
            CompassRenderHook.renderItemHook(stack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnsupportedOperationException("reflect into renderModel failed");
        }
    }
}