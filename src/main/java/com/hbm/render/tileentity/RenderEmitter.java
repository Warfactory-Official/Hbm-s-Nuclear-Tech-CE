package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockEmitter.TileEntityEmitter;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

@AutoRegister
public class RenderEmitter extends TileEntitySpecialRenderer<TileEntityEmitter> {

	@Override
	public void render(TileEntityEmitter emitter, double x, double y, double z, float f, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		GlStateManager.rotate(90, 0F, 1F, 0F);

		switch(emitter.getBlockMetadata()) {
		case 0:
			GlStateManager.translate(0.0D, 0.5D, -0.5D);
			GlStateManager.rotate(90, 1F, 0F, 0F); break;
		case 1:
			GlStateManager.translate(0.0D, 0.5D, 0.5D);
			GlStateManager.rotate(90, -1F, 0F, 0F); break;
		case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		}

		GlStateManager.translate(0, 0.5, 0.5);
		int range = emitter.beam - 1;
		int originalColor = emitter.color == 0 ? Color.HSBtoRGB(emitter.getWorld().getTotalWorldTime() / 50.0F, 0.5F, 0.25F) & 16777215 : emitter.color;
		float girth = emitter.girth;
		int r = (originalColor & 0xff0000) >> 16;
		int g = (originalColor & 0x00ff00) >> 8;
		int b = (originalColor & 0x0000ff);
		float innerMult = 0.85F;
		float outerMult = 0.1F;
		int colorInner = ((int)(r * innerMult) << 16) | ((int)(g * innerMult) << 8) | ((int)(b * innerMult));
		int colorOuter = ((int)(r * outerMult) << 16) | ((int)(g * outerMult) << 8) | ((int)(b * outerMult));

		if(range > 0) {

			int segments = (int) Math.max(Math.sqrt(girth * 50), 2);
			BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, colorOuter, colorInner, 0, 1, 0F, segments, girth);

			long time = emitter.getWorld().getTotalWorldTime();

			if(emitter.effect == 1) {
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.RANDOM, EnumBeamType.SOLID, colorOuter, colorInner, (int) time / 2, (int) Math.max(range / girth / 2, 1), girth * 2, 4, girth * 0.1F);
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.RANDOM, EnumBeamType.SOLID, colorOuter, colorInner, (int) time / 2 + 15, (int) Math.max(range / girth / 4, 1), girth * 2, 4, girth * 0.1F);
			}

			if(emitter.effect == 2) {
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, colorOuter, colorInner, (int) ((time + f) * -10 % 360), (int) Math.max(range / girth / 2, 1), girth * 2, 4, girth * 0.1F);
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, colorOuter, colorInner, (int) ((time + f) * -10 % 360) + 180, (int) Math.max(range / girth / 2, 1), girth * 2, 4, girth * 0.1F);
			}

			if(emitter.effect == 3) {
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, colorOuter, colorInner, (int) ((time + f) * -10 % 360), (int) Math.max(range / girth / 2, 1), girth * 2, 4, girth * 0.1F);
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, colorOuter, colorInner, (int) ((time + f) * -10 % 360) + 120, (int) Math.max(range / girth / 2, 1), girth * 2, 4, girth * 0.1F);
				BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, colorOuter, colorInner, (int) ((time + f) * -10 % 360) + 240, (int) Math.max(range / girth / 2, 1), girth * 2, 4, girth * 0.1F);
			}
		}

		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
}
