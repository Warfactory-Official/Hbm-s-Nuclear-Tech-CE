package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.CeleritasCameraTransformAccess;
import com.hbm.render.chunk.ChunkSpanningTesrHelper;
import com.hbm.render.chunk.IExtraExtentsHolder;
import net.minecraft.tileentity.TileEntity;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.data.BuiltRenderSectionData;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinRenderSectionManager {

    @Dynamic
    @Inject(method = "updateSectionInfo", at = @At("HEAD"), require = 1)
    private void hbm$updateChunkSpanningTesrSet(RenderSection render, BuiltRenderSectionData info,
                                                CallbackInfoReturnable<Boolean> cir) {
        BuiltRenderSectionData oldInfo = render.getBuiltContext();
        TileEntity[] old = oldInfo != null
                ? ((IExtraExtentsHolder) oldInfo).hbm$getChunkSpanningTesrs()
                : IExtraExtentsHolder.EMPTY_TE_ARR;
        TileEntity[] nu = info != null
                ? ((IExtraExtentsHolder) info).hbm$getChunkSpanningTesrs()
                : IExtraExtentsHolder.EMPTY_TE_ARR;
        if (old.length != 0 || nu.length != 0) {
            ChunkSpanningTesrHelper.updateChunkSpanningTesrs(old, nu);
        }
    }

    @Dynamic
    @Inject(method = "destroy", at = @At("HEAD"), require = 1)
    private void hbm$clearOnDestroy(CallbackInfo ci) {
        ChunkSpanningTesrHelper.clear();
    }

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
