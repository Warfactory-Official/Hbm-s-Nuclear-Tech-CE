package com.hbm.render.item;

import com.hbm.items.weapon.ItemMissile.PartType;
import com.hbm.render.misc.MissilePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ItemRenderMissilePart extends TEISRBase {

	MissilePart part;

	public ItemRenderMissilePart(MissilePart part) {
		this.part = part;
	}

	// 1.7-exact frames need identity binding (see ItemRenderFrames17); the old translate(-0.25,0.25,-0.25)
	// prologue and rewritten GUI angles were compensating for the pre-e6fceb4a9 non-identity TEISR binding.
	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.inventory(item, ItemCameraTransforms.DEFAULT);
	}

	@Override
	public boolean useIdentityTransform(Item item) {
		return true;
	}

	@Override
	public void renderByItem(ItemStack item) {
		if(part == null)
			return;

		GlStateManager.pushMatrix();

		// ItemRenderFrames17 base frame + verbatim 1.7 ItemRenderMissilePart body. 1.7 EQUIPPED and
		// EQUIPPED_FIRST_PERSON both translate(0.5,0,0) then fall through to ENTITY's scale(0.4).
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
			GL11.glTranslated(0.5, 0, 0);
			GL11.glScaled(0.4, 0.4, 0.4);
			bindAndRender();
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case HEAD:
			GlStateManager.multMatrix(type == TransformType.HEAD ? ItemRenderFrames17.HEAD
					: type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_LEFT
					: ItemRenderFrames17.THIRD_PERSON);
			GL11.glTranslated(0.5, 0, 0);
			GL11.glScaled(0.4, 0.4, 0.4);
			bindAndRender();
			break;
		case GROUND:
			GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
			GL11.glScaled(0.4, 0.4, 0.4);
			bindAndRender();
			break;
		case FIXED:
			GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
			GL11.glScaled(0.4, 0.4, 0.4);
			bindAndRender();
			break;
		case GUI:
			GlStateManager.multMatrix(ItemRenderFrames17.GUI);

			double height = part.guiheight;
			if(height == 0D)
				height = 4D;

			double size = 10;
			double scale = size / height;

			GL11.glTranslated(height / 2 * scale, 0, 0);
			GL11.glRotated(135, 0, 0, 1);
			GL11.glRotated(145, 1, 0, 0);

			if(part.type == PartType.WARHEAD) {
				GL11.glTranslated(0, height / 8 * scale, 0);
			}
			if(part.type == PartType.FUSELAGE) {
				GL11.glTranslated(0, height / 4 * scale, 0);
			}

			GL11.glTranslated(3.5, 14, 0);
			GL11.glScaled(-scale, -scale, -scale);

			GL11.glRotatef(System.currentTimeMillis() / 25 % 360, 0, -1, 0);
			bindAndRender();
			break;
		default: break;
		}

		GlStateManager.popMatrix();
	}

	private void bindAndRender() {
		Minecraft.getMinecraft().renderEngine.bindTexture(part.texture);
		part.model.renderAll();
	}
}
