package com.hbm.mixin;

import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.block.BlockLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves {
    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true, require = 1)
    private void hbm$irradiatedDecay(World world, BlockPos pos, CallbackInfo ci) {
        if (GeneralConfig.enableRads && RadiationConfig.worldRadEffects
                && ChunkRadiationManager.proxy.getRadiation(world, pos) >= 5D) {
            world.setBlockToAir(pos);
            ci.cancel();
        }
    }
}
