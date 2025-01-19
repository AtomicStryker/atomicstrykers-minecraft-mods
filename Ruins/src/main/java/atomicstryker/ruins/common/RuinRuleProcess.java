package atomicstryker.ruins.common;


import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

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

    public void doBlock(Level world, RandomSource random) {
        rule.doBlock(world, random, new BlockPos(x, y, z), rotate);
    }
}