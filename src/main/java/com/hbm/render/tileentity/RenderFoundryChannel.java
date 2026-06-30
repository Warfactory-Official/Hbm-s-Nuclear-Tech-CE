package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.machine.FoundryChannel;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


@AutoRegister
public class RenderFoundryChannel extends TileEntitySpecialRenderer<TileEntityFoundryChannel> {
    public static final ResourceLocation LAVA_TEXTURE = new ResourceLocation(Tags.MODID, "textures/models/machines/lava_gray.png");

    @Override
    public void render(TileEntityFoundryChannel tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        BlockPos pos = tile.getPos();
        if (getWorld().isAirBlock(pos) || !tile.hasWorld()) {
            return;
        }

        World world = tile.getWorld();
        FoundryChannel channel = (FoundryChannel) tile.getBlockType();

		boolean doRender = (tile.amount > 0 || tile.renderAmount > 0) && (tile.type != null || tile.renderType != null);
		if (!doRender) {
			return;
		}

		NTMMaterial mat = tile.renderType != null ? tile.renderType : tile.type;
		int hex = mat.moltenColor;
        double brightener = 0.7D;
        int rComp = (hex >> 16) & 0xFF;
        int gComp = (hex >> 8) & 0xFF;
        int bComp = hex & 0xFF;
        rComp = Math.min((int) (rComp / 0.7D), 255);
        gComp = Math.min((int) (gComp / 0.7D), 255);
        bComp = Math.min((int) (bComp / 0.7D), 255);
        float nr = (float) (255D - (255D - rComp) * brightener) / 255F;
        float ng = (float) (255D - (255D - gComp) * brightener) / 255F;
        float nb = (float) (255D - (255D - bComp) * brightener) / 255F;

		double maxLevel = 0.25D;
		int capacity = tile.getCapacity();
		if(capacity <= 0) capacity = 1;

		double fraction = (double) tile.renderAmount / capacity;
		double prevFrac = (double) tile.prevRenderAmount / capacity;
		double interpFrac = prevFrac + (fraction - prevFrac) * partialTicks;
		interpFrac = Math.max(0, Math.min(1, interpFrac));

		double level = interpFrac * maxLevel;

		double droop = MathHelper.sin((float) (interpFrac * Math.PI)) * 0.03F;
		double baseY = 0.125D + level;

		bindTexture(LAVA_TEXTURE);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();

		GlStateManager.color(nr, ng, nb, 1.0F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

		if (channel.canConnectTo(world, pos, EnumFacing.EAST)) { // +X
			renderSloped(buffer, 0.625D, 0.3125D, 1D, 0.6875D, baseY, droop);
		}
		if (channel.canConnectTo(world, pos, EnumFacing.WEST)) { // -X
			renderSloped(buffer, 0D, 0.3125D, 0.375D, 0.6875D, baseY, droop);
		}
		if (channel.canConnectTo(world, pos, EnumFacing.SOUTH)) { // +Z
			renderSloped(buffer, 0.3125D, 0.625D, 0.6875D, 1D, baseY, droop);
		}
		if (channel.canConnectTo(world, pos, EnumFacing.NORTH)) { // -Z
			renderSloped(buffer, 0.3125D, 0D, 0.6875D, 0.375D, baseY, droop);
		}

		renderSloped(buffer, 0.375D, 0.375D, 0.625D, 0.625D, baseY, droop);

        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static double slopedHeight(double vx, double vz, double baseY, double droop) {
        double dx = vx - 0.5;
        double dz = vz - 0.5;
        return baseY - droop + droop * (dx * dx + dz * dz) * 2;
    }

    private void renderSloped(BufferBuilder buffer, double minX, double minZ, double maxX, double maxZ, double baseY, double droop) {
        double h1 = slopedHeight(minX, minZ, baseY, droop);
        double h2 = slopedHeight(minX, maxZ, baseY, droop);
        double h3 = slopedHeight(maxX, maxZ, baseY, droop);
        double h4 = slopedHeight(maxX, minZ, baseY, droop);
        double bottomY = 0.125D;

        buffer.pos(minX, bottomY, minZ).tex(0, 0).endVertex();
        buffer.pos(minX, bottomY, maxZ).tex(0, 1).endVertex();
        buffer.pos(maxX, bottomY, maxZ).tex(1, 1).endVertex();
        buffer.pos(maxX, bottomY, minZ).tex(1, 0).endVertex();

        buffer.pos(minX, h1, minZ).tex(0, 0).endVertex();
        buffer.pos(minX, h2, maxZ).tex(0, 1).endVertex();
        buffer.pos(maxX, h3, maxZ).tex(1, 1).endVertex();
        buffer.pos(maxX, h4, minZ).tex(1, 0).endVertex();
    }
}
