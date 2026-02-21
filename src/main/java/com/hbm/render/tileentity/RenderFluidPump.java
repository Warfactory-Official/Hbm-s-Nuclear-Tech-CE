package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.FluidPump;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderFluidPump extends TileEntitySpecialRenderer<FluidPump.TileEntityFluidPump> implements IItemRendererProvider {

    @Override
    public void render(FluidPump.TileEntityFluidPump tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (tile.getBlockMetadata()) {
            case 2 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(90, 0F, 1F, 0F);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fluid_pump_tex);
        ResourceManager.fluid_pump.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.fluid_pump);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(5, 5, 5);
            }
            public void renderCommon() {
                GlStateManager.scale(2, 2, 2);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fluid_pump_tex);
                ResourceManager.fluid_pump.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
