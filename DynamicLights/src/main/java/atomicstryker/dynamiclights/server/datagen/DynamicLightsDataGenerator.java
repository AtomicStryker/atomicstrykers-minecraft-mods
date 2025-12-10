package atomicstryker.dynamiclights.server.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

/**
 * wrapper for vanilla data generation, this means the lit blocks dont have to be added with jsons
 */
public class DynamicLightsDataGenerator {

    public static void start(GatherDataEvent event) {

        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();

        boolean client = event.includeClient();
        dataGenerator.addProvider(client, new DynamicLightsModelProvider(packOutput));
    }
}
