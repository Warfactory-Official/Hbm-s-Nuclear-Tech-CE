package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.inventory.control_panel.modular.INodeMenuCreator;
import com.hbm.inventory.control_panel.modular.NTMControlPanelRegistry;
import com.hbm.inventory.control_panel.nodes.*;
import com.hbm.main.ClientProxy;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.util.*;

public class SubElementNodeEditor extends SubElement {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_placement_front.png");
	public static ResourceLocation grid = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/grid.png");
	
	public GuiButton btn_back;
	public GuiButton btn_variables;

	public ItemList addMenu;

	public NodeSystem currentSystem;
	public Deque<NodeSystem> systemHistoryStack = new ArrayDeque<>();

	public ControlEvent currentEvent;
	public List<ControlEvent> sendEvents;
	public boolean gridGrabbed = false;
	public float gridX = 0;
	public float gridY = 0;
	public float gridScale = 1;
	public float prevMouseX;
	public float prevMouseY;
	
	public SubElementNodeEditor(GuiControlEdit gui){
		super(gui);
	}
	
	protected void setData(Map<String, NodeSystem> map, ControlEvent c, List<ControlEvent> sendEvents){
		currentSystem = map.computeIfAbsent(c.name, e -> new NodeSystem(gui.currentEditControl, this));
		currentSystem.nodeEditor = this;
		currentSystem.gui = gui;
		currentSystem.activeNode = null;
		currentSystem.selectedNodes = new ArrayList<>();
		currentSystem.drag = false;
		currentSystem.dragDist = 0;
		
		currentEvent = c;
		gridX = 0;
		gridY = 0;
		gridScale = 1;
		this.sendEvents = sendEvents;
	}

	private void descendSubsystem(Node node) {
		systemHistoryStack.push(currentSystem);
		currentSystem = currentSystem.subSystems.get(node);

		currentSystem.nodeEditor = this;
		currentSystem.gui = gui;
		currentSystem.activeNode = null;
		currentSystem.selectedNodes = new ArrayList<>();
		currentSystem.drag = false;
		currentSystem.dragDist = 0;
	}
	
	@Override
	protected void initGui(){
		int cX = gui.width/2;
		int cY = gui.height/2;
		btn_back = gui.addButton(new GuiButton(gui.currentButtonId(), gui.getGuiLeft()+7, gui.getGuiTop()+13, 30, 20, "Back"));
		btn_variables = gui.addButton(new GuiButton(gui.currentButtonId(), gui.getGuiLeft()+54, gui.getGuiTop()+13, 58, 20, "Variables"));
		super.initGui();
	}
	
	@Override
	protected void keyTyped(char typedChar, int code){
		if(code == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
			if(addMenu != null){
				addMenu.close();
			}
			addMenu = new ItemList(gui.mouseX, gui.mouseY, 32, s -> {
				final float x = (gui.mouseX-gui.getGuiLeft())*gridScale + gui.getGuiLeft() + gridX;
				final float y = (gui.mouseY-gui.getGuiTop())*gridScale + gui.getGuiTop() - gridY;
				List<INodeMenuCreator> controllers = NTMControlPanelRegistry.addMenuControl.get(s.substring(("{expandable}").length()));
				if (controllers != null) {
					ItemList list = new ItemList(0, 0, 32,s2 -> {
						for (INodeMenuCreator controller : controllers) {
							Node node = controller.selectItem(s2,x,y,this);
							if (node != null) {
								addMenu.close();
								addMenu = null;
								currentSystem.addNode(node);
								currentSystem.activeNode = node;
								break;
							}
						}
						return null;
					});
					for (INodeMenuCreator controller : controllers)
						controller.addItems(list,x,y,this);
					return list;
				}
				return null;
			});
			for (String item : NTMControlPanelRegistry.addMenuCategories)
				addMenu.addItems("{expandable}"+item);
		}
		if(code == Keyboard.KEY_DELETE || code == Keyboard.KEY_X){
			List<Node> selected = new ArrayList<>(currentSystem.selectedNodes);
			for(Node n : selected){
				currentSystem.removeNode(n);
			}
		}
		if(currentSystem != null){
			currentSystem.keyTyped(typedChar, code);
		}
	}
	
	@Override
	protected void renderBackground(){
		gui.mc.getTextureManager().bindTexture(texture);
		gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
	}
	
	@Override
	protected void drawScreen(){
		float dWheel = Mouse.getDWheel();
		float dScale = dWheel*gridScale*0.00075F;
		
		//Correction so we scale around mouse position
		float prevX = (gui.mouseX-gui.getGuiLeft())*gridScale;
		float prevY = (gui.mouseY-gui.getGuiTop())*gridScale;
		gridScale = MathHelper.clamp(gridScale-dScale, 0.25F, 2.5F);
		float currentX = (gui.mouseX-gui.getGuiLeft())*gridScale;
		float currentY = (gui.mouseY-gui.getGuiTop())*gridScale;
		gridX += prevX-currentX;
		gridY -= prevY-currentY;
		
		if(gridGrabbed){
			float dX = gui.mouseX-prevMouseX;
			float dY = gui.mouseY-prevMouseY;
			gridX -= dX*gridScale;
			gridY += dY*gridScale;
			prevMouseX = gui.mouseX;
			prevMouseY = gui.mouseY;
		}
		GlStateManager.disableLighting();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		int cX = gui.width/2;
		int cY = gui.height/2;
		int minX = (cX-72)*gui.res.getScaleFactor();
		int minY = (cY-114)*gui.res.getScaleFactor();
		int maxX = (cX+120)*gui.res.getScaleFactor();
		int maxY = (cY+78)*gui.res.getScaleFactor();
		//System.out.println(cY);
		//System.out.println(Minecraft.getMinecraft().displayHeight/2);
		GL11.glScissor(minX, minY, maxX-minX, maxY-minY);
		
		////GlStateManager.matrixMode(GL11.GL_TEXTURE);
		//GlStateManager.pushMatrix();
		gui.mc.getTextureManager().bindTexture(grid);
		float x = gridX/gui.getXSize();
		float y = -gridY/gui.getYSize();
		//float scalePointX = x + gui.mouseX/gui.getXSize();
		//float scalePointY = y + gui.mouseY/gui.getYSize();
		//GlStateManager.translate(scalePointX, scalePointY, 0);
		//GL11.glScaled(gridScale, gridScale, 0);
		//GlStateManager.translate(-scalePointX, -scalePointY, 0);
		NTMRenderHelper.drawGuiRectColor(gui.getGuiLeft(), gui.getGuiTop(), x, y, gui.getXSize(), gui.getYSize(), gridScale+x, gridScale+y, 0.2F, 0.2F, 0.2F, 1);
		//GlStateManager.popMatrix();
		//GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
		GlStateManager.pushMatrix();
		
		float spX = gui.getGuiLeft();
		float spY = gui.getGuiTop();
		GlStateManager.translate(spX, spY, 0);
		GL11.glScaled(1/gridScale, 1/gridScale, 1/gridScale);
		GlStateManager.translate(-spX, -spY, 0);
		GlStateManager.translate(-gridX, gridY, 0);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
		Matrix4f mat = new Matrix4f();
		mat.load(ClientProxy.AUX_GL_BUFFER);
		//System.out.println(gui.getGuiLeft() + " " + gui.getGuiTop() + " " + mat.m30 + " " + mat.m31);
		ClientProxy.AUX_GL_BUFFER.rewind();
		float gridMX = (gui.mouseX-gui.getGuiLeft())*gridScale + gui.getGuiLeft() + gridX;
		float gridMY = (gui.mouseY-gui.getGuiTop())*gridScale + gui.getGuiTop() - gridY;
		currentSystem.render(gridMX, gridMY);
		GlStateManager.popMatrix();
		if(addMenu != null){
			addMenu.render(gui.mouseX, gui.mouseY);
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button){
		if(addMenu != null && button == 0){
			if(NTMRenderHelper.intersects2DBox(mouseX, mouseY, addMenu.getBoundingBox())){
				addMenu.mouseClicked(mouseX, mouseY);
			} else {
				addMenu.close();
				addMenu = null;
			}
		} else if(button == 0){
			// doing this here for now cus i want buttons to be able to make gui changes
			NodeElement pressed = currentSystem.getNodeElementPressed(mouseX, mouseY);
			if (pressed != null) {
				switch (pressed.name) {
					case "Edit Body": {
						descendSubsystem(pressed.parent);
					}
				}
			}
			currentSystem.onClick(mouseX, mouseY);
		}
		if(button == 2){
			gridGrabbed = true;
			prevMouseX = gui.mouseX;
			prevMouseY = gui.mouseY;
		}
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state){
		if(state == 2){
			gridGrabbed = false;
		}
		if(addMenu == null && state == 0){
			currentSystem.clickReleased(mouseX, mouseY);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button){
		if(button == btn_back){
			if(currentSystem != null){
				currentSystem.removeClientData();
				currentSystem = null;

				if (!systemHistoryStack.isEmpty()) {
					currentSystem = systemHistoryStack.getFirst();
					systemHistoryStack.pop();
					return;
				}
			}
			gui.popElement();
		}
		if (button == btn_variables) {
			gui.pushElement(gui.variables);
		}
	}
	
	@Override
	public void onClose(){
		if(currentSystem != null){
			currentSystem.removeClientData();
			currentSystem = null;
		}
	}
	
	@Override
	protected void enableButtons(boolean enable){
		btn_back.enabled = enable;
		btn_back.visible = enable;
		btn_variables.enabled = enable;
		btn_variables.visible = enable;
	}
	
}
