package ic2.api.energy.tile;

import ic2.api.Direction;
import net.minecraft.tileentity.TileEntity;

/**
 * For internal usage only.
 *
 * @see IEnergySink
 * @see IEnergyConductor
 */
public interface IEnergyAcceptor extends IEnergyTile {
	/**
	 * Determine if this acceptor can accept current from an adjacent emitter in a direction.
	 * 
	 * @param emitter energy emitter
	 * @param direction direction the energy is being received from
	 */
	boolean acceptsEnergyFrom(TileEntity emitter, Direction direction);
}

