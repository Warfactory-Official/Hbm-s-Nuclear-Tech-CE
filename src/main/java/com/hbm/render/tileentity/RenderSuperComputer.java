package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineSuperComputer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderSuperComputer extends TileEntitySpecialRenderer<TileEntityMachineSuperComputer> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineSuperComputer computer, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		switch(computer.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		}

		bindTexture(ResourceManager.supercomputer_tex);
		ResourceManager.supercomputer.renderPart("Computer");

		if(!computer.didProcess) GlStateManager.color(0F, 0F, 0F);

		float scroll = computer.getWorld().getTotalWorldTime() % 20 + interp;
		scroll /= 20F;

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		GlStateManager.disableLighting();
		GlStateManager.pushAttrib();

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();

		bindTexture(ResourceManager.supercomputer_scan_tex);
		GlStateManager.translate(-scroll, 0, 0);
		ResourceManager.supercomputer.renderPart("Lights");

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.color(1F, 1F, 1F);

		GlStateManager.enableLighting();
		GlStateManager.popAttrib();

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_supercomputer);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {

		return new ItemRenderBase() {

			public void renderInventory() {
				GlStateManager.translate(0, -2.5, 0);
				GlStateManager.scale(2.5, 2.5, 2.5);
			}
			public void renderCommon(ItemStack item) {
				GlStateManager.scale(0.5, 0.5, 0.5);
				GlStateManager.translate(-2, 0, 0);
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.supercomputer_tex);
				ResourceManager.supercomputer.renderPart("Computer");
				bindTexture(ResourceManager.supercomputer_scan_tex);
				ResourceManager.supercomputer.renderPart("Lights");
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}};
	}
}
