package com.hbm.mixin.mod.celeritas;

import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.render.chunk.IExtraExtentsHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.compile.task.ChunkBuilderMeshingTask;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class MixinChunkBuilderMeshingTask {

    @Shadow
    @Final
    private RenderSection render;

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;
    @Unique
    private TileEntity[] hbm$spanningTesrs;
    @Unique
    private int hbm$spanningTesrCount;
    @Unique
    private int hbm$originX, hbm$originY, hbm$originZ;

    @Dynamic
    @SuppressWarnings("UnreachableCode")
    @Inject(method = "execute", at = @At("HEAD"), require = 1)
    private void hbm$cacheOrigin(ChunkBuildContext context, CancellationToken cancellationToken,
                                 CallbackInfoReturnable<ChunkBuildOutput> cir) {
        hbm$originX = render.getOriginX();
        hbm$originY = render.getOriginY();
        hbm$originZ = render.getOriginZ();
    }

    @Dynamic
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasTileEntity(Lnet/minecraft/block/state/IBlockState;)Z"), require = 1)
    private boolean hbm$trackBlockExtents(Block block, IBlockState state, Operation<Boolean> original,
                                          @Local BlockPos.MutableBlockPos blockPos) {
        int[] extents = StaticTesrBakedModels.getManagedRenderExtents(state);
        if (extents != null) {
            int localX = blockPos.getX() - hbm$originX;
            int localY = blockPos.getY() - hbm$originY;
            int localZ = blockPos.getZ() - hbm$originZ;

            hbm$negX = Math.max(hbm$negX, extents[4] - localX);
            hbm$posX = Math.max(hbm$posX, localX + extents[5] - 15);
            hbm$negY = Math.max(hbm$negY, extents[1] - localY);
            hbm$posY = Math.max(hbm$posY, localY + extents[0] - 15);
            hbm$negZ = Math.max(hbm$negZ, extents[2] - localZ);
            hbm$posZ = Math.max(hbm$posZ, localZ + extents[3] - 15);
        }
        return original.call(block, state);
    }

    @Dynamic
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;isGlobalRenderer(Lnet/minecraft/tileentity/TileEntity;)Z"), remap = true, require = 1)
    private boolean hbm$trackOversizedTesr(TileEntitySpecialRenderer<TileEntity> renderer, TileEntity te) {
        boolean isGlobal = renderer.isGlobalRenderer(te);
        if (!isGlobal) {
            AxisAlignedBB bb = te.getRenderBoundingBox();
            if (bb != TileEntity.INFINITE_EXTENT_AABB) {
                int sx = hbm$originX;
                int sy = hbm$originY;
                int sz = hbm$originZ;
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
        return isGlobal;
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
    @SuppressWarnings({"ConstantValue", "UnreachableCode"})
    @Inject(method = "execute", at = @At("RETURN"), require = 2)
    private void hbm$publishExtents(ChunkBuildContext context, CancellationToken cancellationToken,
                                    CallbackInfoReturnable<ChunkBuildOutput> cir) {
        ChunkBuildOutput output = cir.getReturnValue();
        if (output == null || output.info == null) {
            return;
        }
        IExtraExtentsHolder holder = (IExtraExtentsHolder) output.info;
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
    }
}
