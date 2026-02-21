package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFirebox extends TileEntitySpecialRenderer<TileEntityHeaterFirebox> implements IItemRendererProvider {
    @Override
    public boolean isGlobalRenderer(TileEntityHeaterFirebox te) {
        return true;
    }

    @Override
    public void render(TileEntityHeaterFirebox firebox, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        boolean prevLighting = RenderUtil.isLightingEnabled();
        boolean prevCull = RenderUtil.isCullEnabled();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (firebox.getBlockMetadata() - BlockDummyable.offset) {
            case 3 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 2 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(270, 0F, 1F, 0F);
        }

        GlStateManager.rotate(-90, 0F, 1F, 0F);

        bindTexture(ResourceManager.heater_firebox_tex);
        ResourceManager.heater_firebox.renderPart("Main");

        GlStateManager.pushMatrix();
        float door = firebox.prevDoorAngle + (firebox.doorAngle - firebox.prevDoorAngle) * partialTicks;
        GlStateManager.translate(1.375, 0, 0.375);
        GlStateManager.rotate(door, 0F, -1F, 0F);
        GlStateManager.translate(-1.375, 0, -0.375);
        ResourceManager.heater_firebox.renderPart("Door");
        GlStateManager.popMatrix();

        if (firebox.wasOn) {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();

            // Full brightness
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

            ResourceManager.heater_firebox.renderPart("InnerBurning");
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        } else {
            ResourceManager.heater_firebox.renderPart("InnerEmpty");
        }
        if (prevCull) GlStateManager.enableCull(); else GlStateManager.disableCull();
        if (prevLighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }


    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.heater_firebox);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -1, 0);
                GlStateManager.scale(3.25, 3.25, 3.25);
            }

            public void renderCommon() {
                int prevShade = RenderUtil.getShadeModel();
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.heater_firebox_tex);
                ResourceManager.heater_firebox.renderAll();
                GlStateManager.shadeModel(prevShade);
            }
        };
    }
}
