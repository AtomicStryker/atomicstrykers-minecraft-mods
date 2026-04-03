package atomicstryker.dynamiclights.server.datagen;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Blocks;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * wrapper for vanilla data generation, this means the lit blocks dont have to be added with jsons
 */
public class DynamicLightsBlockModelGenerator extends BlockModelGenerators {

    public DynamicLightsBlockModelGenerator(Consumer<BlockModelDefinitionGenerator> consumer, ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelInstanceBiConsumer) {
        super(consumer, itemModelOutput, modelInstanceBiConsumer);
    }

    @Override
    public void run() {
        final Identifier airModel = Identifier.parse("block/air");
        // use the existing helper methods for air like blocks, otherwise this could be multi variants of air block
        this.createAirLikeBlock(DynamicLights.LIT_AIR_BLOCK.get(), new Material(airModel));
        this.createAirLikeBlock(DynamicLights.LIT_CAVE_AIR_BLOCK.get(), new Material(airModel));
        // no helper exists for water, but multi variants copy existing blocks such as water here
        this.blockStateOutput.accept(MultiVariantGenerator.dispatch(DynamicLights.LIT_WATER_BLOCK.get(), new MultiVariant(WeightedList.of(new Variant(ModelLocationUtils.getModelLocation(Blocks.WATER))))));
    }
}
