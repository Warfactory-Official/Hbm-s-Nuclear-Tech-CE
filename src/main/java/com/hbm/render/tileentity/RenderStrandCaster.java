package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

@AutoRegister
public class RenderStrandCaster extends TileEntitySpecialRenderer<TileEntityMachineStrandCaster> implements IItemRendererProvider {
    public static final ResourceLocation lava = new ResourceLocation(Tags.MODID, "textures/models/machines/lava_gray.png");
    private static DoubleBuffer buf = null;

    @Override
    public void render(@NotNull TileEntityMachineStrandCaster caster, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (buf == null) {
            buf = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        switch (caster.getBlockMetadata() - BlockDummyable.offset) {
            case 4 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 2 -> GlStateManager.rotate(0, 0F, 1F, 0F);
        }
        GlStateManager.translate(0.5, 0, 0.5);
        GlStateManager.rotate(180, 0, 1, 0);

        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.strand_caster_tex);
        ResourceManager.strand_caster.renderPart("caster");

        if (caster.amount != 0 && caster.getInstalledMold() != null) {

            double level = ((double) caster.amount / (double) caster.getCapacity()) * 0.675D;
            double offset = ((double) caster.amount / (double) caster.getInstalledMold().getCost()) * 0.375D;

            int color = caster.type.moltenColor;

            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;

            boolean prevLighting = RenderUtil.isLightingEnabled();
            GlStateManager.disableLighting();

            GlStateManager.pushMatrix();
            GlStateManager.color(r / 255F, g / 255F, b / 255F);
            GL11.glEnable(GL11.GL_CLIP_PLANE0);
            buf.put(new double[]{0, 0, -1, 0.5});
            buf.rewind();
            GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);
            GlStateManager.translate(0, 0, Math.max(-offset + 3.4, 0));
            ResourceManager.strand_caster.renderPart("plate");
            GL11.glDisable(GL11.GL_CLIP_PLANE0);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            bindTexture(lava);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            float rf = r / 255F;
            float gf = g / 255F;
            float bf = b / 255F;
            double yPlane = 2.3 + level;

            buffer.pos(-0.9, yPlane, -0.999).tex(0, 0).color(rf, gf, bf, 1.0F).normal(0F, 1F, 0F).endVertex();
            buffer.pos(-0.9, yPlane, 0.999).tex(0, 1).color(rf, gf, bf, 1.0F).normal(0F, 1F, 0F).endVertex();
            buffer.pos(0.9, yPlane, 0.999).tex(1, 1).color(rf, gf, bf, 1.0F).normal(0F, 1F, 0F).endVertex();
            buffer.pos(0.9, yPlane, -0.999).tex(1, 0).color(rf, gf, bf, 1.0F).normal(0F, 1F, 0F).endVertex();
            tessellator.draw();

            GlStateManager.popMatrix();
            if (prevLighting) GlStateManager.enableLighting();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_strand_caster);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(2, 0, 2);
                GlStateManager.scale(2, 2, 2);
            }

            public void renderCommon() {
                int prevShade = RenderUtil.getShadeModel();
                GlStateManager.scale(1, 1, 1);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.strand_caster_tex);
                ResourceManager.strand_caster.renderAll();
                GlStateManager.shadeModel(prevShade);
            }
        };
    }
}
