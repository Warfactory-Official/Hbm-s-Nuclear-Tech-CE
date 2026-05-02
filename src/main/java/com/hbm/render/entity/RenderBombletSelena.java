package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.missile.EntityBombletSelena;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderBombletSelena extends Render<EntityBombletSelena> {

	public static final IRenderFactory<EntityBombletSelena> FACTORY = (RenderManager man) -> {return new RenderBombletSelena(man);};

	private static final ResourceLocation boyTexture = new ResourceLocation(Tags.MODID, "textures/models/misc/universalDark.png");

	protected RenderBombletSelena(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityBombletSelena entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(2, 2, 2);

        bindTexture(boyTexture);
        ResourceManager.bomblet_selena.renderAll();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBombletSelena entity) {
		return boyTexture;
	}

}
