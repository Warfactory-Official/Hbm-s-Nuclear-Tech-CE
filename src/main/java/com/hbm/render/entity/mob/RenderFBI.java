package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityFBI;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelFBI;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(entity = EntityFBI.class, factory = "FACTORY")
public class RenderFBI extends RenderBiped<EntityFBI> {

	public static final IRenderFactory<EntityFBI> FACTORY = man -> new RenderFBI(man);
	
	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":" + "textures/entity/fbi.png");
	
	public RenderFBI(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelFBI(), 1.0F);
	}
	
	@Override
	public void doRender(EntityFBI entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFBI entity) {
		return texture;
	}

}
