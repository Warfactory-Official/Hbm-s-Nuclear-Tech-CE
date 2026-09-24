package com.hbm.particle.lightning_test;

import com.hbm.render.GLCompat;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.List;

public class TrailRenderer2 {

	public static final int BYTES_PER_VERTEX = 3*4 + 2*2 + 4;
	
	public static ByteBuffer aux_buf = GLAllocation.createDirectByteBuffer(512);
	public static int array_buf;
	public static int element_buf;
	public static int vao;
	public static int currentPointCount;
	private static boolean init = false;
	
	public static float[] color = new float[]{1, 1, 1, 1};
	
	public static void init(){
		array_buf = GLCompat.genBuffers();
		element_buf = GLCompat.genBuffers();
		vao = GLCompat.genVertexArrays();
		GLCompat.bindVertexArray(vao);
		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, array_buf);
		GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, element_buf);
		//pos
		GLCompat.vertexAttribPointer(0, 3, GL11.GL_FLOAT, false, BYTES_PER_VERTEX, 0);
		GLCompat.enableVertexAttribArray(0);
		//tex
		GLCompat.vertexAttribPointer(1, 2, GL11.GL_UNSIGNED_SHORT, true, BYTES_PER_VERTEX, 12);
		GLCompat.enableVertexAttribArray(1);
		//color
		GLCompat.vertexAttribPointer(2, 4, GL11.GL_UNSIGNED_BYTE, true, BYTES_PER_VERTEX, 16);
		GLCompat.enableVertexAttribArray(2);
		
		GLCompat.bindVertexArray(0);
		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
		GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public static void draw(Vec3d playerPos, List<Vec3d> points, float scale){
		draw(playerPos, points, scale, false, false, null);
	}
	
	public static void draw(Vec3d playerPos, List<Vec3d> points, float scale, boolean fadeEnd, boolean fadeEnd2, @Nullable IColorGetter c){
		generateAndBindVao(playerPos, points, scale, fadeEnd, fadeEnd2, c);
		drawGeneratedVao();
		unbindVao();
	}
	
	public static void drawGeneratedVao(){
		GL11.glDrawElements(GL11.GL_TRIANGLES, (currentPointCount-1)*12, GL11.GL_UNSIGNED_INT, 0);
	}
	
	public static void generateAndBindVao(Vec3d playerPos, List<Vec3d> points, float scale, boolean fadeEnd, boolean fadeEnd2, @Nullable IColorGetter c){
		if(!init){
			init = true;
			init();
		}
		currentPointCount = points.size();
		int size = BYTES_PER_VERTEX * (points.size()*3+2);
		if(size > aux_buf.capacity()){
			aux_buf = GLAllocation.createDirectByteBuffer(size);
		}
		Vec3d first = points.get(0);
		Vec3d cross = points.get(1).subtract(first).crossProduct(playerPos.subtract(first)).normalize().scale(scale * (fadeEnd ? 0.1F : 1));
		if(c != null){
			color = c.color(0);
		} else {
			color = new float[]{1, 1, 1, 1};
		}
		putVertex(first.add(cross), 0F, 1F);
		putVertex(first.add(cross.scale(-1)), 0F, 0F);
		for(int i = 1; i < points.size(); i ++){
			Vec3d last = points.get(i-1);
			Vec3d current = points.get(i);
			Vec3d next = points.get(i);
			if(i < points.size()-1){
				next = points.get(i+1);
			}
			Vec3d toNext = points.get(i).subtract(last);
			Vec3d tangent = next.subtract(last);
			
			float iN = (float)(i)/(float)(points.size()-1);
			float bruh = 1-MathHelper.clamp((iN-0.8F)*5, 0, 1);
			if(fadeEnd)
				bruh *= MathHelper.clamp(iN*5, 0, 1);
			if(!fadeEnd2)
				bruh = 1;
			cross = tangent.crossProduct(playerPos.subtract(last)).normalize().scale(scale*Math.max(bruh, 0.1));
			float uMiddle = (float)(i-0.5F)/(float)(points.size()-1);
			if(c != null){
				color = c.color(uMiddle);
			}
			putVertex(last.add(toNext.scale(0.5)), uMiddle, 0.5F);
			if(c != null){
				color = c.color(iN);
			}
			putVertex(current.add(cross), iN, 1F);
			putVertex(current.add(cross.scale(-1)), iN, 0F);
		}
		GLCompat.bindVertexArray(vao);
		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, array_buf);
		aux_buf.rewind();
		GLCompat.bufferData(GLCompat.GL_ARRAY_BUFFER, aux_buf, GLCompat.GL_DYNAMIC_DRAW);
		
		for(int i = 0; i < points.size()-1; i ++){
			int offset = i*3;
			aux_buf.putInt(0+offset);
			aux_buf.putInt(2+offset);
			aux_buf.putInt(1+offset);
			
			aux_buf.putInt(0+offset);
			aux_buf.putInt(3+offset);
			aux_buf.putInt(2+offset);
			
			aux_buf.putInt(2+offset);
			aux_buf.putInt(3+offset);
			aux_buf.putInt(4+offset);
			
			aux_buf.putInt(2+offset);
			aux_buf.putInt(4+offset);
			aux_buf.putInt(1+offset);
		}
		
		GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, element_buf);
		aux_buf.rewind();
		GLCompat.bufferData(GLCompat.GL_ELEMENT_ARRAY_BUFFER, aux_buf, GLCompat.GL_DYNAMIC_DRAW);
		GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, BYTES_PER_VERTEX, 0);
		GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, BYTES_PER_VERTEX, 12);
		GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, BYTES_PER_VERTEX, 16);
		GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public static void unbindVao(){
		GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
		GLCompat.bindVertexArray(0);
		GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
		GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private static void putVertex(Vec3d pos, float texU, float texV){
		aux_buf.putFloat((float) pos.x);
		aux_buf.putFloat((float) pos.y);
		aux_buf.putFloat((float) pos.z);
		aux_buf.putShort((short)(texU*65535));
		aux_buf.putShort((short)(texV*65535));
		aux_buf.put((byte)(color[0]*255));
		aux_buf.put((byte)(color[1]*255));
		aux_buf.put((byte)(color[2]*255));
		aux_buf.put((byte)(color[3]*255));
	}
	
	public static interface IColorGetter {
		public float[] color(float position);
	}
}
