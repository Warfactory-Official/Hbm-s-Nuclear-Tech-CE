package com.hbm.render.util;

import com.hbm.handler.HbmShaderManager2;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import com.hbm.util.BobMathUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nullable;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BakedModelUtil {

	private static BufferBuilder buffer = new BufferBuilder(1024*16);
	private static final int BYTES_PER_VERTEX = 4*3 + 4*1 + 4*2 + 2*2 + 2*2;
	
	public static enum DecalType {
		REGULAR,
		VBO,
		FLOW;
	}
	
	public static int[] generateDecalMesh(World world, Vec3d normal, float scale, float offsetX, float offsetY, float offsetZ, DecalType type){
		return generateDecalMesh(world, normal, scale, offsetX, offsetY, offsetZ, type, null);
	}
	
	public static int[] generateDecalMesh(World world, Vec3d normal, float scale, float offsetX, float offsetY, float offsetZ, DecalType type, @Nullable ResourceLocation texture, int... data){
		Vec3d euler = BobMathUtil.getEulerAngles(normal);
		float roll = world.rand.nextFloat()*2F*(float)Math.PI;
		Matrix3f rot = eulerToMat((float)Math.toRadians(euler.x), (float)Math.toRadians(euler.y+90), roll);
		Vec3d c1 = new Vec3d(rot.m00, rot.m01, rot.m02);
		Vec3d c2 = new Vec3d(rot.m10, rot.m11, rot.m12);
		Vec3d c3 = new Vec3d(rot.m20, rot.m21, rot.m22);
		//System.out.println(c1);
		//System.out.println(c2);
		//System.out.println(c3);
		//System.out.println(normal);
		//System.out.println();
		
		float[][] planes = new float[6][4];
		planes[0] = new float[]{(float)c1.x, (float)c1.y, (float)c1.z, scale};
		planes[1] = new float[]{(float)-c1.x, (float)-c1.y, (float)-c1.z, scale};
		planes[2] = new float[]{(float)c2.x, (float)c2.y, (float)c2.z, scale};
		planes[3] = new float[]{(float)-c2.x, (float)-c2.y, (float)-c2.z, scale};
		planes[4] = new float[]{(float)c3.x, (float)c3.y, (float)c3.z, scale*3};
		planes[5] = new float[]{(float)-c3.x, (float)-c3.y, (float)-c3.z, scale*3};
		
		AxisAlignedBB box = getBox(c1.scale(scale), c2.scale(scale), c3.scale(scale*3)).offset(offsetX, offsetY, offsetZ);
		
		List<Triangle> tris = new ArrayList<>();
		for(int i = (int) Math.floor(box.minX); i <= Math.ceil(box.maxX); i ++){
			for(int j = (int) Math.floor(box.minY); j <= Math.ceil(box.maxY); j ++){
				for(int k = (int) Math.floor(box.minZ); k <= Math.ceil(box.maxZ); k ++){
					BlockPos pos = new BlockPos(i, j, k);
					IBlockState state = world.getBlockState(pos);
					state = state.getActualState(world, pos);
					if(state.getRenderType() != EnumBlockRenderType.MODEL)
						continue;
					IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
					List<Triangle> block_tris = triangulateBlockModel(world, pos, model, state, MathHelper.getPositionRandom(pos), -offsetX, -offsetY, -offsetZ, true);
					Iterator<Triangle> itr = block_tris.iterator();
					while(itr.hasNext()){
						Triangle t = itr.next();
						Vec3d tnorm = t.v2.pos.subtract(t.v1.pos).crossProduct(t.v3.pos.subtract(t.v1.pos)).normalize();
						if(tnorm.dotProduct(normal) > -0.2)
							itr.remove();
					}
					List<Triangle> newTris;
					for(int p = 0; p < planes.length; p ++){
						newTris = new ArrayList<>();
						float[] plane = planes[p];
						for(Triangle t : block_tris){
							Triangle[][] clipped = planeClipTriangle(t, plane);
							if(clipped[0][0] != null)
								newTris.add(clipped[0][0]);
							if(clipped[0][1] != null)
								newTris.add(clipped[0][1]);
						}
						block_tris = newTris;
					}
					tris.addAll(block_tris);
				}
			}
		}
		int[] geo = new int[]{-1, -1};
		if(type == DecalType.VBO){
			geo[0] = GLCompat.genBuffers();
			geo[1] = tris.size()*3;
			GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, geo[0]);
			ByteBuffer buf = buffer.getByteBuffer();
			buf.clear();
			for(Triangle t : tris){
				if(BYTES_PER_VERTEX*3 > buf.remaining()){
					continue;
				}
				for(Vertex v : t.vertices()){
					buf.putFloat((float)v.pos.x);
					buf.putFloat((float) v.pos.y);
					buf.putFloat((float)v.pos.z);
					buf.put((byte)((int)(v.r*255F)));
					buf.put((byte)((int)(v.g*255F)));
					buf.put((byte)((int)(v.b*255F)));
					buf.put((byte)((int)(v.a*255F)));
					buf.putFloat(v.u);
					buf.putFloat(v.v);
					buf.putShort((short)(v.lmapU*65535));
					buf.putShort((short)(v.lmapV*65535));
					Vector3f projTex = new Vector3f((float)v.pos.x, (float)v.pos.y, (float)v.pos.z);
					rot.transform(projTex);
					projTex.x = projTex.x*0.5F/scale + 0.5F;
					projTex.y = projTex.y*0.5F/scale + 0.5F;
					buf.putShort((short) (projTex.x*65535));
					buf.putShort((short) (projTex.y*65535));
				}
			}
			buf.flip();
			GLCompat.bufferData(GLCompat.GL_ARRAY_BUFFER, buf, GLCompat.GL_STATIC_DRAW);
			buf.clear();
			GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
		} else {
			if(type == DecalType.FLOW){
				geo = new int[7];
				if(texture == null){
					throw new RuntimeException("Null texture");
				}
			}
			//Render into display list
			geo[0] = GlStateManager.glGenLists(1);
			GlStateManager.glNewList(geo[0], GL11.GL_COMPILE);
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buf = tes.getBuffer();
			buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
			for(Triangle t : tris){
				Vec3d norm = t.v2.pos.subtract(t.v1.pos).crossProduct(t.v3.pos.subtract(t.v1.pos)).normalize();
				Vector3f tex1 = new Vector3f((float)t.v1.pos.x, (float)t.v1.pos.y, (float)t.v1.pos.z);
				rot.transform(tex1);
				Vector3f tex2 = new Vector3f((float)t.v2.pos.x, (float)t.v2.pos.y, (float)t.v2.pos.z);
				rot.transform(tex2);
				Vector3f tex3 = new Vector3f((float)t.v3.pos.x, (float)t.v3.pos.y, (float)t.v3.pos.z);
				rot.transform(tex3);
				float invScale = 1F/scale;
				buf.pos(t.v1.pos.x, t.v1.pos.y, t.v1.pos.z).tex(tex1.x*0.5*invScale+0.5, tex1.y*0.5*invScale+0.5).normal((float)norm.x, (float)norm.y, (float)norm.z).endVertex();
				buf.pos(t.v2.pos.x, t.v2.pos.y, t.v2.pos.z).tex(tex2.x*0.5*invScale+0.5, tex2.y*0.5*invScale+0.5).normal((float)norm.x, (float)norm.y, (float)norm.z).endVertex();
				buf.pos(t.v3.pos.x, t.v3.pos.y, t.v3.pos.z).tex(tex3.x*0.5*invScale+0.5, tex3.y*0.5*invScale+0.5).normal((float)norm.x, (float)norm.y, (float)norm.z).endVertex();
				
				//Vec3d tangent = new Vec3d(1, 0, 0).crossProduct(c1);
				//Vector3f test1 = new Vector3f(0, 1, 0);
				//Vector3f test2 = new Vector3f((float)norm.x, (float)norm.y, (float)norm.z);
				//rot.transform(test1);
				//rot.transform(test2);
				//System.out.println(test1 + " " + test2);
				//System.out.println(tangent + " " + norm.crossProduct(c2));
				//System.out.println(tangent.normalize().dotProduct(new Vec3d(0, -1, 0)));
				//System.out.println();
			}
			tes.draw();
			GlStateManager.glEndList();
			if(type == DecalType.FLOW){
				Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				int texId = Minecraft.getMinecraft().getTextureManager().getTexture(texture).getGlTextureId();
				int width = geo[5] = GlStateManager.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
				int height = geo[6] = GlStateManager.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
				
				int fbo = geo[1] = GLCompat.genFramebuffers();
				int fbo2 = geo[3] = GLCompat.genFramebuffers();
				int gravmap = geo[2] = GlStateManager.generateTexture();
				int gravmap2 = geo[4] = GlStateManager.generateTexture();
				
				int depth = GLCompat.genRenderbuffers();
				GLCompat.bindRenderbuffer(GLCompat.GL_RENDERBUFFER, depth);
				GLCompat.renderbufferStorage(GLCompat.GL_RENDERBUFFER, GLCompat.GL_DEPTH_COMPONENT24, width, height);
				
				GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, fbo);
				GlStateManager.bindTexture(gravmap);
				GlStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (IntBuffer)null);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
				GLCompat.framebufferTexture2D(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, gravmap, 0);
				GLCompat.framebufferRenderbuffer(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GLCompat.GL_RENDERBUFFER, depth);
				
				GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, fbo2);
				GlStateManager.bindTexture(gravmap2);
				GlStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (IntBuffer)null);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
				GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
				GLCompat.framebufferTexture2D(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, gravmap2, 0);
				GLCompat.framebufferRenderbuffer(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GLCompat.GL_RENDERBUFFER, depth);
				
				
				GlStateManager.colorMask(true, true, true, true);
		        GlStateManager.enableDepth();
		        GlStateManager.depthMask(true);
		        GlStateManager.enableTexture2D();
		        GlStateManager.disableLighting();
		        GlStateManager.disableAlpha();
		        GlStateManager.disableCull();
		        
		        GlStateManager.enableColorMaterial();

		        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				
		        if(data.length > 0){
		        	int texIdx = data[0];
		        	int rows = data[1];
		        	GlStateManager.matrixMode(GL11.GL_TEXTURE);
					GlStateManager.pushMatrix();
					GlStateManager.loadIdentity();
					float size = 1F/rows;
			        float u = (texIdx%rows)*size;
			        float v = (texIdx/4)*size;
			        GlStateManager.translate(u, v, 0);
			        GlStateManager.scale(size, size, 1);
					GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		        }
		        
				GlStateManager.viewport(0, 0, width, height);
				GlStateManager.bindTexture(texId);
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				ResourceManager.gravitymap_render.use();
				
				//Don't really know why this needs a negative roll but ok.
				Matrix3f rot2 = eulerToMat((float)Math.toRadians(euler.x), (float)Math.toRadians(euler.y+90), 0);
				Matrix3f mR = new Matrix3f();
				mR.rotZ(-roll);
				mR.mul(rot2);
				Vec3d tanDir = new Vec3d(mR.m10, mR.m11, mR.m12);
				
				ResourceManager.gravitymap_render.uniform3f("tanDirection", (float)tanDir.x, (float)tanDir.y, (float)tanDir.z);
				Vector3f gravity = new Vector3f(0, 0.5F, 0);
				//rot.transform(gravity);
				ResourceManager.gravitymap_render.uniform3f("gravity", gravity.x, gravity.y, gravity.z);
				for(int i = 0; i < 3; i ++)
					for(int j = 0; j < 3; j ++)
						ClientProxy.AUX_GL_BUFFER.put(rot.getElement(i, j));
				ClientProxy.AUX_GL_BUFFER.flip();
				ResourceManager.gravitymap_render.uniformMatrix3("matrix", false, ClientProxy.AUX_GL_BUFFER);
				ClientProxy.AUX_GL_BUFFER.clear();
				GlStateManager.clearDepth(1);
				GlStateManager.clearColor(0.5F, 0.5F, 0F, 1F);
				GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
				GlStateManager.callList(geo[0]);
				//RenderHelper.renderFullscreenTriangle(true);
				
				GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, fbo);
				GlStateManager.clearDepth(1);
				GlStateManager.clearColor(0.5F, 0.5F, 0F, 1F);
				GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
				GlStateManager.callList(geo[0]);
				//RenderHelper.renderFullscreenTriangle(true);
				HbmShaderManager2.releaseShader();
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GlStateManager.popMatrix();
				
				GLCompat.framebufferRenderbuffer(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GLCompat.GL_RENDERBUFFER, 0);
				GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, fbo2);
				GLCompat.framebufferRenderbuffer(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GLCompat.GL_RENDERBUFFER, 0);
				GLCompat.deleteRenderbuffers(depth);
				
				if(data.length > 0){
					GlStateManager.matrixMode(GL11.GL_TEXTURE);
					GlStateManager.popMatrix();
					GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				}
				
				GlStateManager.depthMask(true);
		        GlStateManager.enableDepth();
		        GlStateManager.enableAlpha();
		        GlStateManager.enableCull();
		        //GlStateManager.enableLighting();
		        GlStateManager.colorMask(true, true, true, true);
				
				Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
				
			}
		}
		
		return geo;
	}
	
	public static void enableBlockShaderVBOs(){
		GLCompat.vertexAttribPointer(0, 3, GL11.GL_FLOAT, false, BYTES_PER_VERTEX, 0);
		GLCompat.enableVertexAttribArray(0);
		//Color
		GLCompat.vertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE, true, BYTES_PER_VERTEX, 12);
		GLCompat.enableVertexAttribArray(1);
		//Texcoord
		GLCompat.vertexAttribPointer(3, 2, GL11.GL_FLOAT, false, BYTES_PER_VERTEX, 16);
		GLCompat.enableVertexAttribArray(3);
		//Lmap texcoord
		GLCompat.vertexAttribPointer(4, 2, GL11.GL_UNSIGNED_SHORT, true, BYTES_PER_VERTEX, 24);
		GLCompat.enableVertexAttribArray(4);
		//Projected texcoord
		GLCompat.vertexAttribPointer(5, 2, GL11.GL_UNSIGNED_SHORT, true, BYTES_PER_VERTEX, 28);
		GLCompat.enableVertexAttribArray(5);
	}
	
	public static void disableBlockShaderVBOs(){
		GLCompat.disableVertexAttribArray(0);
		GLCompat.disableVertexAttribArray(1);
		GLCompat.disableVertexAttribArray(3);
		GLCompat.disableVertexAttribArray(4);
		GLCompat.disableVertexAttribArray(5);
	}
	
	public static AxisAlignedBB getBox(Vec3d a, Vec3d b, Vec3d c){
		double x = Math.max(Math.max(Math.abs(a.x), Math.abs(b.x)), Math.abs(c.x));
		double y = Math.max(Math.max(Math.abs(a.y), Math.abs(b.y)), Math.abs(c.y));
		double z = Math.max(Math.max(Math.abs(a.z), Math.abs(b.z)), Math.abs(c.z));
		
		return new AxisAlignedBB(-x, -y, -z, x, y, z);
	}
	
	public static Matrix3f normalToMat(Vec3d normal, float roll){
		Vec3d euler = BobMathUtil.getEulerAngles(normal);
		return eulerToMat((float)Math.toRadians(euler.x), (float)Math.toRadians(euler.y+90), roll);
	}
	
	public static Matrix3f eulerToMat(float yaw, float pitch, float roll){
		//Could this be more optimized? Yeah, but it's not like I'm calling it that often.
		Matrix3f mY = new Matrix3f();
		mY.rotY(-yaw);
		Matrix3f mP = new Matrix3f();
		mP.rotX(pitch);
		Matrix3f mR = new Matrix3f();
		mR.rotZ(roll);
		mR.mul(mP);
		mR.mul(mY);
		return mR;
	}
	
	public static List<Triangle> triangulateBlockModel(IBakedModel b, IBlockState state, long rand, float offsetX, float offsetY, float offsetZ){
		return triangulateBlockModel(null, null, b, state, rand, offsetX, offsetY, offsetZ, false);
	}
	
	public static List<Triangle> triangulateBlockModel(World world, BlockPos pos, IBakedModel b, IBlockState state, long rand, float offsetX, float offsetY, float offsetZ, boolean checkSides){
		List<Triangle> tris = new ArrayList<>();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, b, state, pos, buffer, true);
		buffer.finishDrawing();
		IntBuffer buf = buffer.getByteBuffer().asIntBuffer();
		int[] vertexData = new int[DefaultVertexFormats.BLOCK.getIntegerSize()*4];
		for(int i = 0; i < buf.limit(); i += DefaultVertexFormats.BLOCK.getIntegerSize()*4){
			buf.get(vertexData);
			Vertex v0 = new Vertex(vertexData, 0, offsetX, offsetY, offsetZ);
			Vertex v1 = new Vertex(vertexData, 1, offsetX, offsetY, offsetZ);
			Vertex v2 = new Vertex(vertexData, 2, offsetX, offsetY, offsetZ);
			Vertex v3 = new Vertex(vertexData, 3, offsetX, offsetY, offsetZ);
			tris.add(new Triangle(v0, v1, v2));
			tris.add(new Triangle(v2, v3, v0));
		}
		
		/*List<BakedQuad> l = new ArrayList<>();
		for(EnumFacing e : EnumFacing.VALUES){
			if(!checkSides || state.shouldSideBeRendered(world, pos, e)){
				l.addAll(b.getQuads(state, e, rand));
			}
		}
		l.addAll(b.getQuads(state, null, rand));
		
		for(BakedQuad quad : l){
			int[] vertexData = quad.getVertexData();
			Vertex v0 = new Vertex(vertexData, 0, offsetX, offsetY, offsetZ);
			Vertex v1 = new Vertex(vertexData, 1, offsetX, offsetY, offsetZ);
			Vertex v2 = new Vertex(vertexData, 2, offsetX, offsetY, offsetZ);
			Vertex v3 = new Vertex(vertexData, 3, offsetX, offsetY, offsetZ);
			tris.add(new Triangle(v0, v1, v2));
			tris.add(new Triangle(v2, v3, v0));
		}*/
		
		return tris;
	}
	
	public static Triangle[][] planeClipTriangle(Triangle t, float[] plane){
		Triangle[][] clipped = new Triangle[2][2];
		clipped[0] = new Triangle[]{null, null};
		clipped[1] = new Triangle[]{null, null};
		//Clip each triangle to the plane.
		//TODO move this to a generic helper method for clipping triangles?
		boolean p1 = t.v1.pos.x*plane[0]+t.v1.pos.y*plane[1]+t.v1.pos.z*plane[2]+plane[3] > 0;
		boolean p2 = t.v2.pos.x*plane[0]+t.v2.pos.y*plane[1]+t.v2.pos.z*plane[2]+plane[3] > 0;
		boolean p3 = t.v3.pos.x*plane[0]+t.v3.pos.y*plane[1]+t.v3.pos.z*plane[2]+plane[3] > 0;
		//If all points on positive side, add to side 1 
		if(p1 && p2 && p3){
			clipped[0][0] = t;
		//else if all on negative side, add to size 2
		} else if(!p1 && !p2 && !p3){
			clipped[1][0] = t;
		//else if only one is positive, clip and add 1 triangle to side 1, 2 to side 2
		} else if(p1 ^ p2 ^ p3){
			Vertex a, b, c;
			if(p1){
				a = t.v1;
				b = t.v2;
				c = t.v3;
			} else if(p2){
				a = t.v2;
				b = t.v3;
				c = t.v1;
			} else {
				a = t.v3;
				b = t.v1;
				c = t.v2;
			}
			Vec3d rAB = b.pos.subtract(a.pos);
			Vec3d rAC = c.pos.subtract(a.pos);
			float interceptAB = (float) rayPlaneIntercept(a.pos, rAB, plane);
			float interceptAC = (float) rayPlaneIntercept(a.pos, rAC, plane);
			Vertex d = a.lerp(b, interceptAB);
			Vertex e = a.lerp(c, interceptAC);
			
			clipped[1][0] = new Triangle(d, b, e);
			clipped[1][1] = new Triangle(b, c, e);
			clipped[0][0] = new Triangle(a, d, e);
			
		//else one is negative, clip and add 2 triangles to side 1, 1 to side 2.
		} else {
			Vertex a, b, c;
			if(!p1){
				a = t.v1;
				b = t.v2;
				c = t.v3;
			} else if(!p2){
				a = t.v2;
				b = t.v3;
				c = t.v1;
			} else {
				a = t.v3;
				b = t.v1;
				c = t.v2;
			}
			//Duplicated code. Eh, I don't feel like redesigning this.
			Vec3d rAB = b.pos.subtract(a.pos);
			Vec3d rAC = c.pos.subtract(a.pos);
			float interceptAB = (float) rayPlaneIntercept(a.pos, rAB, plane);
			float interceptAC = (float) rayPlaneIntercept(a.pos, rAC, plane);
			Vertex d = a.lerp(b, interceptAB);
			Vertex e = a.lerp(c, interceptAC);
			
			clipped[0][0] = new Triangle(d, b, e);
			clipped[0][1] = new Triangle(b, c, e);
			clipped[1][0] = new Triangle(a, d, e);
		}
		return clipped;
	}
	
	public static double rayPlaneIntercept(Vec3d start, Vec3d ray, float[] plane){
		double num = -(plane[0]*start.x + plane[1]*start.y + plane[2]*start.z + plane[3]);
		double denom = plane[0]*ray.x + plane[1]*ray.y + plane[2]*ray.z;
		return num/denom;
	}
	
	public static class Vertex {
		public Vec3d pos;
		public float a, r, g, b;
		public float u, v;
		public float lmapU;
		public float lmapV;
		
		public Vertex(int[] vertexData, int offset, float oX, float oY, float oZ) {
			offset *= 7;
			float x = Float.intBitsToFloat(vertexData[0+offset])+oX;
			float y = Float.intBitsToFloat(vertexData[1+offset])+oY;
			float z = Float.intBitsToFloat(vertexData[2+offset])+oZ;
			pos = new Vec3d(x, y, z);
			int color = vertexData[3+offset];
			a = ((color >> 24) & 255) / 255F;
			b = ((color >> 16) & 255) / 255F;
			g = ((color >> 8) & 255) / 255F;
			r = ((color) & 255) / 255F;
			u = Float.intBitsToFloat(vertexData[4+offset]);
			v = Float.intBitsToFloat(vertexData[5+offset]);
			int i = vertexData[6+offset];
			int j = i >>> 16 & 65535;
	        int k = i & 65535;
	        lmapU = j/255F;
	        lmapV = k/255F;
		}
		
		public Vertex() {
		}
		
		public Vertex lerp(Vertex other, float amount){
			Vertex l = new Vertex();
			l.pos = pos.add(other.pos.subtract(pos).scale(amount));
			l.a = a + (other.a-a)*amount;
			l.r = r + (other.r-r)*amount;
			l.g = g + (other.g-g)*amount;
			l.b = b + (other.b-b)*amount;
			l.u = u + (other.u-u)*amount;
			l.v = v + (other.v-v)*amount;
			l.lmapU = lmapU + (other.lmapU-lmapU)*amount;
			l.lmapV = lmapV + (other.lmapV-lmapV)*amount;
			return l;
		}
	}
	
	public static class Triangle {
		
		public Vertex v1, v2, v3;
		
		public Triangle(Vertex v1, Vertex v2, Vertex v3) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
		}
		
		public Vertex[] vertices(){
			return new Vertex[]{v1, v2, v3};
		}
	}
}
