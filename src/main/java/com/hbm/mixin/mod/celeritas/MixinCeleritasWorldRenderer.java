package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.ChunkSpanningTesrHelper;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;
import org.taumc.celeritas.impl.render.terrain.VintageRenderSectionManager;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public abstract class MixinCeleritasWorldRenderer extends SimpleWorldRenderer<WorldClient, VintageRenderSectionManager, BlockRenderLayer, TileEntity, CeleritasWorldRenderer.TileEntityRenderContext> {

    @Unique
    private final ReferenceOpenHashSet<TileEntity> hbm$renderedTileEntities = new ReferenceOpenHashSet<>();

    @Dynamic
    @Inject(method = "renderBlockEntities", at = @At("HEAD"), require = 1)
    private void hbm$beginTileEntityFrame(CeleritasWorldRenderer.TileEntityRenderContext context,
                                          CallbackInfoReturnable<Integer> cir) {
        hbm$renderedTileEntities.clear();
    }

    @Dynamic
    @Redirect(method = "renderBlockEntityList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"), remap = true, require = 1)
    private void hbm$renderTileEntityOnce(TileEntityRendererDispatcher dispatcher, TileEntity tileEntity,
                                          float partialTicks, int destroyStage) {
        if (destroyStage >= 0 || hbm$renderedTileEntities.add(tileEntity)) {
            dispatcher.render(tileEntity, partialTicks, destroyStage);
        }
    }

    @Dynamic
    @Inject(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V", shift = At.Shift.BEFORE), require = 1)
    private void hbm$renderChunkSpanningTesrs(CeleritasWorldRenderer.TileEntityRenderContext context,
                                              CallbackInfoReturnable<Integer> cir) {
        int pass = MinecraftForgeClient.getRenderPass();
        float partialTicks = context.partialTicks();
        RenderSectionManager mgr = renderSectionManager;

        for (TileEntity tileEntity : ChunkSpanningTesrHelper.getChunkSpanningTesrs()) {
            if (tileEntity.isInvalid()) continue;
            if (!tileEntity.shouldRenderInPass(pass)) continue;
            if (!hbm$isTesrVisibleInSections(mgr, tileEntity)) continue;
            if (!hbm$renderedTileEntities.add(tileEntity)) continue;
            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
        }
    }

    @Unique
    private static boolean hbm$isTesrVisibleInSections(RenderSectionManager mgr, TileEntity tileEntity) {
        AxisAlignedBB bb = tileEntity.getRenderBoundingBox();
        if (bb == null || bb == TileEntity.INFINITE_EXTENT_AABB) {
            return false;
        }
        int minSx = MathHelper.floor(bb.minX) >> 4;
        int minSy = MathHelper.floor(bb.minY) >> 4;
        int minSz = MathHelper.floor(bb.minZ) >> 4;
        int maxSx = MathHelper.floor(Math.nextAfter(bb.maxX, Double.NEGATIVE_INFINITY)) >> 4;
        int maxSy = MathHelper.floor(Math.nextAfter(bb.maxY, Double.NEGATIVE_INFINITY)) >> 4;
        int maxSz = MathHelper.floor(Math.nextAfter(bb.maxZ, Double.NEGATIVE_INFINITY)) >> 4;
        for (int sx = minSx; sx <= maxSx; sx++) {
            for (int sy = minSy; sy <= maxSy; sy++) {
                for (int sz = minSz; sz <= maxSz; sz++) {
                    if (mgr.isSectionVisible(sx, sy, sz)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
