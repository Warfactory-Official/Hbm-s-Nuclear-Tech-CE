package com.hbm.inventory.control_panel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

public class GuiLinkerButton extends GuiButton {
	public int actualX;
	public int actualWidth;
	public GuiTextField field;
	FontRenderer font;
	public GuiLinkerButton(FontRenderer font,int buttonId,int x,int y,int widthIn,int heightIn,String buttonText) {
		super(buttonId,x+widthIn-20,y,widthIn-20,heightIn,"X");
		actualX = x;
		actualWidth = widthIn;
		this.font = font;
		field = new GuiTextField(
				-1,
				font,x,y,
				widthIn-20,heightIn
		);
		field.setText(buttonText);
	}
	public void update() {
		field.updateCursorCounter();
	}
	@Override
	public void drawButton(Minecraft mc,int mouseX,int mouseY,float partialTicks) {
		super.drawButton(mc,mouseX,mouseY,partialTicks);
		field.drawTextBox();
	}
	public void keyTyped(char key,int code) {
		field.textboxKeyTyped(key,code);
	}
	public void mouseClicked(int x,int y,int i) {
		field.mouseClicked(x,y,i);
	}
}
