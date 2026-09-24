package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineIGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@AutoRegister
public class RenderIGenerator extends TileEntitySpecialRenderer<TileEntityMachineIGenerator> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineIGenerator te, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);

		switch(te.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		}

		GlStateManager.translate(0, 0, -1);

		GlStateManager.enableLighting();
		GlStateManager.disableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableRescaleNormal();

		GlStateManager.translate(0, 0, 1);
		GlStateManager.scale(1D / 6D, 1D / 6D, 1D / 6D);
		GlStateManager.translate(0, 0, -0.5);

		bindTexture(ResourceManager.igen_tex);
		ResourceManager.igen.renderPart("Body");
		ResourceManager.igen.renderPart("Rotor");

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_industrial_generator);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -4, 0);
				GlStateManager.scale(3, 3, 3);
			}

			public void renderCommon() {
				GlStateManager.scale(1D / 6D, 1D / 6D, 1D / 6D);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GlStateManager.disableCull();

				bindTexture(ResourceManager.igen_tex);
				ResourceManager.igen.renderPart("Body");
				ResourceManager.igen.renderPart("Rotor");

				GlStateManager.enableCull();
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}
		};
	}
}
