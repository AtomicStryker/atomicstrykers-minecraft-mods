package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.WorldEvent;

public class EnergyTileEvent extends WorldEvent {
	public final IEnergyTile energyTile;
	
	public EnergyTileEvent(IEnergyTile energyTile) {
		super(((TileEntity) energyTile).worldObj);
		
		this.energyTile = energyTile;
	}
}

