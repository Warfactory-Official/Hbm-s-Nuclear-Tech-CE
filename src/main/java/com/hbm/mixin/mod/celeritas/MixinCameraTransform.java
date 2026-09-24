package com.hbm.mixin.mod.celeritas;

import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.render.chunk.CeleritasCameraTransformAccess.*;

@Mixin(value = CameraTransform.class, remap = false)
public abstract class MixinCameraTransform {

    @Dynamic
    @SuppressWarnings("UnreachableCode")
    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void hbm$repairConstructorOutput(double x, double y, double z, CallbackInfo ci) {
        CameraTransform self = (CameraTransform) (Object) this;
        U.putInt(self, INT_X_OFFSET, (int) x);
        U.putInt(self, INT_Y_OFFSET, (int) y);
        U.putInt(self, INT_Z_OFFSET, (int) z);
        U.putFloat(self, FRAC_X_OFFSET, hbm$fractional(x));
        U.putFloat(self, FRAC_Y_OFFSET, hbm$fractional(y));
        U.putFloat(self, FRAC_Z_OFFSET, hbm$fractional(z));
        U.putDouble(self, X_OFFSET, x);
        U.putDouble(self, Y_OFFSET, y);
        U.putDouble(self, Z_OFFSET, z);
    }

    @Unique
    private static float hbm$fractional(double value) {
        float fraction = (float) (value - (int) value);
        float modifier = Math.copySign(128.0F, fraction);
        return (fraction + modifier) - modifier;
    }
}
