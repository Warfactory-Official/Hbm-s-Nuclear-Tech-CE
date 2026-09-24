package com.hbm.render.model;

import com.hbm.entity.mob.EntityBlockSpider;
import com.hbm.main.ResourceManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class ModelBlockSpider extends ModelBase {

	@Override
	public void render(@NotNull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		if(!(entity instanceof EntityBlockSpider spider)) return;

		IBlockState state = spider.getRenderState();
		if(state == null) return;

		float rot = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount * 57.3F;

		GlStateManager.pushMatrix();

		GlStateManager.rotate(90, 0, -1, 0);
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.translate(0, -1.5F, 0);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, rot * 0.005, 0);
		GlStateManager.rotate(rot, 0, 1, 0);
		ResourceManager.blockspider.renderPart("Leg1");
		ResourceManager.blockspider.renderPart("Leg3");
		ResourceManager.blockspider.renderPart("Leg5");
		ResourceManager.blockspider.renderPart("Leg7");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, rot * -0.005, 0);
		GlStateManager.rotate(rot, 0, -1, 0);
		ResourceManager.blockspider.renderPart("Leg2");
		ResourceManager.blockspider.renderPart("Leg4");
		ResourceManager.blockspider.renderPart("Leg6");
		ResourceManager.blockspider.renderPart("Leg8");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(-0.5, 0.25, -0.5);
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, spider.getBrightness());
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}
}
