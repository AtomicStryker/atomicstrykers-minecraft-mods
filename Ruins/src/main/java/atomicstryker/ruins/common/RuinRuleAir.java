package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class RuinRuleAir extends RuinTemplateRule
{

    public RuinRuleAir(PrintWriter debugPrinter, RuinTemplate r, String rule) throws Exception
    {
        super(debugPrinter, r, "0,100,air");
    }

    @Override
    public void doBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        // This will preserve blocks correctly.
        if (canReplace(Blocks.air, world.getBlock(x, y, z), world, x, y ,z))
        {
            world.setBlock(x, y, z, Blocks.air, 0, 3);
        }
    }

    @Override
    public boolean runLater()
    {
        return false;
    }
}