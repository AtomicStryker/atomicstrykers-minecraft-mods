package atomicstryker.powerconverters.common;

import buildcraft.BuildCraftEnergy;

public class TileEntityOilFabricator extends TileEntityLiquidFab
{
	public TileEntityOilFabricator()
	{
		super(PowerConverterCore.oilUnitCostInEU * 5, BuildCraftEnergy.oilStill.blockID, PowerConverterCore.oilUnitCostInEU);
	}
}
