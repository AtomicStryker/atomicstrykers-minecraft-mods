package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.Map;

public class InfernalCommandFindEntityClass {
    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("feclass")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .then(Commands.argument("entClass", StringArgumentType.greedyString())
                            .executes((caller) -> {
                                execute(caller.getSource(), StringArgumentType.getString(caller, "entClass"));
                                return 1;
                            }));

    private static void execute(CommandSource source, String entClass) {

        StringBuilder result = new StringBuilder("Found Entity classes: ");
        boolean found = false;
        for (Map.Entry<ResourceLocation, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            String entclass = entry.getKey().getPath();
            if (entclass.toLowerCase().contains(entClass.toLowerCase())) {
                if (!found) {
                    result.append(entclass);
                    found = true;
                } else {
                    result.append(", ").append(entclass);
                }
            }
        }

        if (!found) {
            result.append("Nothing found.");
        }

        InfernalMobsCore.LOGGER.log(Level.INFO, source.getName() + ": " + result);
    }
}
