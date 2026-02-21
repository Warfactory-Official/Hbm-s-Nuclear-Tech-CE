package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.controls.ControlType;
import com.hbm.render.loader.IModelCustom;
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

	public String name;
	public ControlPanel panel;
	//Set of block positions this control is connected to. When an event is sent, it gets sent to each one
	public List<BlockPos> connectedSet = new ArrayList<>();
	//A map of event names to node system for events this control is sending out to connected blocks
	public Map<String, NodeSystem> sendNodeMap = new HashMap<>();
	//A map of event names to node systems for events this control is receiving
	public Map<String, NodeSystem> receiveNodeMap = new HashMap<>();
	//A map of all variables, either used internally by the control or in the node systems
	public Map<String, DataValue> vars = new HashMap<>();
	public Map<String, DataValue> varsPrev = new HashMap<>();
	//A set of the custom variables the user is allowed to remove
	public Set<String> customVarNames = new HashSet<>();
	// map of (static) initial configurations for a control e.g. color, size
	public Map<String, DataValue> configMap = new HashMap<>();
	public float posX;
	public float posY;


	public Control(String name, ControlPanel panel){
		this.name = name;
		this.panel = panel;
	}

	public abstract ControlType getControlType();

	public abstract float[] getSize();

	public Map<String, DataValue> getConfigs() {
		return configMap;
	}
	public void applyConfigs(Map<String, DataValue> configs) {
		configMap = configs;
	}

	public void renderBatched(){};
	public void render(){};
	public List<String> getOutEvents(){return Collections.emptyList();};
	public List<String> getInEvents(){return Arrays.asList("tick", "initialize");};
	@SideOnly(Side.CLIENT)
	public abstract IModelCustom getModel();
	@SideOnly(Side.CLIENT)
	public abstract ResourceLocation getGuiTexture();

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
		float width = getSize()[0];
		float length = getSize()[1];
		return new float[] {posX, posY, posX + width, posY + length};
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
		tag.setString("name", ControlRegistry.getName(this.getClass()));
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

		return tag;
	}
	
	public void readFromNBT(NBTTagCompound tag){
		NBTTagCompound vars = tag.getCompoundTag("vars");
		for(String k : vars.getKeySet()) {
			NBTBase base = vars.getTag(k);
			DataValue val = DataValue.newFromNBT(base);
			if(val != null) {
				this.vars.put(k, val);
			}
		}
		
		sendNodeMap.clear();
		receiveNodeMap.clear();
		
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
		
		this.posX = tag.getFloat("X");
		this.posY = tag.getFloat("Y");

		NBTTagCompound configs = tag.getCompoundTag("configs");
		for (String e : configs.getKeySet()) {
			configMap.put(e, DataValue.newFromNBT(configs.getTag(e)));
		}
	}


}
