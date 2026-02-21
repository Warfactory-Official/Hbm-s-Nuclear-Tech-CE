package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityCyberCrab;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelCrab;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(entity = EntityCyberCrab.class, factory = "FACTORY")
public class RenderCyberCrab extends RenderLiving<EntityCyberCrab> {

	public static final IRenderFactory<EntityCyberCrab> FACTORY = (RenderManager man) -> {return new RenderCyberCrab(man);};
	
	public static final ResourceLocation crab_rl = new ResourceLocation(Tags.MODID + ":textures/entity/Crab.png");
	
	public RenderCyberCrab(RenderManager manager) {
		super(manager, new ModelCrab(), 1.0F);
		this.shadowOpaque = 0.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCyberCrab entity) {
		return crab_rl;
	}

}
