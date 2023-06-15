package atomicstryker.dynamiclights.server;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;

/**
 * @author AtomicStryker
 * <p>
 * Container class to keep track of IDynamicLightSource instances.
 * Remembers their last position and calls World updates if they move.
 */
public class DynamicLightSourceContainer {
    private final IDynamicLightSource lightSource;

    private final BlockPos.MutableBlockPos activeLightPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos curSourcePos = new BlockPos.MutableBlockPos();

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
            BlockPos nextPos = findNewCurLightPos(ent.level());
            if (nextPos != null && !nextPos.equals(activeLightPos)) {
                removeLight(ent.level());
                addLight(ent.level(), nextPos, lightSource.getLightLevel());
            }
            // note: if no new position can be found, the light will actually remain active at the previous position
        }

        return false;
    }

    /**
     * get the current light block position, not that this is not necessarily the same as the light source position
     */
    public BlockPos getLightPos() {
        return activeLightPos;
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
        if (!newPos.equals(curSourcePos)) {
            curSourcePos.set(newPos);
            return true;
        }

        return false;
    }

    public void removeLight(Level world) {
        Block previousBlock = world.getBlockState(activeLightPos).getBlock();
        for (Map.Entry<Block, Block> vanillaBlockToLitBlockEntry : DynamicLights.vanillaBlocksToLitBlocksMap.entrySet()) {
            if (vanillaBlockToLitBlockEntry.getValue().equals(previousBlock)) {
                // is light substitution block, replace it with the dark original again
                world.setBlock(activeLightPos, vanillaBlockToLitBlockEntry.getKey().defaultBlockState(), 3);
            }
        }
    }

    // when the desired coordinate cannot be substituted, try the adjacent block coords ... up, down, left, right, forward, back
    final int[][] candidatePositionOffsets = {{0, 0, 0}, {0, 0, 1}, {0, 0, -1}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

    // try and find a new light substitution block position, return null if none can be found
    private BlockPos findNewCurLightPos(Level world) {
        for (int[] offsetTriple : candidatePositionOffsets) {
            BlockPos posWithOffset = curSourcePos.offset(offsetTriple[0], offsetTriple[1], offsetTriple[2]);
            BlockState blockState = world.getBlockState(posWithOffset);
            Block currentBlock = blockState.getBlock();
            for (Map.Entry<Block, Block> vanillaBlockToLitBlockEntry : DynamicLights.vanillaBlocksToLitBlocksMap.entrySet()) {
                if (currentBlock.equals(vanillaBlockToLitBlockEntry.getKey())) {
                    // this check prevents lit water from substituting anything but a "full" water block
                    if (currentBlock instanceof LiquidBlock) {
                        if (blockState.getValue(LiquidBlock.LEVEL) != 0) {
                            continue;
                        }
                    }
                    return posWithOffset;
                }
            }
        }

        return null;
    }

    private void addLight(Level world, BlockPos nextPos, int lightLevel) {
        // add light block on for which we already determined substitution is possible
        BlockState blockState = world.getBlockState(nextPos);
        Block currentBlock = blockState.getBlock();
        for (Map.Entry<Block, Block> vanillaBlockToLitBlockEntry : DynamicLights.vanillaBlocksToLitBlocksMap.entrySet()) {
            if (currentBlock.equals(vanillaBlockToLitBlockEntry.getKey())) {
                world.setBlock(nextPos, vanillaBlockToLitBlockEntry.getValue().defaultBlockState().setValue(BlockStateProperties.POWER, lightLevel), 3);
                // schedule a block tick 5 seconds into the future, as fallback for the block to clean itself up
                world.scheduleTick(nextPos, vanillaBlockToLitBlockEntry.getValue(), 150);
                activeLightPos.set(nextPos);
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DynamicLightSourceContainer other) {
            return other.lightSource == this.lightSource;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return lightSource.getAttachmentEntity().hashCode();
    }
}