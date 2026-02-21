package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelRubble;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Random;
@AutoRegister(factory = "FACTORY")
public class RenderRubble extends Render<EntityRubble> {

	public static final IRenderFactory<EntityRubble> FACTORY = (RenderManager man) -> {return new RenderRubble(man);};

	ModelRubble mine;
	Random rand;
	
	public RenderRubble(RenderManager renderManager) {
		super(renderManager);
		mine = new ModelRubble();
		rand = new Random();
	}
	
	@Override
	public void doRender(EntityRubble rocket, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(1.0F, 1.0F, 1.0F);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.rotate((rocket.ticksExisted % 360) * 10, 1, 1, 1);

		try {
			int block = rocket.getDataManager().get(EntityRubble.BLOCKID);
			int meta = rocket.getDataManager().get(EntityRubble.BLOCKMETA);
			@SuppressWarnings("deprecation")
			//Minecraft uses it to load blocks. Guess I will, too. I wonder what I'm supposed to use?
			IBlockState state = Block.getBlockById(block).getStateFromMeta(meta);
			String s = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state).getParticleTexture().toString();
			s = s.substring(25, (s.indexOf("',")));
			
			if(s == null || s.isEmpty() || s.equals("missingno") || s.equals("missing"))
				s = "minecraft:blocks/stone";
	
			String[] split = s.split(":");
			
			String prefix = "";
			String suffix = "";
			
			if(split.length == 2) {
				prefix = split[0];
				suffix = split[1];
			} else {
				prefix = "minecraft";
				suffix = s;
			}
			bindTexture(new ResourceLocation(prefix + ":textures/" + suffix + ".png"));
			
			mine.renderAll(0.0625F);
		} catch(Exception ex) { }
		
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRubble entity) {
		return new ResourceLocation(Tags.MODID + ":textures/models/ModelRubbleScrap.png");
	}

}
