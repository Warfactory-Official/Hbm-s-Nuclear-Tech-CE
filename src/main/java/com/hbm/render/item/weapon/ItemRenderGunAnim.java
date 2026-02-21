package com.hbm.render.item.weapon;

import com.hbm.Tags;
import com.hbm.items.ModItems;
import com.hbm.items.special.weapon.GunB92;
import com.hbm.render.model.ModelB92;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ItemRenderGunAnim extends TileEntityItemStackRenderer {

	public static final ItemRenderGunAnim INSTANCE = new ItemRenderGunAnim();

	protected ModelB92 b92;

	public TransformType type;
	public IBakedModel b92ItemModel;

	private ItemRenderGunAnim(){
		b92 = new ModelB92();
	}
	
	@Override
	public void renderByItem(ItemStack item) {
        float lever = 0;
		
		GlStateManager.popMatrix();
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
			GlStateManager.translate(0.1, 0, 0);
		case FIRST_PERSON_RIGHT_HAND:
			GlStateManager.pushMatrix();
			
			
			
				GlStateManager.enableCull();

				if(item.getItem() == ModItems.gun_b92)
					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID +":textures/models/weapons/ModelB92SM.png"));
				
				//GlStateManager.rotate(-135.0F, 0.0F, 0.0F, 1.0F);
				//GlStateManager.translate(-0.5F, 0.0F, -0.2F);
				//GlStateManager.scale(0.5F, 0.5F, 0.5F);
				//GlStateManager.scale(0.5F, 0.5F, 0.5F);
				//GlStateManager.translate(-0.2F, -0.1F, -0.1F);
				
				//GL11.glRotated(180, 0, 0, 1);
				//GL11.glRotated(-90, 0, 1, 0);
				//GL11.glRotated(20, 0, 0, 1);
				//GlStateManager.translate(-0.05, -0.0, 0.1);
				GL11.glScaled(0.25D, 0.25D, 0.25D);
				GL11.glRotated(180, 1, 0, 0);
				GL11.glRotated(90, 0, 1, 0);
				GL11.glRotated(20, 0, 0, 1);
				GlStateManager.translate(0, -1, -0.5);
				
				if(item.getItem() == ModItems.gun_b92 && GunB92.getRotationFromAnim(item, Minecraft.getMinecraft().getRenderPartialTicks()) > 0) {
					float off = GunB92.getRotationFromAnim(item, Minecraft.getMinecraft().getRenderPartialTicks()) * 2;
					GlStateManager.rotate(GunB92.getRotationFromAnim(item, Minecraft.getMinecraft().getRenderPartialTicks()) * -90, 0.0F, 0.0F, 1.0F);
					//b92Ani.apply();
					GlStateManager.translate(off * -0.5F, off * -0.5F, 0.0F);
				}

				if(item.getItem() == ModItems.gun_b92)
					b92.renderAnim(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, GunB92.getTransFromAnim(item, Minecraft.getMinecraft().getRenderPartialTicks()));
				
			GlStateManager.popMatrix();
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case HEAD:
		case FIXED:
		case GROUND:
			GlStateManager.pushMatrix();
				if(item.getItem() == ModItems.gun_b92)
					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID +":textures/models/weapons/ModelB92SM.png"));
				
				GL11.glScaled(0.25, 0.25, 0.25);
				GL11.glRotated(180, 1, 0, 0);
				GL11.glRotated(90, 0, 1, 0);
				GlStateManager.translate(0, -0.5, 0);


				if(item.getItem() == ModItems.gun_b92)
					b92.renderAnim(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, GunB92.getTransFromAnim(item, Minecraft.getMinecraft().getRenderPartialTicks()));
			GlStateManager.popMatrix();
		default: break;
		}
		GlStateManager.pushMatrix();
	}
}