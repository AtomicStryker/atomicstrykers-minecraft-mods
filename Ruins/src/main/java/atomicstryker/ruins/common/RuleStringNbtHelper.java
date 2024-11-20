package atomicstryker.ruins.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;


public class RuleStringNbtHelper {
    private static int throttleEntityWarning = 4;

    public static String StringFromBlockState(BlockState blockState, BlockEntity tileEntity) {
        CompoundTag tagCompound = NbtUtils.writeBlockState(blockState);
        if (tileEntity != null) {
            CompoundTag parameters = new CompoundTag();
            CompoundTag tagTileEntity = tileEntity.saveWithFullMetadata(tileEntity.getLevel().registryAccess());
            tagTileEntity.remove("id");
            tagTileEntity.remove("x");
            tagTileEntity.remove("y");
            tagTileEntity.remove("z");
            parameters.put("entity", tagTileEntity);
            tagCompound.put("Ruins", parameters);
        }
        return tagCompound.toString();
    }

    public static BlockState blockStateFromCompound(CompoundTag input) {
        CompoundTag nbtTagCompound = input.copy();
        // strip this away here
        nbtTagCompound.remove("ruinsTE");
        try {
            return NbtUtils.readBlockState(RuinsMod.getInstance().getLastLoadedLevel().holderLookup(Registries.BLOCK), nbtTagCompound);
        } catch (Exception e) {
            RuinsMod.LOGGER.error("failed translating CompoundTag {} to block", nbtTagCompound, e);
            return Blocks.AIR.defaultBlockState();
        }
    }

    public static CompoundTag tileEntityNBTFromCompound(CompoundTag defaultValue, CompoundTag input) {
        CompoundTag teNbt = defaultValue;
        if (input.contains("ruinsTE", 10)) {
            // emit a few deprecation warnings, then demote to debug
            final org.apache.logging.log4j.Level level = throttleEntityWarning > 0
                    ? org.apache.logging.log4j.Level.WARN
                    : org.apache.logging.log4j.Level.DEBUG;
            RuinsMod.LOGGER.log(level, "{ruinsTE:{...}} is deprecated; use {Ruins:{entity:{...}}} instead");
            if (throttleEntityWarning > 0 && --throttleEntityWarning < 1) {
                RuinsMod.LOGGER.warn("suppressing ruinsTE deprecation warnings; limit reached");
            }
            if (defaultValue == null) {
                teNbt = input.getCompound("ruinsTE").copy();
                teNbt.remove("id");
                teNbt.remove("x");
                teNbt.remove("y");
                teNbt.remove("z");
            }
        }
        return teNbt;
    }

    // assuming we can have multiple blockstates {nbt}{nbt}{nbt}, split them into a TAG_Compound list. a normal rule will have 1
    public static List<CompoundTag> splitRuleByBrackets(String rule) {
        List<CompoundTag> result = new ArrayList<>();
        int currentBracketStartIndex = 0;
        int bracketCounter = 0;
        char quote = 0;
        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (quote != 0) {
                if (quote == c) {
                    quote = 0;
                } else if ('\\' == c) {
                    ++i;
                }
            } else if ('{' == c) {
                bracketCounter++;
                if (bracketCounter == 1) {
                    currentBracketStartIndex = i;
                }
            } else if ('}' == c) {
                bracketCounter--;
                if (bracketCounter < 0) {
                    RuinsMod.LOGGER.error("Error in rule {} at character {}: unbalanced brackets!", rule, i);
                    return null;
                } else if (bracketCounter == 0) {
                    CompoundTag nbtTagCompound;
                    try {
                        nbtTagCompound = TagParser.parseTag(rule.substring(currentBracketStartIndex, i + 1));
                    } catch (CommandSyntaxException e) {
                        RuinsMod.LOGGER.error("Error in rule {} starting at character {}: unbalanced brackets!", rule, currentBracketStartIndex);
                        return null;
                    }
                    result.add(nbtTagCompound);
                }
            } else if ('"' == c || '\'' == c) {
                quote = c;
            }
        }
        if (quote != 0) {
            RuinsMod.LOGGER.error("Error in rule {} unbalanced quotes!", rule);
            return null;
        }
        if (bracketCounter > 0) {
            RuinsMod.LOGGER.error("Error in rule {} unbalanced brackets!", rule);
            return null;
        }
        return result;
    }
}
