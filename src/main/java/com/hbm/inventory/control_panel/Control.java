package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.controls.*;
import com.hbm.inventory.control_panel.controls.configs.SubElementBaseConfig;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.Map.Entry;

public abstract class Control {

	static final String PLACEHOLDER_META_TAG = "ntmPlaceholderMeta";
	static final String PLACEHOLDER_BOX_MIN_X = "boxMinX";
	static final String PLACEHOLDER_BOX_MIN_Y = "boxMinY";
	static final String PLACEHOLDER_BOX_MAX_X = "boxMaxX";
	static final String PLACEHOLDER_BOX_MAX_Y = "boxMaxY";
	static final String PLACEHOLDER_SIZE_HEIGHT = "sizeHeight";
	static final String PLACEHOLDER_HAS_BOUNDING_BOX = "hasBoundingBox";
	static final String PLACEHOLDER_BOUNDS_MIN_X = "boundsMinX";
	static final String PLACEHOLDER_BOUNDS_MIN_Y = "boundsMinY";
	static final String PLACEHOLDER_BOUNDS_MIN_Z = "boundsMinZ";
	static final String PLACEHOLDER_BOUNDS_MAX_X = "boundsMaxX";
	static final String PLACEHOLDER_BOUNDS_MAX_Y = "boundsMaxY";
	static final String PLACEHOLDER_BOUNDS_MAX_Z = "boundsMaxZ";

	public String name;
	public final String registryName;
	public ControlPanel panel;
	//Set of block positions this control is connected to. When an event is sent, it gets sent to each one
	public List<BlockPos> connectedSet = new ArrayList<>();
	//A map of event names to node system for events this control is sending out to connected blocks
	public Map<String, NodeSystem> sendNodeMap = new Object2ObjectLinkedOpenHashMap<>();
	//A map of event names to node systems for events this control is receiving
	public Map<String, NodeSystem> receiveNodeMap = new Object2ObjectLinkedOpenHashMap<>();
	//A map of all variables, either used internally by the control or in the node systems
	public Map<String, DataValue> vars = new Object2ObjectLinkedOpenHashMap<>();
	public Map<String, DataValue> varsPrev = new Object2ObjectLinkedOpenHashMap<>();
	//A set of the custom variables the user is allowed to remove
	public Set<String> customVarNames = new ObjectOpenHashSet<>();
	// map of (static) initial configurations for a control e.g. color, size
	public Map<String, DataValue> configMap = new Object2ObjectLinkedOpenHashMap<>();
	public float posX;
	public float posY;


	public Control(String name,String registryName,ControlPanel panel){
		this.name = name;
		this.registryName = registryName;
		this.panel = panel;
	}

	public abstract ControlType getControlType();

	public abstract float[] getSize();

	@SideOnly(Side.CLIENT)
	public SubElementBaseConfig getConfigSubElement(GuiControlEdit gui,Map<String, DataValue> configs) {
		return new SubElementBaseConfig(gui,configs);
	}
	public Map<String, DataValue> getConfigs() {
		return configMap;
	}
	public void applyConfigs(Map<String, DataValue> configs) {
		configMap = new Object2ObjectLinkedOpenHashMap<>(configs);
		refreshConfigs();
	}

	public void refreshConfigs() {
		onConfigMapChanged();
	}

	protected void onConfigMapChanged() {
	}

	public void renderBatched(){};
	public void render(){};
	public List<String> getOutEvents(){return Collections.emptyList();};
	public List<String> getInEvents(){return Arrays.asList("tick", "initialize", "redstone_input");};
	@SideOnly(Side.CLIENT)
	public abstract IModelCustom getModel();
	@SideOnly(Side.CLIENT)
	public abstract ResourceLocation getGuiTexture();

	@SideOnly(Side.CLIENT)
	protected final void appendGuiQuad(NTMBufferBuilder buf,float minX,float minY,float maxX,float maxY,float minU,float minV,float maxU,float maxV,int packedColor) {
		buf.appendPositionTexColorQuadUnchecked(
				minX, maxY, 0.0D, minU, maxV, packedColor,
				maxX, maxY, 0.0D, maxU, maxV, packedColor,
				maxX, minY, 0.0D, maxU, minV, packedColor,
				minX, minY, 0.0D, minU, minV, packedColor
		);
	}

	@SideOnly(Side.CLIENT)
	public void renderControl(float[] renderBox,Control selectedControl,GuiControlEdit gui) {
		NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexColorQuads(1);
		int packedColor = NTMBufferBuilder.packColor(1.0F, this == selectedControl ? 0.8F : 1.0F, 1.0F, 1.0F);
		appendGuiQuad(buf, renderBox[0], renderBox[1], renderBox[2], renderBox[3], 0.0F, 0.0F, 1.0F, 1.0F, packedColor);
		NTMImmediate.INSTANCE.draw();
	}

	public AxisAlignedBB getBoundingBox() {
		float width = getSize()[0];
		float length = getSize()[1];
		float height = getSize()[2];
		// offset to fix placement position error for controls not 1x1.
		return new AxisAlignedBB(-width/2, 0, -length/2, width/2, height, length/2).offset(posX+((width>1?Math.abs(1-width)/2:(width-1)/2)), 0, posY+((length>1)? Math.abs(1-length)/2 : (length-1)/2));
//				.offset(posX+((width>1)?Math.abs(1-width/2):0), 0, posY+Math.abs(1-length)/2);
//		GlStateManager.translate((width>1)? Math.abs(1-width)/2 : (width-1)/2, 0, (length>1)? Math.abs(1-length)/2 : 0);
	}

	public float[] getBox() {
		float[] box = new float[4];
		fillBox(box);
		return box;
	}

	public void fillBox(float[] box) {
		float width = getSize()[0];
		float length = getSize()[1];
		box[0] = posX;
		box[1] = posY;
		box[2] = posX + width;
		box[3] = posY + length;
	}

	public abstract Control newControl(ControlPanel panel);

	public abstract void populateDefaultNodes(List<ControlEvent> receiveEvents);

	public void receiveEvent(ControlEvent evt){
		NodeSystem sys = receiveNodeMap.get(evt.name);
		if(sys != null){
			sys.resetCachedValues();
			sys.receiveEvent(panel, this, evt);
		}
	}
	
	public DataValue getVar(String name){
		return vars.getOrDefault(name, new DataValueFloat(0));
	}
	
	public DataValue getGlobalVar(String name){
		return panel.getVar(name);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag){
		tag.setString("name", registryName);
		tag.setString("myName", name);
		NBTTagCompound vars = new NBTTagCompound();
		for(Entry<String, DataValue> e : this.vars.entrySet()) {
			vars.setTag(e.getKey(), e.getValue().writeToNBT());
		}
		tag.setTag("vars", vars);
		
		NBTTagCompound sendNodes = new NBTTagCompound();
		for(Entry<String, NodeSystem> e : sendNodeMap.entrySet()){
			NBTTagCompound eventNodeMap = e.getValue().writeToNBT(new NBTTagCompound());
			sendNodes.setTag(e.getKey(), eventNodeMap);
		}
		tag.setTag("SN", sendNodes);
		
		NBTTagCompound receiveNodes = new NBTTagCompound();
		for(Entry<String, NodeSystem> e : receiveNodeMap.entrySet()){
			receiveNodes.setTag(e.getKey(), e.getValue().writeToNBT(new NBTTagCompound()));
		}
		tag.setTag("RN", receiveNodes);
		
		NBTTagCompound customVarNames = new NBTTagCompound();
		int i = 0;
		for(String s : this.customVarNames){
			customVarNames.setString("var" + i, s);
			i++;
		}
		tag.setTag("customvars", customVarNames);
		
		NBTTagCompound connectedSet = new NBTTagCompound();
		for(i = 0; i < this.connectedSet.size(); i ++){
			connectedSet.setInteger("px"+i, this.connectedSet.get(i).getX());
			connectedSet.setInteger("py"+i, this.connectedSet.get(i).getY());
			connectedSet.setInteger("pz"+i, this.connectedSet.get(i).getZ());
		}
		tag.setTag("conset", connectedSet);
		
		tag.setFloat("X", posX);
		tag.setFloat("Y", posY);

		NBTTagCompound configs = new NBTTagCompound();
		for (Entry<String, DataValue> e : configMap.entrySet()) {
			configs.setTag(e.getKey(), e.getValue().writeToNBT());
		}
		tag.setTag("configs", configs);
		NBTTagCompound placeholderMeta = new NBTTagCompound();
		float[] box = getBox();
		if(box != null && box.length >= 4) {
			placeholderMeta.setFloat(PLACEHOLDER_BOX_MIN_X, box[0]);
			placeholderMeta.setFloat(PLACEHOLDER_BOX_MIN_Y, box[1]);
			placeholderMeta.setFloat(PLACEHOLDER_BOX_MAX_X, box[2]);
			placeholderMeta.setFloat(PLACEHOLDER_BOX_MAX_Y, box[3]);
		}
		float[] size = getSize();
		if(size != null && size.length >= 3) {
			placeholderMeta.setFloat(PLACEHOLDER_SIZE_HEIGHT, Math.max(size[2], 0.1F));
		}
		AxisAlignedBB boundingBox = getBoundingBox();
		placeholderMeta.setBoolean(PLACEHOLDER_HAS_BOUNDING_BOX, boundingBox != null);
		if(boundingBox != null) {
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MIN_X, boundingBox.minX);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MIN_Y, boundingBox.minY);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MIN_Z, boundingBox.minZ);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MAX_X, boundingBox.maxX);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MAX_Y, boundingBox.maxY);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MAX_Z, boundingBox.maxZ);
		}
		tag.setTag(PLACEHOLDER_META_TAG, placeholderMeta);

		return tag;
	}
	
	public void readFromNBT(NBTTagCompound tag){
		if(tag.hasKey("myName")) {
			this.name = tag.getString("myName");
		}

		NBTTagCompound vars = tag.getCompoundTag("vars");
		for(String k : vars.getKeySet()) {
			NBTBase base = vars.getTag(k);
			DataValue val = DataValue.newFromNBT(base);
			if(val == null) {
				throw new IllegalStateException("Failed to deserialize variable '" + k + "' for control '" + registryName + "'");
			}
			this.vars.put(k, val);
		}
		
		sendNodeMap.clear();
		receiveNodeMap.clear();
		customVarNames.clear();
		connectedSet.clear();

		NBTTagCompound customVarNames = tag.getCompoundTag("customvars");
		for(int i = 0; i < customVarNames.getKeySet().size(); i ++){
			this.customVarNames.add(customVarNames.getString("var"+i));
		}

		NBTTagCompound connectedSet = tag.getCompoundTag("conset");
		for(int i = 0; i < connectedSet.getKeySet().size()/3; i ++){
			int x = connectedSet.getInteger("px"+i);
			int y = connectedSet.getInteger("py"+i);
			int z = connectedSet.getInteger("pz"+i);
			this.connectedSet.add(new BlockPos(x, y, z));
		}

		NBTTagCompound sendNodes = tag.getCompoundTag("SN");
		for(String s : sendNodes.getKeySet()){
			NodeSystem sys = new NodeSystem(this);
			sendNodeMap.put(s, sys);
			sys.readFromNBT(sendNodes.getCompoundTag(s));
		}
		NBTTagCompound receiveNodes = tag.getCompoundTag("RN");
		for(String s : receiveNodes.getKeySet()){
			NodeSystem sys = new NodeSystem(this);
			receiveNodeMap.put(s, sys);
			sys.readFromNBT(receiveNodes.getCompoundTag(s));
		}

		this.posX = tag.getFloat("X");
		this.posY = tag.getFloat("Y");

		NBTTagCompound configs = tag.getCompoundTag("configs");
		for (String e : configs.getKeySet()) {
			DataValue value = DataValue.newFromNBT(configs.getTag(e));
			if(value == null) {
				throw new IllegalStateException("Failed to deserialize config '" + e + "' for control '" + registryName + "'");
			}
			configMap.put(e, value);
		}
	}


}
