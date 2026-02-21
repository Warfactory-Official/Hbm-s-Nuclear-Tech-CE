package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityGlowingOne;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(entity = EntityGlowingOne.class, factory = "FACTORY")
public class RenderGlowingOne extends RenderZombie {

	public static final IRenderFactory<EntityGlowingOne> FACTORY = man -> new RenderGlowingOne(man);
	
	public static final ResourceLocation glowingone = new ResourceLocation(Tags.MODID, "textures/entity/glowingone.png");
	
	public RenderGlowingOne(RenderManager p_i47211_1_) {
		super(p_i47211_1_);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityZombie entity) {
		return glowingone;
	}

}
