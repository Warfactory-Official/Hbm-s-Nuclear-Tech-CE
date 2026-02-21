package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControl;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlAuto;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlManual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
@AutoRegister(tileentity = TileEntityRBMKControlManual.class)
@AutoRegister(tileentity = TileEntityRBMKControlAuto.class)
public class RenderRBMKControlRod extends TileEntitySpecialRenderer<TileEntityRBMKControl>{

	private ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control.png");
	
	@Override
	public boolean isGlobalRenderer(TileEntityRBMKControl te){
		return true;
	}

	@Override
	public void render(TileEntityRBMKControl control, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		int offset = 1;
		for (int o = 1; o < 16; o++){
			if (control.getWorld().getBlockState(control.getPos().up(o)).getBlock() == control.getBlockType()){
				offset = o;
			} else break;
		}
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		// Render column stack
		renderControlColumn(control, offset + 1);

		// Render animated lid
		renderControlLid(control, partialTicks, offset);

		GlStateManager.popMatrix();
	}

	private void renderControlColumn(TileEntityRBMKControl control, int offset) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(((RBMKBase) control.getBlockType()).columnTexture);

		GlStateManager.pushMatrix();
		for(int i = 0; i < offset; i++) {
			ResourceManager.rbmk_rods.renderPart("Column");
			GlStateManager.translate(0, 1, 0);
		}
		GlStateManager.popMatrix();
	}

	private void renderControlLid(TileEntityRBMKControl control, float partialTicks, int offset) {
		GlStateManager.pushMatrix();

		// Calculate animated lid position
		double level = control.lastLevel + (control.level - control.lastLevel) * partialTicks;
		GlStateManager.translate(0, offset + level, 0);

		// Bind lid texture
		ResourceLocation lidTexture = (control.getBlockType() instanceof RBMKBase)
				? ((RBMKBase) control.getBlockType()).coverTexture
				: texture;
		Minecraft.getMinecraft().getTextureManager().bindTexture(lidTexture);

		// Render lid
		ResourceManager.rbmk_rods.renderPart("Lid");

		GlStateManager.popMatrix();
	}
}
