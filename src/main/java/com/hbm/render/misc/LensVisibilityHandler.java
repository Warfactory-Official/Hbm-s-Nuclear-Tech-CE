package com.hbm.render.misc;

import com.hbm.Tags;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public class LensVisibilityHandler {

	static Map<Integer, LensSpikeInfo> lensSpikes = new HashMap<>();
	static int currentId = 0;
	public static int checkSphere = -1;
	
	public static int findId(){
		while(lensSpikes.containsKey(currentId)){
			currentId ++;
		}
		return currentId;
	}
	
	public static int generate(FloatBuffer matrix){
		int id = findId();
		float[] mat = new float[16];
		matrix.get(mat);
		matrix.rewind();
		LensSpikeInfo i = new LensSpikeInfo(mat);
		lensSpikes.put(id, i);
		return id;
	}
	
	public static void delete(int id){
		LensSpikeInfo i = lensSpikes.get(id);
		if(i != null){
			i.cleanup();
			lensSpikes.remove(id);
		}
	}
	
	public static float getVisibility(int id){
		LensSpikeInfo i = lensSpikes.get(id);
		if(i != null){
			return i.visibility;
		}
		return 0.0F;
	}
	
	public static float[] getMatrixBuf(int id){
		LensSpikeInfo i = lensSpikes.get(id);
		if(i != null){
			return i.modelviewMatrix;
		}
		return null;
	}
	
	public static void putMatrixBuf(int id, FloatBuffer matrix){
		LensSpikeInfo i = lensSpikes.get(id);
		if(i != null){
			matrix.get(i.modelviewMatrix);
			matrix.rewind();
		}
	}
	
	@SubscribeEvent
	public static void renderLast(RenderWorldLastEvent event) {
		for(LensSpikeInfo i : lensSpikes.values()){
			i.updateVisibility();
		}
	}
	
	public static class LensSpikeInfo {
		public float[] modelviewMatrix;
		public float visibility = 0.0F;
		private int totalFragmentsQuery;
		private int fragmentsPassedQuery;
		
		public LensSpikeInfo(float[] matrix) {
			this.modelviewMatrix = matrix;
			totalFragmentsQuery = GLCompat.genQueries();
			fragmentsPassedQuery = GLCompat.genQueries();
			
			GlStateManager.colorMask(false, false, false, false);
			GlStateManager.depthMask(false);
			GlStateManager.disableCull();
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.turbofan_blades_tex);
			
			GlStateManager.pushMatrix();
			ClientProxy.AUX_GL_BUFFER.put(matrix);
			ClientProxy.AUX_GL_BUFFER.rewind();
			GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);
			GL11.glScaled(0.05, 0.05, 0.05);
			GlStateManager.disableDepth();
			GLCompat.beginQuery(GLCompat.GL_SAMPLES_PASSED, totalFragmentsQuery);
			GL11.glCallList(checkSphere);
			GLCompat.endQuery(GLCompat.GL_SAMPLES_PASSED);
			
			GlStateManager.enableDepth();
			GLCompat.beginQuery(GLCompat.GL_SAMPLES_PASSED, fragmentsPassedQuery);
			GL11.glCallList(checkSphere);
			GLCompat.endQuery(GLCompat.GL_SAMPLES_PASSED);
			GlStateManager.popMatrix();
			
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();
		}
		
		public void updateVisibility(){
			int totalDone = GLCompat.getQueryObject(totalFragmentsQuery, GLCompat.GL_QUERY_RESULT_AVAILABLE);
			int passedDone = GLCompat.getQueryObject(fragmentsPassedQuery, GLCompat.GL_QUERY_RESULT_AVAILABLE);
			if(totalDone != 0 && passedDone != 0){
				float total = GLCompat.getQueryObject(totalFragmentsQuery, GLCompat.GL_QUERY_RESULT);
				float passed = GLCompat.getQueryObject(fragmentsPassedQuery, GLCompat.GL_QUERY_RESULT);
				visibility = passed/total;
				
				GlStateManager.colorMask(false, false, false, false);
				GlStateManager.depthMask(false);
				GlStateManager.disableCull();
				Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.turbofan_blades_tex);
				
				GlStateManager.pushMatrix();
				ClientProxy.AUX_GL_BUFFER.put(modelviewMatrix);
				ClientProxy.AUX_GL_BUFFER.rewind();
				GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);
				GL11.glScaled(0.1, 0.1, 0.1);
				GlStateManager.disableDepth();
				GLCompat.beginQuery(GLCompat.GL_SAMPLES_PASSED, totalFragmentsQuery);
				GL11.glCallList(checkSphere);
				GLCompat.endQuery(GLCompat.GL_SAMPLES_PASSED);
				
				GlStateManager.enableDepth();
				GLCompat.beginQuery(GLCompat.GL_SAMPLES_PASSED, fragmentsPassedQuery);
				GL11.glCallList(checkSphere);
				GLCompat.endQuery(GLCompat.GL_SAMPLES_PASSED);
				GlStateManager.popMatrix();
				
				GlStateManager.colorMask(true, true, true, true);
				GlStateManager.depthMask(true);
				GlStateManager.enableCull();
			}
		}
		
		public void cleanup(){
			GLCompat.deleteQueries(totalFragmentsQuery);
			GLCompat.deleteQueries(fragmentsPassedQuery);
			//That modelViewMatrix not being deleted might cause a memory leak, but if it does, I don't know what to do about it in java!
		}
	}
	
	
}
