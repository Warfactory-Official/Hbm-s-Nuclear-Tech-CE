package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.machine.Radiobox;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelRadio;
import com.hbm.tileentity.machine.TileEntityRadiobox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
@AutoRegister
public class RenderRadiobox extends TileEntitySpecialRenderer<TileEntityRadiobox> {

	private static final ResourceLocation texture7 = new ResourceLocation(Tags.MODID + ":" + "textures/models/turrets/ModelRadio.png");
	private ModelRadio model7;
	
	public RenderRadiobox() {
		this.model7 = new ModelRadio();
	}
	
	@Override
	public void render(TileEntityRadiobox te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.enableLighting();
		GlStateManager.enableLighting();
		this.bindTexture(texture7);
		switch(te.getWorld().getBlockState(te.getPos()).getValue(Radiobox.FACING))
		{
		case WEST:
			GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case NORTH:
			GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case EAST:
			GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case SOUTH:
			GlStateManager.rotate(180, 0F, 1F, 0F); break;
		default:
			break;
		}
		GlStateManager.translate(0, 0, 1);
		this.model7.renderModel(0.0625F, te.getWorld().getBlockState(te.getPos()).getValue(Radiobox.STATE) ? 160 : 20);
		GlStateManager.popMatrix();
	}
}
