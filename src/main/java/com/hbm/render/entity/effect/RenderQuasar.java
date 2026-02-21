package com.hbm.render.entity.effect;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityQuasar;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.render.entity.RenderBlackHole;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY", entity = EntityQuasar.class)
public class RenderQuasar extends RenderBlackHole {

	public static final IRenderFactory<EntityQuasar> FACTORY = man -> new RenderQuasar(man);
	
	protected ResourceLocation quasar = new ResourceLocation(Tags.MODID, "textures/entity/bholeD.png");
	
	public RenderQuasar(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks){
		if(!ClientProxy.renderingConstant)
			return;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();

		float size = entity.getDataManager().get(EntityBlackHole.SIZE);

		GlStateManager.scale(size, size, size);

		bindTexture(hole);
		blastModel.renderAll();
		
		renderDisc(entity, partialTicks);
		renderJets(entity, partialTicks);

		GlStateManager.enableCull();
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation discTex() {
		return this.quasar;
	}

	@Override
	protected void setColorFromIteration(int iteration, float alpha, float[] col) {
		float r = 1.0F;
		float g = (float) Math.pow(iteration / 15F, 2);
		float b = (float) Math.pow(iteration / 15F, 2);
		
		col[0] = r;
		col[1] = g;
		col[2] = b;
		col[3] = alpha;
	}

	@Override
	protected int steps() {
		return 15;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlackHole entity){
		return super.getEntityTexture(entity);
	}
}
