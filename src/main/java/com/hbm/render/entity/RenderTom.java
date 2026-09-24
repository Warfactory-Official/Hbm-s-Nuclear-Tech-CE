package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityRailgunBlast;
import com.hbm.entity.projectile.EntityTom;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.main.ClientProxy;
import com.hbm.render.misc.TomPronter2;
import com.hbm.render.util.TomPronter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(entity = EntityRailgunBlast.class, factory = "RAIL_FACTORY")
@AutoRegister(entity = EntityTom.class, factory = "TOM_FACTORY")
public class RenderTom<T extends Entity> extends Render<T> {

	public static final IRenderFactory<EntityRailgunBlast> RAIL_FACTORY = (RenderManager man) -> {
		return new RenderTom<EntityRailgunBlast>(man);
	};
	public static final IRenderFactory<EntityTom> TOM_FACTORY = (RenderManager man) -> {
		return new RenderTom<EntityTom>(man);
	};

	protected RenderTom(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if(entity instanceof EntityRailgunBlast) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

			int i = 0;

			// if(entity instanceof EntityShell || entity instanceof
			// EntityMissileShell)
			// i = 1;

			TomPronter.prontTom(i);
			GlStateManager.popMatrix();
		} else if(entity instanceof EntityTom) {
			if(ClientProxy.renderingConstant) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y - 50, z);

				TomPronter2.prontTom();
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		return null;
	}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
		if(entityIn instanceof IConstantRenderer)
			return;
		super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
	}

}
