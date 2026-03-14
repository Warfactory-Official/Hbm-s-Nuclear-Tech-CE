package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl.DefaultSkins;
import com.hbm.tileentity.TileEntityDoorGeneric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RenderVaultDoor implements IRenderDoors {
	
	public static final RenderVaultDoor INSTANCE = new RenderVaultDoor();

	@Override
	public void render(TileEntityDoorGeneric door, DoubleBuffer buf) {

		ResourceLocation doorTex = DefaultSkins.pheo_vault_door_3;
		ResourceLocation labelTex = DefaultSkins.pheo_label_101;
		
		switch(door.getSkinIndex()) {
		case 1: labelTex = DefaultSkins.pheo_label_87; break;
		case 2: labelTex = DefaultSkins.pheo_label_106; break;
		case 3: doorTex = DefaultSkins.pheo_vault_door_4; labelTex = DefaultSkins.pheo_label_81; break;
		case 4: doorTex = DefaultSkins.pheo_vault_door_4; labelTex = DefaultSkins.pheo_label_111; break;
		case 5: doorTex = DefaultSkins.pheo_vault_door_s; labelTex = DefaultSkins.pheo_label_2; break;
		case 6: doorTex = DefaultSkins.pheo_vault_door_s; labelTex = DefaultSkins.pheo_label_99; break;
		}
		
		double pull = 0;
		double slide = 0;
		
		if(door.state == DoorState.OPEN) {
			pull = 1;
			slide = 1;
		}
		
		if(door.currentAnimation != null) {
			pull = IRenderDoors.getRelevantTransformation("PULL", door.currentAnimation)[2];
			slide = IRenderDoors.getRelevantTransformation("SLIDE", door.currentAnimation)[0];
		}
		
		double diameter = 4.25D;
		double circumference = diameter * Math.PI;
		slide *= 5D;
		double roll = 360D * slide / circumference;

		Minecraft.getMinecraft().getTextureManager().bindTexture(doorTex);
		ResourceManager.pheo_vault_door.renderPart("Frame");
		GlStateManager.translate(-pull, 0, 0);
		GlStateManager.translate(0, 0, slide);
		GlStateManager.translate(0, 2.5, 0);
		GlStateManager.rotate((float) roll, 1, 0, 0);
		GlStateManager.translate(0, -2.5, 0);
		ResourceManager.pheo_vault_door.renderPart("Door");
		Minecraft.getMinecraft().getTextureManager().bindTexture(labelTex);
		ResourceManager.pheo_vault_door.renderPart("Label");
	}
}
