package com.hbm.render.item.weapon;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
@AutoRegister(item = "redstone_sword", instanceField = "INSTANCE")
public class ItemRedstoneSwordRender extends TileEntityItemStackRenderer {
	
	public static final ItemRedstoneSwordRender INSTANCE = new ItemRedstoneSwordRender();
	
	public TransformType type;
	public IBakedModel itemModel;
	
	public ModelSword swordModel;
	
	public ItemRedstoneSwordRender(){
		swordModel = new ModelSword();
	}
	@Override
	public void renderByItem(ItemStack stack) {
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
			if(type != TransformType.GUI){
				if(type == TransformType.GROUND){
					//GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
					//GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
					GlStateManager.scale(0.4F, 0.4F, 0.4F);
					GlStateManager.translate(0.7F, 0.8F, 0.7F);
				}
				if(type == TransformType.FIRST_PERSON_LEFT_HAND || type == TransformType.FIRST_PERSON_RIGHT_HAND){
					GlStateManager.scale(0.6F, 0.6F, 0.6F);
					GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
					GlStateManager.translate(0.2F, 1.0F, 0.1F);
					if(type == TransformType.FIRST_PERSON_LEFT_HAND){
						GlStateManager.translate(0.25F, 0.0F, 0.0F);
					}
				}
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID +":textures/models/weapons/model_sword_redstone.png"));
				GlStateManager.rotate(180.0F, 0.0F, 0.0F, 0.0F);
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.5F, -0.2F, -0.5F);
				swordModel.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			} else {
				GlStateManager.translate(0.5, 0.5, 0);
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, itemModel);
			}
		GlStateManager.popMatrix();
		GlStateManager.enableCull();
		super.renderByItem(stack);
	}
	
	
	
}
