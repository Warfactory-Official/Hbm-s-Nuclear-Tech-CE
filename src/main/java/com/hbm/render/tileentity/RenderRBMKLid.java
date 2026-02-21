package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.blocks.machine.rbmk.RBMKRod;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBoiler;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKHeater;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderRBMKLid extends TileEntitySpecialRenderer<TileEntityRBMKBase> {

	private static final ResourceLocation texture_glass = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_blank_glass.png");
	private static final ResourceLocation texture_rods = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_element_colorable.png");
	
	@Override
	public boolean isGlobalRenderer(TileEntityRBMKBase te){
		return true;
	}

	@Override
	public void render(TileEntityRBMKBase control, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		boolean hasRod = false;
		boolean cherenkov = false;
		float fuelR = 0F;
		float fuelG = 0F;
		float fuelB = 0F;
		float cherenkovR = 0F;
		float cherenkovG = 0F;
		float cherenkovB = 0F;
		float cherenkovA = 0.1F;

		if(control instanceof TileEntityRBMKRod rod) {
            if(rod.hasRod) {
				hasRod = true;
				fuelR = rod.fuelR;
				fuelG = rod.fuelG;
				fuelB = rod.fuelB;
				cherenkovR = rod.cherenkovR;
				cherenkovG = rod.cherenkovG;
				cherenkovB = rod.cherenkovB;
			}
			if(rod.fluxQuantity > 5) {
				cherenkov = true;
				cherenkovA = (float) Math.max(0.25F, Math.log(rod.fluxQuantity) * 0.01F);
			}
		}
		int offset = 1;
		for (int o = 1; o < 16; o++){
			if (control.getWorld().getBlockState(control.getPos().up(o)).getBlock() == control.getBlockType()) {
				offset = o;
				int meta = control.getWorld().getBlockState(control.getPos().up(o)).getBlock().getMetaFromState(control.getWorld().getBlockState(control.getPos().up(o)));
				if (meta > 5 && meta < 12) break;
			} else break;
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);

		if(!(control.getBlockType() instanceof RBMKBase block)) {
			GlStateManager.popMatrix();
			return;
		}

        Minecraft.getMinecraft().getTextureManager().bindTexture(block.columnTexture);
		renderColumnStack(control, offset + 1);

		if(control.hasLid()) {
			renderLid(control, offset);
		}

		if(hasRod) {
			renderFuelRodStack(control, fuelR, fuelG, fuelB, offset + 1);
		}

		if(cherenkov) {
			renderCherenkovEffect(control, cherenkovR, cherenkovG, cherenkovB, cherenkovA, offset);
		}

		GlStateManager.popMatrix();
	}

	// New helper methods
	private void renderColumnStack(TileEntityRBMKBase control, int offset) {
		GlStateManager.pushMatrix();

		// Get the correct model from the main render method
		IModelCustom columnModel = getColumnModelForBlock(control.getBlockType());

		for(int i = 0; i < offset; i++) {
			columnModel.renderPart("Column");  // Use the selected model
			GlStateManager.translate(0, 1, 0);
		}

		GlStateManager.popMatrix();
	}

	private IModelCustom getColumnModelForBlock(Block block) {
		if(block == ModBlocks.rbmk_boiler || block == ModBlocks.rbmk_heater) {
			return ResourceManager.rbmk_rods;
		} else if(block instanceof RBMKRod) {
			return ResourceManager.rbmk_element;
		}
		return ResourceManager.rbmk_reflector;  // Default
	}

	private void renderFuelRodStack(TileEntityRBMKBase control, float r, float g, float b, int offset) {
		GlStateManager.pushMatrix();
		try {
			GlStateManager.color(r, g, b, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture_rods);

			// Render full segments
			for (int i = 0; i < offset; i++) {
				ResourceManager.rbmk_element.renderPart("Rods");
				GlStateManager.translate(0, 1, 0);
			}
			GlStateManager.color(1, 1, 1, 1);
		}finally {
			GlStateManager.popMatrix();
		}
	}

	private void renderLid(TileEntityRBMKBase control, int offset) {
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, offset + control.jumpheight, 0);

		int meta = control.getBlockMetadata() - RBMKBase.offset;
		ResourceLocation lidTexture = (meta == RBMKBase.DIR_GLASS_LID.ordinal()) ?
				texture_glass : ((RBMKBase) control.getBlockType()).coverTexture;

		Minecraft.getMinecraft().getTextureManager().bindTexture(lidTexture);

		if((control instanceof TileEntityRBMKBoiler || control instanceof TileEntityRBMKHeater) && meta != RBMKBase.DIR_GLASS_LID.ordinal()) {
			ResourceManager.rbmk_rods.renderPart("Lid");
		}
		ResourceManager.rbmk_element.renderPart("Lid");

		GlStateManager.popMatrix();
	}

	private void renderCherenkovEffect(TileEntityRBMKBase control, float r, float g, float b, float a, int offset) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.75, 0);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		for(double j = 0; j <= offset; j += 0.25) {
			buf.pos(-0.5, j, -0.5).color(r, g, b, a).endVertex();
			buf.pos(-0.5, j, 0.5).color(r, g, b, a).endVertex();
			buf.pos(0.5, j, 0.5).color(r, g, b, a).endVertex();
			buf.pos(0.5, j, -0.5).color(r, g, b, a).endVertex();
		}
		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
