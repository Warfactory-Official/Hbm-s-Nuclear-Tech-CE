package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ParticleHadron extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/hadron.png");
	
	public ParticleHadron(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = 10;
		this.particleScale = 1F;
	}

	public ParticleHadron makeSmall(boolean small) {
		if(!small) return this;
		this.particleScale = 0.5F;
		this.particleMaxAge = 5;
		return this;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		RenderHelper.disableStandardItemLighting();

		this.particleAlpha = 1 - (((float)this.particleAge + partialTicks) / (float)this.particleMaxAge);
		this.particleAlpha = MathHelper.clamp(this.particleAlpha, 0, 1);
		float f4 = (this.particleAge + partialTicks) * 0.15F * this.particleScale;

	    float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
	    float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
	    float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

		float rX = rotationX * f4;
		float rZ = rotationZ * f4;
		float rXY = rotationXY * f4;
		float rYZ = rotationYZ * f4;
		float rXZ = rotationXZ * f4;

		NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(1.0F, 1.0F, 1.0F, this.particleAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 - rX - rXY, f6 - rZ, f7 - rYZ - rXZ, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 - rX + rXY, f6 + rZ, f7 - rYZ + rXZ, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + rX + rXY, f6 + rZ, f7 + rYZ + rXZ, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + rX - rXY, f6 - rZ, f7 + rYZ - rXZ, 0, 1, packedColor, packedLightmap);
        NTMImmediate.INSTANCE.draw();
        
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
	}

}
