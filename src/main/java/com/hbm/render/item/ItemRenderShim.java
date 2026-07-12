package com.hbm.render.item;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "shimmer_sledge")
@AutoRegister(item = "shimmer_axe")
@AutoRegister(item = "stopsign")
@AutoRegister(item = "sopsign")
@AutoRegister(item = "chernobylsign")
public class ItemRenderShim extends TEISRBase {

	// 1.7 shim items are flat 2D icons in inventory (their IItemRenderer does not handle INVENTORY); keep the
	// flat GUI model, DEFAULT transforms give identity held-binding so ItemRenderFrames17 reproduces the 1.7
	// held/ground poses (the old per-hand ops were compensating for the pre-e6fceb4a9 non-identity binding).
	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.inventoryWithGuiModel(item, ItemCameraTransforms.DEFAULT, getGuiTexture(item));
	}

	private ResourceLocation getGuiTexture(Item item) {
		if (item == ModItems.shimmer_sledge) {
			return new ResourceLocation(Tags.MODID, "items/shimmer_sledge_original");
		}
		if (item == ModItems.shimmer_axe) {
			return new ResourceLocation(Tags.MODID, "items/shimmer_axe");
		}
		return new ResourceLocation(Tags.MODID, "items/" + item.getRegistryName().getPath());
	}

	@Override
	public void renderByItem(ItemStack stack) {
		GlStateManager.pushMatrix();

		boolean sign = stack.getItem() == ModItems.stopsign || stack.getItem() == ModItems.sopsign || stack.getItem() == ModItems.chernobylsign;

		// ItemRenderFrames17 base frame + verbatim 1.7 ItemRenderShim body.
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
			// 1.7 EQUIPPED_FIRST_PERSON adds a sign-only prefix before the shared body.
			if(sign) {
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glRotatef(-90, 0, 0, 1);
				GL11.glTranslatef(-1, -1.5F, 0);
			}
			shimBody(stack, sign);
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
			// shim items are ItemSword-based (WeaponSpecial → ItemSword), so isFull3D()=true → the full-3D
			// held branch.
			GlStateManager.multMatrix(type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_FULL3D_LEFT : ItemRenderFrames17.THIRD_PERSON_FULL3D);
			shimBody(stack, sign);
			break;
		case HEAD:
			GlStateManager.multMatrix(ItemRenderFrames17.HEAD);
			shimBody(stack, sign);
			break;
		case GROUND:
			GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
			shimBody(stack, sign);
			break;
		case FIXED:
			GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
			shimBody(stack, sign);
			break;
		default:
			break;
		}

		GlStateManager.popMatrix();
	}

	// 1.7 shared EQUIPPED/ENTITY body: bind texture, per-type pose, render.
	private static void shimBody(ItemStack stack, boolean sign) {
		if(stack.getItem() == ModItems.shimmer_sledge)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_sledge_tex);
		if(stack.getItem() == ModItems.shimmer_axe)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_axe_tex);
		if(stack.getItem() == ModItems.stopsign)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.stopsign_tex);
		if(stack.getItem() == ModItems.sopsign)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.sopsign_tex);
		if(stack.getItem() == ModItems.chernobylsign)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.chernobylsign_tex);

		if(stack.getItem() == ModItems.shimmer_sledge || stack.getItem() == ModItems.shimmer_axe) {
			GL11.glRotatef(-135, 0, 0, 1);
			GL11.glRotatef(180, 0, 0, 1);
			GL11.glScalef(1.5F, 1.5F, 1.5F);
			GL11.glTranslatef(0.45F, -0.3F, 0);
		}
		if(sign) {
			GL11.glRotatef(45, 0, 0, 1);
			GL11.glScalef(0.35F, 0.35F, 0.35F);
			GL11.glTranslatef(2, -2, 0);
			GL11.glRotatef(90, 0, 1, 0);
		}

		if(stack.getItem() == ModItems.shimmer_sledge)
			ResourceManager.shimmer_sledge.renderAll();
		if(stack.getItem() == ModItems.shimmer_axe)
			ResourceManager.shimmer_axe.renderAll();
		if(sign)
			ResourceManager.stopsign.renderAll();
	}
}
