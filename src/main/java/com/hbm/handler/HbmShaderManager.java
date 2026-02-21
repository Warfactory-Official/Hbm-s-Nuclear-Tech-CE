package com.hbm.handler;

import com.hbm.Tags;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
// Th3_Sl1ze: sorry bud, I have to clean this up at least. Probably no one's going to use old shaders, so..
// Someone had to do it anyway, right?
@SideOnly(Side.CLIENT)
@Deprecated
public class HbmShaderManager {

	private static final boolean arbShaders = !GLContext.getCapabilities().OpenGL21;

	private static final int VERT = arbShaders ? ARBVertexShader.GL_VERTEX_SHADER_ARB : GL20.GL_VERTEX_SHADER;
	private static final int FRAG = arbShaders ? ARBFragmentShader.GL_FRAGMENT_SHADER_ARB : GL20.GL_FRAGMENT_SHADER;
	private static final int VALIDATE_STATUS = arbShaders ? ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB : GL20.GL_VALIDATE_STATUS;
	private static final int INFO_LOG_LENGTH = arbShaders ? ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB : GL20.GL_INFO_LOG_LENGTH;

	public static final Uniform TIME = Uniform.createUniform("time", () -> System.currentTimeMillis()/1000F);
	public static int bfg_worm;
	public static int bfg_beam;
	
	public static int noise1;
	public static int noise2;

	public static void loadShaders() {
		if(GeneralConfig.useShaders2){
			bfg_worm = createShader("bfg_worm.frag", "bfg_worm.vert");
			bfg_beam = createShader("bfg_beam.frag", "bfg_worm.vert");
			SimpleTexture tex = new SimpleTexture(new ResourceLocation(Tags.MODID, "textures/misc/perlin1.png"));
			try {
				tex.loadTexture(Minecraft.getMinecraft().getResourceManager());
			} catch(IOException e) {
				e.printStackTrace();
			}
			noise1 = tex.getGlTextureId();
			GlStateManager.bindTexture(noise1);
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
			tex = new SimpleTexture(new ResourceLocation(Tags.MODID, "textures/misc/perlin2.png"));
			try {
				tex.loadTexture(Minecraft.getMinecraft().getResourceManager());
			} catch(IOException e) {
				e.printStackTrace();
			}
			noise2 = tex.getGlTextureId();
			GlStateManager.bindTexture(noise2);
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
		}
	}

	private static int createShader(String frag, String vert) {
		int prog = OpenGlHelper.glCreateProgram();
		if(prog == 0)
			return 0;
		int vertexShader = createVertexShader("/assets/" + Tags.MODID + "/shaders/" + vert);
		int fragShader = createFragShader("/assets/" + Tags.MODID + "/shaders/" + frag);
		OpenGlHelper.glAttachShader(prog, vertexShader);
		OpenGlHelper.glAttachShader(prog, fragShader);

		OpenGlHelper.glLinkProgram(prog);
		if(OpenGlHelper.glGetProgrami(prog, OpenGlHelper.GL_LINK_STATUS) == GL11.GL_FALSE) {
			MainRegistry.logger.log(Level.ERROR, "Error creating shader " + frag + " " + vert);
			MainRegistry.logger.error(OpenGlHelper.glGetProgramInfoLog(prog, 32768));
			return 0;
		}

		glValidateProgram(prog);

		if(OpenGlHelper.glGetProgrami(prog, VALIDATE_STATUS) == GL11.GL_FALSE) {
			MainRegistry.logger.log(Level.ERROR, "Error validating shader " + frag + " " + vert);
			return 0;
		}
		return prog;
	}

	private static int createFragShader(String shaderSource) {
		int shader = 0;
		try {
			shader = OpenGlHelper.glCreateShader(FRAG);
			if(shader == 0)
				return 0;
			OpenGlHelper.glShaderSource(shader, readFileToBuf(shaderSource));
			OpenGlHelper.glCompileShader(shader);

			if(OpenGlHelper.glGetShaderi(shader, OpenGlHelper.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				MainRegistry.logger.error(OpenGlHelper.glGetShaderInfoLog(shader, INFO_LOG_LENGTH));
				throw new RuntimeException("Error creating shader: " + shaderSource);
			}
			return shader;
		} catch(Exception x) {
			OpenGlHelper.glDeleteShader(shader);
			x.printStackTrace();
			return -1;
		}
	}

	private static int createVertexShader(String shaderSource) {
		int shader = 0;
		try {
			shader = OpenGlHelper.glCreateShader(VERT);
			if(shader == 0)
				return 0;
			OpenGlHelper.glShaderSource(shader, readFileToBuf(shaderSource));
			OpenGlHelper.glCompileShader(shader);

			if(OpenGlHelper.glGetShaderi(shader, OpenGlHelper.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				MainRegistry.logger.error(OpenGlHelper.glGetShaderInfoLog(shader, INFO_LOG_LENGTH));
				throw new RuntimeException("Error creating shader: " + shaderSource);
			}
			return shader;
		} catch(Exception x) {
			OpenGlHelper.glDeleteShader(shader);
			x.printStackTrace();
			return -1;
		}
	}

	public static ByteBuffer readFileToBuf(String file) throws IOException {

		InputStream in = ShaderManager.class.getResourceAsStream(file);

		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return (ByteBuffer) BufferUtils.createByteBuffer(bytes.length).put(bytes).position(0);
	}

	public static void glValidateProgram(int prog) {
		if(arbShaders) {
			ARBShaderObjects.glValidateProgramARB(prog);
		} else {
			GL20.glValidateProgram(prog);
		}
	}
	
	public static boolean shouldUseShader2() {
		return OpenGlHelper.shadersSupported && GeneralConfig.useShaders2;
	}
	
	public static void useShader2(int shader) {
		if(!shouldUseShader2())
			return;
		OpenGlHelper.glUseProgram(shader);
	}
	
	public static void useWormShader(float offset){
		useShader2(bfg_worm);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GlStateManager.bindTexture( noise1);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GlStateManager.bindTexture( noise2);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL20.glUniform1i(GL20.glGetUniformLocation(bfg_worm, "noise"), 2);
		GL20.glUniform1i(GL20.glGetUniformLocation(bfg_worm, "bigNoise"), 3);
		float worldTime = Minecraft.getMinecraft().world.getTotalWorldTime() + Minecraft.getMinecraft().getRenderPartialTicks() + offset;
		GL20.glUniform1f(GL20.glGetUniformLocation(bfg_worm, "worldTime"), worldTime/4);
	}
	
	public static void releaseShader2() {
		useShader2(0);
	}

	public static class Uniform {

		FloatSupplier ints;
		String name;

		public Uniform(String name, FloatSupplier ints) {
			this.name = name;
			this.ints = ints;
		}

		public static Uniform createUniform(String name, FloatSupplier ints) {
			return new Uniform(name, ints);
		}

		public void assign(int shader) {
			if(arbShaders) {
				ARBShaderObjects.glUniform1fARB(OpenGlHelper.glGetUniformLocation(shader, name), ints.getAsFloat());
			} else {
				GL20.glUniform1f(OpenGlHelper.glGetUniformLocation(shader, name), ints.getAsFloat());
			}
		}

	}

	public interface FloatSupplier {
		float getAsFloat();
	}

}
