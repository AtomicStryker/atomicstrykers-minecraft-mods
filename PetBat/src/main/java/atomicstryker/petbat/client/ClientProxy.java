package atomicstryker.petbat.client;

import net.minecraft.item.ItemStack;
import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.IProxy;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy implements IProxy
{
    
    @Override
    public void onModPreInitLoad()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityPetBat.class, new RenderPetBat());
    }

    @Override
    public void displayGui(ItemStack itemStack)
    {
        FMLClientHandler.instance().getClient().func_147108_a(new GuiPetBatRename(itemStack));
    }
    
}
