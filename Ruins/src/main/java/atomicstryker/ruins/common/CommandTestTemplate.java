package atomicstryker.ruins.common;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.io.File;

class CommandTestTemplate {

    public static RuinTemplate parsedRuin;

    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("testruin")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                            .executes((caller) -> {
                                execute(caller.getSource(), StringArgumentType.getString(caller, "input"));
                                return 1;
                            })).executes((caller) -> {
                execute(caller.getSource(), null);
                return 1;
            });

    private static void execute(CommandSource source, String input) {
        if (source.getEntity() instanceof PlayerEntity) {
            PlayerEntity sender = (PlayerEntity) source.getEntity();
            String[] args = input == null ? new String[0] : input.split(" ");
            RuinsMod.LOGGER.info("called test command with input [{}], args count {}", input, args.length);
            int xpos, ypos, zpos;
            xpos = sender.getPosition().getX();
            ypos = sender.getPosition().getY();
            zpos = sender.getPosition().getZ();
            if (args.length < 4) {
                if (args.length < 1) {
                    if (parsedRuin != null) {
                        final World world = sender.getEntityWorld();
                        parsedRuin.doBuild(world, world.rand, xpos, ypos, zpos, RuinsMod.DIR_NORTH, true, false);
                        parsedRuin = null;
                    } else {
                        sender.sendMessage(new TranslationTextComponent("You need to use the command with the target template name, eg. /testruin beach/LightHouse"));
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
                    sender.sendMessage(new TranslationTextComponent("Invalid coordinates specified"));
                }
            }
        } else {
            source.sendErrorMessage(new TranslationTextComponent("Command is only available for ingame player entities, or with coordinates specified"));
        }
    }

    private static void tryBuild(PlayerEntity sender, String[] args, int x, int y, int z, boolean is_player) {
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
                final World world = sender.getEntityWorld();

                if (parsedRuin != null) {
                    if (y < 0) {
                        final int ceiling = ignore_ceiling ? world.getHeight() : world.getActualHeight();
                        for (y = ceiling - 1; y > 7; y--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            final BlockState b = world.getBlockState(pos);
                            if (parsedRuin.isIgnoredBlock(b)) {
                                continue;
                            }

                            if (parsedRuin.isAcceptableSurface(b)) {
                                break;
                            }
                            sender.sendMessage(new TranslationTextComponent("Could not find acceptable Y coordinate"));
                            return;
                        }
                        ++y;
                    }

                    if (parsedRuin.doBuild(world, world.rand, x, y, z, rotation, is_player, ignore_ceiling) >= 0) {
                        parsedRuin = null;
                    } else {
                        sender.sendMessage(new TranslationTextComponent("EventRuinTemplateSpawn returned as cancelled, not building that."));
                    }
                } else {
                    sender.sendMessage(new TranslationTextComponent("Could not parse Ruin of file " + file));
                }
            } catch (RuinTemplate.IncompatibleModException e) {
                sender.sendMessage(new TranslationTextComponent(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(new TranslationTextComponent("Could not open/write file " + file));
        }
    }

}
