package atomicstryker.ruins.common;

import java.util.Random;

import net.minecraft.world.World;

public class RuinRuleProcess {
	private RuinTemplateRule rule;
	private int x, y, z, rotate;
	private boolean isUnderwater;

	public RuinRuleProcess( RuinTemplateRule r, int xbase, int ybase, int zbase, int rot ) {
		rule = r;
		x = xbase;
		y = ybase;
		z = zbase;
		rotate = rot;
	}

	public void doBlock( World world, Random random ) {
		rule.doBlock( world, random, x, y, z, rotate );
	}
}