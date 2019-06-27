package atomicstryker.ruins.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class RuleStringNbtHelper {


    public static String StringFromBlockState(BlockState blockState, TileEntity tileEntity) {
        CompoundNBT tagCompound = NBTUtil.writeBlockState(blockState);
        if (tileEntity != null) {
            CompoundNBT tagTileEntity = tileEntity.write(new CompoundNBT());
            tagCompound.put("ruinsTE", tagTileEntity);
        }
        return tagCompound.toString();
    }

    public static BlockState blockStateFromString(String input) {
        CompoundNBT nbtTagCompound;
        try {
            nbtTagCompound = JsonToNBT.getTagFromJson(input);
        } catch (CommandSyntaxException e) {
            RuinsMod.LOGGER.error("failed to parse block state " + input, e);
            return Blocks.AIR.getDefaultState();
        }
        // strip this away here
        if (nbtTagCompound.contains("ruinsTE")) {
            nbtTagCompound.remove("ruinsTE");
        }
        return NBTUtil.readBlockState(nbtTagCompound);
    }

    public static CompoundNBT tileEntityNBTFromString(String input, int x, int y, int z) {
        CompoundNBT nbtTagCompound;
        try {
            nbtTagCompound = JsonToNBT.getTagFromJson(input);
        } catch (CommandSyntaxException e) {
            return null;
        }
        if (nbtTagCompound.contains("ruinsTE")) {
            CompoundNBT teNbt = nbtTagCompound.getCompound("ruinsTE");
            teNbt.putInt("x", x);
            teNbt.putInt("y", y);
            teNbt.putInt("z", z);
            return teNbt;
        }
        return null;
    }

    // assuming we can have multiple blockstates {nbt}{nbt}{nbt}, split them into a string array. a normal rule will have 1
    public static String[] splitRuleByBrackets(String rule) {
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
                    RuinsMod.LOGGER.error("Error in rule {} at character {}: unbalanced brackets!", rule, i);
                    return null;
                } else if (bracketCounter == 0) {
                    result.add(rule.substring(currentBracketStartIndex, i + 1));
                }
            }
        }
        if (bracketCounter > 0) {
            RuinsMod.LOGGER.error("Error in rule {} unbalanced brackets!", rule);
            return null;
        }
        String[] output = new String[result.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = result.get(i);
        }
        return output;
    }
}
