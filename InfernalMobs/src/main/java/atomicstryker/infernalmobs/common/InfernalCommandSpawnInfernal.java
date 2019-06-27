package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.Map;

public class InfernalCommandSpawnInfernal {

    public static final LiteralArgumentBuilder<CommandSource> BUILDER =
            Commands.literal("spawninfernal")
                    .requires((caller) -> caller.hasPermissionLevel(2))
                    .then(Commands.argument("x", IntegerArgumentType.integer())
                            .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                            .then(Commands.argument("entClass", StringArgumentType.greedyString())
                                                    .then(Commands.argument("modifiers", StringArgumentType.greedyString())
                                                            .executes((caller) -> {
                                                                execute(caller.getSource(), IntegerArgumentType.getInteger(caller, "x"), IntegerArgumentType.getInteger(caller, "y"), IntegerArgumentType.getInteger(caller, "z"), StringArgumentType.getString(caller, "entClass"), StringArgumentType.getString(caller, "modifiers"));
                                                                return 1;
                                                            }))))));

    private static void execute(CommandSource source, int x, int y, int z, String entClassName, String modifiers) {

        Class<? extends LivingEntity> entClass = null;
        for (Map.Entry<ResourceLocation, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            {
                if (entry.getValue().getClassification().getPeacefulCreature() || entry.getValue().getClassification().getAnimal()) {
                    source.sendErrorMessage(new StringTextComponent("Invalid SpawnInfernal command, no Entity [" + entClassName + "] known or peaceful animal"));
                    return;
                }
            }
            LivingEntity mob = (LivingEntity) entry.getValue().create(source.getWorld());
            mob.setPosition(x + 0.5, y + 0.5, z + 0.5);
            source.getWorld().addEntity(mob);

            InfernalMobsCore.proxy.getRareMobs().remove(mob);
            InfernalMobsCore.instance().addEntityModifiersByString(mob, modifiers);
            MobModifier mod = InfernalMobsCore.getMobModifiers(mob);
            if (mod != null) {
                InfernalMobsCore.LOGGER.log(Level.INFO,
                        source.getName() + " spawned: " + InfernalMobsCore.getMobModifiers(mob).getLinkedModNameUntranslated() + " at [" + x + "|" + y + "|" + z + "]");
            } else {
                source.sendErrorMessage(new StringTextComponent("Error adding Infernal Modifier " + modifiers + " to mob " + mob));
            }
        }
    }
}
