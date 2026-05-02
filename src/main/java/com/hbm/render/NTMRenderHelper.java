package com.hbm.render;

import com.hbm.entity.missile.EntityMissileAntiBallistic;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.lib.Library;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
@SideOnly(Side.CLIENT)
public class NTMRenderHelper {
	
	private static FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
	private static FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
	private static IntBuffer VIEWPORT = GLAllocation.createDirectIntBuffer(16);
	private static FloatBuffer POSITION = GLAllocation.createDirectFloatBuffer(4);
	
	public static boolean useFullPost = true;
	public static boolean flashlightInit = false;
	public static int shadowFbo;
	public static int shadowFboTex;
	
	public static int height = 0;
    public static int width = 0;
	
	public static int deferredFbo;
	public static int deferredColorTex = -1;
	//TODO condense into one texture?
	public static int deferredPositionTex = -1;
	public static int deferredProjCoordTex = -1;
	//Actually it might be possible to reconstruct both position and normal data from the depth buffer, which would't require a shader at all
	//TODO test this?
	public static int deferredNormalTex = -1;
	//Only used for full post processing
	public static int deferredDepthTex = -1;
	
	public static float[] inv_ViewProjectionMatrix = new float[16];
	
	//Flashlights should all be rendered at the end of the render world for their deferred rendering to work.
	//If we're not at the end of render world, add it to a list to be rendered later.
	public static boolean renderingFlashlights = false;
	//If true, no flashlights should be added to the render list. This prevents entities that add flashlights from adding them more than once when we
	//Render them here
	private static boolean flashlightLock = false;
	//List of future flashlights to render;
	private static List<Runnable> flashlightQueue = new ArrayList<>();
	
	/**
	 * 
	 * @param lb
	 * @param rb
	 * @param rt
	 * @param lt
	 * @return left-bottom-right-top
	 */
	public static float[] getScreenAreaFromQuad(Vec3d lb, Vec3d rb, Vec3d rt, Vec3d lt){
		FloatBuffer mmatrix = GLAllocation.createDirectFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, mmatrix);
		FloatBuffer pmatrix = GLAllocation.createDirectFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, pmatrix);
		IntBuffer vport = GLAllocation.createDirectIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, vport);
		
		FloatBuffer[] points = new FloatBuffer[4];
		FloatBuffer buf0 = GLAllocation.createDirectFloatBuffer(3);
		Project.gluProject((float)lb.x, (float)lb.y, (float)lb.z, mmatrix, pmatrix, vport, buf0);
		points[0] = buf0;
		FloatBuffer buf1 = GLAllocation.createDirectFloatBuffer(3);
		Project.gluProject((float)rb.x, (float)rb.y, (float)rb.z, mmatrix, pmatrix, vport, buf1);
		points[1] = buf1;
		FloatBuffer buf2 = GLAllocation.createDirectFloatBuffer(3);
		Project.gluProject((float)rt.x, (float)rt.y, (float)rt.z, mmatrix, pmatrix, vport, buf2);
		points[2] = buf2;
		FloatBuffer buf3 = GLAllocation.createDirectFloatBuffer(3);
		Project.gluProject((float)lt.x, (float)lt.y, (float)lt.z, mmatrix, pmatrix, vport, buf3);
		points[3] = buf3;
		
		float top = buf0.get(1);
		float bottom = buf0.get(1);
		float left = buf0.get(0);
		float right = buf0.get(0);
		
		for(FloatBuffer buf : points){
			if(buf.get(0) > right){
				right = buf.get(0);
			}
			if(buf.get(0) < left){
				left = buf.get(0);
			}
			if(buf.get(1) > top){
				top = buf.get(1);
			}
			if(buf.get(1) < bottom){
				bottom = buf.get(1);
			}
		}
		//System.out.println(top);
		if(bottom < 0)
			bottom = 0;
		if(top > Minecraft.getMinecraft().displayHeight)
			top = Minecraft.getMinecraft().displayHeight;
		if(left < 0)
			left = 0;
		if(right > Minecraft.getMinecraft().displayWidth)
			right = Minecraft.getMinecraft().displayWidth;
		
		if(right <= 0 || top <= 0 || bottom >= Minecraft.getMinecraft().displayHeight || left >= Minecraft.getMinecraft().displayWidth)
			return null;
		//System.out.println(right);
		return new float[]{left, bottom, right, top};
	}
	
	
	public static TextureAtlasSprite getItemTexture(Item item, int meta){
		return getItemTexture(new ItemStack(item, 1, meta));
	}

	//TODO: add some documentation
	public static void addQuad(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2,
                               float x3, float y3, float z3, float x4, float y4, float z4, int r, int g, int b) {
		((NTMBufferBuilder) buffer).appendPositionColorQuadUnchecked(
				x1, y1, z1,
				x2, y2, z2,
				x3, y3, z3,
				x4, y4, z4,
				NTMBufferBuilder.packColor(r, g, b, 255)
		);
	}

	public static TextureAtlasSprite getItemTexture(Item item){
		return getItemTexture(item, 0);
	}
	
	public static TextureAtlasSprite getItemTexture(ItemStack item){
		return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(item, null, null).getParticleTexture();
	}
	
	public static void addVertexWithUV(float x, float y, float z, float u, float v){
		addVertexWithUV(x, y, z, u, v, Tessellator.getInstance());
	}
	public static void addVertex(float x, float y, float z){
		((NTMBufferBuilder) Tessellator.getInstance().getBuffer()).appendPosition(x, y, z);
	}

	public static void addVertexWithUV(float x, float y, float z, float u, float v, Tessellator tes){
		((NTMBufferBuilder) tes.getBuffer()).appendPositionTex(x, y, z, u, v);
	}

	public static void startDrawingTexturedQuads(){
		startDrawingTexturedQuads(Tessellator.getInstance());
	}
	public static void startDrawingQuads(){
		NTMImmediate.INSTANCE.beginPositionQuads(1);
	}
	public static void startDrawingTexturedQuads(Tessellator tes){
		NTMImmediate.INSTANCE.beginPositionTexQuads(1);
	}
	public static void startDrawingColoredTriangles(Tessellator tes){
		NTMImmediate.INSTANCE.beginPositionColor(GL11.GL_TRIANGLE_FAN, 0);
	}
	public static void draw(){
		draw(Tessellator.getInstance());
	}
	public static void draw(Tessellator tes){
		NTMImmediate.INSTANCE.draw();
	}
	
	public static void bindTexture(ResourceLocation resource){
		Minecraft.getMinecraft().renderEngine.bindTexture(resource);
	}
	public static void bindBlockTexture(){
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public static void drawScaledTexture(TextureAtlasSprite icon, int posX, int posY, int sizeX, int sizeY, float zLevel){
		if(sizeX < 0)
			sizeX = 0;
		if(sizeX > 16)
			sizeX = 16;
		if(sizeY < 0)
			sizeY = 0;
		if(sizeY > 16)
			sizeY = 16;
		float up = icon.getInterpolatedV(16);
		float down = icon.getInterpolatedV(16 - sizeY);
		float left = icon.getInterpolatedU(0);
		float right = icon.getInterpolatedU(sizeX);
		addVertexWithUV(posX, posY + sizeY, zLevel, left, up);
		addVertexWithUV(posX + sizeX, posY + sizeY, zLevel, right, up);
		addVertexWithUV(posX + sizeX, posY, zLevel, right, down);
		addVertexWithUV(posX, posY, zLevel, left, down);
	}

	public static void drawFullTexture(TextureAtlasSprite icon, float posX, float posY, float sizeX, float sizeY, float zLevel, boolean flipped){
		float up = icon.getInterpolatedV(0);
		float down = icon.getInterpolatedV(16);
		float left = icon.getInterpolatedU(0);
		float right = icon.getInterpolatedU(16);
		if(flipped){
			addVertexWithUV(posX + sizeX, posY + sizeY, zLevel, right, up);
			addVertexWithUV(posX, posY + sizeY, zLevel, left, up);
			addVertexWithUV(posX, posY, zLevel, left, down);
			addVertexWithUV(posX + sizeX, posY, zLevel, right, down);
		} else {
			addVertexWithUV(posX, posY + sizeY, zLevel, left, up);
			addVertexWithUV(posX + sizeX, posY + sizeY, zLevel, right, up);
			addVertexWithUV(posX + sizeX, posY, zLevel, right, down);
			addVertexWithUV(posX, posY, zLevel, left, down);
		}
	}

	//Drillgon200: using GLStateManager for this because it caches color values
	public static void setColor(int color) {

		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		GlStateManager.color(red, green, blue, 1.0F);
	}
	
	public static void unpackColor(int color, float[] col){
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		col[0] = red;
		col[1] = green;
		col[2] = blue;
	}
	
	public static void resetColor(){
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void startDrawingColored(int i) {
		Tessellator.getInstance().getBuffer().begin(i, DefaultVertexFormats.POSITION_COLOR);
	}
	
	public static void addVertexColor(float x, float y, float z, int red, int green, int blue, int alpha){
		((NTMBufferBuilder) Tessellator.getInstance().getBuffer()).appendPositionColor(x, y, z, NTMBufferBuilder.packColor(red, green, blue, alpha));
	}

	public static void addVertexColor(float x, float y, float z, float red, float green, float blue, float alpha){
		((NTMBufferBuilder) Tessellator.getInstance().getBuffer()).appendPositionColor(x, y, z, NTMBufferBuilder.packColor(red, green, blue, alpha));
	}

	public static void addVertexColor(float x, float y, float z, float red, float green, float blue, float alpha, Tessellator tess){
		((NTMBufferBuilder) tess.getBuffer()).appendPositionColor(x, y, z, NTMBufferBuilder.packColor(red, green, blue, alpha));
	}

	public static void renderAll(IBakedModel boxcar) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
    	for(BakedQuad quad : boxcar.getQuads(null, null, 0)){
    		buf.addVertexData(quad.getVertexData());
    	}
    	tes.draw();
	}
	
	/**
	 * Helper method for getting the real render position from a missile, which updates its position more than once per game tick.
	 * @param missile - the missile to get the actual render pos from
	 * @param partialTicks - render partial ticks
	 * @return A three element double array, containing the render pos x at index 0, y at index 1, and z at index 2
	 */
	public static double[] getRenderPosFromMissile(EntityMissileBaseNT missile, float partialTicks){
		double d0 = missile.prevPosX + (missile.posX - missile.prevPosX) * partialTicks;
		double d1 = missile.prevPosY + (missile.posY - missile.prevPosY) * partialTicks;
		double d2 = missile.prevPosZ + (missile.posZ - missile.prevPosZ) * partialTicks;
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		
		return new double[]{d0 - d3, d1 - d4, d2 - d5};
	}

	public static double[] getRenderPosFromMissile(EntityMissileAntiBallistic missile, float partialTicks){
		double d0 = missile.prevPosX + (missile.posX - missile.prevPosX) * partialTicks;
		double d1 = missile.prevPosY + (missile.posY - missile.prevPosY) * partialTicks;
		double d2 = missile.prevPosZ + (missile.posZ - missile.prevPosZ) * partialTicks;
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

		return new double[]{d0 - d3, d1 - d4, d2 - d5};
	}

	public static double[] getRenderPosFromMissile(EntityMissileCustom missile, float partialTicks){
		double d0 = missile.prevPosX + (missile.posX - missile.prevPosX) * partialTicks;
		double d1 = missile.prevPosY + (missile.posY - missile.prevPosY) * partialTicks;
		double d2 = missile.prevPosZ + (missile.posZ - missile.prevPosZ) * partialTicks;
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		
		return new double[]{d0 - d3, d1 - d4, d2 - d5};
	}
	
	public static void drawGuiRect(float x, float y, float u, float v, float width, float height, float uMax, float vMax){
        NTMBufferBuilder bufferbuilder = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        bufferbuilder.appendPositionTexQuadUnchecked(
                x, y + height, 0.0F, u, vMax,
                x + width, y + height, 0.0F, uMax, vMax,
                x + width, y, 0.0F, uMax, v,
                x, y, 0.0F, u, v
        );
        NTMImmediate.INSTANCE.draw();
	}
	
	public static void drawGuiRectColor(float x, float y, float u, float v, float width, float height, float uMax, float vMax, float r, float g, float b, float a){
        NTMBufferBuilder bufferbuilder = NTMImmediate.INSTANCE.beginPositionTexColorQuads(1);
        int packedColor = NTMBufferBuilder.packColor(r, g, b, a);
        bufferbuilder.appendPositionTexColorQuadUnchecked(
                x, y + height, 0.0F, u, vMax, packedColor,
                x + width, y + height, 0.0F, uMax, vMax, packedColor,
                x + width, y, 0.0F, uMax, v, packedColor,
                x, y, 0.0F, u, v, packedColor
        );
        NTMImmediate.INSTANCE.draw();
	}
	
	public static void drawGuiRectBatched(float x, float y, float u, float v, float width, float height, float uMax, float vMax){
        NTMBufferBuilder bufferbuilder = (NTMBufferBuilder) Tessellator.getInstance().getBuffer();
        bufferbuilder.appendPositionTexQuadUnchecked(
                x, y + height, 0.0F, u, vMax,
                x + width, y + height, 0.0F, uMax, vMax,
                x + width, y, 0.0F, uMax, v,
                x, y, 0.0F, u, v
        );
	}
	
	public static void drawGuiRectBatchedColor(float x, float y, float u, float v, float width, float height, float uMax, float vMax, float r, float g, float b, float a){
        NTMBufferBuilder bufferbuilder = (NTMBufferBuilder) Tessellator.getInstance().getBuffer();
        int packedColor = NTMBufferBuilder.packColor(r, g, b, a);
        bufferbuilder.appendPositionTexColorQuadUnchecked(
                x, y + height, 0.0F, u, vMax, packedColor,
                x + width, y + height, 0.0F, uMax, vMax, packedColor,
                x + width, y, 0.0F, uMax, v, packedColor,
                x, y, 0.0F, u, v, packedColor
        );
	}
	
	@Deprecated
	private static void initializeFL(){
		shadowFbo = GL30.glGenFramebuffers();
		shadowFboTex = GL11.glGenTextures();
		GlStateManager.bindTexture(shadowFboTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, 1024, 1024, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (FloatBuffer)null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, shadowFboTex, 0);
		clearFLShadowBuffer();
		int bruh = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if(bruh != GL30.GL_FRAMEBUFFER_COMPLETE){
			System.out.println("aaaaaa");
		}
	}
	
	@Deprecated
	private static void clearFLShadowBuffer(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
		GlStateManager.clearDepth(1);
		GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}
	
	@Deprecated
	private static void bindFLShadowBuffer(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
		GlStateManager.viewport(0, 0, 1024, 1024);
	}
	
	@Deprecated
	private static void deleteDeferredFbo(){
		GL11.glDeleteTextures(deferredColorTex);
		GL11.glDeleteTextures(deferredPositionTex);
		GL11.glDeleteTextures(deferredProjCoordTex);
		GL11.glDeleteTextures(deferredNormalTex);
		GL11.glDeleteTextures(deferredDepthTex);
		GL30.glDeleteFramebuffers(deferredFbo);
		deferredColorTex = -1;
		deferredPositionTex = -1;
		deferredProjCoordTex = -1;
		deferredNormalTex = -1;
		deferredDepthTex = -1;
	}
	
	@Deprecated
	private static void recreateDeferredFbo(){
		deferredFbo = GL30.glGenFramebuffers();
		deferredColorTex = GL11.glGenTextures();
		if(!useFullPost){
			deferredPositionTex = GL11.glGenTextures();
			deferredProjCoordTex = GL11.glGenTextures();
			deferredNormalTex = GL11.glGenTextures();
		} else {
			deferredDepthTex = GL11.glGenTextures();
		}
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, deferredFbo);
		
		GlStateManager.bindTexture(deferredColorTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (FloatBuffer)null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, deferredColorTex, 0);
		
		if(!useFullPost){
			GlStateManager.bindTexture(deferredPositionTex);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, deferredPositionTex, 0);
			
			GlStateManager.bindTexture(deferredProjCoordTex);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL11.GL_TEXTURE_2D, deferredProjCoordTex, 0);
			
			GlStateManager.bindTexture(deferredNormalTex);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT3, GL11.GL_TEXTURE_2D, deferredNormalTex, 0);
			
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, Minecraft.getMinecraft().getFramebuffer().depthBuffer);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, Minecraft.getMinecraft().getFramebuffer().depthBuffer);
		
			IntBuffer buf = GLAllocation.createDirectIntBuffer(4);
			buf.put(GL30.GL_COLOR_ATTACHMENT0);
			buf.put(GL30.GL_COLOR_ATTACHMENT1);
			buf.put(GL30.GL_COLOR_ATTACHMENT2);
			buf.put(GL30.GL_COLOR_ATTACHMENT3);
			buf.rewind();
			GL20.glDrawBuffers(buf);
		} else {
			GlStateManager.bindTexture(deferredDepthTex);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer)null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, deferredDepthTex, 0);
		}
		
		int bruh = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if(bruh != GL30.GL_FRAMEBUFFER_COMPLETE){
			System.out.println("aaaaaa");
		}
	}
	
	@Deprecated
	private static void clearDeferredBuffer(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, deferredFbo);
		GlStateManager.clearColor(0, 0, 0, 1);
		if(!useFullPost){
			GlStateManager.clearDepth(1);
			GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		} else {
			GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
		}
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}
	
	@Deprecated
	public static void renderFlashlights(){
		NTMRenderHelper.renderingFlashlights = true;
		if(height != Minecraft.getMinecraft().displayHeight || width != Minecraft.getMinecraft().displayWidth){
			height = Minecraft.getMinecraft().displayHeight;
            width = Minecraft.getMinecraft().displayWidth;
            deleteDeferredFbo();
            recreateDeferredFbo();
        }
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
    	GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, deferredFbo);
    	GL30.glBlitFramebuffer(0, 0, NTMRenderHelper.width, NTMRenderHelper.height, 0, 0, NTMRenderHelper.width, NTMRenderHelper.height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		
    	GlStateManager.pushMatrix();
    	//GlStateManager.translate(0, Minecraft.getMinecraft().getRenderViewEntity().getEyeHeight(), 0);
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
		
    	for(Runnable r : flashlightQueue){
			r.run();
		}
		flashlightQueue.clear();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		NTMRenderHelper.renderingFlashlights = false;
	}
	
	@Deprecated
	public static void renderFlashLight(Vec3d start, Vec3d end, float degrees, float brightness, ResourceLocation cookie, float partialTicks){
		if(flashlightLock)
			return;
		if(!renderingFlashlights){
			flashlightQueue.add(() -> renderFlashLight(start, end, degrees, brightness, cookie, partialTicks));
			return;
		}
		flashlightLock = true;
		if(!flashlightInit){
			initializeFL();
			flashlightInit = true;
		}
		clearDeferredBuffer();
		double radians = Math.toRadians(degrees);
		Vec3d startToEnd = end.subtract(start);
		double height = startToEnd.length();
		double radius = height * Math.tan(radians);
		AxisAlignedBB box = new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z).grow(radius);
		box = new AxisAlignedBB(
				MathHelper.floor(box.minX / 16.0D) * 16, MathHelper.floor(MathHelper.clamp(box.minY, 0, 255) / 16.0D) * 16, MathHelper.floor(box.minZ / 16.0D) * 16, 
				MathHelper.ceil(box.maxX / 16.0D) * 16, MathHelper.ceil(MathHelper.clamp(box.maxY, 0, 255) / 16.0D) * 16, MathHelper.ceil(box.maxZ / 16.0D) * 16);
		
		List<RenderChunk> toRender = new ArrayList<>();
		List<Entity> entitiesToRender = new ArrayList<>();
		List<TileEntity> tilesToRender = new ArrayList<>();
		Entity renderView = Minecraft.getMinecraft().getRenderViewEntity();
		for(int i = (int) box.minX; i < box.maxX; i += 16){
			for(int j = (int) box.minY; j < box.maxY; j += 16){
				for(int k = (int) box.minZ; k < box.maxZ; k += 16){
					if(!Minecraft.getMinecraft().world.isBlockLoaded(new BlockPos(i, j, k)))
						continue;
					RenderChunk chunk = getRenderChunk(new BlockPos(i, j, k));
					ClassInheritanceMultiMap<Entity> classinheritancemultimap = Minecraft.getMinecraft().world.getChunk(chunk.getPosition()).getEntityLists()[chunk.getPosition().getY() / 16];
					if(Library.isBoxCollidingCone(chunk.boundingBox, start, end, degrees)){
						toRender.add(chunk);
						for(TileEntity te : chunk.compiledChunk.getTileEntities()){
							if(Library.isBoxCollidingCone(te.getRenderBoundingBox(), start, end, degrees)){
								tilesToRender.add(te);
							}
						}
						for(Entity ent : classinheritancemultimap){
							if(ent != renderView && Library.isBoxCollidingCone(ent.getRenderBoundingBox().grow(0.5D), start, end, degrees)){
								entitiesToRender.add(ent);
							}
						}
					}
				}
			}
		}
		Set<TileEntity> globals = Minecraft.getMinecraft().renderGlobal.setTileEntities;
		synchronized(globals){
			for(TileEntity te : globals){
				if(Library.isBoxCollidingCone(te.getRenderBoundingBox(), start, end, degrees)){
					tilesToRender.add(te);
				}
			}
		}
		
		double entPosX = renderView.lastTickPosX + (renderView.posX - renderView.lastTickPosX)*partialTicks;
        double entPosY = renderView.lastTickPosY + (renderView.posY - renderView.lastTickPosY)*partialTicks;
        double entPosZ = renderView.lastTickPosZ + (renderView.posZ - renderView.lastTickPosZ)*partialTicks;
        Vec3d playerPos = new Vec3d(entPosX, entPosY, entPosZ);
        
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(4);
        RenderGlobal.drawSelectionBoundingBox(box.offset(-entPosX, -entPosY, -entPosZ), 1, 1, 1, 1);
        //WHY WON'T THE TESSELLATOR WORK
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(start.x - entPosX, start.y - entPosY, start.z - entPosZ);
        GL11.glVertex3d(end.x - entPosX, end.y - entPosY, end.z - entPosZ);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        
		bindBlockTexture();
		GlStateManager.resetColor();
		GlStateManager.enablePolygonOffset();
		GlStateManager.enableCull();
		GlStateManager.doPolygonOffset(0, 0);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		
		enableBlockVBOs();
		
        clearFLShadowBuffer();
        bindFLShadowBuffer();
        
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        //Multiply by 2 because the FOV should be the diameter. Why is height multiplied by sqrt2? I honestly have no idea, but it doesn't work
        //correctly if I use height directly, and minecraft also multiplies by sqrt2, so I am, too.
        Project.gluPerspective(degrees*2, 1, 0.05F, (float) height*MathHelper.SQRT_2);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        Vec3d angles = BobMathUtil.getEulerAngles(startToEnd.normalize());
       // GL11.glRotated(180, 0, 1, 0);
        GL11.glRotated(-angles.y+270, 1, 0, 0);
        GL11.glRotated(-angles.x+180, 0, 1, 0);
       
        GlStateManager.translate(-(start.x-entPosX), -(start.y-entPosY), -(start.z-entPosZ));
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, ClientProxy.AUX_GL_BUFFER);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER2);
        float[] projecion = new float[16];
        float[] view = new float[16];
        ClientProxy.AUX_GL_BUFFER.get(projecion);
        ClientProxy.AUX_GL_BUFFER2.get(view);
        ClientProxy.AUX_GL_BUFFER.rewind();
        ClientProxy.AUX_GL_BUFFER2.rewind();
        ResourceManager.flashlight_depth.use();
        
        renderChunks(toRender, entPosX, entPosY, entPosZ);
        disableBlockVBOs();
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        for(Entity ent : entitiesToRender){
        	Minecraft.getMinecraft().getRenderManager().renderEntityStatic(ent, partialTicks, false);
        }
        for(TileEntity te : tilesToRender){
        	TileEntityRendererDispatcher.instance.render(te, partialTicks, -1);
        }
        
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        //FloatBuffer pixels = GLAllocation.createDirectFloatBuffer(512*512);
		//GL11.glReadPixels(0, 0, 512, 512, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, pixels);
		//System.out.println(pixels.get(5) + " " + pixels.get(6));
        
        
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, deferredFbo);
        GlStateManager.viewport(0, 0, NTMRenderHelper.width, NTMRenderHelper.height);
        
        if(useFullPost){
        	GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, deferredFbo);
        	
        	ResourceManager.flashlight_post.use();
        	sendFlashLightPostUniforms(ResourceManager.flashlight_post.getShaderId(), playerPos, start, (float)height, brightness, view, projecion, cookie);
        	renderFullscreenTriangle();
        	
        	HbmShaderManager2.releaseShader();
        	Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	        GlStateManager.enableBlend();
	        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
	        GlStateManager.bindTexture(deferredColorTex);
	        ResourceManager.blit.use();
        }
        renderFullscreenTriangle();
        HbmShaderManager2.releaseShader();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        
        //volumetricRender(start, end, playerPos, (float) radius, degrees);
        
        GlStateManager.depthMask(true);
        flashlightLock = false;
	}
	
	@Deprecated
	private static void volumetricRender(Vec3d start, Vec3d end, Vec3d playerPos, float radius, float degrees){
		Vec3d vec = end.subtract(start);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		ResourceManager.cone_volume.use();
		int shader = ResourceManager.cone_volume.getShaderId();
		float height = (float) vec.length();
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "height"), height);
		vec = vec.normalize();
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "cosAngle"), (float) Math.cos(Math.toRadians(degrees)));
		Vec3d pos = start.subtract(playerPos);
		GL20.glUniform3f(GL20.glGetUniformLocation(shader, "pos"), (float) pos.x, (float) pos.y - Minecraft.getMinecraft().getRenderViewEntity().getEyeHeight(), (float) pos.z);
		GL20.glUniform3f(GL20.glGetUniformLocation(shader, "direction"), (float) vec.x, (float) vec.y, (float) vec.z);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
		GlStateManager.bindTexture(shadowFboTex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "shadow"), 3);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);
		GlStateManager.bindTexture(deferredDepthTex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "depth"), 4);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
		ClientProxy.AUX_GL_BUFFER.put(inv_ViewProjectionMatrix);
		ClientProxy.AUX_GL_BUFFER.rewind();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shader, "inv_ViewProjectionMatrix"), false, ClientProxy.AUX_GL_BUFFER);
		
		GlStateManager.enableCull();
		//renderConeMesh(pos, vec, height, radius, 8);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		renderFullscreenTriangle();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
		
		HbmShaderManager2.releaseShader();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
	}
	
	public static void renderConeMesh(Vec3d start, Vec3d normal, float height, float radius, int sides){
		float[] vertices = new float[(1+sides)*3];
		vertices[0] = 0;
		vertices[1] = 0;
		vertices[2] = 0;
		
		Vec3d vertex = new Vec3d(radius, 0, 0);
		for(int i = 0; i < sides; i ++){
			vertex = vertex.rotateYaw((float) (2*Math.PI*(1F/(float)sides)));
			vertices[(i+1)*3] = (float) vertex.x;
			vertices[(i+1)*3+1] = (float) vertex.y-height;
			vertices[(i+1)*3+2] = (float) vertex.z;
		}
		
		GlStateManager.pushMatrix();
		Vec3d angles = BobMathUtil.getEulerAngles(normal);
		GlStateManager.translate(start.x, start.y, start.z);
		GL11.glRotated(angles.x+180, 0, 1, 0);
		GL11.glRotated(angles.y+180, 1, 0, 0);
        
		
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        for(int i = 2; i <= sides; i ++){
        	buf.pos(0, 0, 0).endVertex();
            buf.pos(vertices[(i-1)*3], vertices[(i-1)*3+1], vertices[(i-1)*3+2]).endVertex();
            buf.pos(vertices[i*3], vertices[i*3+1], vertices[i*3+2]).endVertex();
        }
        buf.pos(0, 0, 0).endVertex();
        buf.pos(vertices[sides*3], vertices[sides*3+1], vertices[sides*3+2]).endVertex();
        buf.pos(vertices[1*3], vertices[1*3+1], vertices[1*3+2]).endVertex();
		
        for(int i = 1; i < sides-1; i ++){
        	buf.pos(vertices[3], vertices[3+1], vertices[3+2]).endVertex();
        	buf.pos(vertices[(i+2)*3], vertices[(i+2)*3+1], vertices[(i+2)*3+2]).endVertex();
        	buf.pos(vertices[(i+1)*3], vertices[(i+1)*3+1], vertices[(i+1)*3+2]).endVertex();
        }
        tes.draw();
        
		GlStateManager.popMatrix();
	}
	
	public static void enableBlockVBOs(){
		GlStateManager.glEnableClientState(32884);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(32888);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(32888);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(32886);
	}
	
	public static void disableBlockVBOs(){
		for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
        {
            VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
            int k1 = vertexformatelement.getIndex();

            switch (vertexformatelement$enumusage)
            {
                case POSITION:
                    GlStateManager.glDisableClientState(32884);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                    GlStateManager.glDisableClientState(32888);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;
                case COLOR:
                    GlStateManager.glDisableClientState(32886);
                    GlStateManager.resetColor();
            }
        }
	}	
	
	public static void renderChunks(Collection<RenderChunk> toRender, double posX, double posY, double posZ){
		for(RenderChunk chunk : toRender){
			GlStateManager.pushMatrix();
			BlockPos chunkPos = chunk.getPosition();
			GlStateManager.translate(chunkPos.getX() - posX, chunkPos.getY() - posY, chunkPos.getZ() - posZ);
			chunk.multModelviewMatrix();
			for(int i = 0; i < 3; i ++){
				if(chunk.getCompiledChunk().isLayerEmpty(BlockRenderLayer.values()[i]) || chunk.getVertexBufferByLayer(i) == null)
					continue;
				if(i == 2){
					Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
				}
				VertexBuffer buf = chunk.getVertexBufferByLayer(i);
	            buf.bindBuffer();
	            GlStateManager.glVertexPointer(3, 5126, 28, 0);
	            GlStateManager.glColorPointer(4, 5121, 28, 12);
	            GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
	            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
	            GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
	            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
	            buf.drawArrays(GL11.GL_QUADS);
	            if(i == 2){
	            	Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	            }
			}
			GlStateManager.popMatrix();
		}
	}
	
	@Deprecated
	private static void sendFlashlightUniforms(int shader, Vec3d playerPos, Vec3d pos, Vec3d normal, float height, float degrees, ResourceLocation flashlight_tex){
		pos = pos.subtract(playerPos);
		pos = BobMathUtil.viewFromLocal(new Vector4f((float)pos.x, (float)pos.y, (float)pos.z, 1))[0];
		normal = BobMathUtil.viewFromLocal(new Vector4f((float)normal.x, (float)normal.y, (float)normal.z, 0))[0];
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "angle"), (float) Math.cos(Math.toRadians(degrees)));
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "height"), height);
		GL20.glUniform3f(GL20.glGetUniformLocation(shader, "pos"), (float)pos.x, (float)pos.y, (float)pos.z);
		GL20.glUniform3f(GL20.glGetUniformLocation(shader, "direction"), (float)normal.x, (float)normal.y, (float)normal.z);
		//GL20.glUniform2f(GL20.glGetUniformLocation(shader, "screenSize"), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
		GlStateManager.bindTexture(shadowFboTex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "shadowTex"), 3);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);
		bindTexture(flashlight_tex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "flashlightTex"), 4);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
	}
	
	@Deprecated
	private static void sendFlashLightPostUniforms(int shader, Vec3d playerPos, Vec3d pos, float height, float brightness, float[] shadowView, float[] shadowProjection, ResourceLocation flashlight_tex){
		pos = pos.subtract(playerPos);
		//pos = BobMathUtil.viewFromLocal(new Vector4f((float)pos.x, (float)pos.y, (float)pos.z, 1))[0];
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "height"), height);
		GL20.glUniform3f(GL20.glGetUniformLocation(shader, "fs_Pos"), (float)pos.x, (float)pos.y, (float)pos.z);
		GL20.glUniform2f(GL20.glGetUniformLocation(shader, "zNearFar"), 0.05F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2);
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "eyeHeight"), Minecraft.getMinecraft().player.getEyeHeight());
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "brightness"), brightness);
		
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
		GlStateManager.bindTexture(Minecraft.getMinecraft().getFramebuffer().framebufferTexture);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "mc_tex"), 3);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);
		GlStateManager.bindTexture(deferredDepthTex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "depthBuffer"), 4);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE5);
		bindTexture(flashlight_tex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "flashlightTex"), 5);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE6);
		GlStateManager.bindTexture(shadowFboTex);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "shadowTex"), 6);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
		
		Matrix4f view = new Matrix4f();
		Matrix4f proj = new Matrix4f();
		
		ClientProxy.AUX_GL_BUFFER.put(shadowView);
		ClientProxy.AUX_GL_BUFFER2.put(shadowProjection);
		ClientProxy.AUX_GL_BUFFER.rewind();
		ClientProxy.AUX_GL_BUFFER2.rewind();
		view.load(ClientProxy.AUX_GL_BUFFER);
		proj.load(ClientProxy.AUX_GL_BUFFER2);
		ClientProxy.AUX_GL_BUFFER.rewind();
		ClientProxy.AUX_GL_BUFFER2.rewind();
		Matrix4f.mul(proj, view, view);
		view.store(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shader, "flashlight_ViewProjectionMatrix"), false, ClientProxy.AUX_GL_BUFFER);
		
		ClientProxy.AUX_GL_BUFFER.put(inv_ViewProjectionMatrix);
		ClientProxy.AUX_GL_BUFFER.rewind();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shader, "inv_ViewProjectionMatrix"), false, ClientProxy.AUX_GL_BUFFER);
		
	}

	public static RenderChunk getRenderChunk(BlockPos pos){
		return Minecraft.getMinecraft().renderGlobal.viewFrustum.getRenderChunk(pos);
	}
	
	public static void renderFullscreenTriangle(){
		renderFullscreenTriangle(false);
	}
	
	public static void renderFullscreenTriangle(boolean alpha){
		GlStateManager.colorMask(true, true, true, alpha);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();

        GlStateManager.enableColorMaterial();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        NTMBufferBuilder bufferbuilder = NTMImmediate.INSTANCE.beginPositionTex(GL11.GL_TRIANGLES, 3);
        bufferbuilder.appendPositionTex(-1, -1, 0.0F, 0, 0);
        bufferbuilder.appendPositionTex(3, -1, 0.0F, 2, 0);
        bufferbuilder.appendPositionTex(-1, 3, 0.0F, 0, 2);
        NTMImmediate.INSTANCE.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.colorMask(true, true, true, true);
	}
	
	public static void resetParticleInterpPos(Entity entityIn, float partialTicks){
		double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
        
        Particle.interpPosX = entPosX;
        Particle.interpPosY = entPosY;
        Particle.interpPosZ = entPosZ;
	}
	
	public static float[] project(float x, float y, float z){
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
		GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
		
		Project.gluProject(x, y, z, MODELVIEW, PROJECTION, VIEWPORT, POSITION);
		return new float[]{POSITION.get(0), POSITION.get(1), POSITION.get(2)};
	}
	
	public static float[] unproject(float x, float y, float z){
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
		GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
		
		Project.gluUnProject(x, y, z, MODELVIEW, PROJECTION, VIEWPORT, POSITION);
		return new float[]{POSITION.get(0), POSITION.get(1), POSITION.get(2)};
	}
	
	public static Vec3d unproject_world(float[] inv_mvp, float x, float y, float z){
		Matrix4f mat = new Matrix4f();
		ClientProxy.AUX_GL_BUFFER.put(inv_mvp);
		ClientProxy.AUX_GL_BUFFER.rewind();
		mat.load(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		
		Vector4f ndcPos = new Vector4f();
		ndcPos.x = (2F*x)/(Minecraft.getMinecraft().displayWidth) - 1;
		ndcPos.y = (2F*y)/(Minecraft.getMinecraft().displayHeight) - 1;
		float near = 0;
		float far = 1;
		ndcPos.z = (2*z - near - far)/(far-near);
		ndcPos.w = 1;
		
		Matrix4f.transform(mat, ndcPos, ndcPos);
		float invW = 1F/ndcPos.w;
		Vector3f worldPos = new Vector3f(ndcPos.x*invW, ndcPos.y*invW, ndcPos.z*invW);
		
		Entity ent = Minecraft.getMinecraft().getRenderViewEntity();
		float partialTicks = MainRegistry.proxy.partialTicks();
		double rPosX = ent.prevPosX + (ent.posX-ent.prevPosX)*partialTicks;
		double rPosY = ent.prevPosY + (ent.posY-ent.prevPosY)*partialTicks;
		double rPosZ = ent.prevPosZ + (ent.posZ-ent.prevPosZ)*partialTicks;
		//Vec3d eyePos = ActiveRenderInfo.getCameraPosition();
		
		return new Vec3d(worldPos.x + rPosX, worldPos.y + rPosY, worldPos.z + rPosZ);
	}
	
	public static boolean intersects2DBox(float x, float y, float[] box){
		return x > box[0] && x < box[2] && y > box[1] && y < box[3];
	}
	
	public static boolean boxesOverlap(float[] box1, float[] box2){
		return box1[0] < box2[2] && box1[2] > box2[0] && box1[1] < box2[3] && box1[3] > box2[1];
	}
	
	public static boolean boxContainsOther(float[] box, float[] other){
		return box[0] <= other[0] && box[1] <= other[1] && box[2] >= other[2] && box[3] >= other[3];
	}
	
	public static float[] getBoxCenter(float[] box){
		return new float[]{box[0]+(box[2]-box[0])*0.5F, box[1]+(box[3]-box[1])*0.5F};
	}



}
