package atomicstryker.ruins.common;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.PrintWriter;
import java.util.Random;

public class RuinRuleAir extends RuinTemplateRule
{

    public RuinRuleAir(PrintWriter debugPrinter, RuinTemplate r) throws Exception
    {
        super(debugPrinter, r, "0,100,air");
    }

    @Override
    public void doBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        // This will preserve blocks correctly.
        if (!owner.preserveBlock(world.getBlockState(new BlockPos(x, y, z)).getBlock()))
        //if (canReplace(Blocks.AIR, world.getBlockState(new BlockPos(x, y, z)).getBlock(), world, x, y ,z))
        {
            world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
        }
    }

    @Override
    public boolean runLater()
    {
        return false;
    }
}