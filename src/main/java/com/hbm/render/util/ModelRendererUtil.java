package com.hbm.render.util;

import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.particle.ParticleSlicedMob;
import com.hbm.physics.Collider;
import com.hbm.physics.ConvexMeshCollider;
import com.hbm.physics.GJK;
import com.hbm.physics.RigidBody;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.util.Triangle.TexVertex;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import javax.annotation.Nullable;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ModelRendererUtil {

	//Need to call this because things like bats do extra scaling
	public static final MethodHandle rGetEntityTexture = MethodHandleHelper.findVirtual(Render.class, "getEntityTexture", "func_110775_a", MethodType.methodType(ResourceLocation.class, Entity.class));
	public static final MethodHandle rHandleRotationFloat = MethodHandleHelper.findVirtual(RenderLivingBase.class, "handleRotationFloat", "func_77044_a", MethodType.methodType(float.class, EntityLivingBase.class, float.class));
	public static final MethodHandle rApplyRotations = MethodHandleHelper.findVirtual(RenderLivingBase.class, "applyRotations", "func_77043_a", MethodType.methodType(void.class, EntityLivingBase.class, float.class, float.class, float.class));

    public static @NotNull <T extends Entity> ResourceLocation getEntityTexture(T e) {
        Render<T> eRenderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(e);
        return getEntityTexture(e, eRenderer);
    }

    public static @NotNull <T extends Entity> ResourceLocation getEntityTexture(T e, Render<T> eRenderer) {
        ResourceLocation r = null;
        try {
            r = (ResourceLocation) rGetEntityTexture.invokeExact(eRenderer, e);
        } catch(Throwable e1) {
            MainRegistry.logger.catching(e1);
        }
        return r == null ? ResourceManager.turbofan_blades_tex : r;
    }

    public static <T extends EntityLivingBase> List<Pair<Matrix4f, ModelRenderer>> getBoxesFromMob(T e){
		Render<T> eRenderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(e);
		if(eRenderer instanceof RenderLivingBase && e instanceof EntityLivingBase) {
			return getBoxesFromMob(e, ((RenderLivingBase<T>) eRenderer), MainRegistry.proxy.partialTicks());
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("deprecation")
	private static <T extends EntityLivingBase> List<Pair<Matrix4f, ModelRenderer>> getBoxesFromMob(T e, RenderLivingBase<T> render, float partialTicks) {
		ModelBase model = render.getMainModel();
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();
		//So basically we're just going to copy vanialla methods so the 
		model.swingProgress = e.getSwingProgress(partialTicks);
		boolean shouldSit = e.isRiding() && (e.getRidingEntity() != null && e.getRidingEntity().shouldRiderSit());
		model.isRiding = shouldSit;
		model.isChild = e.isChild();
		float f = interpolateRotation(e.prevRenderYawOffset, e.renderYawOffset, partialTicks);
		float f1 = interpolateRotation(e.prevRotationYawHead, e.rotationYawHead, partialTicks);
		float f2 = f1 - f;
		if(shouldSit && e.getRidingEntity() instanceof EntityLivingBase) {
			EntityLivingBase elivingbase = (EntityLivingBase) e.getRidingEntity();
			f = interpolateRotation(elivingbase.prevRenderYawOffset, elivingbase.renderYawOffset, partialTicks);
			f2 = f1 - f;
			float f3 = MathHelper.wrapDegrees(f2);

			if(f3 < -85.0F) {
				f3 = -85.0F;
			}

			if(f3 >= 85.0F) {
				f3 = 85.0F;
			}

			f = f1 - f3;

			if(f3 * f3 > 2500.0F) {
				f += f3 * 0.2F;
			}

			f2 = f1 - f;
		}

		float f7 = e.prevRotationPitch + (e.rotationPitch - e.prevRotationPitch) * partialTicks;
		//renderLivingAt(e, x, y, z);
		//float f8 = e.ticksExisted + partialTicks;
		//GlStateManager.rotate(180.0F - f, 0.0F, 1.0F, 0.0F);
		//if(rPreRenderCallback == null){
		//	rPreRenderCallback = ReflectionHelper.findMethod(RenderLivingBase.class, "preRenderCallback", "func_77041_b", EntityLivingBase.class, float.class);
		//}
		//float f4 = prepareScale(e, partialTicks, render);
		
		float f8 = 0;
		try {
			f8 = (float)rHandleRotationFloat.invokeExact(render, e, partialTicks);
			rApplyRotations.invokeExact(render, e, f8, f, partialTicks);
		} catch(Throwable x){
            MainRegistry.logger.catching(x);
		}
		
        //this.applyRotations(entity, f8, f, partialTicks);
		float f4 = render.prepareScale(e, partialTicks);
		float f5 = 0.0F;
		float f6 = 0.0F;
		if(!e.isRiding()) {
			f5 = e.prevLimbSwingAmount + (e.limbSwingAmount - e.prevLimbSwingAmount) * partialTicks;
			f6 = e.limbSwing - e.limbSwingAmount * (1.0F - partialTicks);

			if(e.isChild()) {
				f6 *= 3.0F;
			}

			if(f5 > 1.0F) {
				f5 = 1.0F;
			}
			f2 = f1 - f; // Forge: Fix MC-1207
		}
		model.setLivingAnimations(e, f6, f5, partialTicks);
		model.setRotationAngles(f6, f5, f8, f2, f7, f4, e);
		ResourceLocation r = getEntityTexture(e, render);
		List<Pair<Matrix4f, ModelRenderer>> list = new ArrayList<>();
		for(ModelRenderer renderer : model.boxList) {
			if(!isChild(renderer, model.boxList))
				generateList(e.world, e, f4, list, renderer, r);
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
		return list;
	}
	
	public static boolean isChild(ModelRenderer r, List<ModelRenderer> list){
		for(ModelRenderer r2 : list){
			if(r2.childModels != null && r2.childModels.contains(r))
				return true;
		}
		return false;
	}
	
	protected static void generateList(World world, EntityLivingBase ent, float scale, List<Pair<Matrix4f, ModelRenderer>> list, ModelRenderer render, ResourceLocation tex){
        //note for !render.compiled:
        //A lot of mobs weirdly replace model renderers and end up with extra ones in the list that aren't ever rendered.
        //Since they're not rendered, they should never be compiled, so this hack tries to detect that.
        //Not the greatest method ever, but it appears to work.
		if(render.isHidden || !render.showModel || !render.compiled)
			return;
		GlStateManager.pushMatrix();
		doTransforms(render, scale);
		if(render.childModels != null)
			for(ModelRenderer renderer : render.childModels) {
				generateList(world, ent, scale, list, renderer, tex);
			}
		GL11.glScaled(scale, scale, scale);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
		Matrix4f mat = new Matrix4f();
		mat.load(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		list.add(Pair.of(mat, render));
		GlStateManager.popMatrix();
	}
	
	public static void doTransforms(ModelRenderer m, float scale) {
		GlStateManager.translate(m.offsetX, m.offsetY, m.offsetZ);
		if(m.rotateAngleX == 0.0F && m.rotateAngleY == 0.0F && m.rotateAngleZ == 0.0F) {
			if(m.rotationPointX == 0.0F && m.rotationPointY == 0.0F && m.rotationPointZ == 0.0F) {
			} else {
				GlStateManager.translate(m.rotationPointX * scale, m.rotationPointY * scale, m.rotationPointZ * scale);
			}
		} else {
			GlStateManager.translate(m.rotationPointX * scale, m.rotationPointY * scale, m.rotationPointZ * scale);
			if(m.rotateAngleZ != 0.0F) {
				GlStateManager.rotate(m.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
			}
			if(m.rotateAngleY != 0.0F) {
				GlStateManager.rotate(m.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
			}
			if(m.rotateAngleX != 0.0F) {
				GlStateManager.rotate(m.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
			}
		}
	}
	
	protected static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
		float f;
		
		for(f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) {
			;
		}

		while(f >= 180.0F) {
			f -= 360.0F;
		}

		return prevYawOffset + partialTicks * f;
	}
	
	public static Triangle[] decompress(VertexData vertices){
		Triangle[] tris = new Triangle[vertices.positionIndices.length/3];
		for(int i = 0; i < vertices.positionIndices.length; i += 3){
			int i0 = vertices.positionIndices[i];
			int i1 = vertices.positionIndices[i+1];
			int i2 = vertices.positionIndices[i+2];
			float[] tex = new float[6];
			tex[0] = vertices.texCoords[(i+0)*2];
			tex[1] = vertices.texCoords[(i+0)*2+1];
			tex[2] = vertices.texCoords[(i+1)*2];
			tex[3] = vertices.texCoords[(i+1)*2+1];
			tex[4] = vertices.texCoords[(i+2)*2];
			tex[5] = vertices.texCoords[(i+2)*2+1];
			tris[i/3] = new Triangle(vertices.positions[i0], vertices.positions[i1], vertices.positions[i2], tex);
		}
		return tris;
	}
	
	public static VertexData compress(Triangle[] tris){
		List<Vec3d> vertices = new ArrayList<>(tris.length*3);
		int[] indices = new int[tris.length*3];
		float[] texCoords = new float[tris.length*6];
		for(int i = 0; i < tris.length; i ++){
			Triangle tri = tris[i];
			double eps = 0.00001D;
			int idx = epsIndexOf(vertices, tri.p1.pos, eps);
			if(idx != -1){
				indices[i*3] = idx;
			} else {
				indices[i*3] = vertices.size();
				vertices.add(tri.p1.pos);
			}
			
			idx = epsIndexOf(vertices, tri.p2.pos, eps);
			if(idx != -1){
				indices[i*3+1] = idx;
			} else {
				indices[i*3+1] = vertices.size();
				vertices.add(tri.p2.pos);
			}
			
			idx = epsIndexOf(vertices, tri.p3.pos, eps);
			if(idx != -1){
				indices[i*3+2] = idx;
			} else {
				indices[i*3+2] = vertices.size();
				vertices.add(tri.p3.pos);
			}
			
			texCoords[i*6+0] = tri.p1.texX;
			texCoords[i*6+1] = tri.p1.texY;
			texCoords[i*6+2] = tri.p2.texX;
			texCoords[i*6+3] = tri.p2.texY;
			texCoords[i*6+4] = tri.p3.texX;
			texCoords[i*6+5] = tri.p3.texY;
		}
		VertexData data = new VertexData();
		data.positions = vertices.toArray(new Vec3d[0]);
		data.positionIndices = indices;
		data.texCoords = texCoords;
		return data;
	}
	
	private static int epsIndexOf(List<Vec3d> l, Vec3d vec, double eps){
		for(int i = 0; i < l.size(); i++){
			if(BobMathUtil.epsilonEquals(vec, l.get(i), eps)){
				return i;
			}
		}
		return -1;
	}
	
	public static VertexData[] cutAndCapModelBox(ModelBox b, float[] plane, @Nullable Matrix4f transform){
		return cutAndCapConvex(triangulate(b, transform), plane);
	}
	
	public static VertexData[] cutAndCapConvex(Triangle[] tris, float[] plane){
		VertexData[] returnData = new VertexData[]{null, null, new VertexData()};
		List<Triangle> side1 = new ArrayList<>();
		List<Triangle> side2 = new ArrayList<>();
		List<Vec3d[]> clippedEdges = new ArrayList<>();
		for(Triangle t : tris){
			//Clip each triangle to the plane.
			//TODO move this to a generic helper method for clipping triangles?
			boolean p1 = t.p1.pos.x*plane[0]+t.p1.pos.y*plane[1]+t.p1.pos.z*plane[2]+plane[3] > 0;
			boolean p2 = t.p2.pos.x*plane[0]+t.p2.pos.y*plane[1]+t.p2.pos.z*plane[2]+plane[3] > 0;
			boolean p3 = t.p3.pos.x*plane[0]+t.p3.pos.y*plane[1]+t.p3.pos.z*plane[2]+plane[3] > 0;
			//If all points on positive side, add to side 1 
			if(p1 && p2 && p3){
				side1.add(t);
			//else if all on negative side, add to size 2
			} else if(!p1 && !p2 && !p3){
				side2.add(t);
			//else if only one is positive, clip and add 1 triangle to side 1, 2 to side 2
			} else if(p1 ^ p2 ^ p3){
				TexVertex a, b, c;
				if(p1){
					a = t.p1;
					b = t.p2;
					c = t.p3;
				} else if(p2){
					a = t.p2;
					b = t.p3;
					c = t.p1;
				} else {
					a = t.p3;
					b = t.p1;
					c = t.p2;
				}
				Vec3d rAB = b.pos.subtract(a.pos);
				Vec3d rAC = c.pos.subtract(a.pos);
				float interceptAB = (float) rayPlaneIntercept(a.pos, rAB, plane);
				float interceptAC = (float) rayPlaneIntercept(a.pos, rAC, plane);
				Vec3d d = a.pos.add(rAB.scale(interceptAB));
				Vec3d e = a.pos.add(rAC.scale(interceptAC));
				float[] deTex = new float[4];
				deTex[0] = a.texX + (b.texX-a.texX)*interceptAB;
				deTex[1] = a.texY + (b.texY-a.texY)*interceptAB;
				deTex[2] = a.texX + (c.texX-a.texX)*interceptAC;
				deTex[3] = a.texY + (c.texY-a.texY)*interceptAC;
				
				side2.add(new Triangle(d, b.pos, e, new float[]{deTex[0], deTex[1], b.texX, b.texY, deTex[2], deTex[3]}));
				side2.add(new Triangle(b.pos, c.pos, e, new float[]{b.texX, b.texY, c.texX, c.texY, deTex[2], deTex[3]}));
				side1.add(new Triangle(a.pos, d, e, new float[]{a.texX, a.texY, deTex[0], deTex[1], deTex[2], deTex[3]}));
				clippedEdges.add(new Vec3d[]{d, e});
				
			//else one is negative, clip and add 2 triangles to side 1, 1 to side 2.
			} else {
				TexVertex a, b, c;
				if(!p1){
					a = t.p1;
					b = t.p2;
					c = t.p3;
				} else if(!p2){
					a = t.p2;
					b = t.p3;
					c = t.p1;
				} else {
					a = t.p3;
					b = t.p1;
					c = t.p2;
				}
				//Duplicated code. Eh, I don't feel like redesigning this.
				Vec3d rAB = b.pos.subtract(a.pos);
				Vec3d rAC = c.pos.subtract(a.pos);
				float interceptAB = (float) rayPlaneIntercept(a.pos, rAB, plane);
				float interceptAC = (float) rayPlaneIntercept(a.pos, rAC, plane);
				Vec3d d = a.pos.add(rAB.scale(interceptAB));
				Vec3d e = a.pos.add(rAC.scale(interceptAC));
				float[] deTex = new float[4];
				deTex[0] = a.texX + (b.texX-a.texX)*interceptAB;
				deTex[1] = a.texY + (b.texY-a.texY)*interceptAB;
				deTex[2] = a.texX + (c.texX-a.texX)*interceptAC;
				deTex[3] = a.texY + (c.texY-a.texY)*interceptAC;
				
				side1.add(new Triangle(d, b.pos, e, new float[]{deTex[0], deTex[1], b.texX, b.texY, deTex[2], deTex[3]}));
				side1.add(new Triangle(b.pos, c.pos, e, new float[]{b.texX, b.texY, c.texX, c.texY, deTex[2], deTex[3]}));
				side2.add(new Triangle(a.pos, d, e, new float[]{a.texX, a.texY, deTex[0], deTex[1], deTex[2], deTex[3]}));
				clippedEdges.add(new Vec3d[]{e, d});
			}
		}
		//Since this is a convex mesh, generating one edge list is fine.
		if(!clippedEdges.isEmpty()){
			Matrix3f mat = BakedModelUtil.normalToMat(new Vec3d(plane[0], plane[1], plane[2]), 0);
			List<Vec3d> orderedClipVertices = new ArrayList<>();
			orderedClipVertices.add(clippedEdges.get(0)[0]);
			while(!clippedEdges.isEmpty()){
				orderedClipVertices.add(getNext(clippedEdges, orderedClipVertices.get(orderedClipVertices.size()-1)));
			}
			Vector3f uv1 = new Vector3f((float)orderedClipVertices.get(0).x, (float)orderedClipVertices.get(0).y, (float)orderedClipVertices.get(0).z);
			mat.transform(uv1);
			Triangle[] cap = new Triangle[orderedClipVertices.size()-2];
			for(int i = 0; i < cap.length; i ++){
				Vector3f uv2 = new Vector3f((float)orderedClipVertices.get(i+2).x, (float)orderedClipVertices.get(i+2).y, (float)orderedClipVertices.get(i+2).z);
				mat.transform(uv2);
				Vector3f uv3 = new Vector3f((float)orderedClipVertices.get(i+1).x, (float)orderedClipVertices.get(i+1).y, (float)orderedClipVertices.get(i+1).z);
				mat.transform(uv3);
				cap[i] = new Triangle(orderedClipVertices.get(0), orderedClipVertices.get(i+2), orderedClipVertices.get(i+1), new float[]{uv1.x, uv1.y, uv2.x, uv2.y, uv3.x, uv3.y});
				
				side1.add(new Triangle(orderedClipVertices.get(0), orderedClipVertices.get(i+2), orderedClipVertices.get(i+1), new float[]{0, 0, 0, 0, 0, 0}));
				side2.add(new Triangle(orderedClipVertices.get(0), orderedClipVertices.get(i+1), orderedClipVertices.get(i+2), new float[]{0, 0, 0, 0, 0, 0}));
			}
			returnData[2] = compress(cap);
		}
		returnData[0] = compress(side1.toArray(new Triangle[side1.size()]));
		returnData[1] = compress(side2.toArray(new Triangle[side2.size()]));
		return returnData;
	}
	
	private static Vec3d getNext(List<Vec3d[]> edges, Vec3d first){
		Iterator<Vec3d[]> itr = edges.iterator();
		while(itr.hasNext()){
			Vec3d[] v = itr.next();
			double eps = 0.00001D;
			if(BobMathUtil.epsilonEquals(v[0], first, eps)){
				itr.remove();
				return v[1];
			} else if(BobMathUtil.epsilonEquals(v[1], first, eps)){
				itr.remove();
				return v[0];
			}
		}
		throw new RuntimeException("Didn't find next in loop!");
	}
	
	public static double rayPlaneIntercept(Vec3d start, Vec3d ray, float[] plane){
		double num = -(plane[0]*start.x + plane[1]*start.y + plane[2]*start.z + plane[3]);
		double denom = plane[0]*ray.x + plane[1]*ray.y + plane[2]*ray.z;
		return num/denom;
	}

	public static Triangle[] triangulate(ModelBox b, @Nullable Matrix4f transform){
		Triangle[] tris = new Triangle[12];
        int i = 0;
        for(TexturedQuad t : b.quadList){
            Vec3d v0 = BobMathUtil.mat4Transform(t.vertexPositions[0].vector3D, transform);
            Vec3d v1 = BobMathUtil.mat4Transform(t.vertexPositions[1].vector3D, transform);
            Vec3d v2 = BobMathUtil.mat4Transform(t.vertexPositions[2].vector3D, transform);
            Vec3d v3 = BobMathUtil.mat4Transform(t.vertexPositions[3].vector3D, transform);
            float[] tex = new float[6];
            tex[0] = t.vertexPositions[0].texturePositionX;
            tex[1] = t.vertexPositions[0].texturePositionY;
            tex[2] = t.vertexPositions[1].texturePositionX;
            tex[3] = t.vertexPositions[1].texturePositionY;
            tex[4] = t.vertexPositions[2].texturePositionX;
            tex[5] = t.vertexPositions[2].texturePositionY;
            tris[i++] = new Triangle(v0, v1, v2, tex);
            tex = new float[6];
            tex[0] = t.vertexPositions[2].texturePositionX;
            tex[1] = t.vertexPositions[2].texturePositionY;
            tex[2] = t.vertexPositions[3].texturePositionX;
            tex[3] = t.vertexPositions[3].texturePositionY;
            tex[4] = t.vertexPositions[0].texturePositionX;
            tex[5] = t.vertexPositions[0].texturePositionY;
            tris[i++] = new Triangle(v2, v3, v0, tex);
        }
        return tris;
	}
	
	public static ParticleSlicedMob[] generateCutParticles(Entity ent, float[] plane, ResourceLocation capTex, float capBloom){
		return generateCutParticles(ent, plane, capTex, capBloom, null);
	}
	
	public static ParticleSlicedMob[] generateCutParticles(Entity ent, float[] plane, ResourceLocation capTex, float capBloom, Consumer<List<Triangle>> capConsumer){
		
		// Cut all mob boxes and store them in separate lists //
		
		List<Pair<Matrix4f, ModelRenderer>> boxes = ModelRendererUtil.getBoxesFromMob((EntityLivingBase) ent);
		List<CutModelData> top = new ArrayList<>();
		List<CutModelData> bottom = new ArrayList<>();
		for(Pair<Matrix4f, ModelRenderer> r : boxes){
			for(ModelBox b : r.getRight().cubeList){
				VertexData[] dat = ModelRendererUtil.cutAndCapModelBox(b, plane, r.getLeft());
				CutModelData tp = null;
				CutModelData bt = null;
				if(dat[0].positionIndices != null && dat[0].positionIndices.length > 0){
					tp = new CutModelData(dat[0], null, false, new ConvexMeshCollider(dat[0].positionIndices, dat[0].vertexArray(), 1));
					top.add(tp);
				}
				if(dat[1].positionIndices != null && dat[1].positionIndices.length > 0){
					bt = new CutModelData(dat[1], null, true, new ConvexMeshCollider(dat[1].positionIndices, dat[1].vertexArray(), 1));
					bottom.add(bt);
				}
				if(dat[2].positionIndices != null && dat[2].positionIndices.length > 0){
					tp.cap = dat[2];
					bt.cap = dat[2];
				}
			}
		}
		
		if(capConsumer != null){
			List<Triangle> tris = new ArrayList<>();
			for(CutModelData d : top){
				if(d.cap != null)
					for(Triangle t : decompress(d.cap)){
						tris.add(t);
					}
			}
			capConsumer.accept(tris);
		}
		
		List<List<CutModelData>> particleChunks = new ArrayList<>();
		generateChunks(particleChunks, top);
		generateChunks(particleChunks, bottom);
		
		List<ParticleSlicedMob> particles = new ArrayList<>(2);
		
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		
		ResourceLocation tex = getEntityTexture(ent);
		
		for(List<CutModelData> l : particleChunks){
			float scale = 3.5F;
			if(l.get(0).flip){
				scale = -scale;
			}
			//Generate the physics body
			RigidBody body = new RigidBody(ent.world, ent.posX, ent.posY, ent.posZ);
			Collider[] colliders = new Collider[l.size()];
			int i = 0;
			for(CutModelData dat : l){
				colliders[i++] = dat.collider;
			}
			body.addColliders(colliders);
			body.impulseVelocityDirect(new Vec3(plane[0]*scale, plane[1]*scale, plane[2]*scale), body.globalCentroid.add(0, 0, 0));
			
			//Create rendering display lists
			int bodyDL = GL11.glGenLists(1);
			int capDL = GL11.glGenLists(1);
			
			GL11.glNewList(bodyDL, GL11.GL_COMPILE);
			buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
			for(CutModelData dat : l){
				dat.data.tessellate(buf, true);
			}
			tes.draw();
			GL11.glEndList();
			
			GL11.glNewList(capDL, GL11.GL_COMPILE);
			buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
			for(CutModelData dat : l){
				if(dat.cap != null)
					dat.cap.tessellate(buf, dat.flip, true);
			}
			tes.draw();
			GL11.glEndList();
			
			particles.add(new ParticleSlicedMob(ent.world, body, bodyDL, capDL, tex, capTex, capBloom));
		}
		
		return particles.toArray(new ParticleSlicedMob[particles.size()]);
	}
	
	public static RigidBody[] generateRigidBodiesFromBoxes(Entity ent, List<Pair<Matrix4f, ModelRenderer>> boxes){
		RigidBody[] arr = new RigidBody[boxes.size()];
		int i = 0;
		for(Pair<Matrix4f, ModelRenderer> p : boxes){
			RigidBody body = new RigidBody(ent.world, ent.posX, ent.posY, ent.posZ);
			Collider[] colliders = new Collider[p.getRight().cubeList.size()];
			int j = 0;
			for(ModelBox b : p.getRight().cubeList){
				Triangle[] data = triangulate(b, p.getLeft());
				VertexData dat = compress(data);
				colliders[j] = new ConvexMeshCollider(dat.positionIndices, dat.vertexArray(), 1); 
				j++;
			}
			body.addColliders(colliders);
			arr[i] = body;
			i++;
		}
		return arr;
	}
	
	public static int[] generateDisplayListsFromBoxes(List<Pair<Matrix4f, ModelRenderer>> boxes){
		int[] lists = new int[boxes.size()];
		int i = 0;
		for(Pair<Matrix4f, ModelRenderer> p : boxes){
			int list = GL11.glGenLists(1);
			GL11.glNewList(list, GL11.GL_COMPILE);
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buf = tes.getBuffer();
			buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
			for(ModelBox b : p.getRight().cubeList){
				Triangle[] data = triangulate(b, p.getLeft());
				VertexData dat = compress(data);
				dat.tessellate(buf, true);
			}
			tes.draw();
			GL11.glEndList();
			lists[i] = list;
			i++;
		}
		return lists;
	}
	
	private static void generateChunks(List<List<CutModelData>> chunks, List<CutModelData> toSort){
		GJK.margin = 0.01F;
		List<CutModelData> chunk = new ArrayList<>();
		boolean removed = false;
		//Basically we're trying to build little islands of colliders that are together so we don't get stuff like floating geometry
		while(!toSort.isEmpty()){
			removed = false;
			//Not as efficient as it could be and not the cleanest code, but it probably won't matter with the amount of cubes we're dealing with.
			List<CutModelData> toAdd = new ArrayList<>(2);
			for(CutModelData c : chunk){
				Iterator<CutModelData> itr = toSort.iterator();
				while(itr.hasNext()){
					CutModelData d = itr.next();
					if(d.collider.localBox.grow(0.01F).intersects(c.collider.localBox)){
						if(GJK.collidesAny(null, null, c.collider, d.collider)){
							//Something got removed, which means we have to iterate again to check if that one is connected to anything.
							removed = true;
							toAdd.add(d);
							itr.remove();
						}
					}
				}
			}
			chunk.addAll(toAdd);
			//If nothing else got added to the chunk, it's complete, so we can add it to the final list.
			if(!removed){
				if(!chunk.isEmpty()){
					chunks.add(chunk);
					chunk = new ArrayList<>();
				}
				chunk.add(toSort.remove(0));
			}
		}
		if(!chunk.isEmpty()){
			chunks.add(chunk);
		}
		GJK.margin = 0;
	}
	
	public static class CutModelData {
		public VertexData data;
		public VertexData cap;
		public boolean flip;
		public ConvexMeshCollider collider;
		
		public CutModelData(VertexData data, VertexData cap, boolean flip, ConvexMeshCollider collider) {
			this.data = data;
			this.cap = cap;
			this.flip = flip;
			this.collider = collider;
		}
		
	}
	
	public static class VertexData {
		public Vec3d[] positions;
		public int[] positionIndices;
		public float[] texCoords;
		
		public void tessellate(BufferBuilder buf, boolean normal){
			tessellate(buf, false, normal);
		}
		
		public void tessellate(BufferBuilder buf, boolean flip, boolean normal){
			if(positionIndices != null)
				for(int i = 0; i < positionIndices.length; i += 3){
					Vec3d a = positions[positionIndices[i]];
					Vec3d b = positions[positionIndices[i+1]];
					Vec3d c = positions[positionIndices[i+2]];
					//Offset into texcoord array
					int tOB = 1;
					int tOC = 2;
					if(flip){
						Vec3d tmp = b;
						b = c;
						c = tmp;
						tOB = 2;
						tOC = 1;
					}
					if(normal){
						Vec3d norm = b.subtract(a).crossProduct(c.subtract(a)).normalize();
						buf.pos(a.x, a.y, a.z).tex(texCoords[i*2+0], texCoords[i*2+1]).normal((float)norm.x, (float)norm.y, (float)norm.z).endVertex();
						buf.pos(b.x, b.y, b.z).tex(texCoords[(i+tOB)*2+0], texCoords[(i+tOB)*2+1]).normal((float)norm.x, (float)norm.y, (float)norm.z).endVertex();
						buf.pos(c.x, c.y, c.z).tex(texCoords[(i+tOC)*2+0], texCoords[(i+tOC)*2+1]).normal((float)norm.x, (float)norm.y, (float)norm.z).endVertex();
					} else {
						buf.pos(a.x, a.y, a.z).tex(texCoords[i*2+0], texCoords[i*2+1]).endVertex();
						buf.pos(b.x, b.y, b.z).tex(texCoords[(i+tOB)*2+0], texCoords[(i+tOB)*2+1]).endVertex();
						buf.pos(c.x, c.y, c.z).tex(texCoords[(i+tOC)*2+0], texCoords[(i+tOC)*2+1]).endVertex();
					}
					
				}
		}

		public float[] vertexArray() {
			float[] verts = new float[positions.length*3];
			for(int i = 0; i < positions.length; i ++){
				Vec3d pos = positions[i];
				verts[i*3] = (float) pos.x;
				verts[i*3+1] = (float) pos.y;
				verts[i*3+2] = (float) pos.z;
			}
			return verts;
		}
	}
	
}
