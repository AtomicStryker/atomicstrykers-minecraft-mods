package atomicstryker.petbat.client;

import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.IProxy;
import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.BatNamePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.io.File;

public class ClientProxy implements IProxy {

    @Override
    public void onModPreInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityPetBat.class, manager -> new RenderPetBat(manager));
    }

    @Override
    public void onClientInit() {

        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
        renderItem.getItemModelMesher().register(PetBatMod.instance().itemBatFlute, new ModelResourceLocation("petbat:bat_flute", "inventory"));
        renderItem.getItemModelMesher().register(PetBatMod.instance().itemPocketedBat, new ModelResourceLocation("petbat:fed_pet_bat", "inventory"));
    }

    @Override
    public void displayGui(ItemStack itemStack) {
        Minecraft.getInstance().displayGuiScreen(new GuiPetBatRename(itemStack));
    }

    @Override
    public File getMcFolder() {
        return Minecraft.getInstance().gameDir;
    }
}
