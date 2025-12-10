package atomicstryker.dynamiclights.server.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

/**
 * wrapper for vanilla data generation, this means the lit blocks dont have to be added with jsons
 */
public class DynamicLightsModelProvider extends ModelProvider {

    public DynamicLightsModelProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected Stream<Block> getKnownBlocks() {
        return super.getKnownBlocks();
    }

    @Override
    protected BlockModelGenerators getBlockModelGenerators(BlockStateGeneratorCollector blocks, ItemInfoCollector items, SimpleModelCollector models) {
        return new DynamicLightsBlockModelGenerator(blocks, items, models);
    }


}
