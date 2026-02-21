package com.hbm.render.item;

import com.hbm.Tags;
import com.hbm.forgefluid.SpecialContainerFillLists.EnumCanister;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
@AutoRegister(item = "canister_full")
public class FluidCanisterRender extends TileEntityItemStackRenderer {

	public static final FluidCanisterRender INSTANCE = new FluidCanisterRender();
	public IBakedModel itemModel;
	public TransformType type;

	public ModelResourceLocation setModelLocation(ItemStack stack) {
		if(EnumCanister.contains(Fluids.fromID(stack.getItemDamage())))
			return EnumCanister.getEnumFromFluid(Fluids.fromID(stack.getItemDamage())).getResourceLocation();
		return new ModelResourceLocation(Tags.MODID + ":canister_empty", "inventory");
	}
	
	@Override
	public void renderByItem(ItemStack stack) {
		IBakedModel model = null;
		if(EnumCanister.contains(Fluids.fromID(stack.getItemDamage())))
			model = EnumCanister.getEnumFromFluid(Fluids.fromID(stack.getItemDamage())).getRenderModel();
		if(model == null)
			model = itemModel;
		
		NTMRenderHelper.bindBlockTexture();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5, 0.5, 0.5);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		GlStateManager.popMatrix();
		super.renderByItem(stack);
	}
	
}
