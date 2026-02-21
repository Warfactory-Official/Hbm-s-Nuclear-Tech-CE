package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.item.EntityFireworks;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelShrapnel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister(entity = EntityShrapnel.class, factory = "FACTORY")
@AutoRegister(entity = EntityFireworks.class, factory = "FACTORY")
public class RenderShrapnel extends Render<Entity> {
	
	public static final IRenderFactory<Entity> FACTORY = man -> new RenderShrapnel(man);
	
	ModelShrapnel mine;

	public RenderShrapnel(RenderManager manage) {
		super(manage);
		mine = new ModelShrapnel();
	}

	@Override
	public void doRender(Entity rocket, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_,
			float p_76986_9_) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) p_76986_2_, (float) p_76986_4_, (float) p_76986_6_);
		GlStateManager.scale(1.0F, 1.0F, 1.0F);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.rotate((rocket.ticksExisted % 360) * 10, 1, 1, 1);

		bindTexture(new ResourceLocation(Tags.MODID + ":textures/entity/shrapnel.png"));
		
		if(rocket instanceof EntityShrapnel) {
			if(rocket.getDataManager().get(EntityShrapnel.TRAIL) == 2) { //scale up lava blobs
				GL11.glScaled(3, 3, 3);
			}
		}
		
		mine.renderAll(0.0625F);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return new ResourceLocation(Tags.MODID + ":textures/models/shrapnel.png");
	}
}
