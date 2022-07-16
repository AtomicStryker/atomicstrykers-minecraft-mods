package atomicstryker.infernalmobs.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

public class InfernalCommandSpawnInfernal {

    public static final LiteralArgumentBuilder<CommandSourceStack> BUILDER =
            Commands.literal("spawninfernal")
                    .requires((caller) -> caller.hasPermission(2))
                    .then(Commands.argument("x", IntegerArgumentType.integer())
                            .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                            .then(Commands.argument("entClass", StringArgumentType.word())
                                                    .then(Commands.argument("modifiers", StringArgumentType.greedyString())
                                                            .executes((caller) -> {
                                                                execute(caller.getSource(), IntegerArgumentType.getInteger(caller, "x"), IntegerArgumentType.getInteger(caller, "y"), IntegerArgumentType.getInteger(caller, "z"), StringArgumentType.getString(caller, "entClass"), StringArgumentType.getString(caller, "modifiers"));
                                                                return 1;
                                                            }))))));

    private static void execute(CommandSourceStack source, int x, int y, int z, String entClassName, String modifiers) {

        EntityType<?> chosenType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entClassName));
        if (chosenType == null || chosenType.getCategory().isFriendly() || chosenType.getCategory().isPersistent()) {
            source.sendFailure(Component.literal("Invalid SpawnInfernal command, no Entity Resource [" + entClassName + "] known or noncombat entity type"));
            return;
        }

        LivingEntity mob = (LivingEntity) chosenType.create(source.getLevel());
        if (mob == null) {
            source.sendFailure(Component.literal("Invalid SpawnInfernal command, failed to create [" + entClassName + "] instance in world"));
        }

        mob.setPos(x + 0.5, y + 0.5, z + 0.5);
        source.getLevel().addFreshEntity(mob);

        SidedCache.getInfernalMobs(mob.level).remove(mob);
        InfernalMobsCore.instance().addEntityModifiersByString(mob, modifiers);
        MobModifier mod = InfernalMobsCore.getMobModifiers(mob);
        if (mod != null) {
            InfernalMobsCore.LOGGER.log(Level.INFO,
                    source.getTextName() + " spawned: " + InfernalMobsCore.getMobModifiers(mob).getLinkedModNameUntranslated() + " at [" + x + "|" + y + "|" + z + "]");
        } else {
            source.sendFailure(Component.literal("Error adding Infernal Modifier " + modifiers + " to mob " + mob));
        }
    }
}
