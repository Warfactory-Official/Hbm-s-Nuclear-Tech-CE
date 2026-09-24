package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityUFO;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderUFO extends Render<EntityUFO> {

	public static final IRenderFactory<EntityUFO> FACTORY = man -> new RenderUFO(man);
	
	protected RenderUFO(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityUFO ufo, double x, double y, double z, float entityYaw, float partialTicks){
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + 1, z);
		
		if(!ufo.isEntityAlive()) {
			float tilt = ufo.deathTime + 30 + partialTicks;
			GlStateManager.rotate(tilt, 1, 0, 1);
		}
		
		double scale = 2D;
		
		this.bindTexture(getEntityTexture(ufo));
		
		GlStateManager.pushMatrix();
		double rot = (ufo.ticksExisted + partialTicks) * 5 % 360D;
		GlStateManager.rotate((float) (rot), 0, 1, 0);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.ufo.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();

		if(ufo.getBeam()) {
			int ix = (int)Math.floor(ufo.posX);
			int iz = (int)Math.floor(ufo.posZ);
			int iy = 0;
			
			for(int i = (int)Math.ceil(ufo.posY); i >= 0; i--) {
				
				if(ufo.world.getBlockState(new BlockPos(ix, i, iz)).getBlock() != Blocks.AIR) {
					iy = i;
					break;
				}
			}
			
			double length = ufo.posY - iy;
			
			if(length > 0) {
				BeamPronter.prontBeam(new Vec3d(0, -length, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x101020, 0x101020, 0, (int)(length + 1), 0F, 6, (float)scale * 0.75F);
				BeamPronter.prontBeam(new Vec3d(0, -length, 0), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x202060, 0x202060, ufo.ticksExisted / 2, (int)(length / 2 + 1), (float)scale * 1.5F, 2, 0.0625F);
				BeamPronter.prontBeam(new Vec3d(0, -length, 0), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x202060, 0x202060, ufo.ticksExisted / 4, (int)(length / 2 + 1), (float)scale * 1.5F, 2, 0.0625F);
			}
		}
		
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityUFO entity){
		return ResourceManager.ufo_tex;
	}

}
