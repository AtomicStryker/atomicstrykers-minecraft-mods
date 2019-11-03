package atomicstryker.ruins.common;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;

public class RuleStringNbtHelper {


    public static String StringFromBlockState(BlockState blockState, TileEntity tileEntity) {
        CompoundNBT tagCompound = NBTUtil.writeBlockState(blockState);
        if (tileEntity != null) {
            CompoundNBT parameters = new CompoundNBT();
            CompoundNBT tagTileEntity = tileEntity.write(new CompoundNBT());
            tagTileEntity.remove("id");
            tagTileEntity.remove("x");
            tagTileEntity.remove("y");
            tagTileEntity.remove("z");
            parameters.put("entity", tagTileEntity);
            tagCompound.put("Ruins", parameters);
        }
        return tagCompound.toString();
    }

    public static BlockState blockStateFromCompound(CompoundNBT input) {
        CompoundNBT nbtTagCompound = input.copy();
        // strip this away here
        nbtTagCompound.remove("ruinsTE");
        return NBTUtil.readBlockState(nbtTagCompound);
    }

    public static CompoundNBT tileEntityNBTFromCompound(CompoundNBT defaultValue, CompoundNBT input) {
        CompoundNBT teNbt = defaultValue;
        if (input.contains("ruinsTE", 10)) {
            RuinsMod.LOGGER.warn("{ruinsTE:{...}} is deprecated; use {Ruins:{entity:{...}}} instead");
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
    public static List<CompoundNBT> splitRuleByBrackets(String rule) {
        List<CompoundNBT> result = new ArrayList<>();
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
                    CompoundNBT nbtTagCompound;
                    try {
                        nbtTagCompound = JsonToNBT.getTagFromJson(rule.substring(currentBracketStartIndex, i + 1));
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
