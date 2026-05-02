package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "wood_gavel")
@AutoRegister(item = "lead_gavel")
@AutoRegister(item = "diamond_gavel")
@AutoRegister(item = "mese_gavel")
public class ItemRenderGavel extends TEISRBase {

	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.inventoryWithGuiModel(item, BakedModelTransforms.defaultItemTransforms());
	}

	@Override
	public void renderByItem(ItemStack item) {
		GlStateManager.translate(0.5, 0.5, 0.5);
		
		if(item.getItem() == ModItems.wood_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_wood);
		if(item.getItem() == ModItems.lead_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_lead);
		if(item.getItem() == ModItems.diamond_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_diamond);
		if(item.getItem() == ModItems.mese_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_mese);
		
		switch(type){
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			GlStateManager.translate(-0.6, 0, 0);
			GL11.glRotated(90, 0, 1, 0);
			GL11.glRotated(45, 1, 0, 0);
			if(item.getItem() == ModItems.mese_gavel)
				GL11.glScaled(2, 2, 2);
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case HEAD:
		case FIXED:
		case GROUND:
			if(item.getItem() == ModItems.mese_gavel) {
				GL11.glScaled(2, 2, 2);
				GlStateManager.translate(0, 0.25, 0);
			}
			break;
		case GUI:
			break;
		case NONE:
			break;
		}
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.gavel.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}
