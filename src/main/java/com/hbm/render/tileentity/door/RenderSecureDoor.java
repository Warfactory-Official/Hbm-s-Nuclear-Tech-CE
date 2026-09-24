package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class RenderSecureDoor implements IRenderDoors {
	
	public static final RenderSecureDoor INSTANCE = new RenderSecureDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.SECURE_ACCESS_DOOR.getSkinFromIndex(door.getSkinIndex()));
		
		double maxRaise = 3.5;
		double raise = 0;
		if(door.state == DoorState.OPEN) raise = maxRaise;
		
		if(door.currentAnimation != null) {
			raise = IRenderDoors.getRelevantTransformation("DOOR", door.currentAnimation)[1] * maxRaise;
		}

		GlStateManager.translate(0, 1, 0);
		ResourceManager.pheo_secure_door.renderPart("Frame");
		GlStateManager.translate(0, MathHelper.clamp(raise, 0, maxRaise), 0);
		ResourceManager.pheo_secure_door.renderPart("Door");
	}
}
