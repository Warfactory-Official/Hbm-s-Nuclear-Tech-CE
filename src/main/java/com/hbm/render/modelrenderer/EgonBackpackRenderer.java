package com.hbm.render.modelrenderer;

import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class EgonBackpackRenderer extends ModelRenderer {

	public static boolean showBackpack = false;
	
	public EgonBackpackRenderer(ModelBase model) {
		super(model);
		this.addBox(0.0F, 0.0F, 0.0F, 0, 0, 0);
	}
	
	@Override
	public void render(float scale) {
		if(!showBackpack)
			return;
		GlStateManager.pushMatrix();
		//Oh neat, bob made the model so it would fit perfectly without screwing around with mostly right translations.
		GlStateManager.translate(0, 0.75F, 0);
		GlStateManager.scale(0.0625, 0.0625, 0.0625);
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.rotate(90, 0, 1, 0);
		int tex = GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.egon_backpack_tex);
		int prevShadeModel = GlStateManager.glGetInteger(GL11.GL_SHADE_MODEL);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.egon_backpack.renderAll();
		GlStateManager.shadeModel(prevShadeModel);
		GlStateManager.bindTexture(tex);
		GlStateManager.popMatrix();
	}

}
