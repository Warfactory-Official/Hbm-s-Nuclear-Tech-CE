package com.hbm.render;

import com.hbm.handler.HbmShaderManager2;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public final class InstancedBillboardBatch {

	public static final int BYTES_PER_INSTANCE = 3 * 4 + 4 + 4 * 4 + 4 * 4 + 2;

	private final float halfWidth;
	private final float halfHeight;
	private boolean initialized = false;
	private int vao;
	private int instanceDataVbo;
	private ByteBuffer instanceBuffer = GLAllocation.createDirectByteBuffer(0);

	public InstancedBillboardBatch() {
		this(0.5F, 0.5F);
	}

	public InstancedBillboardBatch(float halfWidth, float halfHeight) {
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
	}

	public ByteBuffer begin(int instanceCount) {
		requireShader();
		init();
		int bufferSize = instanceCount * BYTES_PER_INSTANCE;
		if(instanceBuffer.capacity() < bufferSize) {
			instanceBuffer = GLAllocation.createDirectByteBuffer(bufferSize);
		}

		instanceBuffer.clear();
		instanceBuffer.limit(bufferSize);
		return instanceBuffer;
	}

	public void draw(int instanceCount) {
		if(instanceCount <= 0) {
			return;
		}
		requireShader();
		instanceBuffer.flip();
		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, instanceDataVbo);
		GLCompat.bufferData(GLCompat.GL_ARRAY_BUFFER, instanceBuffer, GLCompat.GL_DYNAMIC_DRAW);

		GLCompat.bindVertexArray(vao);
		try {
			ResourceManager.lit_particles.use();
			GLCompat.drawArraysInstanced(GL11.GL_QUADS, 0, 4, instanceCount);
		} finally {
			HbmShaderManager2.releaseShader();
			GLCompat.bindVertexArray(0);
			GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
		}
	}

	public static void writeInstance(ByteBuffer buffer, float posX, float posY, float posZ, float scale,
									 float minU, float minV, float sizeU, float sizeV,
									 float red, float green, float blue, float alpha,
									 int lightmapX, int lightmapY) {
		buffer.putFloat(posX);
		buffer.putFloat(posY);
		buffer.putFloat(posZ);
		buffer.putFloat(scale);
		buffer.putFloat(minU);
		buffer.putFloat(minV);
		buffer.putFloat(sizeU);
		buffer.putFloat(sizeV);
		buffer.putFloat(red);
		buffer.putFloat(green);
		buffer.putFloat(blue);
		buffer.putFloat(alpha);
		buffer.put((byte) lightmapX);
		buffer.put((byte) lightmapY);
	}

	private static void requireShader() {
		if(ResourceManager.lit_particles.getShaderId() == 0) {
			throw new IllegalStateException("Instanced rendering was requested without a loaded lit_particles shader");
		}
	}

	private void init() {
		if(initialized) {
			return;
		}

		int quadVbo = GLCompat.genBuffers();
		int dataVbo = GLCompat.genBuffers();
		float[] vertexData = {
				-halfWidth, -halfHeight, 0F,
				halfWidth, -halfHeight, 0F,
				halfWidth, halfHeight, 0F,
				-halfWidth, halfHeight, 0F
		};
		ByteBuffer data = GLAllocation.createDirectByteBuffer(4 * vertexData.length);
		for(float coordinate : vertexData) {
			data.putFloat(coordinate);
		}
		data.rewind();

		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, quadVbo);
		GLCompat.bufferData(GLCompat.GL_ARRAY_BUFFER, data, GLCompat.GL_STATIC_DRAW);

		vao = GLCompat.genVertexArrays();
		GLCompat.bindVertexArray(vao);
		GLCompat.vertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
		GLCompat.enableVertexAttribArray(0);

		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, dataVbo);
		GLCompat.vertexAttribPointer(1, 3, GL11.GL_FLOAT, false, BYTES_PER_INSTANCE, 0);
		GLCompat.enableVertexAttribArray(1);
		GLCompat.vertexAttribPointer(2, 1, GL11.GL_FLOAT, false, BYTES_PER_INSTANCE, 12);
		GLCompat.enableVertexAttribArray(2);
		GLCompat.vertexAttribPointer(3, 4, GL11.GL_FLOAT, false, BYTES_PER_INSTANCE, 16);
		GLCompat.enableVertexAttribArray(3);
		GLCompat.vertexAttribPointer(4, 4, GL11.GL_FLOAT, false, BYTES_PER_INSTANCE, 32);
		GLCompat.enableVertexAttribArray(4);
		GLCompat.vertexAttribPointer(5, 2, GL11.GL_UNSIGNED_BYTE, true, BYTES_PER_INSTANCE, 48);
		GLCompat.enableVertexAttribArray(5);

		GLCompat.vertexAttribDivisor(1, 1);
		GLCompat.vertexAttribDivisor(2, 1);
		GLCompat.vertexAttribDivisor(3, 1);
		GLCompat.vertexAttribDivisor(4, 1);
		GLCompat.vertexAttribDivisor(5, 1);

		GLCompat.bindVertexArray(0);
		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);

		instanceDataVbo = dataVbo;
		initialized = true;
	}
}
