package com.hbm.render.entity;

import com.hbm.entity.logic.EntityEMP;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityTomBlast;
import com.hbm.entity.projectile.EntityWaterSplash;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityEMP.class, factory = "FACTORY")
@AutoRegister(entity = EntityNukeExplosionMK3.class, factory = "FACTORY")
@AutoRegister(entity = EntityWaterSplash.class, factory = "FACTORY")
@AutoRegister(entity = EntityTomBlast.class, factory = "FACTORY")
public class RenderEmpty extends Render<Entity> {

	public static final IRenderFactory<Entity> FACTORY = RenderEmpty::new;
	
	protected RenderEmpty(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}
	
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
