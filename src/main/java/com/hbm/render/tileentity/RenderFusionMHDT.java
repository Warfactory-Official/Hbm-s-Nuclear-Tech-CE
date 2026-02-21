package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionMHDT;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderFusionMHDT extends TileEntitySpecialRenderer<TileEntityFusionMHDT> implements IItemRendererProvider {

    @Override
    public void render(TileEntityFusionMHDT turbine, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch(turbine.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fusion_mhdt_tex);
        ResourceManager.fusion_mhdt.renderPart("Turbine");

        GlStateManager.pushMatrix();
        float rot = (turbine.prevRotor + (turbine.rotor - turbine.prevRotor) * partialTicks) % 15;
        GlStateManager.translate(0, 1.5, 0);
        GlStateManager.rotate(rot, 1, 0, 0);
        GlStateManager.translate(0, -1.5, 0);
        ResourceManager.fusion_mhdt.renderPart("Coils");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.fusion_mhdt);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.scale(2.5, 2.5, 2.5);
                GlStateManager.rotate(90, 0, 1, 0);
            }
            public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fusion_mhdt_tex);
                ResourceManager.fusion_mhdt.renderPart("Turbine");
                double rot = ((double) System.currentTimeMillis() / 5) % 30D;
                rot -= 15;
                GlStateManager.translate(0, 1.5, 0);
                GlStateManager.rotate(rot, 1, 0, 0);
                GlStateManager.translate(0, -1.5, 0);
                ResourceManager.fusion_mhdt.renderPart("Coils");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
