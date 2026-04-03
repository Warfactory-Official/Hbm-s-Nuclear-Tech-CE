package com.hbm.mixin.mod.neonium;

import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.render.chunk.IExtraExtentsHolder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = ChunkRenderRebuildTask.class, remap = false)
public abstract class MixinChunkRenderRebuildTask {

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;
    @Unique
    private final ArrayList<TileEntity> hbm$spanningTesrs = new ArrayList<>();
    @Unique
    private int hbm$currentNorth, hbm$currentSouth, hbm$currentWest, hbm$currentEast, hbm$currentUp, hbm$currentDown;
    @Unique
    private boolean hbm$shouldTrackCurrentBlock;

    @Inject(method = "performBuild", at = @At("HEAD"))
    private void hbm$resetOversizedExtents(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
                                           CancellationSource cancellationSource,
                                           CallbackInfoReturnable<ChunkBuildResult<?>> cir) {
        hbm$negX = 0;
        hbm$posX = 0;
        hbm$negY = 0;
        hbm$posY = 0;
        hbm$negZ = 0;
        hbm$posZ = 0;
        hbm$shouldTrackCurrentBlock = false;
        hbm$spanningTesrs.clear();
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getRenderType()Lnet/minecraft/util/EnumBlockRenderType;"), remap = true)
    private EnumBlockRenderType hbm$trackCurrentBlock(IBlockState state) {
        int[] extents = StaticTesrBakedModels.getManagedRenderExtents(state);
        if (extents == null) {
            hbm$shouldTrackCurrentBlock = false;
            return state.getRenderType();
        }

        hbm$currentNorth = extents[2];
        hbm$currentSouth = extents[3];
        hbm$currentWest = extents[4];
        hbm$currentEast = extents[5];
        hbm$currentUp = extents[0];
        hbm$currentDown = extents[1];
        hbm$shouldTrackCurrentBlock = true;
        return state.getRenderType();
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds$Builder;addBlock(III)V"), remap = false)
    private void hbm$trackRenderedBlock(ChunkRenderBounds.Builder bounds, int relX, int relY, int relZ) {
        if (hbm$shouldTrackCurrentBlock) {
            hbm$negX = Math.max(hbm$negX, hbm$currentWest - relX);
            hbm$posX = Math.max(hbm$posX, relX + hbm$currentEast - 15);
            hbm$negY = Math.max(hbm$negY, hbm$currentDown - relY);
            hbm$posY = Math.max(hbm$posY, relY + hbm$currentUp - 15);
            hbm$negZ = Math.max(hbm$negZ, hbm$currentNorth - relZ);
            hbm$posZ = Math.max(hbm$posZ, relZ + hbm$currentSouth - 15);
        }
        bounds.addBlock(relX, relY, relZ);
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/VisGraph;computeVisibility()Lnet/minecraft/client/renderer/chunk/SetVisibility;"), remap = true)
    private SetVisibility hbm$publishOversizedExtents(VisGraph occluder) {
        SetVisibility visibility = occluder.computeVisibility();
        IExtraExtentsHolder holder = (IExtraExtentsHolder) visibility;
        holder.hbm$setOversizedModelExtents(hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ);
        if (!hbm$spanningTesrs.isEmpty()) {
            holder.hbm$setChunkSpanningTesrs(hbm$spanningTesrs.toArray(new TileEntity[0]));
        }
        return visibility;
    }

    @Dynamic
    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;addBlockEntity(Lnet/minecraft/tileentity/TileEntity;Z)V"), remap = false)
    private void hbm$trackOversizedTesr(ChunkRenderData.Builder renderData, TileEntity te, boolean cull) {
        if (cull) {
            AxisAlignedBB bb = te.getRenderBoundingBox();
            if (bb == TileEntity.INFINITE_EXTENT_AABB) {
                cull = false;
            } else {
                int sx = te.getPos().getX() & ~15;
                int sy = te.getPos().getY() & ~15;
                int sz = te.getPos().getZ() & ~15;
                int negX = (int) Math.ceil(Math.max(0.0D, sx - bb.minX));
                int posX = (int) Math.ceil(Math.max(0.0D, bb.maxX - (sx + 16.0D)));
                int negY = (int) Math.ceil(Math.max(0.0D, sy - bb.minY));
                int posY = (int) Math.ceil(Math.max(0.0D, bb.maxY - (sy + 16.0D)));
                int negZ = (int) Math.ceil(Math.max(0.0D, sz - bb.minZ));
                int posZ = (int) Math.ceil(Math.max(0.0D, bb.maxZ - (sz + 16.0D)));
                hbm$negX = Math.max(hbm$negX, negX);
                hbm$posX = Math.max(hbm$posX, posX);
                hbm$negY = Math.max(hbm$negY, negY);
                hbm$posY = Math.max(hbm$posY, posY);
                hbm$negZ = Math.max(hbm$negZ, negZ);
                hbm$posZ = Math.max(hbm$posZ, posZ);
                if ((negX | posX | negY | posY | negZ | posZ) != 0) {
                    hbm$spanningTesrs.add(te);
                }
            }
        }
        renderData.addBlockEntity(te, cull);
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;setBounds(Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds;)V"), remap = false)
    private void hbm$expandBounds(ChunkRenderData.Builder renderData, ChunkRenderBounds original) {
        if ((hbm$negX | hbm$posX | hbm$negY | hbm$posY | hbm$negZ | hbm$posZ) == 0) {
            renderData.setBounds(original);
            return;
        }

        renderData.setBounds(new ChunkRenderBounds(
                original.x1 - hbm$negX,
                original.y1 - hbm$negY,
                original.z1 - hbm$negZ,
                original.x2 + hbm$posX,
                original.y2 + hbm$posY,
                original.z2 + hbm$posZ
        ));
    }
}
