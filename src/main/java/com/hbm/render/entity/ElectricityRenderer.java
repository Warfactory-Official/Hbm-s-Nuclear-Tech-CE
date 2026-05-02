package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityDischarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
@AutoRegister(factory = "FACTORY")
public class ElectricityRenderer extends Render<EntityDischarge> {

	public static final IRenderFactory<EntityDischarge> FACTORY = (RenderManager man) -> {return new ElectricityRenderer(man);};
	
	protected TextureAtlasSprite tex;
	
	protected ElectricityRenderer(RenderManager renderManager) {
		super(renderManager);
		tex = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.discharge, 1, 0), null, null).getParticleTexture();
	}
	
	@Override
	public void doRender(EntityDischarge entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (tex != null) {
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			GlStateManager.translate(x, y, z);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.scale(7.5F, 7.5F, 7.5F);
			this.bindEntityTexture(entity);

			this.func_77026_a(tex);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

	@Override
	protected ResourceLocation getEntityTexture(EntityDischarge entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	private void func_77026_a(TextureAtlasSprite p_77026_2_) {
		float f = p_77026_2_.getMinU();
		float f1 = p_77026_2_.getMaxU();
		float f2 = p_77026_2_.getMinV();
		float f3 = p_77026_2_.getMaxV();
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		NTMRenderHelper.startDrawingTexturedQuads();
		//p_77026_1_.setNormal(0.0F, 1.0F, 0.0F);
		NTMRenderHelper.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0F, f, f3);
		NTMRenderHelper.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0F, f1, f3);
		NTMRenderHelper.addVertexWithUV(f4 - f5, f4 - f6, 0.0F, f1, f2);
		NTMRenderHelper.addVertexWithUV(0.0F - f5, f4 - f6, 0.0F, f, f2);
		NTMRenderHelper.draw();
	}
	
}
