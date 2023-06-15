package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

public class InfernalCommandFindEntityClass {
    public static final LiteralArgumentBuilder<CommandSourceStack> BUILDER =
            Commands.literal("feclass")
                    .requires((caller) -> caller.hasPermission(2))
                    .then(Commands.argument("entClass", StringArgumentType.word())
                            .executes((caller) -> {
                                execute(caller.getSource(), StringArgumentType.getString(caller, "entClass"));
                                return 1;
                            }));

    private static void execute(CommandSourceStack source, String entClass) {

        StringBuilder stringBuilder = new StringBuilder("Found Entity classes: ");
        boolean found = false;
        for (ResourceLocation entityResource : ForgeRegistries.ENTITY_TYPES.getKeys()) {
            String entclass = entityResource.getPath();
            if (entclass.toLowerCase().contains(entClass.toLowerCase())) {
                if (!found) {
                    stringBuilder.append(entclass);
                    found = true;
                } else {
                    stringBuilder.append(", ").append(entclass);
                }
            }
        }

        if (!found) {
            stringBuilder.append("Nothing found.");
        }
        String output = stringBuilder.toString();
        source.sendSuccess(() -> Component.literal(output), false);
        InfernalMobsCore.LOGGER.log(Level.INFO, source.getTextName() + ": " + output);
    }
}
