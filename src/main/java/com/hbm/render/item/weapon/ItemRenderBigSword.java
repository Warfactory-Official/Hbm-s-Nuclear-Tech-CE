package com.hbm.render.item.weapon;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.render.model.ModelBigSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "big_sword")
public class ItemRenderBigSword extends TEISRBase {

	protected ModelBigSword bigSwordModel;

	public ItemRenderBigSword() {
		bigSwordModel = new ModelBigSword();
	}

	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.inventoryWithGuiModel(item, BakedModelTransforms.defaultItemTransforms(), new ResourceLocation(Tags.MODID, "items/big_sword"));
	}

	@Override
	public void renderByItem(ItemStack itemStackIn) {
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID + ":textures/models/weapons/ModelBigSwordTexture.png"));
			if(type == TransformType.FIRST_PERSON_RIGHT_HAND){
				GlStateManager.translate(0.2, 0.2, 0.5);
				GlStateManager.rotate(135, 0, 0, 1);
				GlStateManager.rotate(90, 0, 1, 0);
			} else {
				GlStateManager.translate(0.7, 0.2, 0.7);
				GlStateManager.rotate(225, 0, 0, 1);
				GlStateManager.rotate(90, 0, 1, 0);
			}
			bigSwordModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case GROUND:
		case FIXED:
		case HEAD:
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Tags.MODID + ":textures/models/weapons/ModelBigSwordTexture.png"));
			GlStateManager.scale(1.5, 1.5, 1.5);
			GlStateManager.rotate(-180.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(5, 1, 0, 0);
			GlStateManager.translate(0.3F, 0F, -0.5F);
			bigSwordModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			break;
		default:
			break;
		}
	}
}
