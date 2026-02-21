package com.hbm.render.entity.projectile;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityRBMKDebris;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderRBMKDebris extends Render<EntityRBMKDebris> {

	public static final IRenderFactory<EntityRBMKDebris> FACTORY = man -> new RenderRBMKDebris(man);
	
	//for fallback only
	private static final ResourceLocation tex_base = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_side.png");
	private static final ResourceLocation tex_element = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_element_fuelchunk.png");
	private static final ResourceLocation tex_control = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control.png");
	private static final ResourceLocation tex_blank = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_blank.png");
	private static final ResourceLocation tex_graphite = new ResourceLocation(Tags.MODID + ":textures/blocks/block_graphite.png");

	protected RenderRBMKDebris(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityRBMKDebris entity, double x, double y, double z, float entityYaw, float partialTicks){
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + 0.125D, z);
		
		EntityRBMKDebris debris = (EntityRBMKDebris)entity;

		GlStateManager.rotate(debris.getEntityId() % 360, 0, 1, 0); //rotate based on entity ID to add unique randomness
		GlStateManager.rotate(debris.lastRot + (debris.rot - debris.lastRot) * partialTicks, 1, 1, 1);
		
		DebrisType type = debris.getType();
		
		switch(type) {
		case BLANK: bindTexture(tex_blank); ResourceManager.deb_blank.renderAll(); break;
		case ELEMENT: bindTexture(tex_element); ResourceManager.deb_element.renderAll(); break;
		case FUEL: bindTexture(tex_element); ResourceManager.deb_fuel.renderAll(); break;
		case GRAPHITE: bindTexture(tex_graphite); ResourceManager.deb_graphite.renderAll(); break;
		case LID: bindTexture(tex_blank); ResourceManager.deb_lid.renderAll(); break;
		case ROD: bindTexture(tex_control); ResourceManager.deb_rod.renderAll(); break;
		default: break;
		}
		
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRBMKDebris entity){
		return tex_base;
	}

}
