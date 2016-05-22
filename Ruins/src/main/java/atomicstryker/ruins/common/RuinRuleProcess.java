package atomicstryker.ruins.common;

import java.util.Random;

import net.minecraft.world.World;

class RuinRuleProcess
{

    private final RuinTemplateRule rule;
    private final int x, y, z, rotate;

    public RuinRuleProcess(RuinTemplateRule r, int xbase, int ybase, int zbase, int rot)
    {
        rule = r;
        x = xbase;
        y = ybase;
        z = zbase;
        rotate = rot;
    }

    public void doBlock(World world, Random random)
    {
        rule.doBlock(world, random, x, y, z, rotate);
    }
}