package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelBroadcaster;
import com.hbm.tileentity.machine.TileEntityRadioRec;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderRadioRec extends TileEntitySpecialRenderer<TileEntityRadioRec> {

	private static final ResourceLocation texture8 = new ResourceLocation(Tags.MODID + ":" + "textures/models/deco/ModelRadioReceiver.png");
	private ModelBroadcaster model6;
	
	public RenderRadioRec() {
		this.model6 = new ModelBroadcaster();
	}
	
	@Override
	public void render(TileEntityRadioRec te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GL11.glRotated(180, 0, 1, 0);
		GlStateManager.enableLighting();
		GlStateManager.enableLighting();
		this.bindTexture(texture8);
		switch(te.getBlockMetadata())
		{
		case 4:
			GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 2:
			GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 5:
			GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 3:
			GlStateManager.rotate(0, 0F, 1F, 0F); break;
		}
		this.model6.renderModel(0.0625F);
		GlStateManager.popMatrix();
	}
}
