package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class ModelBackTesla extends ModelArmorBase {

	public ModelBackTesla() {
		super(1);
		body = new ModelRendererObj(ResourceManager.armor_mod_tesla);
	}


	@Override
	public void renderArmor(Entity par1Entity, float par7) {
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mod_tesla);
		body.render(par7);
	}
}