package atomicstryker.ruins.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class CommandParseTemplate {

    private static PlayerEntity player;
    private static String templateName;
    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("parseruin")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                            .executes((caller) -> {
                                execute(caller.getSource(), StringArgumentType.getString(caller, "input"));
                                return 1;
                            }));

    private static void execute(CommandSource source, String input) {
        if (source.getEntity() instanceof PlayerEntity) {
            if (input == null || input.isEmpty()) {
                source.sendErrorMessage(new TranslationTextComponent("You need to use the command with the target template name, eg. /parseruin funhouse"));
            } else {
                player = (PlayerEntity) source.getEntity();
                templateName = input;
                source.sendFeedback(new TranslationTextComponent("Template parser ready to create " + templateName + ". Break any block of the baseplate now."), false);
            }
        } else {
            source.sendErrorMessage(new TranslationTextComponent("Command only available for ingame player entities."));
        }
    }

    @SubscribeEvent
    public void onBlockBroken(BreakEvent event) {
        if (event.getPlayer() == player) {
            // have to defer parsing to main thread, else all Tile Entities read as null
            MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            server.deferTask(new World2TemplateParser(player, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), templateName));
            player = null;
            event.setCanceled(true);
        }
    }

}
