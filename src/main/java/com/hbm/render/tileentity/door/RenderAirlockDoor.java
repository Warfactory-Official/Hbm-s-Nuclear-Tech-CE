package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.interfaces.IDoor.DoorState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;

import net.minecraft.client.Minecraft;

public class RenderAirlockDoor implements IRenderDoors {
	
	public static final RenderAirlockDoor INSTANCE = new RenderAirlockDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.ROUND_AIRLOCK_DOOR.getSkinFromIndex(door.getSkinIndex()));
		
		double maxOpen = 1.5;
		double open = 0;
		if(door.state == DoorState.OPEN) open = maxOpen;
		
		if(door.currentAnimation != null) {
			open = IRenderDoors.getRelevantTransformation("DOOR", door.currentAnimation)[1] * maxOpen;
		}

		GlStateManager.disableCull();
		GlStateManager.translate(0, 0, 0.5);
		ResourceManager.pheo_airlock_door.renderPart("Frame");
		
		GL11.glEnable(GL11.GL_CLIP_PLANE0);
		buf.put(new double[] { 0.0, 0.0, 1, 1.999 }); buf.rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);
		
		GL11.glEnable(GL11.GL_CLIP_PLANE1);
		buf.put(new double[] { 0.0, 0.0, -1, 1.999 }); buf.rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE1, buf);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, MathHelper.clamp(open, 0, maxOpen));
		ResourceManager.pheo_airlock_door.renderPart("Left");
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, -MathHelper.clamp(open, 0, maxOpen));
		ResourceManager.pheo_airlock_door.renderPart("Right");
		GlStateManager.popMatrix();

		GL11.glDisable(GL11.GL_CLIP_PLANE0);
		GL11.glDisable(GL11.GL_CLIP_PLANE1);
	}
}
