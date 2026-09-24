package com.hbm.particle_instanced;

import net.minecraft.world.World;

import java.nio.ByteBuffer;

public class ParticleRBMKMushInstanced extends ParticleInstanced {

	private static final int SEGMENTS = 30;
	private static final float FRAME_HEIGHT = 1F / SEGMENTS;
	private static final RenderType RENDER_TYPE = RenderType.RBMK_MUSH;

	public ParticleRBMKMushInstanced(World worldIn, double posXIn, double posYIn, double posZIn, float scale) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = 80;
		this.particleScale = scale;
	}

	@Override
	public RenderType getRenderType() {
		return RENDER_TYPE;
	}

	@Override
	public void addDataToBuffer(ByteBuffer buf, float partialTicks) {
		int progress = this.particleAge * SEGMENTS / this.particleMaxAge;
		writeFullbrightBillboard(buf, getInterpX(partialTicks), getInterpY(partialTicks) + this.particleScale, getInterpZ(partialTicks), this.particleScale * 2F,
				0F, progress * FRAME_HEIGHT, 1F, FRAME_HEIGHT,
				1F, 1F, 1F, 1F);
	}
}
