package com.hbm.render.loader;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.ArrayList;

public class GroupObject {
	public String name;
	public ArrayList<Face> faces = new ArrayList<Face>();
	public int glDrawingMode;

	public GroupObject() {
		this("");
	}

	public GroupObject(String name) {
		this(name, -1);
	}

	public GroupObject(String name, int glDrawingMode) {
		this.name = name;
		this.glDrawingMode = glDrawingMode;
	}

	public void render() {
		if (this.faces.size() > 0) {
			Tessellator tessellator = Tessellator.getInstance();
			tessellator.getBuffer().begin(glDrawingMode, DefaultVertexFormats.POSITION_TEX_NORMAL);
			render(tessellator);
			tessellator.draw();
		}
	}

	public void render(Tessellator tessellator) {
		if (this.faces.size() > 0) {
			for (Face face : this.faces) {
				face.addFaceForRender(tessellator.getBuffer());
			}
		}
	}
}
