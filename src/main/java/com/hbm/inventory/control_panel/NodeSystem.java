package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.inventory.control_panel.nodes.*;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.NTMImmediate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NodeSystem {

	public static final ResourceLocation node_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/node.png");
	private static final String CLIPBOARD_ANCHOR_X = "clipboardAnchorX";
	private static final String CLIPBOARD_ANCHOR_Y = "clipboardAnchorY";
	
	@SideOnly(Side.CLIENT)
	public SubElementNodeEditor nodeEditor;
	@SideOnly(Side.CLIENT)
	public GuiControlEdit gui;
	@SideOnly(Side.CLIENT)
	public Node activeNode;
	@SideOnly(Side.CLIENT)
	public List<Node> selectedNodes;
	@SideOnly(Side.CLIENT)
	public NodeConnection connectionInProgress;
	@SideOnly(Side.CLIENT)
	public ITypableNode currentTypingBox;
	@SideOnly(Side.CLIENT)
	protected boolean drag;
	@SideOnly(Side.CLIENT)
	protected float dragDist;
	@SideOnly(Side.CLIENT)
	protected float lastMouseX;
	@SideOnly(Side.CLIENT)
	protected float lastMouseY;

	public Control parent;
	public List<Node> nodes = new ArrayList<>();
	public List<NodeOutput> outputNodes = new ArrayList<>();
	private Map<String, DataValue> vars = new Object2ObjectOpenHashMap<>();

	// an array of subsystems owned by the various nodes sharing a system layer (sublayering is then done recursively)
	// ○|￣|_   <-- me
	public Map<Node, NodeSystem> subSystems = new Object2ObjectOpenHashMap<>();

	public NodeSystem(Control parent){
		this.parent = parent;
	}
	
	public NodeSystem(Control parent, SubElementNodeEditor gui){
		this(parent);
		nodeEditor = gui;
		this.gui = gui.gui;
		activeNode = null;
		selectedNodes = new ArrayList<>();
		drag = false;
		dragDist = 0;
	}
	
	public void setVar(String name, DataValue val){
		vars.put(name, val);
	}
	
	public DataValue getVar(String name){
		DataValue val = vars.get(name);
		if(val == null)
			return new DataValueFloat(0);
		return val;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		NBTTagCompound nodes = new NBTTagCompound();

		for (int i = 0; i < this.nodes.size(); i ++) {
			Node node = this.nodes.get(i);
			NBTTagCompound nodeTag = node.writeToNBT(new NBTTagCompound(), this);
			if (node instanceof NodeFunction) {
				nodeTag.setTag("SS", subSystems.get(node).writeToNBT(new NBTTagCompound()));
			}
			nodes.setTag("n"+i, nodeTag);
		}
		tag.setTag("N", nodes);

		NBTTagCompound vars = new NBTTagCompound();
		for (Entry<String, DataValue> e : this.vars.entrySet()) {
			vars.setTag(e.getKey(), e.getValue().writeToNBT());
		}
		tag.setTag("V", vars);

		return tag;
	}

	public void readFromNBT(NBTTagCompound tag) {
		this.nodes.clear();
		this.outputNodes.clear();
		this.subSystems.clear();

		NBTTagCompound nodes = tag.getCompoundTag("N");
		for (int i = 0; i < nodes.getKeySet().size(); i ++) {
			NBTTagCompound nodeTag = nodes.getCompoundTag("n"+i);
			Node node = Node.nodeFromNBT(nodeTag, this);

			if (node instanceof NodeOutput) {
				outputNodes.add((NodeOutput) node);
			}
			if (node instanceof NodeFunction && nodeTag.hasKey("SS")) {
				NodeSystem subsystem = new NodeSystem(parent);
				subsystem.readFromNBT(nodeTag.getCompoundTag("SS"));
				subSystems.put(node, subsystem);
			}
			this.nodes.add(node);
		}
		for (int i = 0; i < this.nodes.size(); i ++) {
			this.nodes.get(i).readFromNBT(nodes.getCompoundTag("n"+i), this);
		}

		NBTTagCompound vars = tag.getCompoundTag("V");
		for (String k : vars.getKeySet()) {
			NBTBase base = vars.getTag(k);
			DataValue val = DataValue.newFromNBT(base);
			if (val != null) {
				this.vars.put(k, val);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void removeClientData(){
		nodeEditor = null;
		gui = null;
		activeNode = null;
		selectedNodes.clear();
		connectionInProgress = null;
		currentTypingBox = null;
	}

	@SideOnly(Side.CLIENT)
	public void render(float mX, float mY){
		if(drag && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
			float distX = gui.mouseX - lastMouseX;
			float distY = gui.mouseY - lastMouseY;
			dragDist += Math.sqrt(distX*distX + distY*distY);
			for(Node n : selectedNodes){
				n.setPosition(n.posX+(gui.mouseX-lastMouseX)*nodeEditor.gridScale, n.posY+(gui.mouseY-lastMouseY)*nodeEditor.gridScale);
			}
			for (Node n : nodes) {
				n.setPosition(n.posX, n.posY); // to fix elements not rendering in edit mode
			}
			lastMouseX = gui.mouseX;
			lastMouseY = gui.mouseY;
		}
		GlStateManager.disableTexture2D();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.glLineWidth(3);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		NTMImmediate.INSTANCE.beginPosition(GL11.GL_LINES, 0);
		float nodeMx = mX;
		float nodeMy = mY;
		if(connectionInProgress != null){
			end:
			for(int i = nodes.size()-1; i >= 0; i --){
				Node n = nodes.get(i);
				if(NTMRenderHelper.intersects2DBox(mX, mY, n.getExtendedBoundingBox())){
					for(NodeConnection c : (connectionInProgress.isInput ? n.outputs : n.inputs)){
						if(connectionInProgress.parent != c.parent && NTMRenderHelper.intersects2DBox(mX, mY, c.getPortBox())){
							float[] center = NTMRenderHelper.getBoxCenter(c.getPortBox());
							nodeMx = center[0];
							nodeMy = center[1];
							break end;
						}
					}
				}
			}
		}
		for(Node node : nodes){
			node.drawConnections(nodeMx, nodeMy);
		}
		NTMImmediate.INSTANCE.draw();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GlStateManager.glLineWidth(2);
		GlStateManager.enableTexture2D();
		for(Node node : nodes){
			node.render(mX, mY, activeNode == node, selectedNodes.contains(node));
		}
	}
	
	public void addNode(Node n){
		nodes.add(n);

		if (n instanceof NodeFunction && !subSystems.containsKey(n)) {
			subSystems.put(n, new NodeSystem(parent));
		}
		if (n instanceof NodeOutput) {
			outputNodes.add((NodeOutput) n);
		}
	}
	
	public void removeNode(Node n){
		if(activeNode == n)
			activeNode = null;
		selectedNodes.remove(n);
		outputNodes.remove(n);
		subSystems.remove(n);
		nodes.remove(n);
	}

	public boolean hasSelectedNodes() {
		return selectedNodes != null && !selectedNodes.isEmpty();
	}

	public NBTTagCompound writeSelectedToNBT() {
		if(!hasSelectedNodes())
			return null;

		List<Node> orderedSelection = new ArrayList<>();
		for(Node node : nodes) {
			if(selectedNodes.contains(node)) {
				orderedSelection.add(node);
			}
		}
		if(orderedSelection.isEmpty())
			return null;

		NodeSystem subset = new NodeSystem(parent);
		subset.nodes.addAll(orderedSelection);
		subset.vars.putAll(vars);
		for(Node node : orderedSelection) {
			if(node instanceof NodeOutput) {
				subset.outputNodes.add((NodeOutput) node);
			}
			if(node instanceof NodeFunction && subSystems.containsKey(node)) {
				subset.subSystems.put(node, subSystems.get(node));
			}
		}

		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		for(Node node : orderedSelection) {
			minX = Math.min(minX, node.posX);
			minY = Math.min(minY, node.posY);
		}

		NBTTagCompound tag = subset.writeToNBT(new NBTTagCompound());
		tag.setFloat(CLIPBOARD_ANCHOR_X, minX);
		tag.setFloat(CLIPBOARD_ANCHOR_Y, minY);
		return tag;
	}

	public List<Node> pasteFromNBT(NBTTagCompound tag, float x, float y) {
		if(tag == null || tag.isEmpty())
			return new ArrayList<>();

		NodeSystem pastedSystem = new NodeSystem(parent);
		pastedSystem.readFromNBT(tag);

		float anchorX = tag.hasKey(CLIPBOARD_ANCHOR_X) ? tag.getFloat(CLIPBOARD_ANCHOR_X) : 0;
		float anchorY = tag.hasKey(CLIPBOARD_ANCHOR_Y) ? tag.getFloat(CLIPBOARD_ANCHOR_Y) : 0;
		float dX = x - anchorX;
		float dY = y - anchorY;

		List<Node> pastedNodes = new ArrayList<>(pastedSystem.nodes.size());
		for(Node node : pastedSystem.nodes) {
			node.setPosition(node.posX + dX, node.posY + dY);
			addNode(node);
			if(node instanceof NodeFunction) {
				NodeSystem subsystem = pastedSystem.subSystems.get(node);
				if(subsystem != null) {
					subSystems.put(node, subsystem);
				}
			}
			pastedNodes.add(node);
		}
		return pastedNodes;
	}

	public boolean canPasteFromNBT(NBTTagCompound tag, ControlEvent evt, boolean sendGraph) {
		if(tag == null || tag.isEmpty() || evt == null)
			return false;

		NodeSystem pastedSystem = new NodeSystem(parent);
		pastedSystem.readFromNBT(tag);
		Map<String, DataValue> allowedVars = new Object2ObjectOpenHashMap<>(evt.vars);
		if(sendGraph) {
			allowedVars.put("to index", new DataValueFloat(0));
		} else {
			allowedVars.put("tag", new DataValueString(""));
			allowedVars.put("from index", new DataValueFloat(0));
		}
		return pastedSystem.hasCompatibleGraphNodes(sendGraph)
				&& pastedSystem.hasCompatibleEventInputs(allowedVars, sendGraph ? "to index" : "from index")
				&& pastedSystem.hasCompatibleVariableReferences();
	}

	private boolean hasCompatibleGraphNodes(boolean sendGraph) {
		for(Node node : nodes) {
			if(sendGraph) {
				if(node instanceof NodeCancelEvent) {
					return false;
				}
			} else if(node instanceof NodeEventBroadcast) {
				return false;
			}
			if(node instanceof NodeFunction) {
				NodeSystem subsystem = subSystems.get(node);
				if(subsystem != null && !subsystem.hasCompatibleGraphNodes(sendGraph)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasCompatibleEventInputs(Map<String, DataValue> allowedVars, String indexName) {
		for(Node node : nodes) {
			if(node instanceof NodeInput input && !isCompatibleEventInput(input, allowedVars, indexName)) {
				return false;
			}
			if(node instanceof NodeFunction) {
				NodeSystem subsystem = subSystems.get(node);
				if(subsystem != null && !subsystem.hasCompatibleEventInputs(allowedVars, indexName)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasCompatibleVariableReferences() {
		for(Node node : nodes) {
			if(node instanceof NodeGetVar getVar && !hasCompatibleVariableReference(getVar.global, getVar.varName, getVar.outputs)) {
				return false;
			}
			if(node instanceof NodeSetVar setVar && !hasCompatibleVariableReference(setVar.global, setVar.varName, setVar.inputs)) {
				return false;
			}
			if(node instanceof NodeFunction) {
				NodeSystem subsystem = subSystems.get(node);
				if(subsystem != null && !subsystem.hasCompatibleVariableReferences()) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasCompatibleVariableReference(boolean global, String varName, List<NodeConnection> ports) {
		if(varName.isEmpty()) {
			return true;
		}
		DataValue value = global ? parent.panel.globalVars.get(varName) : parent.vars.get(varName);
		if(value == null || ports.isEmpty()) {
			return false;
		}
		DataValue.DataType expectedType = ports.get(0).type;
		return expectedType == DataValue.DataType.GENERIC || expectedType == value.getType();
	}

	private boolean isCompatibleEventInput(NodeInput input, Map<String, DataValue> allowedVars, String indexName) {
		if(!"Event Data".equals(input.name)) {
			return true;
		}
		for(NodeConnection output : input.outputs) {
			if(indexName.equals(output.name)) {
				if(output.type != DataValue.DataType.NUMBER) {
					return false;
				}
				continue;
			}
			DataValue allowed = allowedVars.get(output.name);
			if(allowed == null || allowed.getType() != output.type) {
				return false;
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public NodeElement getNodeElementPressed(float x, float y) {
		lastMouseX = gui.mouseX;
		lastMouseY = gui.mouseY;
		float gridMX = (gui.mouseX-gui.getGuiLeft())*nodeEditor.gridScale + gui.getGuiLeft() + nodeEditor.gridX;
		float gridMY = (gui.mouseY-gui.getGuiTop())*nodeEditor.gridScale + gui.getGuiTop() - nodeEditor.gridY;
		for (int i = nodes.size()-1; i >= 0; i--) {
			for (NodeElement e : nodes.get(i).otherElements) {
				if (e instanceof NodeButton) {
					if (e.onClick(gridMX, gridMY)) {
						return e;
					}
				}
			}
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void onClick(float x, float y){
		lastMouseX = gui.mouseX;
		lastMouseY = gui.mouseY;
		float gridMX = (gui.mouseX-gui.getGuiLeft())*nodeEditor.gridScale + gui.getGuiLeft() + nodeEditor.gridX;
		float gridMY = (gui.mouseY-gui.getGuiTop())*nodeEditor.gridScale + gui.getGuiTop() - nodeEditor.gridY;
		//Click handling
		for(int i = nodes.size()-1; i >= 0; i --){
			if(nodes.get(i).onClick(gridMX, gridMY))
				return;
		}
		//Do line connection handling
		for(int i = nodes.size()-1; i >= 0; i --){
			Node n = nodes.get(i);
			if(NTMRenderHelper.intersects2DBox(gridMX, gridMY, n.getExtendedBoundingBox())){
				List<NodeConnection> union = new ArrayList<>();
				union.addAll(n.inputs);
				union.addAll(n.outputs);
				for(NodeConnection c : union){
					if(NTMRenderHelper.intersects2DBox(gridMX, gridMY, c.getPortBox())){
						if(c.connection != null){
							connectionInProgress = c.removeConnection();
							connectionInProgress.drawsLine = true;
						} else {
							connectionInProgress = c;
							c.drawsLine = true;
						}
						return;
					}
				}
			}
		}
		drag = true;
		dragDist = 0;
		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		if(!shift && selectedNodes.size() <= 1){
			selectedNodes.clear();
			activeNode = null;
		}
		boolean clear = true;
		for(int i = nodes.size()-1; i >= 0; i --){
			Node n = nodes.get(i);
			boolean intersectsBox = NTMRenderHelper.intersects2DBox(gridMX, gridMY, n.getBoundingBox());
			for(NodeConnection c : n.inputs){
				if(c.enumSelector != null){
					if(currentTypingBox != null){
						currentTypingBox.stopTyping();
						currentTypingBox = null;
					}
					if(c.enumSelector.onClick(gridMX, gridMY))
						return;
				}
				if(intersectsBox && NTMRenderHelper.intersects2DBox(gridMX, gridMY, c.getValueBox())){
					if(currentTypingBox != c && c.connection == null){
						c.isTyping = true;
						c.startTyping();
						if(currentTypingBox != null){
							currentTypingBox.stopTyping();
						}
						currentTypingBox = c;
					}
				}
			}
			for (NodeElement o : n.otherElements) {
				if (o instanceof NodeTextBox b) {
					if(intersectsBox && NTMRenderHelper.intersects2DBox(gridMX, gridMY, b.getValueBox())){
						if(currentTypingBox != b){
							b.isTyping = true;
							b.startTyping();
							if(currentTypingBox != null){
								currentTypingBox.stopTyping();
							}
							currentTypingBox = b;
						}
					}
				}
			}
			if(intersectsBox){
				clear = false;
				if(activeNode == n && selectedNodes.size() <= 1){
					selectedNodes.remove(n);
					activeNode = null;
				} else {
					if(!selectedNodes.contains(n))
						selectedNodes.add(n);
					activeNode = n;
				}
				break;
			}
		}
		if(currentTypingBox != null && !NTMRenderHelper.intersects2DBox(gridMX, gridMY, currentTypingBox.getValueBox())){
			currentTypingBox.stopTyping();
			currentTypingBox = null;
		}
		if(clear){
			selectedNodes.clear();
			activeNode = null;
		}
	}

	@SideOnly(Side.CLIENT)
	public void clickReleased(float x, float y){
		float gridMX = (gui.mouseX-gui.getGuiLeft())*nodeEditor.gridScale + gui.getGuiLeft() + nodeEditor.gridX;
		float gridMY = (gui.mouseY-gui.getGuiTop())*nodeEditor.gridScale + gui.getGuiTop() - nodeEditor.gridY;
		if(connectionInProgress != null){
			for(int i = nodes.size()-1; i >= 0; i --){
				Node n = nodes.get(i);
				if(NTMRenderHelper.intersects2DBox(gridMX, gridMY, n.getExtendedBoundingBox())){
					for(NodeConnection c : (connectionInProgress.isInput ? n.outputs : n.inputs)){
						if(connectionInProgress.parent != c.parent && NTMRenderHelper.intersects2DBox(gridMX, gridMY, c.getPortBox())){
							c.removeConnection();
							//Only input nodes draw lines, so we don't have to maintain a connection list at each output
							if(c.isInput){
								connectionInProgress.drawsLine = false;
								c.drawsLine = true;
								c.connection = connectionInProgress.parent;
								c.connectionIndex = connectionInProgress.index;
							} else {
								connectionInProgress.connection = n;
								connectionInProgress.connectionIndex = c.index;
							}
							connectionInProgress = null;
							return;
						}
					}
				}
			}
			connectionInProgress.drawsLine = false;
			connectionInProgress = null;
		}
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && dragDist == 0){
			for(int i = nodes.size()-1; i >= 0; i --){
				Node n = nodes.get(i);
				if(NTMRenderHelper.intersects2DBox(gridMX, gridMY, n.getBoundingBox())){
					selectedNodes.clear();
					selectedNodes.add(n);
					activeNode = n;
					break;
				}
			}
		}
		drag = false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean keyTyped(char c, int key){
		if(currentTypingBox != null){
			currentTypingBox.keyTyped(c, key);
			if(!currentTypingBox.isTyping())
				currentTypingBox = null;
			return true;
		}
		return false;
	}

	public void resetCachedValues(){
		for(Node n : nodes){
			n.cacheValid = false;
		}
	}

	public void receiveEvent(ControlPanel panel, Control ctrl, ControlEvent evt) {
		resetCachedValues();
		for (Node n : nodes) {
			if (n instanceof NodeInput) {
				((NodeInput)n).setOutputFromVars(evt.vars);
			}
			if (n instanceof NodeFunction) {
				if (n.evaluate(0).getBoolean()) {
					subSystems.get(n).receiveEvent(panel, ctrl, evt);
				}
			}
		}
		for (NodeOutput o : outputNodes) {
			o.doOutput(panel.parent, ctrl.sendNodeMap, ctrl.taggedLinks);
		}
	}

}
