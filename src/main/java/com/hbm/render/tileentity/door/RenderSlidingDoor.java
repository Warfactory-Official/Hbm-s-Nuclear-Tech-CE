package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl.DefaultSkins;
import com.hbm.tileentity.TileEntityDoorGeneric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class RenderSlidingDoor implements IRenderDoors {
	
	public static final RenderSlidingDoor INSTANCE = new RenderSlidingDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(DefaultSkins.pheo_sliding_door_tex);
		
		double maxOpen = 0.95;
		double open = 0;
		if(door.state == DoorState.OPEN) open = maxOpen;
		
		if(door.currentAnimation != null) {
			open = IRenderDoors.getRelevantTransformation("DOOR", door.currentAnimation)[1] * maxOpen;
		}

		GlStateManager.disableCull();
		GlStateManager.translate(0.53125, 0.001, 0.5);
		ResourceManager.pheo_sliding_door.renderPart("Frame");
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, MathHelper.clamp(open, 0, maxOpen));
		ResourceManager.pheo_sliding_door.renderPart("Left");
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, -MathHelper.clamp(open, 0, maxOpen));
		ResourceManager.pheo_sliding_door.renderPart("Right");
		GlStateManager.popMatrix();
	}
}
