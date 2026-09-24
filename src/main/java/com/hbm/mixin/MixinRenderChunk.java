package com.hbm.mixin;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderChunk.class)
public abstract class MixinRenderChunk {

    @Final
    @Shadow
    private BlockPos.MutableBlockPos position;

    @Inject(method = "makeCompileTaskChunk", at = @At("RETURN"), require = 1)
    private void hbm$captureSectionGeometry(CallbackInfoReturnable<ChunkCompileTaskGenerator> cir) {
        ((ISectionGeometryHolder) cir.getReturnValue()).hbm$sectionGeometry(
                SectionGeometry.capture(position.getX() >> 4, position.getY() >> 4, position.getZ() >> 4));
    }
}
