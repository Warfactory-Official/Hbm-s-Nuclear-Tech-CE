package com.hbm.mixin.mod.neonium;

import com.hbm.render.chunk.ChunkSpanningTesrHelper;
import com.hbm.render.chunk.IExtraExtentsHolder;
import com.hbm.render.chunk.IVisibleSectionSetHolder;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class MixinSodiumWorldRenderer {

    @Shadow
    private ChunkRenderManager<?> chunkRenderManager;

    @Unique
    private final ReferenceOpenHashSet<TileEntity> hbm$renderedTileEntities = new ReferenceOpenHashSet<>();

    @Invoker("renderTE")
    protected abstract void hbm$invokeRenderTE(TileEntity tileEntity, int pass, float partialTicks, int damageProgress);

    @Dynamic
    @Inject(method = "renderTileEntities", at = @At("HEAD"))
    private void hbm$beginTileEntityFrame(float partialTicks, Map<Integer, DestroyBlockProgress> damagedBlocks,
                                          CallbackInfo ci) {
        hbm$renderedTileEntities.clear();
    }

    @Dynamic
    @Redirect(method = "renderTileEntities", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;renderTE(Lnet/minecraft/tileentity/TileEntity;IFI)V"))
    private void hbm$renderTileEntityOnce(SodiumWorldRenderer instance, TileEntity tileEntity, int pass,
                                          float partialTicks, int damageProgress) {
        if (damageProgress >= 0 || hbm$renderedTileEntities.add(tileEntity)) {
            hbm$invokeRenderTE(tileEntity, pass, partialTicks, damageProgress);
        }
    }

    @Dynamic
    @Inject(method = "renderTileEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V", shift = At.Shift.BEFORE))
    private void hbm$renderChunkSpanningTesrs(float partialTicks, Map<Integer, DestroyBlockProgress> damagedBlocks,
                                              CallbackInfo ci) {
        int pass = MinecraftForgeClient.getRenderPass();
        LongSet visibleSections =
                ((IVisibleSectionSetHolder) chunkRenderManager).hbm$getVisibleSections();

        for (TileEntity tileEntity : ChunkSpanningTesrHelper.getChunkSpanningTesrs()) {
            if (tileEntity.isInvalid()) {
                continue;
            }
            if (!tileEntity.shouldRenderInPass(pass)) {
                continue;
            }
            if (!ChunkSpanningTesrHelper.intersectsVisibleSections(tileEntity, visibleSections)) {
                continue;
            }
            if (!hbm$renderedTileEntities.add(tileEntity)) {
                continue;
            }
            hbm$invokeRenderTE(tileEntity, pass, partialTicks, -1);
        }
    }

    @Dynamic
    @Inject(method = "initRenderer", at = @At("HEAD"))
    private void hbm$clearOnReload(CallbackInfo ci) {
        ChunkSpanningTesrHelper.clear();
    }

    @Dynamic
    @Inject(method = "onChunkRenderUpdated", at = @At("HEAD"))
    private void hbm$updateChunkSpanningTesrSet(int x, int y, int z, ChunkRenderData meshBefore,
                                                ChunkRenderData meshAfter, CallbackInfo ci) {
        // ChunkRenderData.ABSENT has null occlusion data
        TileEntity[] old = hbm$getSpanningTesrs(meshBefore);
        TileEntity[] nu = hbm$getSpanningTesrs(meshAfter);
        if (old.length != 0 || nu.length != 0) {
            ChunkSpanningTesrHelper.updateChunkSpanningTesrs(old, nu);
        }
    }

    @Unique
    private static TileEntity[] hbm$getSpanningTesrs(ChunkRenderData data) {
        SetVisibility occlusion = data.getOcclusionData();
        if (occlusion == null) return IExtraExtentsHolder.EMPTY_TE_ARR;
        return ((IExtraExtentsHolder) occlusion).hbm$getChunkSpanningTesrs();
    }
}
