package com.hbm.mixin;

import com.hbm.render.chunk.ISectionGeometryTile;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileEntitySpecialRenderer.class)
public abstract class MixinTileEntitySpecialRenderer {

    @ModifyReturnValue(method = "isGlobalRenderer", at = @At("RETURN"))
    private boolean hbm$sectionGeometryGlobal(boolean original, @Local(argsOnly = true) TileEntity tile) {
        return original || ((ISectionGeometryTile) tile).hbm$globalRender();
    }
}
