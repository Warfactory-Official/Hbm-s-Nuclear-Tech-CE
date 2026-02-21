package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockControlPanelType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.control_panel.*;
import com.hbm.packet.toclient.ControlPanelUpdatePacket;
import com.hbm.tileentity.IGUIProvider;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityControlPanel extends TileEntity implements ITickable, IControllable, IControlReceiver, SimpleComponent, IGUIProvider {

	public ItemStackHandler inventory;
	public ControlPanel panel;
	public BlockControlPanelType panelType = BlockControlPanelType.CUSTOM_PANEL;

	public TileEntityControlPanel() {
		inventory = new ItemStackHandler(1){
			@Override
			protected void onContentsChanged(int slot){
				markDirty();
			}
		};
		this.panel = new ControlPanel(this, 0.25F, (float) Math.toRadians(20), 0, 0, 0.25F, 0);
	}

	@Override
	public void onLoad(){
		if(world.isRemote)
			loadClient();
		else {
			for(Control c : panel.controls){
				for(BlockPos b : c.connectedSet){
					ControlEventSystem.get(world).subscribeTo(this, b);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void updateTransform() {
		Matrix4f mat = new Matrix4f();

		boolean isDown = ((getBlockMetadata() >> 2) == 1);
		boolean isUp = ((getBlockMetadata() >> 3) == 1);

		// ○|￣|_
		// it works, ignore
		if (isUp) {
			mat.translate(new Vector3f(0.5F, panel.height, 0.5F));
			rotateByMetadata(mat, getBlockMetadata());
			mat.rotate(-panel.angle, new Vector3f(0, 0, 1));
			mat.rotate((float) Math.toRadians(90), new Vector3f(0, 1, 0));
		} else if (isDown) {
			mat.translate(new Vector3f(0.5F, 1-panel.height, 0.5F));
			rotateByMetadata(mat, getBlockMetadata());
			mat.rotate((float) Math.toRadians(180), new Vector3f(1, 0, 0));
			mat.rotate(-panel.angle, new Vector3f(0, 0, 1));
			mat.rotate((float) Math.toRadians(90), new Vector3f(0, 1, 0));
		} else {
			mat.translate(new Vector3f(0.5F, 0, 0.5F));
			rotateByMetadata(mat, getBlockMetadata());
			mat.rotate((float) Math.toRadians(-90), new Vector3f(1, 0, 0));
			mat.rotate((float) Math.toRadians(-90), new Vector3f(0, 0, 1));
			mat.translate(new Vector3f(0, panel.height-0.5F, 0.5F));
			mat.rotate((float) Math.toRadians(-180), new Vector3f(0, 1, 0));
			mat.rotate(panel.angle, new Vector3f(1, 0, 0));
		}

		mat.scale(new Vector3f(0.1F, 0.1F, 0.1F));
		mat.translate(new Vector3f(0.5F, 0, 0.5F));
		panel.setTransform(mat);
	}

	private void rotateByMetadata(Matrix4f mat, int meta) {
		switch ((meta & 3) + 2) {
			case 4:
				mat.rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0));
				break;
			case 2:
				mat.rotate((float) Math.toRadians(90), new Vector3f(0, 1, 0));
				break;
			case 3:
				mat.rotate((float) Math.toRadians(270), new Vector3f(0, 1, 0));
				break;
		}
	}

	@SideOnly(Side.CLIENT)
	public void loadClient(){
		updateTransform();
	}

	@Override
	public void update(){
		panel.update();
		if(!panel.changedVars.isEmpty()) {
			markDirty();
			PacketThreading.createSendToAllTrackingThreadedPacket(new ControlPanelUpdatePacket(pos, panel.changedVars), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 1));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		compound.setTag("panel", panel.writeToNBT(new NBTTagCompound()));
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		panel.readFromNBT(compound.getCompoundTag("panel"));
		super.readFromNBT(compound);
	}

	@Override
	public void receiveEvent(BlockPos from, ControlEvent e){
		panel.receiveEvent(from, e);
	}

	@Override
	public List<String> getInEvents(){
		return Arrays.asList("tick");
	}

	@Override
	public BlockPos getControlPos(){
		return getPos();
	}

	@Override
	public World getControlWorld(){
		return getWorld();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, -1, getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean hasPermission(EntityPlayer player){
		return true;
	}

	@Override
	public void receiveControl(NBTTagCompound data){
		if(data.hasKey("full_set")) {
			markDirty();
			for(Control c : panel.controls){
				for(BlockPos b : c.connectedSet){
					ControlEventSystem.get(world).unsubscribeFrom(this, b);
				}
			}
			this.panel.readFromNBT(data);
			for(Control c : panel.controls){
				for(BlockPos b : c.connectedSet){
					ControlEventSystem.get(world).subscribeTo(this, b);
				}
			}
            PacketThreading.createSendToAllTrackingThreadedPacket(new ControlPanelUpdatePacket(pos, data), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 1));
		} else if(data.hasKey("click_control")) {
			ControlEvent evt = ControlEvent.readFromNBT(data);
			panel.controls.get(data.getInteger("click_control")).receiveEvent(evt);
		}
	}

	public void validate() {
		super.validate();
		ControlEventSystem.get(this.world).addControllable(this);
	}

	public void invalidate() {
		super.invalidate();
		ControlEventSystem.get(this.world).removeControllable(this);
	}

	public float[] getBox() {
		float baseSizeX = 1-(panel.b_off+panel.d_off);
		float baseSizeY = 1-(panel.a_off+panel.c_off);

		double base_hyp = 1/Math.cos(Math.abs(panel.angle));
		double panel_hyp = baseSizeY/Math.cos(Math.abs(panel.angle));

		float box_width = 10;
		float box_height = (float) (base_hyp*10);
		float minX = (-box_width/2) + (panel.d_off*box_width);
		float minY = (-box_height/2) + (panel.a_off*box_height);

		return new float[] { minX, minY, minX+baseSizeX*10, (float) (minY+panel_hyp*10)};
	}

	public AxisAlignedBB getBoundingBox(boolean isUp, boolean isDown, EnumFacing facing) {
		AxisAlignedBB defAABB = null;
		float height1 = ControlPanel.getSlopeHeightFromZ(1-panel.c_off, panel.height, -panel.angle);
		float height0 = ControlPanel.getSlopeHeightFromZ(panel.a_off, panel.height, -panel.angle);

		if (isUp) {
			defAABB = new AxisAlignedBB(panel.d_off, 0, panel.a_off, 1 - panel.b_off, Math.max(height0, height1), 1 - panel.c_off);
		} else if (isDown) {
			defAABB = new AxisAlignedBB(1-panel.d_off, 1, panel.a_off, panel.b_off, 1-Math.max(height0, height1), 1-panel.c_off);
		} else {
			defAABB = new AxisAlignedBB(panel.d_off, 1-panel.a_off, 0, 1-panel.b_off, panel.c_off, Math.max(height0, height1));
		}
		defAABB = rotateAABB(defAABB, facing);

		return defAABB;
	}

	public static AxisAlignedBB rotateAABB(AxisAlignedBB box, EnumFacing facing){
		switch(facing){
			case NORTH:
				return new AxisAlignedBB(1-box.minX, box.minY, 1-box.maxZ, 1-box.maxX, box.maxY, 1-box.minZ);
			case SOUTH:
				return box;
			case EAST:
				return new AxisAlignedBB(box.minZ, box.minY, 1-box.minX, box.maxZ, box.maxY, 1-box.maxX);
			case WEST:
				return new AxisAlignedBB(1-box.minZ, box.minY, box.minX, 1-box.maxZ, box.maxY, box.maxX);
			default:
				return box;
		}
	}

	// opencomputers interface

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "control_panel";
	}

	@Callback()
	@Optional.Method(modid = "opencomputers")
	public Object[] listControls(Context context, Arguments args) {
		List<String> ctrlList = new ArrayList<>();
		for (int i=0; i < panel.controls.size(); i++) {
			ctrlList.add(panel.controls.get(i).name + " ("+i+")");
		}
		return new Object[]{ctrlList};
	}

	@Callback()
	@Optional.Method(modid = "opencomputers")
	public Object[] listGlobalVars(Context context, Arguments args) {
		return new Object[]{panel.globalVars};
	}

	@Callback(doc = "getGlobalVar(name:str);")
	@Optional.Method(modid = "opencomputers")
	public Object[] getGlobalVar(Context context, Arguments args) {
		String name = args.checkString(0);
		DataValue value = panel.getVar(name);
		if (Objects.requireNonNull(value.getType()) == DataValue.DataType.NUMBER) {
			return new Object[]{value.getNumber()};
		}
		return new Object[]{value.toString()};
	}

	@Callback(doc = "setGlobalVar(name:str, value:[bool,str,double,int]);")
	@Optional.Method(modid = "opencomputers")
	public Object[] setGlobalVar(Context context, Arguments args) {
		String name = args.checkString(0);

		if (args.isBoolean(1))
			panel.globalVars.put(name, new DataValueFloat(args.checkBoolean(1)));
		else if (args.isString(1))
			panel.globalVars.put(name, new DataValueString(args.checkString(1)));
		else if (args.isDouble(1))
			panel.globalVars.put(name, new DataValueFloat((float) args.checkDouble(1)));
		else if (args.isInteger(1))
			panel.globalVars.put(name, new DataValueFloat((float) args.checkInteger(1)));
		else
			return new Object[]{"ERROR: unsupported value type"};

		return new Object[]{};
	}

	@Callback(doc = "listLocalVars(ID:int); list local vars for control ID.")
	@Optional.Method(modid = "opencomputers")
	public Object[] listLocalVars(Context context, Arguments args) {
		return new Object[]{panel.controls.get(args.checkInteger(0)).vars};
	}

	@Callback(doc = "getLocalVar(ID:int, name:str); get var for control ID.")
	@Optional.Method(modid = "opencomputers")
	public Object[] getLocalVar(Context context, Arguments args) {
		int index = args.checkInteger(0);
		String name = args.checkString(1);
		DataValue value = panel.controls.get(index).getVar(name);
		if (Objects.requireNonNull(value.getType()) == DataValue.DataType.NUMBER) {
			return new Object[]{value.getNumber()};
		}
		return new Object[]{value.toString()};
	}

	@Callback(doc = "getLocalVar(ID:int, name:str, value:[bool,str,double,int]); set var for control ID.")
	@Optional.Method(modid = "opencomputers")
	public Object[] setLocalVar(Context context, Arguments args) {
		int index = args.checkInteger(0);
		String name = args.checkString(1);

		if (args.isBoolean(2))
			panel.controls.get(index).vars.put(name, new DataValueFloat(args.checkBoolean(2)));
		else if (args.isString(2)) {
			DataValue value = panel.controls.get(index).vars.get(name);
			String newValue = args.checkString(2);

			if (value.getType().equals(DataValue.DataType.ENUM)) {
				if (((DataValueEnum) value).enumClass.equals(EnumDyeColor.class)) {
					for (EnumDyeColor c : EnumDyeColor.values()) {
						if (c.getName().equals(newValue)) {
							panel.controls.get(index).vars.put(name, new DataValueEnum<>(EnumDyeColor.valueOf(newValue.toUpperCase())));
							return new Object[]{};
						}
					}
					return new Object[]{"ERROR: '" + newValue + "' not found for EnumDyeColor"};
				}
				return new Object[]{"ERROR: unsupported enum class"};
			}
			else {
				panel.controls.get(index).vars.put(name, new DataValueString(newValue));
			}
		}
		else if (args.isDouble(2))
			panel.controls.get(index).vars.put(name, new DataValueFloat((float) args.checkDouble(2)));
		else if (args.isInteger(2))
			panel.controls.get(index).vars.put(name, new DataValueFloat(args.checkInteger(2)));
		else
			return new Object[]{"ERROR: unsupported value type"};

		return new Object[]{};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerControlEdit(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GuiControlEdit(player.inventory, this);
	}

}
