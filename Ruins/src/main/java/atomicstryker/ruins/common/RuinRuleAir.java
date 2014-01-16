package atomicstryker.ruins.common;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class RuinRuleAir extends RuinTemplateRule
{

    public RuinRuleAir(RuinTemplate r, String rule) throws Exception
    {
        super(r, "0,100,0");
    }

    @Override
    public void doBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        // This will preserve blocks correctly.
        if (canReplace(Blocks.air, world.func_147439_a(x, y, z)))
        {
            world.func_147465_d(x, y, z, Blocks.air, 0, 3);
        }
    }

    @Override
    public boolean runLater()
    {
        return false;
    }
}