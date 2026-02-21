package com.hbm.render.loader;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Face {
	
	public int[] verticesID;
	public Vertex[] vertices;
	public Vertex[] vertexNormals;
	public Vertex faceNormal;
	public TextureCoordinate[] textureCoordinates;
	
	public float normalX;
	public float normalY;
	public float normalZ;

	public void addFaceForRender(BufferBuilder tessellator) {
		addFaceForRender(tessellator, 0.0F);
	}

	public void addFaceForRender(BufferBuilder tessellator, float textureOffset) {
		
		if (this.faceNormal == null) {
			this.faceNormal = calculateFaceNormal();
		}
		
		normalX = this.faceNormal.x;
		normalY = this.faceNormal.y;
		normalZ = this.faceNormal.z;

		float averageU = 0.0F;
		float averageV = 0.0F;
		
		if ((this.textureCoordinates != null) && (this.textureCoordinates.length > 0)) {
			
			for (int i = 0; i < this.textureCoordinates.length; i++) {
				averageU += this.textureCoordinates[i].u;
				averageV += this.textureCoordinates[i].v;
			}
			
			averageU /= this.textureCoordinates.length;
			averageV /= this.textureCoordinates.length;
		}
		
		for (int i = 0; i < this.vertices.length; i++) {
			
			if ((this.textureCoordinates != null) && (this.textureCoordinates.length > 0)) {
				
				float offsetU = textureOffset;
				float offsetV = textureOffset;
				
				if (this.textureCoordinates[i].u > averageU) {
					offsetU = -offsetU;
				}
				if (this.textureCoordinates[i].v > averageV) {
					offsetV = -offsetV;
				}
				if ((this.vertexNormals != null) && (i < this.vertexNormals.length)) {
					normalX = this.vertexNormals[i].x;
					normalY = this.vertexNormals[i].y;
					normalZ = this.vertexNormals[i].z;
				}
				
				tessellator.pos(this.vertices[i].x, this.vertices[i].y, this.vertices[i].z).tex(this.textureCoordinates[i].u + offsetU, this.textureCoordinates[i].v + offsetV).normal(normalX, normalY, normalZ).endVertex();
			} else {
				tessellator.pos(this.vertices[i].x, this.vertices[i].y, this.vertices[i].z).tex(0, 0).normal(normalX, normalY, normalZ).endVertex();
			}
		}
	}

	public Vertex calculateFaceNormal() {

		final float x = this.vertices[0].x;
		final float y = this.vertices[0].y;
		final float z = this.vertices[0].z;
		
		Vec3d v1 = new Vec3d(this.vertices[1].x - x, this.vertices[1].y - y, this.vertices[1].z - z);
		Vec3d v2 = new Vec3d(this.vertices[2].x - x, this.vertices[2].y - y, this.vertices[2].z - z);
		Vec3d normalVector = v1.crossProduct(v2).normalize();

		return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
	}
}
