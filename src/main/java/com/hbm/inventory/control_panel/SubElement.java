package com.hbm.inventory.control_panel;

import net.minecraft.client.gui.GuiButton;

public class SubElement {
	
	public GuiControlEdit gui;
	
	public boolean lock = false;
	
	public SubElement(GuiControlEdit gui) {
		this.gui = gui;
	}
	protected void actionPerformed(GuiButton button){
	}
	protected void initGui(){
		enableButtons(false);
	}
	protected void drawScreen(){
	}
	protected void renderBackground(){
	}
	protected void enableButtons(boolean enable){
	}
	protected void keyTyped(char typedChar, int code){
	}
	protected void mouseClicked(int mouseX, int mouseY, int button){
	}
	protected void mouseReleased(int mouseX, int mouseY, int state){
	}
	protected void update(){
	}
	public void onClose(){
	}
	public void onElementOpen() {
	}
	public void onElementClose() {
	}
}