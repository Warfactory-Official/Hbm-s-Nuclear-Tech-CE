package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockControlPanelType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.types.*;
import com.hbm.inventory.control_panel.types.DataValue.DataType;
import com.hbm.packet.toclient.ControlPanelUpdatePacket;
import com.hbm.tileentity.IGUIProvider;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.block.state.IBlockState;
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

import java.util.*;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityControlPanel extends TileEntity implements ITickable, IControllable, IControlReceiver, SimpleComponent, IGUIProvider {

	public ItemStackHandler inventory;
	public ControlPanel panel;
	public BlockControlPanelType panelType = BlockControlPanelType.CUSTOM_PANEL;
	private AxisAlignedBB bb;
	private final int[] redstoneInputPower = new int[EnumFacing.VALUES.length];
	private final int[] redstoneInputWeak = new int[EnumFacing.VALUES.length];
	private final int[] redstoneInputStrong = new int[EnumFacing.VALUES.length];
	private final int[] redstoneOutputWeak = new int[EnumFacing.VALUES.length];
	private final int[] redstoneOutputStrong = new int[EnumFacing.VALUES.length];
	private final int[] redstoneOutputWeakPending = new int[EnumFacing.VALUES.length];
	private final int[] redstoneOutputStrongPending = new int[EnumFacing.VALUES.length];
	private final Deque<RedstoneInputSnapshot> pendingRedstoneInputSnapshots = new ArrayDeque<>();
	private RedstoneInputSnapshot activeRedstoneInputSnapshot;
	private int redstoneOutputCollectionDepth = 0;

	public TileEntityControlPanel() {
		inventory = new ItemStackHandler(1){
			@Override
			protected void onContentsChanged(int slot){
				markDirty();
			}
		};
		this.panel = new ControlPanel(this, 0.25F, (float) Math.toRadians(20), 0, 0, 0.25F, 0);
		Arrays.fill(redstoneInputPower, -1);
		Arrays.fill(redstoneInputWeak, -1);
		Arrays.fill(redstoneInputStrong, -1);
	}

	@Override
	public void onLoad(){
		if(world.isRemote)
			loadClient();
		else {
			for(Control c : panel.controls){
				for (BlockPos b : c.taggedLinks.values()) {
					ControlEventSystem.get(world).subscribeTo(this, b);
				}
			}
			reinitializePanelState();
			if(hasAnyRedstoneOutput()) {
				notifyRedstoneNeighbors();
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
		if(!world.isRemote) {
			dispatchPendingRedstoneInputEvents(false);
		}
		panel.update();
		if(!panel.changedVars.isEmpty()) {
			markDirty();
			PacketThreading.createSendToAllTrackingThreadedPacket(new ControlPanelUpdatePacket(pos, panel.changedVars), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 1));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		compound.setTag("panel", panel.writeToNBT(new NBTTagCompound()));
		NBTTagCompound weakOutput = new NBTTagCompound();
		NBTTagCompound strongOutput = new NBTTagCompound();
		for(EnumFacing facing : EnumFacing.VALUES) {
			weakOutput.setInteger(facing.getName(), redstoneOutputWeak[facing.getIndex()]);
			strongOutput.setInteger(facing.getName(), redstoneOutputStrong[facing.getIndex()]);
		}
		compound.setTag("redstoneWeakOut", weakOutput);
		compound.setTag("redstoneStrongOut", strongOutput);
		return super.writeToNBT(compound);
	}

	@Override
	protected void setWorldCreate(World worldIn) {
		this.world = worldIn; // WHY IS THIS NOT THE DEFAULT SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH SMH
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		panel.readFromNBT(compound.getCompoundTag("panel"));
		NBTTagCompound weakOutput = compound.getCompoundTag("redstoneWeakOut");
		NBTTagCompound strongOutput = compound.getCompoundTag("redstoneStrongOut");
		for(EnumFacing facing : EnumFacing.VALUES) {
			redstoneOutputWeak[facing.getIndex()] = clampRedstoneStrength(weakOutput.getInteger(facing.getName()));
			redstoneOutputStrong[facing.getIndex()] = clampRedstoneStrength(strongOutput.getInteger(facing.getName()));
		}
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
				for (BlockPos b : c.taggedLinks.values()) {
					ControlEventSystem.get(world).unsubscribeFrom(this, b);
				}
			}
			this.panel.readFromNBT(data);
			for(Control c : panel.controls){
				for (BlockPos b : c.taggedLinks.values()) {
					ControlEventSystem.get(world).subscribeTo(this, b);
				}
			}
			reinitializePanelState();
            PacketThreading.createSendToAllTrackingThreadedPacket(new ControlPanelUpdatePacket(pos, data), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 1));
		} else if(data.hasKey("click_control")) {
			ControlEvent evt = ControlEvent.readFromNBT(data);
			panel.receiveDirectEvent(panel.controls.get(data.getInteger("click_control")), evt);
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

	public void captureRedstoneInputChanges() {
		captureRedstoneInputChanges(false);
	}

	private void captureRedstoneInputChanges(boolean forceEventDispatch) {
		if(world == null || world.isRemote) return;

		int[] nextPower = Arrays.copyOf(redstoneInputPower, redstoneInputPower.length);
		int[] nextWeak = Arrays.copyOf(redstoneInputWeak, redstoneInputWeak.length);
		int[] nextStrong = Arrays.copyOf(redstoneInputStrong, redstoneInputStrong.length);
		List<EnumFacing> changedFaces = new ArrayList<>();
		for(EnumFacing facing : EnumFacing.VALUES) {
			int idx = facing.getIndex();
			BlockPos neighborPos = getPos().offset(facing);
			IBlockState neighborState = world.getBlockState(neighborPos);
			int effectivePower = world.getRedstonePower(neighborPos, facing); // Vanilla samples neighbor power as getRedstonePower(pos.offset(facing), facing).
			int weakPower = neighborState.getWeakPower(world, neighborPos, facing);
			int strongPower = neighborState.getStrongPower(world, neighborPos, facing);

			boolean changed = effectivePower != redstoneInputPower[idx]
					|| weakPower != redstoneInputWeak[idx]
					|| strongPower != redstoneInputStrong[idx];
			nextPower[idx] = effectivePower;
			nextWeak[idx] = weakPower;
			nextStrong[idx] = strongPower;
			if(changed || forceEventDispatch) {
				changedFaces.add(facing);
			}
		}

		System.arraycopy(nextPower, 0, redstoneInputPower, 0, redstoneInputPower.length);
		System.arraycopy(nextWeak, 0, redstoneInputWeak, 0, redstoneInputWeak.length);
		System.arraycopy(nextStrong, 0, redstoneInputStrong, 0, redstoneInputStrong.length);
		for(EnumFacing facing : changedFaces) {
			pendingRedstoneInputSnapshots.addLast(new RedstoneInputSnapshot(facing, nextPower, nextWeak, nextStrong));
		}
	}

	private static ControlEvent createRedstoneInputEvent(EnumFacing facing, int effectivePower, int weakPower, int strongPower) {
		return ControlEvent.newEvent("redstone_input")
				.setVar("facing", facing)
				.setVar("power", effectivePower)
				.setVar("weak", weakPower)
				.setVar("strong", strongPower)
				.setVar("isPowered", effectivePower > 0)
				.setVar("isWeaklyPowered", weakPower > 0)
				.setVar("isStronglyPowered", strongPower > 0);
	}

	private void dispatchPendingRedstoneInputEvents(boolean clearOutputsFirst) {
		if(world == null || world.isRemote || pendingRedstoneInputSnapshots.isEmpty()) {
			return;
		}

		beginRedstoneOutputCollection(clearOutputsFirst);
		try {
			BlockPos controlPos = getPos();
			while(!pendingRedstoneInputSnapshots.isEmpty()) {
				activeRedstoneInputSnapshot = pendingRedstoneInputSnapshots.removeFirst();
				panel.receiveEvent(controlPos, activeRedstoneInputSnapshot.toControlEvent());
			}
		} finally {
			activeRedstoneInputSnapshot = null;
			finishRedstoneOutputCollection();
		}
	}

	private void reinitializePanelState() {
		if(world == null || world.isRemote) {
			return;
		}

		pendingRedstoneInputSnapshots.clear();
		beginRedstoneOutputCollection(true);
		try {
			for(Control control : panel.controls) {
				control.receiveEvent(ControlEvent.newEvent("initialize"));
			}
			captureRedstoneInputChanges(true);
			dispatchPendingRedstoneInputEvents(false);
		} finally {
			finishRedstoneOutputCollection();
		}
	}

	public int getWeakRedstoneOutput(EnumFacing facing) {
		return redstoneOutputWeak[facing.getIndex()];
	}

	public int getStrongRedstoneOutput(EnumFacing facing) {
		return redstoneOutputStrong[facing.getIndex()];
	}

	private void notifyRedstoneNeighbors() {
		world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
		for(EnumFacing facing : EnumFacing.VALUES) {
			world.notifyNeighborsOfStateChange(pos.offset(facing), getBlockType(), true);
		}
	}

	private boolean hasAnyRedstoneOutput() {
		for(EnumFacing facing : EnumFacing.VALUES) {
			if(redstoneOutputWeak[facing.getIndex()] > 0 || redstoneOutputStrong[facing.getIndex()] > 0) {
				return true;
			}
		}
		return false;
	}

	public void beginRedstoneOutputCollection() {
		beginRedstoneOutputCollection(false);
	}

	public void beginRedstoneOutputCollection(boolean clearPending) {
		if(redstoneOutputCollectionDepth++ == 0) {
			if(clearPending) {
				Arrays.fill(redstoneOutputWeakPending, 0);
				Arrays.fill(redstoneOutputStrongPending, 0);
			} else {
				System.arraycopy(redstoneOutputWeak, 0, redstoneOutputWeakPending, 0, redstoneOutputWeak.length);
				System.arraycopy(redstoneOutputStrong, 0, redstoneOutputStrongPending, 0, redstoneOutputStrong.length);
			}
		}
	}

	public void finishRedstoneOutputCollection() {
		if(redstoneOutputCollectionDepth == 0) {
			return;
		}
		if(--redstoneOutputCollectionDepth == 0) {
			applyRedstoneOutputs(redstoneOutputWeakPending, redstoneOutputStrongPending);
		}
	}

	public int getRedstoneInputPower(EnumFacing facing) {
		if(activeRedstoneInputSnapshot != null) {
			return activeRedstoneInputSnapshot.getPower(facing);
		}
		return redstoneInputPower[facing.getIndex()] < 0 ? 0 : redstoneInputPower[facing.getIndex()];
	}

	public int getRedstoneInputWeak(EnumFacing facing) {
		if(activeRedstoneInputSnapshot != null) {
			return activeRedstoneInputSnapshot.getWeak(facing);
		}
		return redstoneInputWeak[facing.getIndex()] < 0 ? 0 : redstoneInputWeak[facing.getIndex()];
	}

	public int getRedstoneInputStrong(EnumFacing facing) {
		if(activeRedstoneInputSnapshot != null) {
			return activeRedstoneInputSnapshot.getStrong(facing);
		}
		return redstoneInputStrong[facing.getIndex()] < 0 ? 0 : redstoneInputStrong[facing.getIndex()];
	}

	public void setWeakRedstoneOutput(EnumFacing facing, int strength) {
		int idx = facing.getIndex();
		int clamped = clampRedstoneStrength(strength);
		if(redstoneOutputCollectionDepth > 0) {
			redstoneOutputWeakPending[idx] = clamped;
			return;
		}
		int[] weakOutputs = Arrays.copyOf(redstoneOutputWeak, redstoneOutputWeak.length);
		weakOutputs[idx] = clamped;
		applyRedstoneOutputs(weakOutputs, redstoneOutputStrong);
	}

	public void setStrongRedstoneOutput(EnumFacing facing, int strength) {
		int idx = facing.getIndex();
		int clamped = clampRedstoneStrength(strength);
		if(redstoneOutputCollectionDepth > 0) {
			redstoneOutputStrongPending[idx] = clamped;
			return;
		}
		int[] strongOutputs = Arrays.copyOf(redstoneOutputStrong, redstoneOutputStrong.length);
		strongOutputs[idx] = clamped;
		applyRedstoneOutputs(redstoneOutputWeak, strongOutputs);
	}

	private void applyRedstoneOutputs(int[] weakOutputs, int[] strongOutputs) {
		boolean changed = false;
		for(EnumFacing facing : EnumFacing.VALUES) {
			int idx = facing.getIndex();
			int weak = clampRedstoneStrength(weakOutputs[idx]);
			int strong = clampRedstoneStrength(strongOutputs[idx]);
			if(redstoneOutputWeak[idx] != weak) {
				redstoneOutputWeak[idx] = weak;
				changed = true;
			}
			if(redstoneOutputStrong[idx] != strong) {
				redstoneOutputStrong[idx] = strong;
				changed = true;
			}
		}
		if(changed) {
			markDirty();
			notifyRedstoneNeighbors();
		}
	}

	private static int clampRedstoneStrength(int strength) {
		return Math.max(0, Math.min(15, strength));
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
		if (value.getType() == DataType.COMPOSITE) {
			return new Object[]{((DataValueComposite) value).snapshot()};
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
		else if (args.isTable(1))
			panel.globalVars.put(name, DataValueComposite.fromLuaTable(args.checkTable(1)));
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
		if (value.getType() == DataType.COMPOSITE) {
			return new Object[]{((DataValueComposite) value).snapshot()};
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
		else if (args.isTable(2))
			panel.controls.get(index).vars.put(name, DataValueComposite.fromLuaTable(args.checkTable(2)));
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

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		return bb;
	}

	private static final class RedstoneInputSnapshot {
		private final EnumFacing changedFace;
		private final int[] powerByFace;
		private final int[] weakByFace;
		private final int[] strongByFace;

		private RedstoneInputSnapshot(EnumFacing changedFace, int[] powerByFace, int[] weakByFace, int[] strongByFace) {
			this.changedFace = changedFace;
			this.powerByFace = Arrays.copyOf(powerByFace, powerByFace.length);
			this.weakByFace = Arrays.copyOf(weakByFace, weakByFace.length);
			this.strongByFace = Arrays.copyOf(strongByFace, strongByFace.length);
		}

		private int getPower(EnumFacing facing) {
			return Math.max(0, powerByFace[facing.getIndex()]);
		}

		private int getWeak(EnumFacing facing) {
			return Math.max(0, weakByFace[facing.getIndex()]);
		}

		private int getStrong(EnumFacing facing) {
			return Math.max(0, strongByFace[facing.getIndex()]);
		}

		private ControlEvent toControlEvent() {
			return createRedstoneInputEvent(changedFace, getPower(changedFace), getWeak(changedFace), getStrong(changedFace));
		}
	}

}
