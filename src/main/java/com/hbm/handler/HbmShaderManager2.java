package com.hbm.handler;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.HbmShaderManager2.Shader.Uniform;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//Same as the other class except with less junk (hopefully)
public class HbmShaderManager2 {
	
	public static final FloatBuffer AUX_GL_BUFFER = GLAllocation.createDirectFloatBuffer(16);
	
	public static final Uniform MODELVIEW_PROJECTION_MATRIX = shader -> {
		//No idea if all these rewind calls are necessary. I'll have to check that later.
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		Matrix4f mvMatrix = new Matrix4f();
		mvMatrix.load(AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		Matrix4f pMatrix = new Matrix4f();
		pMatrix.load(AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		
		Matrix4f.mul(pMatrix, mvMatrix, mvMatrix).store(AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		
		shader.uniformMatrix4("modelViewProjectionMatrix", false, AUX_GL_BUFFER);
	};
	
	public static final Uniform MODELVIEW_MATRIX = shader -> {
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("modelview", false, AUX_GL_BUFFER);
	};
	
	public static final Uniform PROJECTION_MATRIX = shader -> {
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("projection", false, AUX_GL_BUFFER);
	};
	
	public static final Uniform INV_PLAYER_ROT_MATRIX = shader -> {
		Entity entityIn = Minecraft.getMinecraft().getRenderViewEntity();
		float partialTicks = Minecraft.getMinecraft().isGamePaused() ? Minecraft.getMinecraft().renderPartialTicksPaused : Minecraft.getMinecraft().getRenderPartialTicks();
		float yaw = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw)*partialTicks;
		float pitch = entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch)*partialTicks;
		Matrix4f mat = new Matrix4f();
		mat.rotate((float) Math.toRadians(-yaw+180), new Vector3f(0, 1, 0));
		mat.rotate((float) Math.toRadians(-pitch), new Vector3f(1, 0, 0));
		mat.store(AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("invPlayerRot", false, AUX_GL_BUFFER);
	};
	
	public static final Uniform LIGHTMAP = shader -> {
		shader.uniform1i("lightmap", 1);
	};
	
	public static final Uniform WINDOW_SIZE = shader -> {
		shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
	};
	
	public static int height = 0;
    public static int width = 0;
    
    public static final int bloomLayers = 4;
    public static Framebuffer[] bloomBuffers;
    public static Framebuffer bloomData;
    public static Framebuffer distortionBuffer;
    
    public static int depthFrameBuffer = -1;
    public static int depthTexture = -1;
    
    public static float[] inv_ViewProjectionMatrix = new float[16];
	
    public static void createInvMVP(){
		GlStateManager.pushMatrix();
    	GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
    	GlStateManager.popMatrix();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, ClientProxy.AUX_GL_BUFFER2);
		Matrix4f view = new Matrix4f();
		Matrix4f proj = new Matrix4f();
		view.load(ClientProxy.AUX_GL_BUFFER);
		proj.load(ClientProxy.AUX_GL_BUFFER2);
		ClientProxy.AUX_GL_BUFFER.rewind();
		ClientProxy.AUX_GL_BUFFER2.rewind();
		view.invert();
		proj.invert();
		Matrix4f.mul(view, proj, view);
		view.store(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		ClientProxy.AUX_GL_BUFFER.get(inv_ViewProjectionMatrix);
		ClientProxy.AUX_GL_BUFFER.rewind();
	}
    
    public static void blitDepth(){
    	if(!GeneralConfig.depthEffects)
    		return;
    	if(height != Minecraft.getMinecraft().displayHeight || width != Minecraft.getMinecraft().displayWidth || depthFrameBuffer == -1){
    		GL11.glDeleteTextures(depthTexture);
    		GLCompat.deleteFramebuffers(depthFrameBuffer);
    		
    		depthFrameBuffer = GLCompat.genFramebuffers();
    		GLCompat.bindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, depthFrameBuffer);
    		depthTexture = GL11.glGenTextures();
    		GlStateManager.bindTexture(depthTexture);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer)null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GLCompat.framebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);
			int bruh = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER);
			if(bruh != OpenGlHelper.GL_FRAMEBUFFER_COMPLETE){
				System.out.println("Failed to create depth texture framebuffer! This is an error!");
			}
    	}
    	GLCompat.bindFramebuffer(GLCompat.GL_READ_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
    	GLCompat.bindFramebuffer(GLCompat.GL_DRAW_FRAMEBUFFER, depthFrameBuffer);
    	GLCompat.blitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
    	
    	Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
    }
    
	public static void postProcess(){
		if(!GeneralConfig.useShaders2)
			return;
		if(height != Minecraft.getMinecraft().displayHeight || width != Minecraft.getMinecraft().displayWidth){
			height = Minecraft.getMinecraft().displayHeight;
            width = Minecraft.getMinecraft().displayWidth;
            if(GeneralConfig.bloom)
            	recreateBloomFBOs();
            if(GeneralConfig.heatDistortion)
            	recreateDistortionBuffer();
        }
		if(GeneralConfig.bloom){
			bloom();
		}
		if(GeneralConfig.heatDistortion){
			heatDistortion();
		}
		GlStateManager.enableDepth();
	}
	
	private static void heatDistortion(){
		GL11.glFlush();
		ResourceManager.heat_distortion_post.use();
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
		GLCompat.activeTexture(GLCompat.GL_TEXTURE0+3);
		GlStateManager.bindTexture(Minecraft.getMinecraft().getFramebuffer().framebufferTexture);
		GlStateManager.bindTexture(Minecraft.getMinecraft().getFramebuffer().framebufferTexture);
		GLCompat.uniform1i(GLCompat.getUniformLocation(ResourceManager.heat_distortion_post.getShaderId(), "mc_tex"), 3);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		renderFboTriangle(distortionBuffer);
		releaseShader();
		
		distortionBuffer.bindFramebuffer(true);
		GlStateManager.clearColor(distortionBuffer.framebufferColor[0], distortionBuffer.framebufferColor[1], distortionBuffer.framebufferColor[2], distortionBuffer.framebufferColor[3]);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}
	
	public static void distort(float strength, Runnable render){
		distortionBuffer.bindFramebuffer(false);
		ResourceManager.heat_distortion_new.use();
		GLCompat.uniform1f(GLCompat.getUniformLocation(ResourceManager.heat_distortion_new.getShaderId(), "amount"), strength);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
		render.run();
		GlStateManager.disableBlend();
		releaseShader();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
	}
	
	private static void bloom(){
		downsampleBloomData();
		GlStateManager.enableBlend();
		for(int i = bloomLayers-1; i >= 0; i --){
			GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ZERO);
			bloomBuffers[i*2+1].bindFramebuffer(true);
			ResourceManager.bloom_h.use();
			GLCompat.uniform1f(GLCompat.getUniformLocation(ResourceManager.bloom_h.getShaderId(), "frag_width"), 1F/(float)bloomBuffers[i*2].framebufferWidth);
			renderFboTriangle(bloomBuffers[i*2], bloomBuffers[i*2+1].framebufferWidth, bloomBuffers[i*2+1].framebufferHeight);
			
			GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
			int tWidth, tHeight;
			if(i == 0){
				Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
				tWidth = Minecraft.getMinecraft().getFramebuffer().framebufferWidth;
				tHeight = Minecraft.getMinecraft().getFramebuffer().framebufferHeight;
			} else {
				GLCompat.blendEquation(GLCompat.GL_MAX);
				bloomBuffers[(i-1)*2].bindFramebuffer(true);
				tWidth = bloomBuffers[(i-1)*2].framebufferWidth;
				tHeight = bloomBuffers[(i-1)*2].framebufferHeight;
			}
			ResourceManager.bloom_v.use();
			GLCompat.uniform1f(GLCompat.getUniformLocation(ResourceManager.bloom_v.getShaderId(), "frag_height"), 1F/(float)bloomBuffers[i*2].framebufferHeight);
			renderFboTriangle(bloomBuffers[i*2+1], tWidth, tHeight);
			GLCompat.blendEquation(GLCompat.GL_FUNC_ADD);
		}
		releaseShader();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
		bloomData.bindFramebuffer(true);
		GlStateManager.clearColor(bloomData.framebufferColor[0], bloomData.framebufferColor[1], bloomData.framebufferColor[2], bloomData.framebufferColor[3]);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
	}
	
	public static void downsampleBloomData(){
		bloomBuffers[0].bindFramebuffer(true);
		ResourceManager.downsample.use();
		GLCompat.uniform2f(GLCompat.getUniformLocation(ResourceManager.downsample.getShaderId(), "texel"), 1F/(float)bloomData.framebufferTextureWidth, 1F/(float)bloomData.framebufferTextureHeight);
		renderFboTriangle(bloomData, bloomBuffers[0].framebufferWidth, bloomBuffers[0].framebufferHeight);
		for(int i = 1; i < bloomLayers; i ++){
			bloomBuffers[i*2].bindFramebuffer(true);
			GLCompat.uniform2f(GLCompat.getUniformLocation(ResourceManager.downsample.getShaderId(), "texel"), 1F/(float)bloomBuffers[(i-1)*2].framebufferTextureWidth, 1F/(float)bloomBuffers[(i-1)*2].framebufferTextureHeight);
			renderFboTriangle(bloomBuffers[(i-1)*2], bloomBuffers[i*2].framebufferWidth, bloomBuffers[i*2].framebufferHeight);
		}
		releaseShader();
	}
	
	public static void recreateDistortionBuffer(){
		if(distortionBuffer != null){
			distortionBuffer.deleteFramebuffer();
		}
		distortionBuffer = new Framebuffer(width, height, true);
		distortionBuffer.bindFramebufferTexture();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_SHORT, (IntBuffer)null);
		distortionBuffer.bindFramebuffer(false);
		GLCompat.bindRenderbuffer(GLCompat.GL_RENDERBUFFER, Minecraft.getMinecraft().getFramebuffer().depthBuffer);
		OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, Minecraft.getMinecraft().getFramebuffer().depthBuffer);
		distortionBuffer.setFramebufferFilter(GL11.GL_LINEAR);
		distortionBuffer.setFramebufferColor(0, 0, 0, 0);
		distortionBuffer.framebufferClear();
	}
	
	public static void recreateBloomFBOs(){
		if(bloomBuffers != null)
			for(Framebuffer buf : bloomBuffers){
				buf.deleteFramebuffer();
			}
		if(bloomData != null)
			bloomData.deleteFramebuffer();
		bloomData = new Framebuffer(width, height, true);
		bloomData.bindFramebufferTexture();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_SHORT, (IntBuffer)null);
		bloomData.bindFramebuffer(false);
		GLCompat.bindRenderbuffer(GLCompat.GL_RENDERBUFFER, Minecraft.getMinecraft().getFramebuffer().depthBuffer);
		GLCompat.framebufferRenderbuffer(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GLCompat.GL_RENDERBUFFER, Minecraft.getMinecraft().getFramebuffer().depthBuffer);
		bloomData.setFramebufferFilter(GL11.GL_LINEAR);
		bloomData.setFramebufferColor(0, 0, 0, 0);
		bloomData.framebufferClear();
		bloomBuffers = new Framebuffer[bloomLayers*2];
		float bloomW = width;
		float bloomH = height;
		for(int i = 0; i < bloomLayers; i ++){
			
			bloomBuffers[i*2] = new Framebuffer((int)bloomW, (int)bloomH, false);
			bloomBuffers[i*2+1] = new Framebuffer((int)bloomW, (int)bloomH, false);
			bloomBuffers[i*2].bindFramebufferTexture();
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_RGBA16F, (int)bloomW, (int)bloomH, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_SHORT, (IntBuffer)null);
			bloomBuffers[i*2+1].bindFramebufferTexture();
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_RGBA16F, (int)bloomW, (int)bloomH, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_SHORT, (IntBuffer)null);
			bloomBuffers[i*2].setFramebufferFilter(GL11.GL_LINEAR);
			bloomBuffers[i*2+1].setFramebufferFilter(GL11.GL_LINEAR);
			bloomBuffers[i*2].setFramebufferColor(0, 0, 0, 0);
			bloomBuffers[i*2+1].setFramebufferColor(0, 0, 0, 0);
			if(i < 2){
				bloomW *= 0.25F;
				bloomH *= 0.25F;
			} else {
				bloomW *= 0.5F;
				bloomH *= 0.5F;
			}
		}
	}
	
	public static void renderFboTriangle(Framebuffer buf){
		renderFboTriangle(buf, buf.framebufferWidth, buf.framebufferHeight);
	}
	
	public static void renderFboTriangle(Framebuffer buf, int width, int height){
		GlStateManager.colorMask(true, true, true, false);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.viewport(0, 0, width, height);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();

        GlStateManager.enableColorMaterial();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        buf.bindFramebufferTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-1, -1, 0.0D).tex(0, 0).endVertex();
        bufferbuilder.pos(3, -1, 0.0D).tex(2, 0).endVertex();
        bufferbuilder.pos(-1, 3, 0.0D).tex(0, 2).endVertex();
        tessellator.draw();
        buf.unbindFramebufferTexture();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
	}
	
    public static Framebuffer buf;
    
   /* public static void doPostProcess(){
        if(height != Minecraft.getMinecraft().displayHeight || width != Minecraft.getMinecraft().displayWidth){
            recreateFBOs();
            height = Minecraft.getMinecraft().displayHeight;
            width = Minecraft.getMinecraft().displayWidth;
        }
        GlStateManager.pushMatrix();
        
        buf.bindFramebuffer(false);
        
        ResourceManager.testlut.use();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.lut);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GL20.glUniform1i(GL20.glGetUniformLocation(ResourceManager.testlut.getShaderId(), "tempTest"), 3);
        
        Minecraft.getMinecraft().getFramebuffer().framebufferRender(buf.framebufferWidth, buf.framebufferHeight);
        
        HbmShaderManager2.releaseShader();
        
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buf.framebufferObject);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
       
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();
    }
    
    public static void recreateFBOs(){
        if(buf != null)
            buf.deleteFramebuffer();
        buf = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, false);
    }*/
	
    public static Shader loadShader(ResourceLocation file) {
    	return loadShader(file, null);
    }
    
	public static Shader loadShader(ResourceLocation file, Consumer<Integer> attribBinder) {
		return loadShader(file, attribBinder, false);
	}

	public static Shader loadShader(ResourceLocation file, Consumer<Integer> attribBinder, boolean allowWithoutShaders2) {
		return loadShader(file, attribBinder, allowWithoutShaders2, false);
	}

	public static Shader loadShader(ResourceLocation file, Consumer<Integer> attribBinder, boolean allowWithoutShaders2, boolean failOnError) {
		if(!allowWithoutShaders2 && !GeneralConfig.useShaders2 || !GLCompat.error.isEmpty()){
			if(failOnError) {
				String reason = !GLCompat.error.isEmpty() ? "advanced rendering is not supported: " + GLCompat.error : "shader effects are disabled";
				throw new IllegalStateException("Failed to load shader " + file + " because " + reason);
			}
			return new Shader(0);
		}
		int vertexShader = 0;
		int fragmentShader = 0;
		try {
			int program = GLCompat.createProgram();
			
			vertexShader = GLCompat.createShader(GLCompat.GL_VERTEX_SHADER);
			GLCompat.shaderSource(vertexShader, readFileToBuf(new ResourceLocation(file.getNamespace(), file.getPath() + ".vert")));
			GLCompat.compileShader(vertexShader);
			if(GLCompat.getShaderi(vertexShader, GLCompat.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				MainRegistry.logger.error(GLCompat.getShaderInfoLog(vertexShader, GLCompat.GL_INFO_LOG_LENGTH));
				throw new RuntimeException("Error creating vertex shader: " + file);
			}
			
			fragmentShader = GLCompat.createShader(GLCompat.GL_FRAGMENT_SHADER);
			GLCompat.shaderSource(fragmentShader, readFileToBuf(new ResourceLocation(file.getNamespace(), file.getPath() + ".frag")));
			GLCompat.compileShader(fragmentShader);
			if(GLCompat.getShaderi(fragmentShader, GLCompat.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				MainRegistry.logger.error(GLCompat.getShaderInfoLog(fragmentShader, GLCompat.GL_INFO_LOG_LENGTH));
				throw new RuntimeException("Error creating fragment shader: " + file);
			}
			
			GLCompat.attachShader(program, vertexShader);
			GLCompat.attachShader(program, fragmentShader);
			if(attribBinder != null)
				attribBinder.accept(program);
			GLCompat.linkProgram(program);
			if(GLCompat.getProgrami(program, GLCompat.GL_LINK_STATUS) == GL11.GL_FALSE) {
				MainRegistry.logger.error(GLCompat.getProgramInfoLog(program, GLCompat.GL_INFO_LOG_LENGTH));
				throw new RuntimeException("Error linking shader: " + file);
			}
			
			GLCompat.deleteShader(vertexShader);
			GLCompat.deleteShader(fragmentShader);
			
			return new Shader(program);
		} catch(Exception x) {
			GLCompat.deleteShader(vertexShader);
			GLCompat.deleteShader(fragmentShader);
			if(failOnError) {
				throw new RuntimeException("Failed to load shader: " + file, x);
			}
			x.printStackTrace();
		}
		return new Shader(0);
	}

	private static ByteBuffer readFileToBuf(ResourceLocation file) throws IOException {
		InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(file).getInputStream();
		byte[] bytes = IOUtils.toByteArray(in);
		IOUtils.closeQuietly(in);
		ByteBuffer buf = BufferUtils.createByteBuffer(bytes.length);
		buf.put(bytes);
		buf.rewind();
		return buf;
	}
	
	public static void releaseShader(){
		GLCompat.useProgram(0);
	}
	
	public static class Shader {

		private int shader;
		private List<Uniform> uniforms = new ArrayList<>(2);
		
		public Shader(int shader) {
			this.shader = shader;
		}
		
		public Shader withUniforms(Uniform... uniforms){
			for(Uniform u : uniforms){
				this.uniforms.add(u);
			}
			return this;
		}
		
		public void use(){
			if(shader == 0)
				return;
			GLCompat.useProgram(shader);
			for(Uniform u : uniforms){
				u.apply(this);
			}
		}
		
		public int getShaderId(){
			return shader;
		}
		
		public void uniform1i(String name, int v0){
			if(shader == 0)
				return;
			GLCompat.uniform1i(GLCompat.getUniformLocation(shader, name), v0);
		}
		
		public void uniform1f(String name, float v0){
			if(shader == 0)
				return;
			GLCompat.uniform1f(GLCompat.getUniformLocation(shader, name), v0);
		}
		
		public void uniform2f(String name, float v0, float v1){
			if(shader == 0)
				return;
			GLCompat.uniform2f(GLCompat.getUniformLocation(shader, name), v0, v1);
		}
		
		public void uniform3f(String name, float v0, float v1, float v2){
			if(shader == 0)
				return;
			GLCompat.uniform3f(GLCompat.getUniformLocation(shader, name), v0, v1, v2);
		}
		
		public void uniform4f(String name, float v0, float v1, float v2, float v3){
			if(shader == 0)
				return;
			GLCompat.uniform4f(GLCompat.getUniformLocation(shader, name), v0, v1, v2, v3);
		}
		
		public void uniformMatrix3(String name, boolean transpose, FloatBuffer matrix){
			if(shader == 0)
				return;
			GLCompat.uniformMatrix3(GLCompat.getUniformLocation(shader, name), transpose, matrix);
		}
		
		public void uniformMatrix4(String name, boolean transpose, FloatBuffer matrix){
			if(shader == 0)
				return;
			GLCompat.uniformMatrix4(GLCompat.getUniformLocation(shader, name), transpose, matrix);
		}
		
		public static interface Uniform {
			public void apply(Shader shader);
		}
	}
}
