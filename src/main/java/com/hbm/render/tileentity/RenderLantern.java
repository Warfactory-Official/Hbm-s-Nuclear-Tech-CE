package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.deco.TileEntityLantern;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;

@AutoRegister
public class RenderLantern extends TileEntitySpecialRenderer<TileEntityLantern> implements IItemRendererProvider {

    @Override
    public void render(TileEntityLantern te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        bindTexture(ResourceManager.lantern_tex);
        ResourceManager.lantern.renderPart("Lantern");

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        if (prevLighting) GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        float mult = (float) (Math.sin(System.currentTimeMillis() / 200D) / 2 + 0.5) * 0.1F + 0.9F;
        GlStateManager.color(mult, mult, 0.7F * mult);
        ResourceManager.lantern.renderPart("Light");
        GlStateManager.color(1F, 1F, 1F);
        if (prevLighting) GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.lantern);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -5, 0);
                double scale = 2.75;
                GlStateManager.scale(scale, scale, scale);
            }
            public void renderCommon() {
                bindTexture(ResourceManager.lantern_tex);
                ResourceManager.lantern.renderPart("Lantern");
                GlStateManager.disableTexture2D();
                float mult = (float) (Math.sin(System.currentTimeMillis() / 200D) / 2 + 0.5) * 0.1F + 0.9F;
                GlStateManager.color(mult, mult, 0.7F * mult);
                ResourceManager.lantern.renderPart("Light");
                GlStateManager.color(1F, 1F, 1F);
                GlStateManager.enableTexture2D();
            }};
    }
}
