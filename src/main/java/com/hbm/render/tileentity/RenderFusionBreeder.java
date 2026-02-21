package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionBreeder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFusionBreeder extends TileEntitySpecialRenderer<TileEntityFusionBreeder>
        implements IItemRendererProvider {

    @Override
    public void render(TileEntityFusionBreeder breeder, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        int meta = breeder.getBlockMetadata() - BlockDummyable.offset;
        float rotationY = switch (meta) {
            case 3 -> 270f;
            case 5 -> 0f;
            case 4 -> 180f;
            default -> 90f;
        };
        GlStateManager.rotate(rotationY, 0F, 1F, 0F);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fusion_breeder_tex);
        ResourceManager.fusion_breeder.renderPart("Breeder");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.fusion_breeder);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(5, 5, 5);
				GL11.glRotated(90, 0, 1, 0);
			}
			public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.fusion_breeder_tex);
				ResourceManager.fusion_breeder.renderPart("Breeder");
                GlStateManager.shadeModel(GL11.GL_FLAT);
			}};
	}
}
