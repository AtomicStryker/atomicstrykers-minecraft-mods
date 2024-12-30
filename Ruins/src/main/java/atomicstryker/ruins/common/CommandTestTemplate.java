package atomicstryker.ruins.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


import java.io.File;

class CommandTestTemplate {

    public static RuinTemplate parsedRuin;

    public static final LiteralArgumentBuilder<CommandSourceStack> BUILDER =
            Commands.literal("testruin")
                    .requires((caller) -> caller.hasPermission(2))
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                            .executes((caller) -> {
                                execute(caller.getSource().source, StringArgumentType.getString(caller, "input"));
                                return 1;
                            })).executes((caller) -> {
                        execute(caller.getSource().source, null);
                        return 1;
                    });

    private static void execute(CommandSource source, String input) {
        if (source instanceof ServerPlayer) {
            ServerPlayer sender = (ServerPlayer) source;
            String[] args = input == null ? new String[0] : input.split(" ");
            RuinsMod.LOGGER.info("called test command with input [{}], args count {}", input, args.length);
            int xpos, ypos, zpos;
            xpos = (int) sender.getX();
            ypos = (int) sender.getY();
            zpos = (int) sender.getZ();
            if (args.length < 4) {
                if (args.length < 1) {
                    if (parsedRuin != null) {
                        final Level world = sender.getCommandSenderWorld();
                        parsedRuin.doBuild(world, world.random, xpos, ypos, zpos, RuinsMod.DIR_NORTH, true, false);
                        parsedRuin = null;
                    } else {
                        sender.sendSystemMessage(Component.literal("You need to use the command with the target template name, eg. /testruin beach/LightHouse"));
                    }
                } else {
                    tryBuild(sender, args, xpos, ypos, zpos, true);
                }
            } else {
                try {
                    if (args[2].equals("_")) {
                        int x = Integer.valueOf(args[1]);
                        int z = Integer.valueOf(args[3]);
                        tryBuild(sender, args, x, -1, z, true);
                    } else {
                        int x = Integer.valueOf(args[1]);
                        int y = Integer.valueOf(args[2]);
                        int z = Integer.valueOf(args[3]);
                        tryBuild(sender, args, x, y, z, true);
                    }
                } catch (NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal("Invalid coordinates specified"));
                }
            }
        } else {
            source.sendSystemMessage(Component.literal("Command is only available for ingame player entities, or with coordinates specified"));
        }
    }

    private static void tryBuild(ServerPlayer sender, String[] args, int x, int y, int z, boolean is_player) {
        String target = args[0];
        if (!target.contains("/")) {
            target = "templateparser/" + target;
        }

        File file = new File(RuinsMod.getMinecraftBaseDir(), RuinsMod.TEMPLATE_PATH_MC_EXTRACTED + target + ".tml");
        if (file.exists() && file.canWrite()) {
            try {
                parsedRuin = new RuinTemplate(file.getCanonicalPath(), file.getName(), is_player);
                int rotation = (args.length > 4) ? Integer.parseInt(args[4]) : RuinsMod.DIR_NORTH;
                final boolean ignore_ceiling = args.length > 5 && Boolean.parseBoolean(args[5]);
                final Level world = sender.getCommandSenderWorld();

                if (parsedRuin != null) {
                    if (y < world.getMinY()) {
                        final int ceiling = world.getHeight();
                        for (y = ceiling - 1; y > world.getMinY(); y--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            final BlockState b = world.getBlockState(pos);
                            if (parsedRuin.isIgnoredBlock(b)) {
                                continue;
                            }

                            if (parsedRuin.isAcceptableSurface(world, b, pos)) {
                                break;
                            }
                            sender.sendSystemMessage(Component.literal("Could not find acceptable Y coordinate"));
                            return;
                        }
                        ++y;
                    }

                    if (parsedRuin.doBuild(world, world.random, x, y, z, rotation, is_player, ignore_ceiling) > world.getMinY()) {
                        parsedRuin = null;
                    } else {
                        sender.sendSystemMessage(Component.literal("EventRuinTemplateSpawn returned as cancelled, not building that."));
                    }
                } else {
                    sender.sendSystemMessage(Component.literal("Could not parse Ruin of file " + file));
                }
            } catch (RuinTemplate.IncompatibleModException e) {
                sender.sendSystemMessage(Component.literal(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sender.sendSystemMessage(Component.literal("Could not open/write file " + file));
        }
    }

}
