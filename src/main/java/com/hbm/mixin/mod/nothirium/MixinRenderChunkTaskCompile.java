package com.hbm.mixin.mod.nothirium;

import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.render.chunk.IExtraExtentsHolder;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderChunkTaskCompile.class, remap = false)
public abstract class MixinRenderChunkTaskCompile {

    @Shadow
    @Final
    protected AbstractRenderChunk renderChunk;

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;

    @Inject(method = "run", at = @At("HEAD"))
    private void hbm$resetOversizedExtents(CallbackInfoReturnable<?> cir) {
        hbm$negX = 0;
        hbm$posX = 0;
        hbm$negY = 0;
        hbm$posY = 0;
        hbm$negZ = 0;
        hbm$posZ = 0;
    }

    @Redirect(method = "renderBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z"), remap = true)
    private boolean hbm$trackOversizedBlock(BlockRendererDispatcher dispatcher, IBlockState state, BlockPos pos,
                                            IBlockAccess world, BufferBuilder buffer) {
        boolean result = dispatcher.renderBlock(state, pos, world, buffer);
        if (result) {
            int[] extents = StaticTesrBakedModels.getManagedRenderExtents(state);
            if (extents != null) {
                int localX = pos.getX() - renderChunk.getX();
                int localY = pos.getY() - renderChunk.getY();
                int localZ = pos.getZ() - renderChunk.getZ();

                hbm$negX = Math.max(hbm$negX, extents[4] - localX);
                hbm$posX = Math.max(hbm$posX, localX + extents[5] - 15);
                hbm$negY = Math.max(hbm$negY, extents[1] - localY);
                hbm$posY = Math.max(hbm$posY, localY + extents[0] - 15);
                hbm$negZ = Math.max(hbm$negZ, extents[2] - localZ);
                hbm$posZ = Math.max(hbm$posZ, localZ + extents[3] - 15);
            }
        }
        return result;
    }

    @Redirect(method = "compileSection(Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Lmeldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult;", at = @At(value = "INVOKE", target = "Lmeldexun/nothirium/util/VisibilityGraph;compute()Lmeldexun/nothirium/util/VisibilitySet;"), remap = false)
    private meldexun.nothirium.util.VisibilitySet hbm$publishOversizedExtents(meldexun.nothirium.util.VisibilityGraph visibilityGraph) {
        meldexun.nothirium.util.VisibilitySet visibilitySet = visibilityGraph.compute();
        ((IExtraExtentsHolder) visibilitySet).hbm$setOversizedModelExtents(hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ);
        return visibilitySet;
    }
}
