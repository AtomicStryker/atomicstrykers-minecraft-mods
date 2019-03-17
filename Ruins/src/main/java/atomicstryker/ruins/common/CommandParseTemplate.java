package atomicstryker.ruins.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandParseTemplate {

    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("parseruin")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .then(Commands.argument("input", StringArgumentType.greedyString()))
                    .executes((caller) -> {
                        execute(caller.getSource(), StringArgumentType.getString(caller, "input"));
                        return 1;
                    });

    private static EntityPlayer player;
    private static String templateName;

    private static void execute(CommandSource source, String input) {
        if (source.getEntity() instanceof EntityPlayer) {
            if (input == null || input.isEmpty()) {
                source.sendErrorMessage(new TextComponentTranslation("You need to use the command with the target template name, eg. /parseruin funhouse"));
            } else {
                player = (EntityPlayer) source.getEntity();
                templateName = input;
                source.sendFeedback(new TextComponentTranslation("Template parser ready to create " + templateName + ". Break any block of the baseplate now."), false);
            }
        } else {
            source.sendErrorMessage(new TextComponentTranslation("Command only available for ingame player entities."));
        }
    }

    public CommandParseTemplate() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBroken(BreakEvent event) {
        if (event.getPlayer() == player) {
            new World2TemplateParser(player, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), templateName).start();
            player = null;
            event.setCanceled(true);
        }
    }

}
