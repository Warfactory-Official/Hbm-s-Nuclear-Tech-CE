package com.hbm.render.item;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.render.model.ModelMultitoolClaw;
import com.hbm.render.model.ModelMultitoolFist;
import com.hbm.render.model.ModelMultitoolOpen;
import com.hbm.render.model.ModelMultitoolPointer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "multitool_dig")
@AutoRegister(item = "multitool_silk")
@AutoRegister(item = "multitool_ext")
@AutoRegister(item = "multitool_miner")
@AutoRegister(item = "multitool_hit")
@AutoRegister(item = "multitool_beam")
@AutoRegister(item = "multitool_sky")
@AutoRegister(item = "multitool_mega")
@AutoRegister(item = "multitool_joule")
@AutoRegister(item = "multitool_decon")
public class ItemRenderMultitool extends TEISRBase {

	protected ModelMultitoolOpen open;
	protected ModelMultitoolClaw claw;
	protected ModelMultitoolFist fist;
	protected ModelMultitoolPointer pointer;
    public RenderPlayer renderPlayer;
	
    public ItemRenderMultitool() {
    	open = new ModelMultitoolOpen();
		claw = new ModelMultitoolClaw();
		fist = new ModelMultitoolFist();
		pointer = new ModelMultitoolPointer();
		renderPlayer = new RenderPlayer(null);
	}
    
	@Override
	public void renderByItem(ItemStack item) {
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			GlStateManager.pushMatrix();
				GlStateManager.enableCull();
				
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID +":textures/models/tools/ModelMultitool.png"));
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				if(type == TransformType.FIRST_PERSON_RIGHT_HAND){
					GL11.glRotated(-39, 0, 0, 1);
					GlStateManager.translate(0.5, 1.5, 1.5);
					GL11.glRotated(180, 1, 0, 0);
				} else {
					GL11.glRotated(39, 0, 0, 1);
					GlStateManager.translate(0.9, 0.4, 1.7);
					GL11.glRotated(180, 0, 0, 1);
				}
				
				if(item != null && item.getItem() == ModItems.multitool_dig)
					claw.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_silk)
					claw.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_ext)
					open.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_miner)
					pointer.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_hit)
					fist.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_beam)
					pointer.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_sky)
					open.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_mega)
					fist.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_joule)
					fist.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_decon)
					open.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				
				GlStateManager.scale(2.0F, 2.0F, 2.0F);
				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.translate(6 * 0.0625F, -12 * 0.0625F, 0 * 0.0625F);
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/entity/steve.png"));
		        renderPlayer.getMainModel().bipedRightArm.render(0.0625F);

			GlStateManager.popMatrix();
			break;
		case GROUND:
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case FIXED:
		case HEAD:
			GlStateManager.pushMatrix();
			GlStateManager.enableCull();
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID +":textures/models/tools/ModelMultitool.png"));
				GlStateManager.scale(0.75F, 0.75F, 0.75F);

				GL11.glRotated(180, 1, 0, 0);
				GL11.glRotated(90, 0, 1, 0);

				//GlStateManager.translate(0, 0, 1);
				GlStateManager.translate(8 * 0.0625F, 1 * 0.0625F, 10.5F * 0.0625F);
				
				if(item != null && item.getItem() == ModItems.multitool_dig)
					claw.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_silk)
					claw.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_ext)
					open.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_miner)
					pointer.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_hit)
					fist.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_beam)
					pointer.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_sky)
					open.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_mega)
					fist.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_joule)
					fist.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				if(item != null && item.getItem() == ModItems.multitool_decon)
					open.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				
			GlStateManager.popMatrix();
		default: break;
		}
	}
}
