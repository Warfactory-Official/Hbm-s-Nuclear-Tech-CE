package com.hbm.render.util;

import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.wiaj.WorldInAJar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL14;

import java.util.*;

public class RenderOverhead {

	public static void renderTag(EntityLivingBase living, double x, double y, double z, RenderLivingBase renderer, String name, boolean depthTest) {

		EntityPlayer thePlayer = Minecraft.getMinecraft().player;

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

		if(shouldRenderTag(living)) {
            double distSq = living.getDistanceSq(thePlayer);
			float range = living.isSneaking() ? RenderLivingBase.NAME_TAG_RANGE_SNEAK : RenderLivingBase.NAME_TAG_RANGE;

			if(distSq < (double) (range * range)) {
				String s = name;
				drawTagAware(living, x, y, z, name, depthTest);
			}
		}
	}

	protected static boolean shouldRenderTag(EntityLivingBase p_110813_1_) {
		return Minecraft.isGuiEnabled() && p_110813_1_ != Minecraft.getMinecraft().player && !p_110813_1_.isInvisibleToPlayer(Minecraft.getMinecraft().player) && !p_110813_1_.isBeingRidden();
	}

	protected static void drawTagAware(EntityLivingBase entity, double x, double y, double z, String string, boolean depthTest) {
		if(entity.isPlayerSleeping()) {
			drawTag(entity, string, x, y - 1.5D, z, 64, depthTest);
		} else {
			drawTag(entity, string, x, y, z, 64, depthTest);
		}
	}

	protected static void drawTag(Entity entity, String name, double x, double y, double z, int dist, boolean depthTest) {

		double distsq = entity.getDistanceSq(Minecraft.getMinecraft().player);

		if(distsq <= (double) (dist * dist)) {
			FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
			float f = 1.6F;
			float scale = 0.016666668F * f;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x + 0.0F, (float) y + entity.height + 0.75F, (float) z);
			GlStateManager.color(0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(-scale, -scale, scale);
			GlStateManager.disableLighting();
			GlStateManager.depthMask(false);
			if(depthTest) {
				GlStateManager.disableDepth();
			}
			GlStateManager.enableBlend();
			//src alpha, one minus src alpha
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buf = tessellator.getBuffer();
			byte heightOffset = 0;

			if(name.equals("deadmau5")) {
				heightOffset = -10;
			}

			GlStateManager.disableTexture2D();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			int center = fontrenderer.getStringWidth(name) / 2;
			GlStateManager.color(0.0F, 0.0F, 0.0F, 0.25F);
			buf.pos(-center - 1, -1 + heightOffset, 0.0D).endVertex();
			buf.pos(-center - 1, 8 + heightOffset, 0.0D).endVertex();
			buf.pos(center + 1, 8 + heightOffset, 0.0D).endVertex();
			buf.pos(center + 1, -1 + heightOffset, 0.0D).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
			fontrenderer.drawString(name, -fontrenderer.getStringWidth(name) / 2, heightOffset, 553648127);
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			fontrenderer.drawString(name, -fontrenderer.getStringWidth(name) / 2, heightOffset, -1);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.popMatrix();
		}
	}

	protected static void drawTag(float offset, double distsq, String name, double x, double y, double z, int dist, boolean depthTest, int color, int shadowColor) {

		if(distsq <= (double) (dist * dist)) {
			FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
			float f = 1.6F;
			float scale = 0.016666668F * f;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x + 0.0F, (float) y + offset, (float) z);
			GlStateManager.color(0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(-scale, -scale, scale);
			GlStateManager.disableLighting();
			GlStateManager.depthMask(false);
			if(depthTest) {
				GlStateManager.disableDepth();
			}
			GlStateManager.enableBlend();
			//src alpha, one minus src alpha
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buf = tessellator.getBuffer();
			byte heightOffset = 0;

			if(name.equals("deadmau5")) {
				heightOffset = -10;
			}

			GlStateManager.disableTexture2D();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			int center = fontrenderer.getStringWidth(name) / 2;
			GlStateManager.color(0.0F, 0.0F, 0.0F, 0.25F);
			buf.pos(-center - 1, -1 + heightOffset, 0.0D).endVertex();
			buf.pos(-center - 1, 8 + heightOffset, 0.0D).endVertex();
			buf.pos(center + 1, 8 + heightOffset, 0.0D).endVertex();
			buf.pos(center + 1, -1 + heightOffset, 0.0D).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
			fontrenderer.drawString(name, -fontrenderer.getStringWidth(name) / 2, heightOffset, shadowColor);
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			fontrenderer.drawString(name, -fontrenderer.getStringWidth(name) / 2, heightOffset, color);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.popMatrix();
		}
	}

	public static void renderThermalSight(float partialTicks) {

		EntityPlayer player = Minecraft.getMinecraft().player;
		double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double y =  player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double z =  player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

		GlStateManager.pushMatrix();
		GlStateManager.disableColorMaterial();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		for(Entity ent : player.world.loadedEntityList) {

			if(ent == player)
				continue;

			if(ent.getDistanceSq(player) > 4096)
				continue;

			float r, g, b;

            switch (ent) {
                case IMob _ -> {
                    r = 1;
                    g = 0;
                    b = 0;
                }
                case EntityPlayer _ -> {
                    r = 1;
                    g = 0;
                    b = 1;
                }
                case EntityLiving _ -> {
                    r = 0;
                    g = 1;
                    b = 0;
                }
                case EntityItem _ -> {
                    r = 1;
                    g = 1;
                    b = 0.5F;
                }
                case EntityXPOrb _ -> {
                    if (player.ticksExisted % 10 < 5) {
                        r = 1;
                    } else {
                        r = 0.5F;
                    }

                    g = 1;
                    b = 0.5F;
                }
                default -> {
                    continue;
                }
            }

            if(!ent.isNonBoss()) {
                r = 1;
                g = 0.5F;
                b = 0;
            }

            if(ent instanceof EntityLivingBase && ((EntityLivingBase) ent).getHealth() <= 0) {
                r = g = b = 0;
            }

			AxisAlignedBB bb = ent.getEntityBoundingBox();
			buf.pos(bb.minX - x, bb.maxY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.minY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.maxY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.maxY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.maxY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.minY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.minY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.minY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.minY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.minY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.maxY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.maxY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.maxY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.minY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.maxY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.maxY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.maxY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.minY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.maxY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.maxY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.minY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.maxX - x, bb.minY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.minY - y, bb.minZ - z).color(r, g, b, 1).endVertex();
			buf.pos(bb.minX - x, bb.minY - y, bb.maxZ - z).color(r, g, b, 1).endVertex();
		}

		tess.draw();

		GlStateManager.enableColorMaterial();
		GlStateManager.enableTexture2D();
		GL11.glDisable(GL11.GL_POINT_SMOOTH);
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
        GlStateManager.popMatrix();
	}

	public static final HashMap<BlockPos, Marker> queuedMarkers = new HashMap();
	private static final HashMap<BlockPos, Marker> markers = new HashMap();

	public static void renderMarkers(float partialTicks) {

		markers.putAll(queuedMarkers);
		queuedMarkers.clear();

		if(markers.isEmpty())
			return;

		EntityPlayer player = Minecraft.getMinecraft().player;
		double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double y =  player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double z =  player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

		GlStateManager.pushMatrix();
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		Iterator<Map.Entry<BlockPos, Marker>> it = markers.entrySet().iterator();
		List<Map.Entry<BlockPos, Marker>> tagList = new ArrayList();
		while(it.hasNext()) {
			Map.Entry<BlockPos, Marker> entry = it.next();
			BlockPos pos = entry.getKey();
			Marker marker = entry.getValue();

			int pX = pos.getX();
			int pY = pos.getY();
			int pZ = pos.getZ();

			double minX = marker.minX;
			double minY = marker.minY;
			double minZ = marker.minZ;
			double maxX = marker.maxX;
			double maxY = marker.maxY;
			double maxZ = marker.maxZ;

			int r = (marker.color >> 16) & 0xFF;
			int g = (marker.color >> 8) & 0xFF;
			int b = marker.color & 0xFF;
			int a = 255;

			buf.pos(pX + minX - x, pY + maxY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + minY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + maxY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + maxY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + maxY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + minY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + minY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + minY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + minY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + minY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + maxY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + maxY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + maxY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + minY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + maxY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + maxY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + maxY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + minY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + maxY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + maxY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + minY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + maxX - x, pY + minY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + minY - y, pZ + minZ - z).color(r, g, b, a).endVertex();
			buf.pos(pX + minX - x, pY + minY - y, pZ + maxZ - z).color(r, g, b, a).endVertex();

			tagList.add(entry);

			if(marker.expire > 0 && System.currentTimeMillis() > marker.expire) {
				it.remove();
			} else if(marker.maxDist > 0) {
				double aX = pX + (maxX - minX) / 2D;
				double aY = pY + (maxY - minY) / 2D;
				double aZ = pZ + (maxZ - minZ) / 2D;
				Vec3 vec = Vec3.createVectorHelper(x - aX, y - aY, z - aZ);
				if(vec.length() > marker.maxDist) {
					it.remove();
				}
			}
		}

		tess.draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GlStateManager.enableTexture2D();
		GL11.glDisable(GL11.GL_POINT_SMOOTH);
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();

		for(Map.Entry<BlockPos, Marker> entry : tagList) {

			BlockPos pos = entry.getKey();
			Marker marker = entry.getValue();

			int pX = pos.getX();
			int pY = pos.getY();
			int pZ = pos.getZ();

			double minX = marker.minX;
			double minY = marker.minY;
			double minZ = marker.minZ;
			double maxX = marker.maxX;
			double maxY = marker.maxY;
			double maxZ = marker.maxZ;

			double aX = pX + (maxX - minX) / 2D;
			double aY = pY + (maxY - minY) / 2D;
			double aZ = pZ + (maxZ - minZ) / 2D;
			Vec3 vec = Vec3.createVectorHelper(aX - x, aY - y, aZ - z);
			double len = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;
			double sqrt = Math.sqrt(len);
			double mult = Math.min(sqrt, 16D);
			vec.xCoord *= mult / sqrt;
			vec.yCoord *= mult / sqrt;
			vec.zCoord *= mult / sqrt;
			Vec3d look = player.getLookVec();
			Vec3 diff = vec.normalize();
			String label = marker.label;
			if(label == null) {
				label = "";
			}

			if(Math.abs(look.x - diff.xCoord) + Math.abs(look.y - diff.yCoord) + Math.abs(look.z - diff.zCoord) < 0.15) {
				label += (!label.isEmpty() ? " " : "") + ((int) sqrt) + "m";
			}

			if(!label.isEmpty()) drawTag(1F, len, label, vec.xCoord, vec.yCoord, vec.zCoord, 100, true, marker.color, marker.color);
		}
		GlStateManager.popMatrix();
	}


	public static class Marker {
		double minX = 0;
		double minY = 0;
		double minZ = 0;
		double maxX = 1;
		double maxY = 1;
		double maxZ = 1;

		int color;
		String label;

		long expire;
		double maxDist;

		public Marker(int color) {
			this.color = color;
		}

		public Marker setExpire(long expire) {
			this.expire = expire;
			return this;
		}

		public Marker setDist(double maxDist) {
			this.maxDist = maxDist;
			return this;
		}

		public Marker withLabel(String label) {
			this.label = label;
			return this;
		}
	}

	private static WorldInAJar actionPreviewWorld;
	private static int offsetX;
	private static int offsetY;
	private static int offsetZ;
	private static boolean actionPreviewSuccess;
	private static boolean clearPreview;

	public static void setActionPreview(WorldInAJar wiaj, BlockPos pos, boolean canAction) {
		actionPreviewWorld = wiaj;
		offsetX = pos.getX();
		offsetY = pos.getY();
		offsetZ = pos.getZ();
		actionPreviewSuccess = canAction;
		clearPreview = false;
	}

	public static void clearActionPreview() {
		clearPreview = true;
	}

	public static void renderActionPreview(float partialTicks) {
		if (clearPreview) {
			actionPreviewWorld = null;
			clearPreview = false;
		}

		if (actionPreviewWorld == null) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		if (player == null) return;
		final double pX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		final double pY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		final double pZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		final float r = actionPreviewSuccess ? 0.5F : 1.0F;
		final float g = actionPreviewSuccess ? 1.0F : 0.5F;
		final float b = actionPreviewSuccess ? 1.0F : 0.5F;
		final float a = 0.6F;
		RenderHelper.disableStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GL14.glBlendColor(r * a, g * a, b * a, a);
		GlStateManager.tryBlendFuncSeparate(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		buffer.setTranslation(offsetX - pX, offsetY - pY, offsetZ - pZ);
		BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();

		for (int ix = 0; ix < actionPreviewWorld.sizeX; ix++) {
			for (int iy = 0; iy < actionPreviewWorld.sizeY; iy++) {
				for (int iz = 0; iz < actionPreviewWorld.sizeZ; iz++) {
					BlockPos pos = new BlockPos(ix, iy, iz);
					IBlockState state = actionPreviewWorld.getBlockState(pos);
					if (state.getRenderType() == EnumBlockRenderType.INVISIBLE) continue;
					dispatcher.renderBlock(state, pos, actionPreviewWorld, buffer);
				}
			}
		}

		buffer.setTranslation(0, 0, 0);
		tessellator.draw();
		GL14.glBlendColor(0F, 0F, 0F, 1F);
		GlStateManager.disableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.popMatrix();
	}
}
