package com.hbm.render.misc;

import com.hbm.items.weapon.ItemMissile.PartType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;

import java.nio.DoubleBuffer;

public class MissilePronter {
	private static DoubleBuffer buffer;

	public static void prontMissile(MissileMultipart missile, TextureManager tex) {
		
		//if(!missile.hadFuselage())
		//	return;
		
		GlStateManager.pushMatrix();
		
		if(missile.thruster != null && missile.thruster.type.name().equals(PartType.THRUSTER.name())) {
			
			tex.bindTexture(missile.thruster.texture);
			missile.thruster.model.renderAll();
			GlStateManager.translate(0, missile.thruster.height, 0);
		}
		
		if(missile.fuselage != null && missile.fuselage.type.name().equals(PartType.FUSELAGE.name())) {

			if(missile.fins != null && missile.fins.type.name().equals(PartType.FINS.name())) {
				
				tex.bindTexture(missile.fins.texture);
				missile.fins.model.renderAll();
			}
			
			tex.bindTexture(missile.fuselage.texture);
			missile.fuselage.model.renderAll();
			GlStateManager.translate(0, missile.fuselage.height, 0);
		}
		
		if(missile.warhead != null && missile.warhead.type.name().equals(PartType.WARHEAD.name())) {
			
			tex.bindTexture(missile.warhead.texture);
			missile.warhead.model.renderAll();
		}

		GlStateManager.popMatrix();
	}
}
