package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class GuiControlEdit extends GuiContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_placement_back.png");

	public float mouseX, mouseY;
	
	public TileEntityControlPanel control;
	public ContainerControlEdit container;
	
	public Deque<SubElement> subElementStack = new ArrayDeque<>();
	public SubElementPlacement placement;
	public SubElementItemChoice choice;
	public SubElementLinker linker;
	public SubElementEventEditor eventEditor;
	public SubElementNodeEditor nodeEditor;
	public SubElementPanelResize panelResize;
	public SubElementItemConfig itemConfig;
	public SubElementVariables variables;

	public Control currentEditControl;

	public ScaledResolution res;

	public boolean isEditMode = false; // when editing an existing control
	
	public GuiControlEdit(InventoryPlayer i, TileEntityControlPanel te) {
		super(new ContainerControlEdit(i, te));
		container = (ContainerControlEdit)this.inventorySlots;
		control = te;
		this.xSize = 256;
		this.ySize = 256;
		res = new ScaledResolution(Minecraft.getMinecraft());
	}
	
	@Override
	public void onGuiClosed(){
		placement.onClose();
		choice.onClose();
		linker.onClose();
		eventEditor.onClose();
		nodeEditor.onClose();
		panelResize.onClose();
		itemConfig.onClose();
		variables.onClose();
		NBTTagCompound tag = new NBTTagCompound();
		control.panel.writeToNBT(tag);
		tag.setString("full_set", "");
        PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(tag, control.getPos()));
        control.updateTransform();
		isEditMode = false;
	}
	
	@Override
	public void onResize(Minecraft mcIn, int w, int h){
		res = new ScaledResolution(Minecraft.getMinecraft());
		super.onResize(mcIn, w, h);
	}

	@Override
	public void initGui() {
		super.initGui();
		placement = new SubElementPlacement(this);
		choice = new SubElementItemChoice(this);
		linker = new SubElementLinker(this);
		eventEditor = new SubElementEventEditor(this);
		nodeEditor = new SubElementNodeEditor(this);
		panelResize = new SubElementPanelResize(this);
		itemConfig = new SubElementItemConfig(this);
		variables = new SubElementVariables(this);
		placement.initGui();
		choice.initGui();
		linker.initGui();
		eventEditor.initGui();
		nodeEditor.initGui();
		panelResize.initGui();
		itemConfig.initGui();
		variables.initGui();

		subElementStack.addFirst(placement);
		placement.enableButtons(true);
	}
	
	public FontRenderer getFontRenderer(){
		return fontRenderer;
	}
	
	@Override
	public <T extends GuiButton> T addButton(T buttonIn) {
		return super.addButton(buttonIn);
	}

	public int currentButtonId(){
		return this.buttonList.size();
	}
	
	public List<GuiButton> getButtons(){
		return buttonList;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		subElementStack.getFirst().drawScreen();
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		int sWidth = res.getScaledWidth();
        int sHeight = res.getScaledHeight();
        this.mouseX = Mouse.getX() * sWidth / (float)this.mc.displayWidth;
        this.mouseY = sHeight - Mouse.getY() * sHeight / (float)this.mc.displayHeight - 1;
		super.drawDefaultBackground();
		GlStateManager.color(1, 1, 1, 1);
		this.mc.getTextureManager().bindTexture(texture);

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		subElementStack.getFirst().renderBackground();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(!subElementStack.getFirst().lock)
			subElementStack.getFirst().actionPerformed(button);
	}
	
	protected void pushElement(SubElement e){
		subElementStack.getFirst().enableButtons(false);
		e.lock = true;
		e.enableButtons(true);
		subElementStack.addFirst(e);
	}
	
	protected void popElement(){
		SubElement e = subElementStack.removeFirst();
		e.enableButtons(false);
		subElementStack.getFirst().enableButtons(true);
	}
	
	protected void resetStack(){
		subElementStack.getFirst().enableButtons(false);
		subElementStack.clear();
		subElementStack.addFirst(placement);
		placement.enableButtons(true);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(!this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
			super.keyTyped(typedChar, keyCode);
		subElementStack.getFirst().keyTyped(typedChar, keyCode);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		subElementStack.getFirst().lock = false;
		subElementStack.getFirst().mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state){
		super.mouseReleased(mouseX, mouseY, state);
		subElementStack.getFirst().mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void updateScreen(){
		super.updateScreen();
		subElementStack.getFirst().update();
	}
	
}
