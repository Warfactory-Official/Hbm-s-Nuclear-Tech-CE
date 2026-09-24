package com.hbm.particle_instanced;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.nio.ByteBuffer;

public class ParticleRBMKFlameInstanced extends ParticleInstanced {

	private static final float FRAME_WIDTH = 1F / 14F;
	private static final float CENTER_OFFSET = 1F;
	private static final RenderType RENDER_TYPE = RenderType.RBMK_FLAME;

	public ParticleRBMKFlameInstanced(World worldIn, double posXIn, double posYIn, double posZIn, int maxAge) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = maxAge;
		this.particleScale = rand.nextFloat() + 1F;
	}

	@Override
	public RenderType getRenderType() {
		return RENDER_TYPE;
	}

	@Override
	public void addDataToBuffer(ByteBuffer buf, float partialTicks) {
		int texIndex = this.particleAge * 5 % 14;
		float uMin = (texIndex % 5) * FRAME_WIDTH;
		float alpha = 0.5F;

		if(this.particleAge < 20) {
			alpha = this.particleAge / 20F;
		}
		if(this.particleAge > this.particleMaxAge - 20) {
			alpha = (this.particleMaxAge - this.particleAge) / 20F;
		}
		alpha *= 0.5F;

		float yaw = (float) Math.toRadians(-Minecraft.getMinecraft().getRenderManager().playerViewY);
		float x = getInterpX(partialTicks) - MathHelper.cos(yaw) * CENTER_OFFSET;
		float z = getInterpZ(partialTicks) + MathHelper.sin(yaw) * CENTER_OFFSET;

		writeFullbrightBillboard(buf, x, getInterpY(partialTicks), z, this.particleScale * 2F,
				uMin, 0F, FRAME_WIDTH, 1F,
				1F, 1F, 1F, MathHelper.clamp(alpha, 0F, 0.5F));
	}
}
