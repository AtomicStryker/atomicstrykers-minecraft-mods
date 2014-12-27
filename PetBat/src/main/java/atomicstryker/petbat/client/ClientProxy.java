package atomicstryker.petbat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.IProxy;
import atomicstryker.petbat.common.PetBatMod;

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
		RenderingRegistry.registerEntityRenderingHandler(EntityPetBat.class, new RenderPetBat());
		
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher().register(PetBatMod.instance().itemBatFlute, 0, new ModelResourceLocation("petbat:bat_flute", "inventory"));
        for (int i = 0; i < 29; i++)
        {
        	renderItem.getItemModelMesher().register(PetBatMod.instance().itemPocketedBat, i, new ModelResourceLocation("petbat:fed_pet_bat", "inventory"));
        }
	}
    
}
