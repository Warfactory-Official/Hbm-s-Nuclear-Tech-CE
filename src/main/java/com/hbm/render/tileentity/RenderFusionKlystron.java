package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionKlystron;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFusionKlystron extends TileEntitySpecialRenderer<TileEntityFusionKlystron> implements IItemRendererProvider {

	@Override
    public void render(TileEntityFusionKlystron klystron, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        int meta = klystron.getBlockMetadata() - BlockDummyable.offset;
        float rotationY = switch (meta) {
            case 3 -> 270f;
            case 5 -> 0f;
            case 4 -> 180f;
            default -> 90f;
        };
        GlStateManager.rotate(rotationY, 0F, 1F, 0F);

        GlStateManager.translate(-1, 0, 0);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fusion_klystron_tex);
        ResourceManager.fusion_klystron.renderPart("Klystron");

        GlStateManager.pushMatrix();
        float rot = klystron.prevFan + (klystron.fan - klystron.prevFan) * partialTicks;
        GlStateManager.translate(0, 2.5, 0);
        GlStateManager.rotate(rot, 1, 0, 0);
        GlStateManager.translate(0, -2.5, 0);
        ResourceManager.fusion_klystron.renderPart("Rotor");
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.fusion_klystron);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
                GlStateManager.translate(0, -3, 1);
                GlStateManager.scale(3.5, 3.5, 3.5);
                GlStateManager.rotate(90, 0, 1, 0);
			}
			public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.fusion_klystron_tex);
                ResourceManager.fusion_klystron.renderPart("Klystron");
                double rot = ((double) System.currentTimeMillis() / 10) % 360D;
                GlStateManager.translate(0, 2.5, 0);
                GlStateManager.rotate(rot, 1, 0, 0);
                GlStateManager.translate(0, -2.5, 0);
                ResourceManager.fusion_klystron.renderPart("Rotor");
                GlStateManager.shadeModel(GL11.GL_FLAT);
			}};
	}
}
