package com.hbm.inventory.control_panel;

import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.ControlPanelUpdatePacket.VarUpdate;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.util.*;
import java.util.Map.Entry;

public class ControlPanel {

	public IControllable parent;
	public List<Control> controls = new ArrayList<>();
	//Variables that were changed in the last tick
	public List<VarUpdate> changedVars = new ArrayList<>();
	//Global variables, accessible by every control as well as the owner tile entity for state like redstone
	public Map<String, DataValue> globalVars = new Object2ObjectLinkedOpenHashMap<>();
	public Map<String, DataValue> globalVarsPrev = new Object2ObjectLinkedOpenHashMap<>();
	//Client only transform for getting from panel local space to block space. Used for both rendering and figuring out which control a player clicks
	@SideOnly(Side.CLIENT)
	public Matrix4f transform;
	@SideOnly(Side.CLIENT)
	public Matrix4f inv_transform;

	public float height;
	public float angle; // RADIANS
	public float a_off;
	public float b_off;
	public float c_off;
	public float d_off;

	public ControlPanel(IControllable parent, float height, float angle, float a_off, float b_off, float c_off, float d_off) {
		this.parent = parent;
		this.height = height;
		this.angle = angle;
		this.a_off = a_off;
		this.b_off = b_off;
		this.c_off = c_off;
		this.d_off = d_off;

	}

	@SideOnly(Side.CLIENT)
	public ControlPanel setTransform(Matrix4f transform){
		this.transform = transform;
		inv_transform = new Matrix4f();
		Matrix4f.invert(transform, inv_transform);
		return this;
	}

	public void render(){
		for(Control c : controls) {
			float width = c.getSize()[0];
			float length = c.getSize()[1];
			GlStateManager.pushMatrix();
				GlStateManager.translate(((width>1?Math.abs(1-width)/2:(width-1)/2)), 0, ((length>1)? Math.abs(1-length)/2 : (length-1)/2));
				c.render();
			GlStateManager.popMatrix();
		}
	}

	public void update(){
		if(!parent.getControlWorld().isRemote){
			changedVars.clear();
			crossCheckVars(-1, globalVars, globalVarsPrev);
			for(int i = 0; i < controls.size(); i ++){
				Control c = controls.get(i);
				crossCheckVars(i, c.vars, c.varsPrev);
			}
		}
	}
	
	public void crossCheckVars(int idx, Map<String, DataValue> vars, Map<String, DataValue> varsPrev){
		for(String s : vars.keySet()){
			if(!varsPrev.containsKey(s)){
				changedVars.add(new VarUpdate(idx, s, vars.get(s).copy()));
			}
		}
		for(String s : varsPrev.keySet()) {
			if(!vars.containsKey(s)){
				changedVars.add(new VarUpdate(idx, s, null));
			} else if(!varsPrev.get(s).equals(vars.get(s))){
				changedVars.add(new VarUpdate(idx, s, vars.get(s).copy()));
			}
		}
		varsPrev.clear();
		for(Entry<String, DataValue> entry : vars.entrySet()) {
			varsPrev.put(entry.getKey(), entry.getValue().copy());
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		NBTTagCompound globalVars = new NBTTagCompound();
		for(Entry<String, DataValue> e : this.globalVars.entrySet()) {
			globalVars.setTag(e.getKey(), e.getValue().writeToNBT());
		}

		NBTTagCompound controls = new NBTTagCompound();
		for(int i = 0; i < this.controls.size(); i ++){
			controls.setTag("control" + i, this.controls.get(i).writeToNBT(new NBTTagCompound()));
		}
		
		nbt.setTag("globalvars", globalVars);
		nbt.setTag("controls", controls);
		nbt.setFloat("height", height);
		nbt.setFloat("angle", angle);
		nbt.setFloat("a_offset", a_off);
		nbt.setFloat("b_offset", b_off);
		nbt.setFloat("c_offset", c_off);
		nbt.setFloat("d_offset", d_off);
		return nbt;
	}

	public void readFromNBT(NBTTagCompound tag){
		this.globalVars.clear();
		this.controls.clear();
		NBTTagCompound globalVars = tag.getCompoundTag("globalvars");
		for(String k : globalVars.getKeySet()) {
			NBTBase base = globalVars.getTag(k);
			DataValue val = DataValue.newFromNBT(base);
			if(val != null) {
				this.globalVars.put(k, val);
			}
		}

		NBTTagCompound controls = tag.getCompoundTag("controls");
		for(int i = 0; i < controls.getKeySet().size(); i ++){
			NBTTagCompound ct = controls.getCompoundTag("control" + i);
			this.controls.add(readControl(ct));
		}

		height = tag.getFloat("height");
		angle = tag.getFloat("angle");
		a_off = tag.getFloat("a_offset");
		b_off = tag.getFloat("b_offset");
		c_off = tag.getFloat("c_offset");
		d_off = tag.getFloat("d_offset");
	}

	public Control readControl(NBTTagCompound tag) {
		String registryName = tag.getString("name");
		Control control = ControlRegistry.getNew(registryName, this);
		if(control == null) {
			return new UnknownControl(this, tag);
		}

		try {
			control.applyConfigs(readControlDataMap(tag.getCompoundTag("configs"), "config", registryName));
			control.readFromNBT(tag);
		} catch (Exception x) {
			MainRegistry.logger.error("Failed to deserialize control '{}', preserving raw NBT placeholder", registryName, x);
			return new UnknownControl(this, tag);
		}
		return control;
	}

	private static Map<String, DataValue> readControlDataMap(NBTTagCompound tag, String kind, String registryName) {
		Map<String, DataValue> values = new Object2ObjectLinkedOpenHashMap<>();
		for (String key : tag.getKeySet()) {
			DataValue value = DataValue.newFromNBT(tag.getTag(key));
			if(value == null) {
				throw new IllegalStateException("Failed to deserialize " + kind + " '" + key + "' for control '" + registryName + "'");
			}
			values.put(key, value);
		}
		return values;
	}

	public Control cloneControl(Control source) {
		return readControl(source.writeToNBT(new NBTTagCompound()));
	}

	public void receiveEvent(BlockPos from, ControlEvent evt){
		TileEntityControlPanel redstonePanel = parent instanceof TileEntityControlPanel ? (TileEntityControlPanel) parent : null;
		if(redstonePanel != null) {
			redstonePanel.beginRedstoneOutputCollection();
		}
		try {
			for(Control c : controls) {
				int idx = c.connectedSet.indexOf(from);
				if(idx != -1 || parent.getControlPos().equals(from)) {
					evt.setVar("from index", idx);
					c.receiveEvent(evt);
				}
			}
		} finally {
			if(redstonePanel != null) {
				redstonePanel.finishRedstoneOutputCollection();
			}
		}
	}

	public void receiveDirectEvent(Control control, ControlEvent evt) {
		if(control == null) {
			return;
		}
		TileEntityControlPanel redstonePanel = parent instanceof TileEntityControlPanel ? (TileEntityControlPanel) parent : null;
		if(redstonePanel != null) {
			redstonePanel.beginRedstoneOutputCollection();
		}
		try {
			control.receiveEvent(evt);
		} finally {
			if(redstonePanel != null) {
				redstonePanel.finishRedstoneOutputCollection();
			}
		}
	}

	public DataValue getVar(String name){
		return globalVars.getOrDefault(name, new DataValueFloat(0));
	}

	@SideOnly(Side.CLIENT)
	public Control getSelectedControl(Vec3d pos, Vec3d direction){
		BlockPos blockPos = parent.getControlPos();
		Vector4f localPos = new Vector4f((float)pos.x - blockPos.getX(), (float)pos.y - blockPos.getY(), (float)pos.z - blockPos.getZ(), 1);
		Vector4f localDir = new Vector4f((float)direction.x, (float)direction.y, (float)direction.z, 0);

		Matrix4f.transform(inv_transform, localPos, localPos);
		Matrix4f.transform(inv_transform, localDir, localDir);
		Vec3d start = new Vec3d(localPos.x, localPos.y, localPos.z);
		localDir.scale(3);
		Vec3d end = new Vec3d(localPos.x + localDir.x, localPos.y + localDir.y, localPos.z + localDir.z);
		double dist = Double.MAX_VALUE;
		Control ctrl = null;
		for(Control c : controls) {
			if (c.getBoundingBox() != null) {
				RayTraceResult r = c.getBoundingBox().calculateIntercept(start, end);

				if (r != null && r.typeOfHit != Type.MISS) {
					double newDist = r.hitVec.squareDistanceTo(start);
					if (newDist < dist) {
						dist = newDist;
						ctrl = c;
					}
				}
			}
		}
		return ctrl;
	}

	public static float getSlopeHeightFromZ(float z, float height, float angle) {
		double halfH = 0.5 * Math.tan(angle);

		return (float) ((height - halfH) + Math.tan(angle) * z);
	}

}
