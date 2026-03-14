package com.hbm.blocks.machine;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.generic.BlockBakeBase;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Fake HE / RF converters
 *
 * @author Leafia
 */
public class FuckYou extends BlockBakeBase implements ILookOverlay {
	public FuckYou(Material m,String s) {
		super(m,s);
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack,World player,List<String> list,ITooltipFlag advanced) {
		for (String s : I18nUtil.resolveKey("tile.machine_converter.desc").split("\\$")) list.add(TextFormatting.YELLOW+s);
		super.addInformation(stack,player,list,advanced);
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void printHook(Pre event,World world,BlockPos pos) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = event.getResolution();
		GlStateManager.pushMatrix();
		int pX = resolution.getScaledWidth()/2;
		int pY = resolution.getScaledHeight()/2;
		String[] title = I18nUtil.resolveKey("tile.machine_converter.desc").split("\\$");
		int lines = title.length+1;
		GlStateManager.translate(pX,pY,0);
		for (int i = 0; i < lines; i++) {
			GlStateManager.pushMatrix();
			int offset = (int)((i-lines/2f)*20);
			GlStateManager.translate(0,offset,0);
			if (i == 0)
				GlStateManager.scale(1.2,1.2,1);
			else
				GlStateManager.scale(2.2,2.2,1);
			String s;
			if (i == 0)
				s = I18nUtil.resolveKey("tile.machine_converter.fuckyou");
			else
				s = title[i-1];
			mc.fontRenderer.drawString(s,-mc.fontRenderer.getStringWidth(s)/2,-7/2,i == 0 ? 0xFFFFFF : 0xFF0000,true);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}
}
