package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.TEISRBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_vortex")
public class ItemRenderWeaponVortex extends TEISRBase {

	@Override
	public void renderByItem(ItemStack itemStackIn) {
		GlStateManager.translate(0.5, 0.5, 0.5);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.vortex_tex);
		GlStateManager.enableCull();
		
		switch(type){
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			double[] recoil = HbmAnimations.getRelevantTransformation("VORTEX_RECOIL", type == TransformType.FIRST_PERSON_LEFT_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
			//Scaled up by 10 from the regular scale amount so the item bobbing affects the gun less.
			GlStateManager.scale(0.5, 0.4, 0.5);
			if(type == TransformType.FIRST_PERSON_RIGHT_HAND){
				GlStateManager.rotate(178, 0, 1, 0);
				GlStateManager.rotate((float) (27+recoil[1]), 0, 0, 1);
				GlStateManager.translate(14, -16, 3);
			} else {
				GlStateManager.rotate((float) (27+recoil[1]), 0, 0, 1);
				GlStateManager.rotate(2, 0, 1, 0);
				GlStateManager.translate(13, -16, -4);
			}
			
			GlStateManager.translate(recoil[2], 0, 0);
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case HEAD:
		case FIXED:
		case GROUND:
			GlStateManager.translate(0, -0.65, -0.3);
			//This scale makes it a little bit shorter and longer, I think it looks better like that personally.
			GlStateManager.scale(0.08, 0.05, 0.06);
			GlStateManager.rotate(90, 0, 1, 0);
			break;
		case GUI:
			GlStateManager.translate(0, -0.25, 0);
			GlStateManager.scale(0.02, 0.02, 0.02);
			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(45, 0, 0, 1);
			break;
		case NONE:
			break;
		}
		ResourceManager.vortex.renderAll();
	}
}
