package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityLN2;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.HashMap;
import java.util.Map;
@AutoRegister(factory = "FACTORY")
public class RenderLN2 extends Render<EntityLN2> {

	public static final IRenderFactory<EntityLN2> FACTORY = (RenderManager man) -> {return new RenderLN2(man);};
	
	private Map<Item, TextureAtlasSprite> textures = new HashMap<Item, TextureAtlasSprite>();
	
	protected RenderLN2(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityLN2 fx, double x, double y, double z, float entityYaw, float partialTicks) {
		if(textures.isEmpty()){
			textures.put(ModItems.ln2_1, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_1, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_2, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_2, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_3, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_3, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_4, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_4, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_5, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_5, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_6, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_6, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_7, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_7, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_8, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_8, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_9, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_9, 1, 0), null, null).getParticleTexture());
			textures.put(ModItems.ln2_10, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.ln2_10, 1, 0), null, null).getParticleTexture());
		}
		TextureAtlasSprite tex = textures.get(ModItems.ln2_1);
		if(fx.ticksExisted <= fx.maxAge && fx.ticksExisted >= fx.maxAge / 10 * 9)
		{
			tex = textures.get(ModItems.ln2_10);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 9 && fx.ticksExisted >= fx.maxAge / 10 * 8)
		{
			tex = textures.get(ModItems.ln2_9);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 8 && fx.ticksExisted >= fx.maxAge / 10 * 7)
		{
			tex = textures.get(ModItems.ln2_8);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 7 && fx.ticksExisted >= fx.maxAge / 10 * 6)
		{
			tex = textures.get(ModItems.ln2_7);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 6 && fx.ticksExisted >= fx.maxAge / 10 * 5)
		{
			tex = textures.get(ModItems.ln2_6);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 5 && fx.ticksExisted >= fx.maxAge / 10 * 4)
		{
			tex = textures.get(ModItems.ln2_5);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 4 && fx.ticksExisted >= fx.maxAge / 10 * 3)
		{
			tex = textures.get(ModItems.ln2_4);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 3 && fx.ticksExisted >= fx.maxAge / 10 * 2)
		{
			tex = textures.get(ModItems.ln2_3);
		}

		if(fx.ticksExisted < fx.maxAge / 10 * 2 && fx.ticksExisted >= fx.maxAge / 10 * 1)
		{
			tex = textures.get(ModItems.ln2_2);
		}
		
		if(fx.ticksExisted < fx.maxAge / 10 && fx.ticksExisted >= 0 && !fx.isDead)
		{
			tex = textures.get(ModItems.ln2_1);
		}
		

        if (tex != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.scale(7.5F, 7.5F, 7.5F);
            GlStateManager.translate(0.0F, -0.25F, 0.0F);
            this.bindEntityTexture(fx);

            this.func_77026_a(tex);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GlStateManager.popMatrix();
        }
	}
	
	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

	@Override
	protected ResourceLocation getEntityTexture(EntityLN2 entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
	
	private void func_77026_a(TextureAtlasSprite p_77026_2_)
    {
		GlStateManager.disableLighting();
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
       // p_77026_1_.setNormal(0.0F, 1.0F, 0.0F);
        NTMRenderHelper.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0F, f, f3);
        NTMRenderHelper.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0F, f1, f3);
        NTMRenderHelper.addVertexWithUV(f4 - f5, f4 - f6, 0.0F, f1, f2);
        NTMRenderHelper.addVertexWithUV(0.0F - f5, f4 - f6, 0.0F, f, f2);
        NTMRenderHelper.draw();
        GlStateManager.enableLighting();
    }

}
