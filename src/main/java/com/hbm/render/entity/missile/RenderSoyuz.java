package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityRocketSoyuz;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.misc.SoyuzPronter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderSoyuz extends Render<EntityRocketSoyuz> {

	public static final IRenderFactory<EntityRocketSoyuz> FACTORY = (RenderManager man) -> {return new RenderSoyuz(man);};
	
	protected RenderSoyuz(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityRocketSoyuz entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
        GlStateManager.enableCull();
        
        int type = entity.getDataManager().get(EntityRocketSoyuz.SKIN);
        SoyuzPronter.prontSoyuz(type);
		
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRocketSoyuz entity) {
		//just so if there's a mod that is trying to pull a funny
		return ResourceManager.soyuz_payload;
	}

}
