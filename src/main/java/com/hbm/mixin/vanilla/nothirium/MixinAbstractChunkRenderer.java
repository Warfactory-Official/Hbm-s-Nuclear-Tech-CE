package com.hbm.mixin.vanilla.nothirium;

import com.hbm.render.chunk.ChunkSpanningTesrHelper;
import com.hbm.render.chunk.IRenderFrameStamp;
import com.hbm.render.chunk.IShadowRenderFrameStamp;
import com.hbm.render.chunk.RenderPassFrameHolder;
import com.hbm.util.ShaderHelper;
import meldexun.nothirium.renderer.chunk.AbstractChunkRenderer;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import meldexun.nothirium.util.SectionPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbstractChunkRenderer.class, remap = false)
public abstract class MixinAbstractChunkRenderer {

    @Unique
    private final BlockPos.MutableBlockPos hbm$probePos = new BlockPos.MutableBlockPos();

    @Dynamic
    @Redirect(method = "setup",
            at = @At(value = "FIELD",
                    target = "Lmeldexun/nothirium/renderer/chunk/AbstractRenderChunk;lastTimeRecorded:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0),
            require = 1)
    private void hbm$stampVanillaRenderChunkOnVisible(AbstractRenderChunk chunk, int frame) {
        chunk.lastTimeRecorded = frame;
        if (ChunkSpanningTesrHelper.isEmpty()) return;

        ViewFrustum viewFrustum = Minecraft.getMinecraft().renderGlobal.viewFrustum;
        if (viewFrustum == null) return;
        SectionPos pos = chunk.getPos();
        int originX = pos.getBlockX();
        int originY = pos.getBlockY();
        int originZ = pos.getBlockZ();
        RenderChunk vanillaChunk = viewFrustum.getRenderChunk(
                hbm$probePos.setPos(originX, originY, originZ));
        if (vanillaChunk != null) {
            BlockPos vanillaPos = vanillaChunk.getPosition();
            if (vanillaPos.getX() != originX || vanillaPos.getY() != originY || vanillaPos.getZ() != originZ) {
                return;
            }
            if (ShaderHelper.isShadowPass()) {
                ((IShadowRenderFrameStamp) vanillaChunk).hbm$setShadowFrameStamp(
                        RenderPassFrameHolder.currentShadowTerrainFrame);
                return;
            }
            ((IRenderFrameStamp) vanillaChunk).hbm$setFrameStamp(RenderPassFrameHolder.currentTerrainFrame);
        }
    }
}
