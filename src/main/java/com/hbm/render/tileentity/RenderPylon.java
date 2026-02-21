package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelPylon;
import com.hbm.tileentity.network.energy.TileEntityPylon;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
@AutoRegister(tileentity = TileEntityPylon.class)
public class RenderPylon extends RenderPylonBase {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":" + "textures/models/network/ModelPylon.png");

	private ModelPylon pylon;

	public RenderPylon() {
		this.pylon = new ModelPylon();
	}

	@Override
	public boolean isGlobalRenderer(TileEntityPylonBase te) {
		return true;
	}

	@Override
	public void render(TileEntityPylonBase pyl, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if(!(pyl instanceof TileEntityPylon tepylon)) return;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F - ((1F / 16F) * 14F), (float) z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		bindTexture(texture);
		this.pylon.renderAll(0.0625F);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		this.renderLinesGeneric(tepylon, x, y, z);
		GlStateManager.popMatrix();
	}
}
