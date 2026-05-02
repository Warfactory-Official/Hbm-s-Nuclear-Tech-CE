package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ParticleFoundry extends Particle {

	protected int color;
	protected ForgeDirection dir;
	/* how far the metal splooshes down from the base point */
	protected float length;
	/* the material coming right out of the faucet, either above or next to the base point */
	protected float base;
	/* how far the base part goes back */
	protected float offset;

	public static final ResourceLocation lava = new ResourceLocation(Tags.MODID + ":textures/models/machines/lava_gray.png");

	public ParticleFoundry(World world, double x, double y, double z, int color, int direction, float length, float base, float offset) {
		super(world, x, y, z);
		this.color = color;
		this.dir = ForgeDirection.getOrientation(direction);
		this.length = length;
		this.base = base;
		this.offset = offset;
		
		this.particleMaxAge = 20;
	}
	
	@Override
	public void onUpdate() {
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity player, float partialTicks, float x, float y, float z, float oX, float oZ) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer playerEntity = mc.player;

		double dX = playerEntity.lastTickPosX + (playerEntity.posX - playerEntity.lastTickPosX) * partialTicks;
		double dY = playerEntity.lastTickPosY + (playerEntity.posY - playerEntity.lastTickPosY) * partialTicks;
		double dZ = playerEntity.lastTickPosZ + (playerEntity.posZ - playerEntity.lastTickPosZ) * partialTicks;

		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * partialTicks) - dX);
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * partialTicks) - dY);
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks) - dZ);

		ForgeDirection rot = this.dir.getRotation(ForgeDirection.UP);
		float width = (float) (0.0625D + ((this.particleAge + partialTicks) / this.particleMaxAge) * 0.0625D);
		float girth = (float) (0.125D * (1 - ((this.particleAge + partialTicks) / this.particleMaxAge)));

		Color color = new Color(this.color).brighter();
		double brightener = 0.7D;
		int r = (int) (255D - (255D - color.getRed()) * brightener);
		int g = (int) (255D - (255D - color.getGreen()) * brightener);
		int b = (int) (255D - (255D - color.getBlue()) * brightener);

		GlStateManager.color(r / 255F, g / 255F, b / 255F);

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.translate(pX, pY, pZ);
		mc.getTextureManager().bindTexture(lava);

		NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 36);
        int packedColor = NTMBufferBuilder.packColor(r, g, b, 255);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);

		float dirXG = dir.offsetX * girth;
		float dirZG = dir.offsetZ * girth;
		float rotXW = rot.offsetX * width;
		float rotZW = rot.offsetZ * width;

		float uMin = 0.5F - width;
		float uMax = 0.5F + width;
		float vMin = 0.0F;
		float vMax = length;

		float add = (int) (System.currentTimeMillis() / 100 % 16) / 16F;

		// Lower back
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, girth, rotZW, uMax, vMax + add + girth, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, girth, -rotZW, uMin, vMax + add + girth, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, -length, -rotZW, uMin, vMin + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, -length, rotZW, uMax, vMin + add, packedColor, packedLightmap);

		// Lower front
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG + rotXW, 0, dirZG + rotZW, uMax, vMax + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG - rotXW, 0, dirZG - rotZW, uMin, vMax + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG - rotXW, -length, dirZG - rotZW, uMin, vMin + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG + rotXW, -length, dirZG + rotZW, uMax, vMin + add, packedColor, packedLightmap);

		float wMin = 0.0F;
		float wMax = girth;

		// Lower left
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, girth, rotZW, wMin, vMax + add + girth, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG + rotXW, 0, dirZG + rotZW, wMax, vMax + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG + rotXW, -length, dirZG + rotZW, wMax, vMin + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, -length, rotZW, wMin, vMin + add, packedColor, packedLightmap);

		// Lower right
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, girth, -rotZW, wMin, vMax + add + girth, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG - rotXW, 0, dirZG - rotZW, wMax, vMax + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG - rotXW, -length, dirZG - rotZW, wMax, vMin + add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, -length, -rotZW, wMin, vMin + add, packedColor, packedLightmap);

		float dirOX = dir.offsetX * offset;
		float dirOZ = dir.offsetZ * offset;

		vMax = offset;

		// Upper back
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, 0, rotZW, uMax, vMax - add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, 0, -rotZW, uMin, vMax - add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW - dirOX, base, -rotZW - dirOZ, uMin, vMin - add, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW - dirOX, base, rotZW - dirOZ, uMax, vMin - add, packedColor, packedLightmap);

		// Upper front
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, girth, rotZW, uMax, vMax - add + 0.25F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, girth, -rotZW, uMin, vMax - add + 0.25F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW - dirOX, base + girth, -rotZW - dirOZ, uMin, vMin - add + 0.25F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW - dirOX, base + girth, rotZW - dirOZ, uMax, vMin - add + 0.25F, packedColor, packedLightmap);

		// Upper left
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, 0, rotZW, wMax, vMax - add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, girth, rotZW, wMin, vMax - add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW - dirOX, base + girth, rotZW - dirOZ, wMin, vMin - add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW - dirOX, base, rotZW - dirOZ, wMax, vMin - add + 0.75F, packedColor, packedLightmap);

		// Upper right
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, 0, -rotZW, wMax, vMax - add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, girth, -rotZW, wMin, vMax - add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW - dirOX, base + girth, -rotZW - dirOZ, wMin, vMin - add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW - dirOX, base, -rotZW - dirOZ, wMax, vMin - add + 0.75F, packedColor, packedLightmap);

		vMax = 0.125F;

		// Bend
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG + rotXW, 0, dirZG + rotZW, uMax, vMin + add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(dirXG - rotXW, 0, dirZG - rotZW, uMin, vMin + add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rotXW, girth, -rotZW, uMin, vMax + add + 0.75F, packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(rotXW, girth, rotZW, uMax, vMax + add + 0.75F, packedColor, packedLightmap);

		NTMImmediate.INSTANCE.draw();

		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
