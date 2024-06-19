package atomicstryker.dynamiclights.server.datagen;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * @author Tfarcenim
 * <p>
 * Class for creating blockstate jsons and their included models
 */
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, DynamicLights.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        final ResourceLocation airModel = ResourceLocation.parse("block/air");

        simpleBlock(DynamicLights.LIT_AIR_BLOCK.get(), models().getExistingFile(airModel));
        simpleBlock(DynamicLights.LIT_CAVE_AIR_BLOCK.get(), models().getExistingFile(airModel));
        simpleBlock(DynamicLights.LIT_WATER_BLOCK.get(), models().getExistingFile(ResourceLocation.parse("block/water")));
    }
}
