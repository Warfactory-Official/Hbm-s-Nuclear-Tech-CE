package com.hbm.render.misc;

import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class SoyuzLauncherPronter {

	public static void prontLauncher(double rot) {

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		TextureManager tex = Minecraft.getMinecraft().getTextureManager();

		tex.bindTexture(ResourceManager.soyuz_launcher_legs_tex);
		ResourceManager.soyuz_launcher_legs.renderAll();

		tex.bindTexture(ResourceManager.soyuz_launcher_table_tex);
		ResourceManager.soyuz_launcher_table.renderAll();

		tex.bindTexture(ResourceManager.soyuz_launcher_tower_base_tex);
		ResourceManager.soyuz_launcher_tower_base.renderAll();

		GlStateManager.pushMatrix();
			tex.bindTexture(ResourceManager.soyuz_launcher_tower_tex);
			GlStateManager.translate(0, 5.5, 5.5);
			GlStateManager.rotate((float) (rot), 1, 0, 0);
			GlStateManager.translate(0, -5.5, -5.5);
			ResourceManager.soyuz_launcher_tower.renderAll();
		GlStateManager.popMatrix();

		tex.bindTexture(ResourceManager.soyuz_launcher_support_base_tex);
		ResourceManager.soyuz_launcher_support_base.renderAll();

		GlStateManager.pushMatrix();
			tex.bindTexture(ResourceManager.soyuz_launcher_support_tex);
			GlStateManager.translate(0, 5.5, -6.5);
			GlStateManager.rotate((float) (rot), -1, 0, 0);
			GlStateManager.translate(0, -5.5, 6.5);
			ResourceManager.soyuz_launcher_support.renderAll();
		GlStateManager.popMatrix();
		GlStateManager.enableCull();
		
		GlStateManager.popMatrix();
	}
}
