package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

public class InfernalCommandFindEntityClass {
    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("feclass")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .then(Commands.argument("entClass", StringArgumentType.word())
                            .executes((caller) -> {
                                execute(caller.getSource(), StringArgumentType.getString(caller, "entClass"));
                                return 1;
                            }));

    private static void execute(CommandSource source, String entClass) {

        StringBuilder stringBuilder = new StringBuilder("Found Entity classes: ");
        boolean found = false;
        for (ResourceLocation entityResource : ForgeRegistries.ENTITIES.getKeys()) {
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
        source.sendFeedback(new StringTextComponent(output), false);
        InfernalMobsCore.LOGGER.log(Level.INFO, source.getName() + ": " + output);
    }
}
