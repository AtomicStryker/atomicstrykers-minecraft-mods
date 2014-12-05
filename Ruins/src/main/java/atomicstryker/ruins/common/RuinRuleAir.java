package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
        if (!owner.preserveBlock(world.getBlockState(new BlockPos(x, y, z)).getBlock(), world, x, y, z))
        //if (canReplace(Blocks.air, world.getBlockState(new BlockPos(x, y, z)).getBlock(), world, x, y ,z))
        {
            world.setBlockState(new BlockPos(x, y, z), Blocks.air.getDefaultState(), 2);
        }
    }

    @Override
    public boolean runLater()
    {
        return false;
    }
}