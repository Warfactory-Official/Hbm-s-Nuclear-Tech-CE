package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
@AutoRegister
public class RenderPress extends TileEntitySpecialRenderer<TileEntityMachinePress> implements IItemRendererProvider {

	public static Vec3d pos = new Vec3d(0, 0, 0);
	
	public RenderPress() {
		super();
	}

	// mlbv: What happened to this?
	@Override
	public void render(TileEntityMachinePress te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		pos = new Vec3d(x, y, z);
		
		GlStateManager.pushMatrix();

		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GlStateManager.rotate(180, 0F, 1F, 0F);

		this.bindTexture(ResourceManager.press_body_tex);
		ResourceManager.press_body.renderAll();

		
		renderTileEntityAt2(te, x, y, z, partialTicks);
		GlStateManager.popMatrix();
	}

	public void renderTileEntityAt2(TileEntity tileEntity, double x, double y, double z, float f) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1 - 0.125D, 0);
		GlStateManager.scale(0.95F, 1, 0.95F);

		TileEntityMachinePress press = (TileEntityMachinePress) tileEntity;
		float f1 = press.progress * (1 - 0.125F) / TileEntityMachinePress.maxProgress;
		GlStateManager.translate(0, -f1, 0);
		this.bindTexture(ResourceManager.press_head_tex);

		ResourceManager.press_head.renderAll();

		GlStateManager.popMatrix();
		renderTileEntityAt3(tileEntity, x, y, z, f);
	}

	public void renderTileEntityAt3(TileEntity tileEntity, double x, double y, double z, float f) {
		GlStateManager.translate(0, 1, -1);
		GlStateManager.enableLighting();
		GlStateManager.rotate(180, 0F, 1F, 0F);
		GlStateManager.rotate(-90, 1F, 0F, 0F);
		
		TileEntityMachinePress press = (TileEntityMachinePress) tileEntity;
		ItemStack stack = press.syncStack;

		if(stack != null && !(stack.getItem() instanceof ItemBlock) && !stack.isEmpty()) {
			IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, tileEntity.getWorld(), null);
			model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.translate(0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(180, 0F, 1F, 0F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			
			
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		}

	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_press);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
                GlStateManager.translate(0, -4, 0);
				GlStateManager.scale(4.5, 4.5, 4.5);
            }

            public void renderCommon() {
                bindTexture(ResourceManager.press_body_tex);
                ResourceManager.press_body.renderAll();
				GlStateManager.translate(0, 0.5, 0);
                bindTexture(ResourceManager.press_head_tex);
                ResourceManager.press_head.renderAll();
            }
		};
	}

}
