package atomicstryker.dynamiclights.server.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * @author Tfarcenim
 * <p>
 * Datagenerator for assets and data that gets included in the mod
 */
public class ModDatagen {

    public static void start(GatherDataEvent event) {

        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        boolean client = event.includeClient();

        dataGenerator.addProvider(client, new ModBlockStateProvider(packOutput, existingFileHelper));
    }
}
