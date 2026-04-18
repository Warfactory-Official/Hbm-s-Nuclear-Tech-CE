package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.CeleritasCameraTransformAccess;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public abstract class MixinDefaultChunkRenderer {

    @Dynamic
    @Redirect(method = "setModelMatrixUniforms",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intX:I"),
            require = 1)
    private static int hbm$useUnsafeModelIntX(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getIntX(camera);
    }

    @Dynamic
    @Redirect(method = "setModelMatrixUniforms",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intY:I"),
            require = 1)
    private static int hbm$useUnsafeModelIntY(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getIntY(camera);
    }

    @Dynamic
    @Redirect(method = "setModelMatrixUniforms",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intZ:I"),
            require = 1)
    private static int hbm$useUnsafeModelIntZ(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getIntZ(camera);
    }

    @Dynamic
    @Redirect(method = "setModelMatrixUniforms",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;fracX:F"),
            require = 1)
    private static float hbm$useUnsafeModelFracX(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getFracX(camera);
    }

    @Dynamic
    @Redirect(method = "setModelMatrixUniforms",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;fracY:F"),
            require = 1)
    private static float hbm$useUnsafeModelFracY(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getFracY(camera);
    }

    @Dynamic
    @Redirect(method = "setModelMatrixUniforms",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;fracZ:F"),
            require = 1)
    private static float hbm$useUnsafeModelFracZ(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getFracZ(camera);
    }

    @Dynamic
    @Redirect(method = "fillCommandBuffer",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intX:I"),
            require = 1)
    private static int hbm$useUnsafeIntX(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getIntX(camera);
    }

    @Dynamic
    @Redirect(method = "fillCommandBuffer",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intY:I"),
            require = 1)
    private static int hbm$useUnsafeIntY(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getIntY(camera);
    }

    @Dynamic
    @Redirect(method = "fillCommandBuffer",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;intZ:I"),
            require = 1)
    private static int hbm$useUnsafeIntZ(CameraTransform camera) {
        return CeleritasCameraTransformAccess.getIntZ(camera);
    }
}
