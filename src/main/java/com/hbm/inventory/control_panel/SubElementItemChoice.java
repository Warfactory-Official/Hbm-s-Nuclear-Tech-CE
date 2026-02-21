package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SubElementItemChoice extends SubElement {
	public static ResourceLocation bg_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_base.png");

	public GuiButton pageLeft;
	public GuiButton pageRight;
	public int currentPage = 1;
	public int numPages = 1;
	public List<GuiButton> buttons = new ArrayList<>();
	
	public SubElementItemChoice(GuiControlEdit gui){
		super(gui);
	}
	
	@Override
	protected void initGui(){
		int cX = gui.width/2;
		int cY = gui.height/2;
		pageLeft = gui.addButton(new GuiButton(gui.currentButtonId(), cX-80, cY+92, 15, 20, "<"));
		pageRight = gui.addButton(new GuiButton(gui.currentButtonId(), cX+65, cY+92, 15, 20, ">"));

		buttons.add(gui.addButton(new GuiButton(1000, cX-80, (cY-90) + (0%7)*25, 160, 20, "Button")));
		buttons.add(gui.addButton(new GuiButton(1001, cX-80, (cY-90) + (1%7)*25, 160, 20, "Switch")));
		buttons.add(gui.addButton(new GuiButton(1002, cX-80, (cY-90) + (2%7)*25, 160, 20, "Display")));
		buttons.add(gui.addButton(new GuiButton(1003, cX-80, (cY-90) + (3%7)*25, 160, 20, "Indicator")));
		buttons.add(gui.addButton(new GuiButton(1004, cX-80, (cY-90) + (4%7)*25, 160, 20, "Knob")));
		buttons.add(gui.addButton(new GuiButton(1005, cX-80, (cY-90) + (5%7)*25, 160, 20, "Dial")));
		buttons.add(gui.addButton(new GuiButton(1006, cX-80, (cY-90) + (6%7)*25, 160, 20, "Label")));
		buttons.add(gui.addButton(new GuiButton(1007, cX-80, (cY-90) + (7%7)*25, 160, 20, "Slider")));

		numPages = (buttons.size()+6)/7;
		super.initGui();
	}
	
	@Override
	protected void drawScreen(){
		int cX = gui.width/2;
		int cY = gui.height/2;
		String text = currentPage + "/" + numPages;
		gui.getFontRenderer().drawString(text, cX - gui.getFontRenderer().getStringWidth(text) / 2F, cY+98, 0xFF777777, false);
		text = "Select Control Type";
		gui.getFontRenderer().drawString(text, cX - gui.getFontRenderer().getStringWidth(text) / 2F, cY-110, 0xFF777777, false);
	}

	@Override
	protected void renderBackground() {
		gui.mc.getTextureManager().bindTexture(bg_tex);
		gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
	}
	
	private void recalculateVisibleButtons(){
		for(GuiButton b : buttons){
			b.visible = false;
			b.enabled = false;
		}
		int idx = (currentPage-1)*7;
		for(int i = idx; i < idx+7; i ++){
			if(i >= buttons.size())
				break;
			buttons.get(i).visible = true;
			buttons.get(i).enabled = true;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button){
		if(button == pageLeft){
			currentPage = Math.max(1, currentPage - 1);
			recalculateVisibleButtons();
		} else if(button == pageRight){
			currentPage = Math.min(numPages, currentPage + 1);
			recalculateVisibleButtons();
		} else {
			switch (button.id) { //TODO: clean
				case 1000:
					gui.currentEditControl = ControlRegistry.getNew("button_push", gui.control.panel);
					gui.itemConfig.variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
					gui.pushElement(gui.itemConfig);
					break;
				case 1001:
					gui.currentEditControl = ControlRegistry.getNew("switch_toggle", gui.control.panel);
					gui.itemConfig.variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
					gui.pushElement(gui.itemConfig);
					break;
				case 1002:
					gui.currentEditControl = ControlRegistry.getNew("display_7seg", gui.control.panel);
					gui.itemConfig.variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
					gui.pushElement(gui.itemConfig);
					break;
				case 1003:
					gui.currentEditControl = ControlRegistry.getNew("indicator_lamp", gui.control.panel);
					gui.pushElement(gui.linker);
					break;
				case 1004:
					gui.currentEditControl = ControlRegistry.getNew("knob_control", gui.control.panel);
					gui.itemConfig.variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
					gui.pushElement(gui.itemConfig);
					break;
				case 1005:
					gui.currentEditControl = ControlRegistry.getNew("dial_square", gui.control.panel);
					gui.itemConfig.variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
					gui.pushElement(gui.itemConfig);
					break;
				case 1006:
					gui.currentEditControl = ControlRegistry.getNew("label", gui.control.panel);
					gui.itemConfig.variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
					gui.pushElement(gui.itemConfig);
					break;
				case 1007:
					gui.currentEditControl = ControlRegistry.getNew("slider_vertical", gui.control.panel);
					gui.pushElement(gui.linker);
					break;
			}
		}
	}
	
	@Override
	protected void enableButtons(boolean enable) {
		if (enable) {
			recalculateVisibleButtons();
		} else {
			for (GuiButton b : buttons) {
				b.visible = false;
				b.enabled = false;
			}
		}
		pageLeft.visible = enable;
		pageLeft.enabled = enable;
		pageRight.visible = enable;
		pageRight.enabled = enable;
	}
}
