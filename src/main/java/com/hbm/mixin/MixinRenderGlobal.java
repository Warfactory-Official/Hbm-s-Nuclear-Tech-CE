package com.hbm.mixin;

import com.hbm.render.chunk.ChunkSpanningTesrHelper;
import com.hbm.util.ShaderHelper;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos;

    @Unique
    private final LongOpenHashSet hbm$visibleSections = new LongOpenHashSet();
    @Unique
    private final ReferenceOpenHashSet<TileEntity> hbm$renderedTileEntities = new ReferenceOpenHashSet<>();

    @Inject(method = "loadRenderers", at = @At("HEAD"))
    private void hbm$clearOnReload(CallbackInfo ci) {
        ChunkSpanningTesrHelper.clear();
    }

    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void hbm$beginTileEntityFrame(Entity renderViewEntity, ICamera camera, float partialTicks,
                                          CallbackInfo ci) {
        hbm$renderedTileEntities.clear();
        hbm$visibleSections.clear();
        for (RenderGlobal.ContainerLocalRenderInformation info : renderInfos) {
            ChunkSpanningTesrHelper.addVisibleSection(hbm$visibleSections, info.renderChunk.getPosition());
        }
    }

    @Redirect(method = "renderEntities", require = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"))
    private void hbm$renderTileEntityOnce(TileEntityRendererDispatcher dispatcher, TileEntity tileEntity,
                                          float partialTicks, int destroyStage) {
        if (destroyStage >= 0 || hbm$renderedTileEntities.add(tileEntity)) {
            dispatcher.render(tileEntity, partialTicks, destroyStage);
        }
    }

    @Inject(method = "renderEntities", require = 1, at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V", shift = At.Shift.BEFORE))
    private void hbm$renderChunkSpanningTesrs(Entity renderViewEntity, ICamera camera,
                                              float partialTicks, CallbackInfo ci) {
        int pass = MinecraftForgeClient.getRenderPass();
        boolean shadersActive = ShaderHelper.areShadersActive();

        for (TileEntity tileEntity : ChunkSpanningTesrHelper.getChunkSpanningTesrs()) {
            if (tileEntity.isInvalid()) {
                continue;
            }
            if (!tileEntity.shouldRenderInPass(pass)) {
                continue;
            }
            if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
                continue;
            }
            if (!ChunkSpanningTesrHelper.intersectsVisibleSections(tileEntity, hbm$visibleSections)) {
                continue;
            }
            if (!hbm$renderedTileEntities.add(tileEntity)) {
                continue;
            }
            if (shadersActive) {
                ShaderHelper.nextBlockEntity(tileEntity);
            }
            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
        }
    }
}
