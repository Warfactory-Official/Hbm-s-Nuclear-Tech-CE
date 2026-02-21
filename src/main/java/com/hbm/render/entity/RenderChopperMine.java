package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelChopperMine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderChopperMine extends Render<EntityChopperMine> {

	public static final IRenderFactory<EntityChopperMine> FACTORY = (RenderManager man) -> {return new RenderChopperMine(man);};
	
	public static final ResourceLocation mine_rl = new ResourceLocation(Tags.MODID + ":textures/models/projectiles/chopperBomb.png");
	
	ModelChopperMine mine;
	
	protected RenderChopperMine(RenderManager renderManager) {
		super(renderManager);
		mine = new ModelChopperMine();
	}
	
	@Override
	public void doRender(EntityChopperMine entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(1.5F, 1.5F, 1.5F);
		GlStateManager.rotate(180, 1, 0, 0);
		
		bindTexture(getEntityTexture(entity));
		
		mine.renderAll(0.0625F);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityChopperMine entity) {
		return mine_rl;
	}

}
