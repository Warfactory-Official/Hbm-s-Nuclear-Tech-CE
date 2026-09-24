package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class RenderWaterDoor implements IRenderDoors {
	
	public static final RenderWaterDoor INSTANCE = new RenderWaterDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {

		Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.WATER_DOOR.getSkinFromIndex(door.getSkinIndex()));

		double maxRot = 120;
		double rot = 0;
		double bolt = 0;
		if(door.state == DoorState.OPEN) {
			rot = maxRot;
			bolt = 1D;
		}
		
		if(door.currentAnimation != null) {
			rot = IRenderDoors.getRelevantTransformation("DOOR", door.currentAnimation)[1] * maxRot;
			bolt = IRenderDoors.getRelevantTransformation("BOLT", door.currentAnimation)[2];
		}

		GlStateManager.translate(0.375, 0.0, 0.0);
		GlStateManager.rotate(90, 0, 1, 0);
		ResourceManager.pheo_water_door.renderPart("Frame");

		GlStateManager.translate(-1.1875, 0, 0);
		GlStateManager.rotate((float) -rot, 0, 1, 0);
		GlStateManager.translate(1.1875, 0, 0);
		ResourceManager.pheo_water_door.renderPart("Door_Cube.003"); // ah fuck it
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.4 * bolt, 0, 0);
		ResourceManager.pheo_water_door.renderPart("Bolts");
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.40625F, 2.28125, 0);
		GlStateManager.rotate((float) (bolt * 360), 0, 0, 1);
		GlStateManager.translate(-0.40625F, -2.28125, 0);
		ResourceManager.pheo_water_door.renderPart("Top");
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.40625F, 0.71875, 0);
		GlStateManager.rotate((float) (bolt * 360), 0, 0, 1);
		GlStateManager.translate(-0.40625F, -0.71875, 0);
		ResourceManager.pheo_water_door.renderPart("Bottom");
		GlStateManager.popMatrix();
	}
}
