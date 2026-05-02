package com.hbm.render.item.weapon;

import com.hbm.Tags;
import com.hbm.animloader.AnimationWrapper;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_supershotgun")
public class ItemRenderWeaponShotty extends TEISRBase {

	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.inventoryWithGuiModel(item, BakedModelTransforms.defaultItemTransforms(), new ResourceLocation(Tags.MODID, "items/gun_uboinik"));
	}

	@Override
	public void renderByItem(ItemStack item) {
		GlStateManager.popMatrix();
		GlStateManager.disableCull();
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.universal);
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			if(GeneralConfig.ssgAnim) {
				if(type == TransformType.FIRST_PERSON_LEFT_HAND) {
					GlStateManager.translate(-0.81, -0.7, 0.4);
					GL11.glRotated(4, 1, 0, 0);
					GlStateManager.rotate(23F, 0.0F, 0.0F, 1.0F);
					GlStateManager.rotate(5F, 0.0F, 1.0F, 0.0F);
					GL11.glScaled(1, 1.5, 1.5);
				} else {
					GlStateManager.translate(0, -7, -0.6);
					double[] recoil = HbmAnimations.getRelevantTransformation("MEATHOOK_RECOIL", EnumHand.MAIN_HAND);
					GlStateManager.translate(recoil[2], recoil[1], 0);
					if(this.entity != null && this.entity.isSneaking()) {
						GlStateManager.translate(0, 0.20, 0.43);
						GL11.glRotated(-4, 1, 0, 0);
						GL11.glRotated(5, 0, 1, 0);
						GL11.glRotated(-4, 0, 0, 1);
					}
					GL11.glRotated(4, 1, 0, 0);
					GlStateManager.rotate(-23F, 0.0F, 0.0F, 1.0F);
					GlStateManager.rotate(175F, 0.0F, 1.0F, 0.0F);
					GL11.glScaled(10, 10, 10);
					GL11.glScaled(1, 1.5, 1.5);
				}
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				if(item.getTagCompound() != null && item.getTagCompound().hasKey("animation")) {
					NBTTagCompound anim = item.getTagCompound().getCompoundTag("animation");
					if(anim.getInteger("id") == 0)
						ResourceManager.supershotgun.controller.setAnim(new AnimationWrapper(anim.getLong("time"), anim.getFloat("mult"), ResourceManager.ssg_reload));
					else
						ResourceManager.supershotgun.controller.setAnim(AnimationWrapper.EMPTY);
					ResourceManager.supershotgun.renderAnimated(System.currentTimeMillis());
				} else {
					ResourceManager.supershotgun.render();
				}
				GlStateManager.shadeModel(GL11.GL_FLAT);
				GlStateManager.enableCull();
				GlStateManager.pushMatrix();
			} else {
				EnumHand hand = type == TransformType.FIRST_PERSON_RIGHT_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				double[] recoil = HbmAnimations.getRelevantTransformation("SHOTTY_RECOIL", hand);
				double[] eject = HbmAnimations.getRelevantTransformation("SHOTTY_BREAK", hand);
				double[] ejectShell = HbmAnimations.getRelevantTransformation("SHOTTY_EJECT", hand);
				double[] insertShell = HbmAnimations.getRelevantTransformation("SHOTTY_INSERT", hand);
				
				GlStateManager.translate(-5, -0.3, -2.5);
				GL11.glRotated(4, 1, 0, 0);
				GlStateManager.rotate(-23F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(175F, 0.0F, 1.0F, 0.0F);
				GL11.glScaled(2, 2, 2);
				
				if(entity.isSneaking()) {
					GlStateManager.translate(0F, 1.0F, -1.8F);
					GlStateManager.rotate(3.5F, 0.0F, 1.0F, 0.0F);
				} else {
					GL11.glRotated(-eject[2] * 0.25, 0, 0, 1);
				}

				GlStateManager.translate(-recoil[0] * 2, 0, 0);
				GL11.glRotated(recoil[0] * 5, 0, 0, 1);
				
				GlStateManager.pushMatrix();
				GL11.glRotated(-eject[2] * 0.8, 0, 0, 1);
				ResourceManager.shotty.renderPart("Barrel");
				
				GlStateManager.pushMatrix();
				GL11.glRotated(ejectShell[0] * 90, 0, 0, 1);
				GlStateManager.translate(-ejectShell[0] * 10, 0, 0);
				ResourceManager.shotty.renderPart("Shells");
				GlStateManager.popMatrix();
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(-insertShell[0], insertShell[2] * -2, insertShell[2] * -1);
				ResourceManager.shotty.renderPart("Shells");
				GlStateManager.popMatrix();
				
				GlStateManager.popMatrix();
				
				ResourceManager.shotty.renderPart("Handle");
				GlStateManager.enableCull();
				GlStateManager.pushMatrix();
			}
			return;
		case HEAD:
		case FIXED:
		case GROUND:
		case THIRD_PERSON_LEFT_HAND:
			GlStateManager.translate(0.0, -0.2, 0.5);
		case THIRD_PERSON_RIGHT_HAND:
			GlStateManager.translate(0.0, -0.35, 0);
			GL11.glRotated(90, 0, 1, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			break;
		case GUI:
			break;
		default:
			break;
		}
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.shotty.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.enableCull();
		GlStateManager.pushMatrix();
	}
}
