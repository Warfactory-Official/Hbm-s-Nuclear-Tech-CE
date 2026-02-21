package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(entity = EntityDuck.class, factory = "FACTORY")
public class RenderDuck extends RenderChicken {

	public static final IRenderFactory<EntityDuck> FACTORY = man -> new RenderDuck(man);
	
	public static final ResourceLocation ducc = new ResourceLocation(Tags.MODID, "textures/entity/duck.png");
	
	public RenderDuck(RenderManager p_i47211_1_) {
		super(p_i47211_1_);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityChicken entity) {
		return ducc;
	}

}
