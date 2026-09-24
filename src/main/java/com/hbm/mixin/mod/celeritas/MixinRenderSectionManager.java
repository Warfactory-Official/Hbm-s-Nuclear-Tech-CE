package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.CeleritasCameraTransformAccess;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinRenderSectionManager {

    @Dynamic
    @Redirect(method = "update",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;x:D"),
            require = 1)
    private double hbm$useUnsafeCameraX(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getX(transform);
    }

    @Dynamic
    @Redirect(method = "update",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;y:D"),
            require = 1)
    private double hbm$useUnsafeCameraY(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getY(transform);
    }

    @Dynamic
    @Redirect(method = "update",
            at = @At(value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;z:D"),
            require = 1)
    private double hbm$useUnsafeCameraZ(CameraTransform transform) {
        return CeleritasCameraTransformAccess.getZ(transform);
    }
}
