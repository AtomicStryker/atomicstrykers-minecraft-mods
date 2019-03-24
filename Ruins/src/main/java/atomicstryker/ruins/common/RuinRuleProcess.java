package atomicstryker.ruins.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

class RuinRuleProcess {

    private final RuinTemplateRule rule;
    private final int x, y, z, rotate;

    public RuinRuleProcess(RuinTemplateRule r, int xbase, int ybase, int zbase, int rot) {
        rule = r;
        x = xbase;
        y = ybase;
        z = zbase;
        rotate = rot;
    }

    public void doBlock(World world, Random random) {
        rule.doBlock(world, random, new BlockPos(x, y, z), rotate);
    }
}