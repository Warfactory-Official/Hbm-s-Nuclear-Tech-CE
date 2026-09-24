package com.hbm.wiaj.actors;

import com.hbm.wiaj.JarScene;
import com.hbm.wiaj.WorldInAJar;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ActorVillager implements ISpecialActor {
	
	EntityVillager villager = new EntityVillager(Minecraft.getMinecraft().world);
	NBTTagCompound data = new NBTTagCompound();
	
	public ActorVillager() { }
	
	public ActorVillager(NBTTagCompound data) {
		this.data = data;
	}

	@Override
	public void drawForegroundComponent(int w, int h, int ticks, float interp) { }

	@Override
	public void drawBackgroundComponent(WorldInAJar world, int ticks, float interp) {
		double x = data.getDouble("x");
		double y = data.getDouble("y");
		double z = data.getDouble("z");
		double yaw = data.getDouble("yaw");
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate((float) (yaw), 0, 1, 0);
		Minecraft.getMinecraft().getRenderManager().renderEntity(villager, 0D, 0D, 0D, 0F, interp, false);
	}

	@Override
	public void tick(JarScene scene) {
		villager.limbSwingAmount += (1F - villager.limbSwingAmount) * 0.4F;
		villager.limbSwing += villager.limbSwingAmount;
	}

	@Override
	public void setActorData(NBTTagCompound data) {
		
	}

	@Override
	public void setDataPoint(String tag, Object o) {
		
	}
}
