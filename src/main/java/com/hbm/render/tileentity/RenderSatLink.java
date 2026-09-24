package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineSatLink;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderSatLink extends TileEntitySpecialRenderer<TileEntityMachineSatLink> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineSatLink link, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.rotate(180, 0F, 1F, 0F);

		ForgeDirection dir = ForgeDirection.getOrientation(link.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		GlStateManager.translate((dir.offsetX + rot.offsetX) * 0.5, 0, (dir.offsetZ + rot.offsetZ) * 0.5);

		float r = link.prevRot + (link.rot - link.prevRot) * interp;
		float l = link.prevLift + (link.lift - link.prevLift) * interp;

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.satlink_tex);
		ResourceManager.satlink.renderPart("Base");
		GlStateManager.rotate(r, 0, 1, 0);
		ResourceManager.satlink.renderPart("Rotor");
		GlStateManager.translate(0, 7.375, 0);
		GlStateManager.rotate(l, 0, 0, 1);
		GlStateManager.translate(0, -7.375, 0);
		ResourceManager.satlink.renderPart("Dish");
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_satlink);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -5, 0);
				GlStateManager.scale(3.5, 3.5, 3.5);
			}
			public void renderCommon(ItemStack item) {
				GlStateManager.scale(0.5, 0.5, 0.5);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.satlink_tex);
				ResourceManager.satlink.renderPart("Base");
				GlStateManager.rotate(15, 0, 1, 0);
				ResourceManager.satlink.renderPart("Rotor");
				GlStateManager.translate(0, 7.375, 0);
				GlStateManager.rotate(-45, 0, 0, 1);
				GlStateManager.translate(0, -7.375, 0);
				ResourceManager.satlink.renderPart("Dish");
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}};
	}
}
