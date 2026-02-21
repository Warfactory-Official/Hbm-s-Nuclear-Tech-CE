package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionCollector;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFusionCollector extends TileEntitySpecialRenderer<TileEntityFusionCollector>
        implements IItemRendererProvider {

    @Override
    public void render(
            TileEntityFusionCollector collector,
            double x,
            double y,
            double z,
            float partialTicks,
            int destroyStage,
            float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y, z + 0.5F);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        int meta = collector.getBlockMetadata() - BlockDummyable.offset;
        float rotationY = switch (meta) {
            case 3 -> 270f;
            case 5 -> 0f;
            case 4 -> 180f;
            default -> 90f;
        };
        GlStateManager.rotate(rotationY, 0F, 1F, 0F);

        bindTexture(ResourceManager.fusion_collector_tex);
        ResourceManager.fusion_collector.renderAll();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.fusion_collector);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(5, 5, 5);
                GlStateManager.rotate(90, 0, 1, 0);
            }
            public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GL11.glShadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fusion_collector_tex);
                ResourceManager.fusion_collector.renderAll();
                GL11.glShadeModel(GL11.GL_FLAT);
            }};
    }
}
