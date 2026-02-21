package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class RenderFurnaceCombination extends TileEntitySpecialRenderer<TileEntityFurnaceCombination> implements IItemRendererProvider {

    public static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/rbmk_fire.png");

    @Override
    public void render(TileEntityFurnaceCombination furnace, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        this.bindTexture(ResourceManager.combination_oven_tex);
        ResourceManager.combination_oven.renderAll();

        if (furnace.wasOn) {
            this.bindTexture(texture);

            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // additive
            RenderHelper.disableStandardItemLighting();

            int texIndex = (int) ((furnace.getWorld().getTotalWorldTime() / 2) % 14);
            float f0 = 1F / 14F;
            float uMin = (texIndex % 5) * f0;
            float uMax = uMin + f0;
            float vMin = 0F;
            float vMax = 1F;

            GlStateManager.translate(0.0D, 1.75D, 0.0D);
            RenderManager rm = Minecraft.getMinecraft().getRenderManager();
            GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);

            double scaleH = 1.0D;
            double scaleV = 3.0D;
            final int FULL_BRIGHT = 0xF000F0;
            int lx = FULL_BRIGHT & 0xFFFF;
            int ly = (FULL_BRIGHT >> 16) & 0xFFFF;

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

            buf.pos(-scaleH, 0.0D, 0.0D).tex(uMax, vMax).lightmap(lx, ly).color(255, 255, 255, 255).endVertex();
            buf.pos(-scaleH, scaleV, 0.0D).tex(uMax, vMin).lightmap(lx, ly).color(255, 255, 255, 255).endVertex();
            buf.pos( scaleH, scaleV, 0.0D).tex(uMin, vMin).lightmap(lx, ly).color(255, 255, 255, 255).endVertex();
            buf.pos( scaleH, 0.0D, 0.0D).tex(uMin, vMax).lightmap(lx, ly).color(255, 255, 255, 255).endVertex();

            tess.draw();

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
            RenderHelper.enableStandardItemLighting();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.furnace_combination);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase( ) {
            public void renderInventory() {
                GL11.glTranslated(0, -1.5, 0);
                GL11.glScaled(3.25, 3.25, 3.25);
            }
            public void renderCommon() {
                bindTexture(ResourceManager.combination_oven_tex);
                ResourceManager.combination_oven.renderAll();
            }};
    }
}
