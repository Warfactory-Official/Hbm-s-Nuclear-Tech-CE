package com.hbm.particle_instanced;

import com.hbm.Tags;
import com.hbm.render.InstancedBillboardBatch;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.nio.ByteBuffer;

public class ParticleInstanced extends Particle {

    protected static final InstancedBillboardBatch SQUARE_BATCH = new InstancedBillboardBatch();
    protected static final InstancedBillboardBatch VERTICAL_RECT_BATCH = new InstancedBillboardBatch(0.5F, 1F);
    protected static final RenderType DEFAULT_RENDER_TYPE = RenderType.DEFAULT_BLOCK_ATLAS;

    protected ParticleInstanced(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
	}

    protected final float getInterpX(float partialTicks) {
        return (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
    }

    protected final float getInterpY(float partialTicks) {
        return (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
    }

    protected final float getInterpZ(float partialTicks) {
        return (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
    }

    protected final void writeBillboard(ByteBuffer buffer, float posX, float posY, float posZ, float scale, float red,
                                        float green, float blue, float alpha, int lightmapX, int lightmapY) {
        float minU = this.particleTexture.getMinU();
        float minV = this.particleTexture.getMinV();
        float sizeU = this.particleTexture.getMaxU() - this.particleTexture.getMinU();
        float sizeV = this.particleTexture.getMaxV() - this.particleTexture.getMinV();
        InstancedBillboardBatch.writeInstance(buffer, posX, posY, posZ, scale, minU, minV, sizeU, sizeV, red, green,
                blue, alpha, lightmapX, lightmapY);
    }

    protected final void writeFullbrightBillboard(ByteBuffer buffer, float posX, float posY, float posZ, float scale,
                                                  float minU, float minV, float sizeU, float sizeV, float red,
                                                  float green, float blue, float alpha) {
        InstancedBillboardBatch.writeInstance(buffer, posX, posY, posZ, scale, minU, minV, sizeU, sizeV, red, green,
                blue, alpha, 240, 240);
    }

    protected final void writeFullbrightBillboard(ByteBuffer buffer, float posX, float posY, float posZ, float scale,
                                                  float red, float green, float blue, float alpha) {
        writeBillboard(buffer, posX, posY, posZ, scale, red, green, blue, alpha, 240, 240);
    }

    public RenderType getRenderType() {
        return DEFAULT_RENDER_TYPE;
    }

    public void addDataToBuffer(ByteBuffer buf, float partialTicks){
        writeFullbrightBillboard(buf, getInterpX(partialTicks), getInterpY(partialTicks), getInterpZ(partialTicks),
                this.particleScale, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
	}
	
	public int getFaceCount(){
		return 1;
	}

    public enum RenderType {
        DEFAULT_BLOCK_ATLAS(BillboardShape.SQUARE, null, true, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
                0.003921569F, false, false, false),
        RBMK_FLAME(BillboardShape.VERTICAL_RECT, new ResourceLocation(Tags.MODID + ":textures/particle/rbmk_fire.png"),
                false, SourceFactor.SRC_ALPHA, DestFactor.ONE, 0F, true, true, false),
        RBMK_STEAM(BillboardShape.VERTICAL_RECT, new ResourceLocation(Tags.MODID + ":textures/particle/rbmk_jet_steam.png"),
                false, SourceFactor.SRC_ALPHA, DestFactor.ONE, 0F, true, true, false),
        RBMK_MUSH(BillboardShape.SQUARE, new ResourceLocation(Tags.MODID + ":textures/particle/rbmk_mush.png"),
                false, SourceFactor.SRC_ALPHA, DestFactor.ONE, 0F, true, true, false),
        RADIATION_FOG(BillboardShape.SQUARE, new ResourceLocation(Tags.MODID + ":textures/particle/fog.png"),
                false, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, 0.003921569F, true, false, false);

        private static final RenderType[] VALUES = values();

        private final BillboardShape billboardShape;
        private final ResourceLocation texture;
        private final boolean blockAtlas;
        private final SourceFactor sourceFactor;
        private final DestFactor destFactor;
        private final float alphaThreshold;
        private final boolean disableLighting;
        private final boolean disableFog;
        private final boolean disableDepth;

        RenderType(BillboardShape billboardShape, ResourceLocation texture, boolean blockAtlas,
                   SourceFactor sourceFactor, DestFactor destFactor, float alphaThreshold,
                   boolean disableLighting, boolean disableFog, boolean disableDepth) {
            this.billboardShape = billboardShape;
            this.texture = texture;
            this.blockAtlas = blockAtlas;
            this.sourceFactor = sourceFactor;
            this.destFactor = destFactor;
            this.alphaThreshold = alphaThreshold;
            this.disableLighting = disableLighting;
            this.disableFog = disableFog;
            this.disableDepth = disableDepth;
        }

        public static int size() {
            return VALUES.length;
        }

        public static RenderType byId(int id) {
            return VALUES[id];
        }

        public InstancedBillboardBatch getBatch() {
            return this.billboardShape == BillboardShape.VERTICAL_RECT ? VERTICAL_RECT_BATCH : SQUARE_BATCH;
        }

        public SourceFactor getSourceFactor() {
            return sourceFactor;
        }

        public DestFactor getDestFactor() {
            return destFactor;
        }

        public float getAlphaThreshold() {
            return alphaThreshold;
        }

        public boolean shouldDisableLighting() {
            return disableLighting;
        }

        public boolean shouldDisableFog() {
            return disableFog;
        }

        public boolean shouldDisableDepth() {
            return disableDepth;
        }

        public void bindTexture() {
            if (blockAtlas) {
                NTMRenderHelper.bindBlockTexture();
            } else {
                NTMRenderHelper.bindTexture(texture);
            }
        }
    }

    private enum BillboardShape {
        SQUARE,
        VERTICAL_RECT
    }
}
