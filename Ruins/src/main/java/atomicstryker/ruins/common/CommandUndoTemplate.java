package atomicstryker.ruins.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class CommandUndoTemplate {

    private static final ArrayList<TemplateArea> savedLocations = new ArrayList<>();
    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("undoruin")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .executes((caller) -> {
                        execute(caller.getSource());
                        return 1;
                    });
    private static RuinTemplate runningTemplateSpawn;

    public CommandUndoTemplate() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static void execute(CommandSource source) {
        World w = source.getWorld();
        if (savedLocations.isEmpty()) {
            source.sendErrorMessage(new TranslationTextComponent("There is nothing cached to be undone..."));
        } else {
            for (TemplateArea ta : savedLocations) {
                for (int x = 0; x < ta.blockArray.length; x++) {
                    for (int y = 0; y < ta.blockArray[0].length; y++) {
                        for (int z = 0; z < ta.blockArray[0][0].length; z++) {
                            w.setBlockState(new BlockPos(ta.xBase + x, ta.yBase + y, ta.zBase + z), ta.blockArray[x][y][z], 2);
                        }
                    }
                }

                // kill off the resulting entityItems instances
                w.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(new BlockPos(ta.xBase - 1, ta.yBase - 1, ta.zBase - 1),
                        new BlockPos(ta.xBase + ta.blockArray.length + 1, ta.yBase + ta.blockArray[0].length + 1, ta.zBase + ta.blockArray[0][0].length + 1))).forEach(Entity::onKillCommand);
            }
            source.sendFeedback(new TranslationTextComponent("Cleared away " + savedLocations.size() + " template sites."), false);
            savedLocations.clear();
        }
    }

    @SubscribeEvent
    public void onSpawningRuin(EventRuinTemplateSpawn event) {
        if (event.testingRuin || runningTemplateSpawn != null) {
            if (event.isPrePhase) {
                // firing the first template event, adjacents may or may not
                // come
                if (runningTemplateSpawn == null) {
                    runningTemplateSpawn = event.template;
                    // flush the last locations
                    savedLocations.clear();
                } else {
                    System.out.println("Ruins undo command caught adjacent template, saving it too..");
                }

                RuinData data = event.template.getRuinData(event.x, event.y, event.z, event.rotation);
                TemplateArea ta = new TemplateArea();
                ta.xBase = data.xMin;
                ta.yBase = data.yMin;
                ta.zBase = data.zMin;
                ta.blockArray = new BlockState[data.xMax - data.xMin + 1][data.yMax - data.yMin + 1][data.zMax - data.zMin + 1];
                for (int x = 0; x < ta.blockArray.length; x++) {
                    for (int y = 0; y < ta.blockArray[0].length; y++) {
                        for (int z = 0; z < ta.blockArray[0][0].length; z++) {
                            ta.blockArray[x][y][z] = event.getWorld().getBlockState(new BlockPos(ta.xBase + x, ta.yBase + y, ta.zBase + z));
                        }
                    }
                }
                savedLocations.add(ta);

                if (savedLocations.size() > 100) {
                    // safety overflow valve in case something goes wrong
                    savedLocations.clear();
                    runningTemplateSpawn = null;
                }
            } else if (runningTemplateSpawn == event.template) {
                // finished spawning all adjacents, post event of initial
                // template firing
                runningTemplateSpawn = null;
                // since this is null the savedLocations will be cleared then
                // the next spawn occurs
            }
        }
    }

    private class TemplateArea {
        BlockState[][][] blockArray;
        int xBase, yBase, zBase;
    }

}
