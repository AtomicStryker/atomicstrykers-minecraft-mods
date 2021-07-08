package atomicstryker.dynamiclights.server;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/**
 * @author AtomicStryker
 * <p>
 * Container class to keep track of IDynamicLightSource instances.
 * Remembers their last position and calls World updates if they move.
 */
public class DynamicLightSourceContainer {
    private final IDynamicLightSource lightSource;

    private final BlockPos.Mutable prevPos = new BlockPos.Mutable();
    private final BlockPos.Mutable curPos = new BlockPos.Mutable();

    private final int yOffset;

    public DynamicLightSourceContainer(IDynamicLightSource light) {
        lightSource = light;
        yOffset = (int) Math.floor(light.getAttachmentEntity().getEyeHeight());
    }

    /**
     * Update passed on from the World tick. Checks for the Light Source Entity to
     * be alive, and for it to have changed Coordinates. Marks it's current Block
     * for Update if it has moved. When this method returns true, the Light Source
     * Entity has died and it should be removed from the List!
     *
     * @return true when the Light Source has died, false otherwise
     */
    public boolean onUpdate() {
        Entity ent = lightSource.getAttachmentEntity();
        if (!ent.isAlive()) {
            return true;
        }

        if (hasEntityMoved(ent)) {
            removePreviousLight(ent.level);
            addLight(ent.level);
        }

        return false;
    }

    public BlockPos getPos() {
        return curPos;
    }

    public IDynamicLightSource getLightSource() {
        return lightSource;
    }

    /**
     * Checks for the Entity coordinates to have changed. Updates internal
     * Coordinates to new position if so.
     *
     * @return true when Entities x, y or z changed, false otherwise
     */
    private boolean hasEntityMoved(Entity ent) {

        // use yOffset so player positions are +1 y, at eye height
        BlockPos newPos = ent.blockPosition().offset(0, yOffset, 0);
        if (!newPos.equals(curPos)) {
            prevPos.set(curPos);
            curPos.set(newPos);
            return true;
        }

        return false;
    }

    private void removePreviousLight(World world) {
        Block previousBlock = world.getBlockState(prevPos).getBlock();
        for (Map.Entry<Block, Block> vanillaBlockToLitBlockEntry : DynamicLights.vanillaBlocksToLitBlocksMap.entrySet()) {
            if (vanillaBlockToLitBlockEntry.getValue().equals(previousBlock)) {
                // previous block is lit, reset it to default block
                world.setBlock(prevPos, vanillaBlockToLitBlockEntry.getKey().defaultBlockState(), 3);
            }
        }
    }

    public void removeLight(World world) {
        // reset previous and current position light blocks if they exist
        removePreviousLight(world);
        Block currentBlock = world.getBlockState(curPos).getBlock();
        for (Map.Entry<Block, Block> vanillaBlockToLitBlockEntry : DynamicLights.vanillaBlocksToLitBlocksMap.entrySet()) {
            if (vanillaBlockToLitBlockEntry.getValue().equals(currentBlock)) {
                // current block is lit, reset it to default block
                world.setBlock(curPos, vanillaBlockToLitBlockEntry.getKey().defaultBlockState(), 3);
            }
        }
    }

    private void addLight(World world) {
        // add light block on current position, depending on what type (air, water)
        BlockState blockState = world.getBlockState(curPos);
        Block currentBlock = blockState.getBlock();
        for (Map.Entry<Block, Block> vanillaBlockToLitBlockEntry : DynamicLights.vanillaBlocksToLitBlocksMap.entrySet()) {
            if (currentBlock.equals(vanillaBlockToLitBlockEntry.getKey())) {

                // this check prevents lit water from substituting anything but a "full" water block
                if (currentBlock instanceof FlowingFluidBlock) {
                    if (blockState.getValue(FlowingFluidBlock.LEVEL) != 0) {
                        return;
                    }
                }
                world.setBlock(curPos, vanillaBlockToLitBlockEntry.getValue().defaultBlockState(), 3);
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DynamicLightSourceContainer) {
            DynamicLightSourceContainer other = (DynamicLightSourceContainer) o;
            if (other.lightSource == this.lightSource) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return lightSource.getAttachmentEntity().hashCode();
    }
}
