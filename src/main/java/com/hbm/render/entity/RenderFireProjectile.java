package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityFire;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderFireProjectile extends Render<EntityFire> {

	public static final IRenderFactory<EntityFire> FACTORY = (RenderManager man) -> {return new RenderFireProjectile(man, ModItems.flame_1, 0);};
	
	Item item;
	int meta;
	
	protected RenderFireProjectile(RenderManager renderManager, Item item, int meta) {
		super(renderManager);
		this.item = item;
		this.meta = meta;
	}
	
	@Override
	public void doRender(EntityFire fx, double x, double y, double z, float entityYaw, float partialTicks) {

		if(fx.ticksExisted <= fx.maxAge && fx.ticksExisted >= fx.maxAge / 10 * 9)
		{
			item = ModItems.flame_10;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 9 && fx.ticksExisted >= fx.maxAge / 10 * 8)
		{
			item = ModItems.flame_9;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 8 && fx.ticksExisted >= fx.maxAge / 10 * 7)
		{
			item = ModItems.flame_8;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 7 && fx.ticksExisted >= fx.maxAge / 10 * 6)
		{
			item = ModItems.flame_7;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 6 && fx.ticksExisted >= fx.maxAge / 10 * 5)
		{
			item = ModItems.flame_6;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 5 && fx.ticksExisted >= fx.maxAge / 10 * 4)
		{
			item = ModItems.flame_5;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 4 && fx.ticksExisted >= fx.maxAge / 10 * 3)
		{
			item = ModItems.flame_4;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 3 && fx.ticksExisted >= fx.maxAge / 10 * 2)
		{
			item = ModItems.flame_3;
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 2 && fx.ticksExisted >= fx.maxAge / 10 * 1)
		{
			item = ModItems.flame_2;
		}
		
		if(fx.ticksExisted < fx.maxAge / 10 && fx.ticksExisted >= 0 && !fx.isDead)
		{
			item = ModItems.flame_1;
		}
		
		TextureAtlasSprite iicon = NTMRenderHelper.getItemTexture(item);

        if (iicon != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.translate((float)x, (float)y, (float)z);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.scale(7.5F, 7.5F, 7.5F);
            GlStateManager.translate(0.0F, -0.25F, 0.0F);
            this.bindEntityTexture(fx);
            Tessellator tessellator = Tessellator.getInstance();

            this.func_77026_a(tessellator, iicon);
            GlStateManager.disableRescaleNormal();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
	}
	
	private void func_77026_a(Tessellator tes, TextureAtlasSprite p_77026_2_)
    {
        float f = p_77026_2_.getMinU();
        float f1 = p_77026_2_.getMaxU();
        float f2 = p_77026_2_.getMinV();
        float f3 = p_77026_2_.getMaxV();
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        NTMRenderHelper.startDrawingTexturedQuads(tes);
        //Drillgon200: I hope this setNormal isn't needed for anything
        //p_77026_1_.setNormal(0.0F, 1.0F, 0.0F);
        NTMRenderHelper.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0F, f, f3, tes);
        NTMRenderHelper.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0F, f1, f3, tes);
        NTMRenderHelper.addVertexWithUV(f4 - f5, f4 - f6, 0.0F, f1, f2, tes);
        NTMRenderHelper.addVertexWithUV(0.0F - f5, f4 - f6, 0.0F, f, f2, tes);
        tes.draw();
    }
	
	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

	@Override
	protected ResourceLocation getEntityTexture(EntityFire entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
