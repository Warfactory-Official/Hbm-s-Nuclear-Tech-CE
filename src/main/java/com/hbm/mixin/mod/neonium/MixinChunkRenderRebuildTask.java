package com.hbm.mixin.mod.neonium;

import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.render.chunk.IExtraExtentsHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChunkRenderRebuildTask.class, remap = false)
public abstract class MixinChunkRenderRebuildTask {

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;
    @Unique
    private TileEntity[] hbm$spanningTesrs;
    @Unique
    private int hbm$spanningTesrCount;

    @Dynamic
    @WrapOperation(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds$Builder;addBlock(III)V"), remap = false, require = 3)
    private void hbm$trackRenderedBlock(ChunkRenderBounds.Builder bounds, int relX, int relY, int relZ,
                                        Operation<Void> original,
                                        @Local IBlockState blockState) {
        int[] extents = StaticTesrBakedModels.getManagedRenderExtents(blockState);
        if (extents != null) {
            hbm$negX = Math.max(hbm$negX, extents[4] - relX);
            hbm$posX = Math.max(hbm$posX, relX + extents[5] - 15);
            hbm$negY = Math.max(hbm$negY, extents[1] - relY);
            hbm$posY = Math.max(hbm$posY, relY + extents[0] - 15);
            hbm$negZ = Math.max(hbm$negZ, extents[2] - relZ);
            hbm$posZ = Math.max(hbm$posZ, relZ + extents[3] - 15);
        }
        original.call(bounds, relX, relY, relZ);
    }

    @Dynamic
    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/VisGraph;computeVisibility()Lnet/minecraft/client/renderer/chunk/SetVisibility;"), remap = true, require = 1)
    private SetVisibility hbm$publishOversizedExtents(VisGraph occluder) {
        SetVisibility visibility = occluder.computeVisibility();
        IExtraExtentsHolder holder = (IExtraExtentsHolder) visibility;
        holder.hbm$setOversizedModelExtents(hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ);
        int count = hbm$spanningTesrCount;
        if (count != 0) {
            TileEntity[] compact;
            if (count == hbm$spanningTesrs.length) {
                compact = hbm$spanningTesrs;
            } else {
                compact = new TileEntity[count];
                System.arraycopy(hbm$spanningTesrs, 0, compact, 0, count);
            }
            holder.hbm$setChunkSpanningTesrs(compact);
        }
        return visibility;
    }

    @Dynamic
    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;addBlockEntity(Lnet/minecraft/tileentity/TileEntity;Z)V"), remap = false, require = 1)
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
                if ((negX | posX | negY | posY | negZ | posZ) != 0) {
                    hbm$negX = Math.max(hbm$negX, negX);
                    hbm$posX = Math.max(hbm$posX, posX);
                    hbm$negY = Math.max(hbm$negY, negY);
                    hbm$posY = Math.max(hbm$posY, posY);
                    hbm$negZ = Math.max(hbm$negZ, negZ);
                    hbm$posZ = Math.max(hbm$posZ, posZ);
                    hbm$addSpanningTesr(te);
                }
            }
        }
        renderData.addBlockEntity(te, cull);
    }

    @Unique
    private void hbm$addSpanningTesr(TileEntity te) {
        TileEntity[] arr = hbm$spanningTesrs;
        int count = hbm$spanningTesrCount;
        if (arr == null) {
            arr = new TileEntity[4];
            hbm$spanningTesrs = arr;
        } else if (count == arr.length) {
            TileEntity[] grown = new TileEntity[count << 1];
            System.arraycopy(arr, 0, grown, 0, count);
            arr = grown;
            hbm$spanningTesrs = arr;
        }
        arr[count] = te;
        hbm$spanningTesrCount = count + 1;
    }

    @Dynamic
    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;setBounds(Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds;)V"), remap = false, require = 1)
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
