package com.hbm.render.entity;

import com.hbm.entity.logic.EntityBomber;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderBomber extends Render<EntityBomber> {

	public static final IRenderFactory<EntityBomber> FACTORY = (RenderManager man) -> {return new RenderBomber(man);};
	
	protected RenderBomber(RenderManager renderManager) {
		super(renderManager);
		
	}
	
	@Override
	public void doRender(EntityBomber entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (!ClientProxy.renderingConstant) {
			return;
		}
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90, 0F, 0F, 1F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        
        //ayy lmao
        //GlStateManager.rotate(System.currentTimeMillis() / 5 % 360, 1F, 0F, 0F);

        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        
        
        int i = (int) entity.getDataManager().get(EntityBomber.STYLE);
        
        switch(i) {
        case 0: bindTexture(ResourceManager.dornier_0_tex); break;
        case 1: bindTexture(ResourceManager.dornier_1_tex); break;
        case 2: bindTexture(ResourceManager.dornier_2_tex); break;
        case 3: bindTexture(ResourceManager.dornier_3_tex); break;
        case 4: bindTexture(ResourceManager.dornier_4_tex); break;
        case 5: bindTexture(ResourceManager.b29_0_tex); break;
        case 6: bindTexture(ResourceManager.b29_1_tex); break;
        case 7: bindTexture(ResourceManager.b29_2_tex); break;
        case 8: bindTexture(ResourceManager.b29_3_tex); break;
        default: bindTexture(ResourceManager.dornier_1_tex); break;
        }

        switch(i) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4: GlStateManager.scale(5F, 5F, 5F); GlStateManager.rotate(-90, 0F, 1F, 0F); ResourceManager.dornier.renderAll(); break;
        case 5:
        case 6:
        case 7:
        case 8: GlStateManager.scale(30F/3.1F, 30F/3.1F, 30F/3.1F); GlStateManager.rotate(180, 0F, 1F, 0F); ResourceManager.b29.renderAll(); break;
        default: ResourceManager.dornier.renderAll(); break;
        }
        

        GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBomber entity) {
		return ResourceManager.dornier_1_tex;
	}

}
