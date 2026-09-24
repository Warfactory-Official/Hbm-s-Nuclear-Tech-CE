package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class RenderFireDoor implements IRenderDoors {
	
	public static final RenderFireDoor INSTANCE = new RenderFireDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.FIRE_DOOR.getSkinFromIndex(door.getSkinIndex()));
		
		double maxRaise = 2.75;
		double raise = 0;
		if(door.state == DoorState.OPEN) raise = maxRaise;
		
		if(door.currentAnimation != null) {
			raise = IRenderDoors.getRelevantTransformation("DOOR", door.currentAnimation)[1] * maxRaise;
		}

		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(-0.5, 0, 0);
		ResourceManager.pheo_fire_door.renderPart("Frame");
		GlStateManager.translate(0, MathHelper.clamp(raise, 0, maxRaise), 0);
		ResourceManager.pheo_fire_door.renderPart("Door");
	}
}
