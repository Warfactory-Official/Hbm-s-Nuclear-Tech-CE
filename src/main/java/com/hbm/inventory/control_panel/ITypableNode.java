package com.hbm.inventory.control_panel;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITypableNode {
	@SideOnly(Side.CLIENT)
	float[] getValueBox();
	@SideOnly(Side.CLIENT)
	boolean isTyping();
	@SideOnly(Side.CLIENT)
	void startTyping();
	@SideOnly(Side.CLIENT)
	void stopTyping();
	@SideOnly(Side.CLIENT)
	void keyTyped(char c, int key);
}
