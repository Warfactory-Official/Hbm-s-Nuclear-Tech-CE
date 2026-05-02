package com.hbm.particle_instanced;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.nio.ByteBuffer;

public class ParticleRBMKSteamInstanced extends ParticleInstanced {

	private static final float FRAME_WIDTH = 1F / 20F;
	private static final float CENTER_OFFSET = 1F;
	private static final RenderType RENDER_TYPE = RenderType.RBMK_STEAM;

	public ParticleRBMKSteamInstanced(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.particleMaxAge = 10;
		this.particleAlpha = 0.25F;
		this.particleScale = 4F;
	}

	@Override
	public RenderType getRenderType() {
		return RENDER_TYPE;
	}

	@Override
	public void addDataToBuffer(ByteBuffer buf, float partialTicks) {
		int texIndex = Math.max(0, (int) (((double) this.particleAge / (double) this.particleMaxAge) * 20D) % 20 - 1);
		float uMin = texIndex * FRAME_WIDTH;

		float yaw = (float) Math.toRadians(-Minecraft.getMinecraft().getRenderManager().playerViewY);
		float x = getInterpX(partialTicks) - MathHelper.cos(yaw) * CENTER_OFFSET;
		float y = getInterpY(partialTicks) + this.particleScale * 0.5F - 0.25F;
		float z = getInterpZ(partialTicks) + MathHelper.sin(yaw) * CENTER_OFFSET;

		writeFullbrightBillboard(buf, x, y, z, this.particleScale * 0.5F,
				uMin, 0F, FRAME_WIDTH, 1F,
				1F, 1F, 1F, this.particleAlpha);
	}
}
