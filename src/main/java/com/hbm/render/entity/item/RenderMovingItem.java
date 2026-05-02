package com.hbm.render.entity.item;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister(factory = "FACTORY")
public class RenderMovingItem extends Render<EntityMovingItem> {

	public static final IRenderFactory<EntityMovingItem> FACTORY = man -> new RenderMovingItem(man);

	protected RenderMovingItem(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityMovingItem item, double x, double y, double z, float f1, float f2) {

		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		ItemStack stack = item.getItemStack();
		GL11.glScaled(0.5, 0.5, 0.5);
		if(!(stack.getItem() instanceof ItemBlock)) {
			GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(0.0, 0, -0.03);
		} else {
			GlStateManager.translate(0, 0.25, 0);
		}

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, item.world, null);
		model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);

		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMovingItem p_110775_1_) {
		return null;
	}

}