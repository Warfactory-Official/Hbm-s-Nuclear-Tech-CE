package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.machine.FoundryChannel;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
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

        boolean doRender = tile.amount > 0 && tile.type != null;
        if (!doRender) {
            return;
        }

        Color color = new Color(tile.type.moltenColor).brighter();

        if(color != null) {
            double brightener = 0.7D;
            int nr = (int) (255D - (255D - color.getRed()) * brightener);
            int ng = (int) (255D - (255D - color.getGreen()) * brightener);
            int nb = (int) (255D - (255D - color.getBlue()) * brightener);

            color = new Color(nr, ng, nb);
        }

        double level = tile.amount * 0.25D / tile.getCapacity();

        bindTexture(LAVA_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();

        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

        if (channel.canConnectTo(world, pos, EnumFacing.EAST)) { // +X
            renderLiquid(buffer, 0.625D, 0.125D, 0.3125D, 1D, 0.125D + level, 0.6875D);
        }
        if (channel.canConnectTo(world, pos, EnumFacing.WEST)) { // -X
            renderLiquid(buffer, 0D, 0.125D, 0.3125D, 0.375D, 0.125D + level, 0.6875D);
        }
        if (channel.canConnectTo(world, pos, EnumFacing.SOUTH)) { // +Z
            renderLiquid(buffer, 0.3125D, 0.125D, 0.625D, 0.6875D, 0.125D + level, 1D);
        }
        if (channel.canConnectTo(world, pos, EnumFacing.NORTH)) { // -Z
            renderLiquid(buffer, 0.3125D, 0.125D, 0D, 0.6875D, 0.125D + level, 0.375D);
        }

        renderLiquid(buffer, 0.375D, 0.125D, 0.375D, 0.625D, 0.125D + level, 0.625D);

        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderLiquid(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        buffer.pos(minX, minY, minZ).tex(0, 0).endVertex();
        buffer.pos(minX, minY, maxZ).tex(0, 1).endVertex();
        buffer.pos(maxX, minY, maxZ).tex(1, 1).endVertex();
        buffer.pos(maxX, minY, minZ).tex(1, 0).endVertex();

        buffer.pos(minX, maxY, minZ).tex(0, 0).endVertex();
        buffer.pos(minX, maxY, maxZ).tex(0, 1).endVertex();
        buffer.pos(maxX, maxY, maxZ).tex(1, 1).endVertex();
        buffer.pos(maxX, maxY, minZ).tex(1, 0).endVertex();
    }
}
