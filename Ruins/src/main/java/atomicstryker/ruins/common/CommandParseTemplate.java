package atomicstryker.ruins.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraftforge.event.level.BlockEvent;

public class CommandParseTemplate {

    private static ServerPlayer player;
    private static String templateName;
    public static final LiteralArgumentBuilder<CommandSourceStack> BUILDER =
            Commands.literal("parseruin")
                    .requires((caller) -> caller.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                            .executes((caller) -> {
                                execute(caller.getSource().getPlayerOrException(), StringArgumentType.getString(caller, "input"));
                                return 1;
                            }));

    private static void execute(ServerPlayer source, String input) {
        if (input == null || input.isEmpty()) {
            source.sendSystemMessage(Component.literal("You need to use the command with the target template name, eg. /parseruin funhouse"));
        } else {
            player = source;
            templateName = input;
            source.sendSystemMessage(Component.literal("Template parser ready to create " + templateName + ". Break any block of the baseplate now."));
        }
    }

    public static boolean onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == player) {
            // have to defer parsing to main thread, else all Tile Entities read as null
            World2TemplateParser world2TemplateParser = new World2TemplateParser(player, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), templateName);
            world2TemplateParser.execute();
            player = null;
            return true;
        }
        return false;
    }

}
