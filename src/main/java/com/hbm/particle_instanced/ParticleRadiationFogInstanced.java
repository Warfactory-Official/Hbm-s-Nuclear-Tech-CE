package com.hbm.particle_instanced;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.nio.ByteBuffer;
import java.util.Random;

public class ParticleRadiationFogInstanced extends ParticleInstanced {

	private static final int MAX_AGE = 400;
	private static final int MAX_QUADS = 25;
	private static final double[] OFF_X = new double[MAX_QUADS];
	private static final double[] OFF_Y = new double[MAX_QUADS];
	private static final double[] OFF_Z = new double[MAX_QUADS];
	private static final double[] JIT_X = new double[MAX_QUADS];
	private static final double[] JIT_Y = new double[MAX_QUADS];
	private static final double[] JIT_Z = new double[MAX_QUADS];
	private static final double[] SIZE_MUL = new double[MAX_QUADS];
	private static final float[] ALPHA_LUT = new float[MAX_AGE + 1];
	private static final RenderType RENDER_TYPE = RenderType.RADIATION_FOG;

	static {
		Random random = new Random(50L);
		for(int i = 0; i < MAX_QUADS; i++) {
			OFF_X[i] = (random.nextGaussian() - 1D) * 2.5D;
			OFF_Y[i] = (random.nextGaussian() - 1D) * 0.15D;
			OFF_Z[i] = (random.nextGaussian() - 1D) * 2.5D;
			SIZE_MUL[i] = random.nextDouble() * 7.5D;
			JIT_X[i] = random.nextGaussian() * 0.5D;
			JIT_Y[i] = random.nextGaussian() * 0.5D;
			JIT_Z[i] = random.nextGaussian() * 0.5D;
		}

		for(int age = 0; age <= MAX_AGE; age++) {
			float alpha = (float) Math.sin(age * Math.PI / 400F) * 0.125F;
			ALPHA_LUT[age] = MathHelper.clamp(alpha, 0F, 1F);
		}
	}

	private final int maxAge;

	public ParticleRadiationFogInstanced(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.maxAge = 100 + rand.nextInt(40);
		this.particleRed = 0F;
		this.particleGreen = 0F;
		this.particleBlue = 0F;
		this.particleScale = 7.5F;
	}

	@Override
	public RenderType getRenderType() {
		return RENDER_TYPE;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge >= Math.max(this.maxAge, MAX_AGE)) {
			this.setExpired();
			return;
		}

		this.particleAge++;
		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if(this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

	@Override
	public void addDataToBuffer(ByteBuffer buf, float partialTicks) {
		float alpha = ALPHA_LUT[MathHelper.clamp(this.particleAge, 0, MAX_AGE)];
		float red = 0.85F;
		float green = 0.9F;
		float blue = 0.5F;
		float baseX = getInterpX(partialTicks);
		float baseY = getInterpY(partialTicks);
		float baseZ = getInterpZ(partialTicks);

		for(int i = 0; i < MAX_QUADS; i++) {
			float size = (float) SIZE_MUL[i];
			writeFullbrightBillboard(buf,
					baseX + (float) OFF_X[i] + (float) JIT_X[i],
					baseY + (float) OFF_Y[i] + (float) JIT_Y[i],
					baseZ + (float) OFF_Z[i] + (float) JIT_Z[i],
					size,
					0F, 0F, 1F, 1F,
					red, green, blue, alpha);
		}
	}

	@Override
	public int getFaceCount() {
		return MAX_QUADS;
	}
}
