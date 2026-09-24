package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.CeleritasCameraTransformAccess;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Viewport.class, remap = false)
public abstract class MixinViewport {

    @Dynamic
    @Redirect(
            method = {"isBoxVisible(DDDDDD)Z", "isBoxVisible(IIIFFF)Z"},
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intX:I"),
            require = 3)
    private int hbm$useUnsafeIntX(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getIntX(transform);
    }

    @Dynamic
    @Redirect(
            method = {"isBoxVisible(DDDDDD)Z", "isBoxVisible(IIIFFF)Z"},
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intY:I"),
            require = 3)
    private int hbm$useUnsafeIntY(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getIntY(transform);
    }

    @Dynamic
    @Redirect(
            method = {"isBoxVisible(DDDDDD)Z", "isBoxVisible(IIIFFF)Z"},
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intZ:I"),
            require = 3)
    private int hbm$useUnsafeIntZ(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getIntZ(transform);
    }

    @Dynamic
    @Redirect(
            method = {"isBoxVisible(DDDDDD)Z", "isBoxVisible(IIIFFF)Z"},
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;fracX:F"),
            require = 3)
    private float hbm$useUnsafeFracX(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getFracX(transform);
    }

    @Dynamic
    @Redirect(
            method = {"isBoxVisible(DDDDDD)Z", "isBoxVisible(IIIFFF)Z"},
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;fracY:F"),
            require = 3)
    private float hbm$useUnsafeFracY(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getFracY(transform);
    }

    @Dynamic
    @Redirect(
            method = {"isBoxVisible(DDDDDD)Z", "isBoxVisible(IIIFFF)Z"},
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;fracZ:F"),
            require = 3)
    private float hbm$useUnsafeFracZ(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getFracZ(transform);
    }
}
