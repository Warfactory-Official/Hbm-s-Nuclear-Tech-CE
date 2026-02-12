package com.hbm.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface ILookOverlay {

	@SideOnly(Side.CLIENT)
    void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos);

	@SideOnly(Side.CLIENT)
    default void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        printHook(event, world, new BlockPos(x, y, z));
    }

    @SideOnly(Side.CLIENT)
	static void printGeneric(RenderGameOverlayEvent.Pre event, String title, int titleCol, int bgCol, List<String> text) {

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = event.getResolution();

		GlStateManager.pushMatrix();

		int pX = resolution.getScaledWidth() / 2 + 8;
		int pZ = resolution.getScaledHeight() / 2;

		mc.fontRenderer.drawString(title, pX + 1, pZ - 9, bgCol);
		mc.fontRenderer.drawString(title, pX, pZ - 10, titleCol);

		try {
			for(String line : text) {
	
				int color = 0xFFFFFF;
				if(line.startsWith("&[")) {
					int end = line.lastIndexOf("&]");
					color = Integer.parseInt(line.substring(2, end));
					line = line.substring(end + 2);
				}
				
				mc.fontRenderer.drawStringWithShadow(line, pX, pZ, color);
				pZ += 10;
			}
		} catch(Exception ex) {
			mc.fontRenderer.drawStringWithShadow(ex.getClass().getSimpleName(), pX, pZ + 10, 0xff0000);
		}

		GlStateManager.disableBlend();

		GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
}
