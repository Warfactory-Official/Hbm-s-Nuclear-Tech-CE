package com.hbm.tileentity.deco;

import com.hbm.blocks.machine.BlockSpinnyLight;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlEventSystem;
import com.hbm.inventory.control_panel.IControllable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

@AutoRegister
public class TileEntitySpinnyLight extends TileEntity implements IControllable {
	
	public EnumDyeColor color = EnumDyeColor.WHITE;
	public long timeAdded = 0;
	private AxisAlignedBB bb;
	
	@Override
	public void onLoad() {
		timeAdded = world.getTotalWorldTime();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		color = EnumDyeColor.values()[compound.getByte("color")];
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setByte("color", (byte) color.ordinal());
		return super.writeToNBT(compound);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65535.0D;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(this.pos, this.pos.add(1, 1, 1)).grow(10);
		return bb;
	}

	@Override
	public void receiveEvent(BlockPos from, ControlEvent e){
		if(e.name.equals("spinny_light_power")){
			boolean on = e.vars.get("isOn").getBoolean();
			IBlockState state = world.getBlockState(pos);
			boolean power = state.getValue(BlockSpinnyLight.POWERED);
			if(on && !power){
				world.setBlockState(pos, state.withProperty(BlockSpinnyLight.POWERED, true));
				NBTTagCompound tag = writeToNBT(new NBTTagCompound());
				world.getTileEntity(pos).readFromNBT(tag);
			} else if(!on && power){
				world.setBlockState(pos, state.withProperty(BlockSpinnyLight.POWERED, false));
				NBTTagCompound tag = writeToNBT(new NBTTagCompound());
				world.getTileEntity(pos).readFromNBT(tag);
			}
		}
	}
	
	@Override
	public List<String> getInEvents(){
		return Arrays.asList("spinny_light_power");
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
	public void validate(){
		super.validate();
		ControlEventSystem.get(world).addControllable(this);
	}
	
	@Override
	public void invalidate(){
		super.invalidate();
		ControlEventSystem.get(world).removeControllable(this);
	}
}
