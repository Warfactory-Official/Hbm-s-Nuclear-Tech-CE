package com.hbm.render.entity.item;

import com.hbm.Tags;
import com.hbm.entity.item.EntityBoatRubber;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityBoatRubber.class, factory = "FACTORY")
public class RenderBoatRubber extends RenderBoat {

	public static final IRenderFactory<EntityBoatRubber> FACTORY = RenderBoatRubber::new;

	private static final ResourceLocation boatTextures = new ResourceLocation(Tags.MODID, "textures/entity/boat_rubber.png");

	public RenderBoatRubber(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBoat entity) {
		return boatTextures;
	}
}
