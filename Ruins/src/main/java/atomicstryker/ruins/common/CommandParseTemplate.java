package atomicstryker.ruins.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class CommandParseTemplate {

    private static Player player;
    private static String templateName;
    public static final LiteralArgumentBuilder<CommandSourceStack> BUILDER =
            Commands.literal("parseruin")
                    .requires((caller) -> caller.hasPermission(2))
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                            .executes((caller) -> {
                                execute(caller.getSource().source, StringArgumentType.getString(caller, "input"));
                                return 1;
                            }));

    private static void execute(CommandSource source, String input) {
        if (source instanceof Player) {
            if (input == null || input.isEmpty()) {
                source.sendSystemMessage(Component.literal("You need to use the command with the target template name, eg. /parseruin funhouse"));
            } else {
                player = (Player) source;
                templateName = input;
                source.sendSystemMessage(Component.literal("Template parser ready to create " + templateName + ". Break any block of the baseplate now."));
            }
        } else {
            source.sendSystemMessage(Component.literal("Command only available for ingame player entities."));
        }
    }

    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == player) {
            // have to defer parsing to main thread, else all Tile Entities read as null
            MinecraftServer server = player.level.getServer();
            if (server != null) {
                World2TemplateParser world2TemplateParser = new World2TemplateParser(player, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), templateName);
                world2TemplateParser.execute();
            }
            player = null;
            event.setCanceled(true);
        }
    }

}
