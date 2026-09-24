package com.hbm.render.tileentity.door;

import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl.DefaultSkins;
import com.hbm.tileentity.TileEntityDoorGeneric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import java.nio.DoubleBuffer;

public class RenderCargoDoor implements IRenderDoors {

	public static final RenderCargoDoor INSTANCE = new RenderCargoDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {

		Minecraft.getMinecraft().getTextureManager().bindTexture(DefaultSkins.pheo_cargo_door_tex);

		double botMove = 0;
		double topMove = 0;

		if(door.state == DoorState.OPEN) {
			botMove = 2.0;
			topMove = 1.0;
		}

		if(door.currentAnimation != null) {
			double botProgress = MathHelper.clamp(IRenderDoors.getRelevantTransformation("BOT", door.currentAnimation)[1], 0, 1);
			double topProgress = MathHelper.clamp(IRenderDoors.getRelevantTransformation("TOP", door.currentAnimation)[1], 0, 1);
			botMove = botProgress * 2.0;
			topMove = topProgress * 1.0;
		}

		ResourceManager.pheo_cargo_door.renderPart("Frame");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, topMove, 0);
		ResourceManager.pheo_cargo_door.renderPart("DoorTop");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, botMove, 0);
		ResourceManager.pheo_cargo_door.renderPart("DoorBot");
		GlStateManager.popMatrix();
	}
}
