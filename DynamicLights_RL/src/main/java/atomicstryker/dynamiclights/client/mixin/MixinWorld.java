package atomicstryker.dynamiclights.client.mixin;

import atomicstryker.dynamiclights.client.DynamicLights;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class MixinWorld {

    private static final EnumFacing[] FACING_VALUES = EnumFacing.values();

    @Redirect(
            method = "checkLightFor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getRawLight(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/EnumLightType;)I"
            )
    )
    public int getRawLightProxy(World world, BlockPos pos, EnumLightType lightType) {
        if (lightType == EnumLightType.SKY && world.canSeeSky(pos)) {
            return 15;
        } else {
            IBlockState iblockstate = world.getBlockState(pos);
            int i = lightType == EnumLightType.SKY ? 0 : iblockstate.getLightValue();
            i = DynamicLights.getDynamicLightValue(world, pos, i);
            int j = iblockstate.getOpacity(world, pos);

            if (j >= 15 && iblockstate.getLightValue() > 0) {
                j = 1;
            }

            if (j < 1) {
                j = 1;
            }

            if (j >= 15) {
                return 0;
            } else if (i >= 14) {
                return i;
            } else {
                try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
                    for (EnumFacing enumfacing : FACING_VALUES) {
                        blockpos$pooledmutableblockpos.setPos(pos).move(enumfacing);
                        int k = world.getLightFor(lightType, blockpos$pooledmutableblockpos) - j;

                        if (k > i) {
                            i = k;
                        }

                        if (i >= 14) {
                            int l = i;
                            return l;
                        }
                    }
                    return i;
                }
            }
        }
    }
}
