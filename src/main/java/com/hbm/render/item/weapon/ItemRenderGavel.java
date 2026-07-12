package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderFrames17;
import com.hbm.render.item.TEISRBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "wood_gavel")
@AutoRegister(item = "lead_gavel")
@AutoRegister(item = "diamond_gavel")
@AutoRegister(item = "mese_gavel")
public class ItemRenderGavel extends TEISRBase {

	// 1.7 gavel is a flat 2D icon in inventory (its IItemRenderer does not handle INVENTORY), so keep the
	// flat GUI model; DEFAULT transforms give identity held-binding, letting the ItemRenderFrames17 frames
	// reproduce the 1.7 held/ground poses (the old ops had dropped the entire 1.7 EQUIPPED base pose).
	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.inventoryWithGuiModel(item, ItemCameraTransforms.DEFAULT);
	}

	@Override
	public void renderByItem(ItemStack item) {
		GlStateManager.pushMatrix();

		EntityPlayer player = Minecraft.getMinecraft().player;

		if(item.getItem() == ModItems.wood_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_wood);
		if(item.getItem() == ModItems.lead_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_lead);
		if(item.getItem() == ModItems.diamond_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_diamond);
		if(item.getItem() == ModItems.mese_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_mese);

		// ItemRenderFrames17 base frame + verbatim 1.7 ItemRenderGavel body.
		switch(type){
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			// 1.7 EQUIPPED_FIRST_PERSON
			GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
			GL11.glTranslated(1, 0.5, 0);
			if(player != null && player.isActiveItemStackBlocking()) {
				TransformType mainHandType = player.getPrimaryHand() == EnumHandSide.RIGHT ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND;
				EnumHand renderedHand = type == mainHandType ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				if(player.getActiveHand() == renderedHand)
					GL11.glTranslated(-0.5, 0, 0);
			}
			GL11.glRotated(45, 0, 0, 1);
			GL11.glRotated(90, 0, 1, 0);
			if(item.getItem() == ModItems.mese_gavel)
				GL11.glScaled(2, 2, 2);
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
			// 1.7 EQUIPPED. Gavels are ItemSword-based (WeaponSpecial/ItemSwordAbility → ItemSword), so
			// isFull3D()=true → RenderBiped's full-3D held branch.
			GlStateManager.multMatrix(type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_FULL3D_LEFT : ItemRenderFrames17.THIRD_PERSON_FULL3D);
			gavelEquippedBody(item);
			break;
		case HEAD:
			GlStateManager.multMatrix(ItemRenderFrames17.HEAD);
			gavelEquippedBody(item);
			break;
		case GROUND:
			// 1.7 ENTITY: extra translate(-0.5,0,0) then the EQUIPPED body
			GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
			GL11.glTranslated(-0.5, 0, 0);
			gavelEquippedBody(item);
			break;
		case FIXED:
			GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
			GL11.glTranslated(-0.5, 0, 0);
			gavelEquippedBody(item);
			break;
		default:
			break;
		}

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.gavel.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	// 1.7 EQUIPPED body (shared by third-person and, after its lead translate, ENTITY).
	private static void gavelEquippedBody(ItemStack item) {
		GL11.glScaled(0.5, 0.5, 0.5);
		GL11.glRotated(45, 0, 0, 1);
		GL11.glTranslated(1.375, 0, 0);
		GL11.glRotated(90, 0, 1, 0);
		if(item.getItem() == ModItems.mese_gavel) {
			GL11.glScaled(2, 2, 2);
			GL11.glTranslated(0, 0.25, 0);
		}
	}
}
