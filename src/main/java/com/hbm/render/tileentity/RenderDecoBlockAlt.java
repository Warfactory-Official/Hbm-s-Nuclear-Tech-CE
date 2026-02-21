package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.model.ModelGun;
import com.hbm.render.model.ModelStatue;
import com.hbm.tileentity.deco.TileEntityDecoBlockAlt;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister
public class RenderDecoBlockAlt extends TileEntitySpecialRenderer<TileEntityDecoBlockAlt> {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":" + "textures/models/misc/ModelStatue.png");
	private static final ResourceLocation gunTexture = new ResourceLocation(Tags.MODID + ":" + "textures/models/weapons/ModelGun.png");

	private ModelStatue model;
	private ModelGun gun;

	public RenderDecoBlockAlt() {
		this.model = new ModelStatue();
		this.gun = new ModelGun();
	}

	@Override
	public boolean isGlobalRenderer(TileEntityDecoBlockAlt te) {
		return true;
	}

	@Override
	public void render(TileEntityDecoBlockAlt te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		switch(te.getBlockMetadata()) {
		case 4:
			GlStateManager.rotate(90, 0F, 1F, 0F);
			break;
		case 2:
			GlStateManager.rotate(180, 0F, 1F, 0F);
			break;
		case 5:
			GlStateManager.rotate(270, 0F, 1F, 0F);
			break;
		case 3:
			GlStateManager.rotate(0, 0F, 1F, 0F);
			break;
		}

		Block b = te.getBlockType();

		this.bindTexture(texture);
		this.model.renderModel(0.0625F);
		float g = 0.0625F;
		float q = g * 2 + 0.0625F / 3;
		GlStateManager.translate(0.0F, -2 * g, q);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		if(b == ModBlocks.statue_elb_w || b == ModBlocks.statue_elb_f) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0.11, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			ItemStack stack = new ItemStack(ModItems.watch);
			IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, te.getWorld(), null);
			model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
			NTMRenderHelper.bindBlockTexture();

			Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
			GlStateManager.popMatrix();
		}
		GlStateManager.translate(0.0F, 2 * g, -q);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.rotate(90, 0F, 1F, 0F);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		GlStateManager.translate(-g * 20, g * 4, g * 11);
		GlStateManager.rotate(-20, 0F, 0F, 1F);
		this.bindTexture(gunTexture);
		if(b == ModBlocks.statue_elb_g || b == ModBlocks.statue_elb_f)
			this.gun.renderModel(0.0625F);
		GlStateManager.popMatrix();
	}
}
