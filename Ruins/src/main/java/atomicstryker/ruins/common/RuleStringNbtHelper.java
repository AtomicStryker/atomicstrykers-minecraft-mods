package atomicstryker.ruins.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RuleStringNbtHelper {


    public static String StringFromBlockState(IBlockState blockState) {
        NBTTagCompound tagCompound = NBTUtil.writeBlockState(blockState);
        return tagCompound.toString();
    }

    public static IBlockState blockStateFromString(String input, PrintWriter debugPrinter) {
        NBTTagCompound nbtTagCompound;
        try {
            nbtTagCompound = JsonToNBT.getTagFromJson(input);
        } catch (CommandSyntaxException e) {
            e.printStackTrace(debugPrinter);
            return Blocks.AIR.getDefaultState();
        }
        return NBTUtil.readBlockState(nbtTagCompound);
    }

    // assuming we can have multiple blockstates {nbt}{nbt}{nbt}, split them into a string array. a normal rule will have 1
    public static String[] splitRuleByBrackets(String rule, PrintWriter debugPrinter) {
        List<String> result = new ArrayList<>();
        int currentBracketStartIndex = 0;
        int bracketCounter = 0;
        for (int i = 0; i < rule.length(); i++) {
            if ('{' == rule.charAt(i)) {
                bracketCounter++;
                if (bracketCounter == 1) {
                    currentBracketStartIndex = i;
                }
            } else if ('}' == rule.charAt(i)) {
                bracketCounter--;
                if (bracketCounter < 0) {
                    debugPrinter.printf("Error in rule %s at character %d: unbalanced brackets!", rule, i);
                    return null;
                } else if (bracketCounter == 0) {
                    result.add(rule.substring(currentBracketStartIndex, i + 1));
                }
            }
        }
        if (bracketCounter > 0) {
            debugPrinter.printf("Error in rule %s: unbalanced brackets!", rule);
            return null;
        }
        return (String[]) result.toArray();
    }
}
