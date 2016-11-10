package atomicstryker.petbat.client;

import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.IProxy;
import atomicstryker.petbat.common.PetBatMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy implements IProxy
{

    @Override
    public void displayGui(ItemStack itemStack)
    {
        FMLClientHandler.instance().getClient().displayGuiScreen(new GuiPetBatRename(itemStack));
    }

	@Override
	public void onModInit()
	{
		
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher().register(PetBatMod.instance().itemBatFlute, 0, new ModelResourceLocation("petbat:bat_flute", "inventory"));
        for (int i = 0; i < 29; i++)
        {
        	renderItem.getItemModelMesher().register(PetBatMod.instance().itemPocketedBat, i, new ModelResourceLocation("petbat:fed_pet_bat", "inventory"));
        }
	}

    @Override
    public void onModPreInit()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityPetBat.class, new IRenderFactory<EntityPetBat>()
        {
            @Override
            public Render<? super EntityPetBat> createRenderFor(RenderManager manager)
            {
                return new RenderPetBat(manager);
            }
        });
    }
    
}
