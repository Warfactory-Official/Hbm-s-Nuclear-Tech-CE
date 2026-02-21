package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.lib.ForgeDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
	protected double length;
	/* the material coming right out of the faucet, either above or next to the base point */
	protected double base;
	/* how far the base part goes back */
	protected double offset;
	
	public static final ResourceLocation lava = new ResourceLocation(Tags.MODID + ":textures/models/machines/lava_gray.png");

	public ParticleFoundry(World world, double x, double y, double z, int color, int direction, double length, double base, double offset) {
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
		double width = 0.0625 + ((this.particleAge + partialTicks) / this.particleMaxAge) * 0.0625;
		double girth = 0.125 * (1 - ((this.particleAge + partialTicks) / this.particleMaxAge));

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

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		double dirXG = dir.offsetX * girth;
		double dirZG = dir.offsetZ * girth;
		double rotXW = rot.offsetX * width;
		double rotZW = rot.offsetZ * width;

		double uMin = 0.5 - width;
		double uMax = 0.5 + width;
		double vMin = 0;
		double vMax = length;

		double add = (int) (System.currentTimeMillis() / 100 % 16) / 16D;

		// Lower back
		buffer.pos(rotXW, girth, rotZW).tex(uMax, vMax + add + girth).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, girth, -rotZW).tex(uMin, vMax + add + girth).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, -length, -rotZW).tex(uMin, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW, -length, rotZW).tex(uMax, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();

		// Lower front
		buffer.pos(dirXG + rotXW, 0, dirZG + rotZW).tex(uMax, vMax + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG - rotXW, 0, dirZG - rotZW).tex(uMin, vMax + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG - rotXW, -length, dirZG - rotZW).tex(uMin, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG + rotXW, -length, dirZG + rotZW).tex(uMax, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();

		double wMin = 0;
		double wMax = girth;

		// Lower left
		buffer.pos(rotXW, girth, rotZW).tex(wMin, vMax + add + girth).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG + rotXW, 0, dirZG + rotZW).tex(wMax, vMax + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG + rotXW, -length, dirZG + rotZW).tex(wMax, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW, -length, rotZW).tex(wMin, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();

		// Lower right
		buffer.pos(-rotXW, girth, -rotZW).tex(wMin, vMax + add + girth).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG - rotXW, 0, dirZG - rotZW).tex(wMax, vMax + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG - rotXW, -length, dirZG - rotZW).tex(wMax, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, -length, -rotZW).tex(wMin, vMin + add).color(r, g, b, 255).lightmap(240, 240).endVertex();

		double dirOX = dir.offsetX * offset;
		double dirOZ = dir.offsetZ * offset;

		vMax = offset;

		// Upper back
		buffer.pos(rotXW, 0, rotZW).tex(uMax, vMax - add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, 0, -rotZW).tex(uMin, vMax - add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW - dirOX, base, -rotZW - dirOZ).tex(uMin, vMin - add).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW - dirOX, base, rotZW - dirOZ).tex(uMax, vMin - add).color(r, g, b, 255).lightmap(240, 240).endVertex();

		// Upper front
		buffer.pos(rotXW, girth, rotZW).tex(uMax, vMax - add + 0.25).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, girth, -rotZW).tex(uMin, vMax - add + 0.25).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW - dirOX, base + girth, -rotZW - dirOZ).tex(uMin, vMin - add + 0.25).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW - dirOX, base + girth, rotZW - dirOZ).tex(uMax, vMin - add + 0.25).color(r, g, b, 255).lightmap(240, 240).endVertex();

		// Upper left
		buffer.pos(rotXW, 0, rotZW).tex(wMax, vMax - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW, girth, rotZW).tex(wMin, vMax - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW - dirOX, base + girth, rotZW - dirOZ).tex(wMin, vMin - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW - dirOX, base, rotZW - dirOZ).tex(wMax, vMin - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();

		// Upper right
		buffer.pos(-rotXW, 0, -rotZW).tex(wMax, vMax - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, girth, -rotZW).tex(wMin, vMax - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW - dirOX, base + girth, -rotZW - dirOZ).tex(wMin, vMin - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW - dirOX, base, -rotZW - dirOZ).tex(wMax, vMin - add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();

		vMax = 0.125F;

		// Bend
		buffer.pos(dirXG + rotXW, 0, dirZG + rotZW).tex(uMax, vMin + add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(dirXG - rotXW, 0, dirZG - rotZW).tex(uMin, vMin + add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(-rotXW, girth, -rotZW).tex(uMin, vMax + add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();
		buffer.pos(rotXW, girth, rotZW).tex(uMax, vMax + add + 0.75).color(r, g, b, 255).lightmap(240, 240).endVertex();

		Tessellator.getInstance().draw();

		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
