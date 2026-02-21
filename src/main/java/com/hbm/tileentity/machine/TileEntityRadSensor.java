package com.hbm.tileentity.machine;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityRadSensor extends TileEntity implements ITickable, SimpleComponent {

	public double chunkRads = 0;
	public double recievedDose = 0;

	public double lastChunkRads = 0;

	public int redstoneOutput = 0;
	public int comparatorOutput = 0;

	public int lastRedstoneOutput = 0;
	public int lastComparatorOutput = 0;
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		chunkRads = compound.getDouble("chunkRads");
		recievedDose = compound.getDouble("recievedDose");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setDouble("chunkRads", chunkRads);
		compound.setDouble("recievedDose", recievedDose);
		return super.writeToNBT(compound);
	}

	public int getRestoneOutput(){
		if(chunkRads < 0.01)
			return 0;
		if(chunkRads < 0.5)
			return 1;
		if(chunkRads < 1)
			return 2;
		if(chunkRads < 2)
			return 3;
		if(chunkRads < 4)
			return 4;
		if(chunkRads < 8)
			return 5;
		if(chunkRads < 16)
			return 6;
		if(chunkRads < 32)
			return 7;
		if(chunkRads < 64)
			return 8;
		if(chunkRads < 128)
			return 9;
		if(chunkRads < 256)
			return 10;
		if(chunkRads < 512)
			return 11;
		if(chunkRads < 1024)
			return 12;
		if(chunkRads < 2048)
			return 13;
		if(chunkRads < 4096)
			return 14;
		return 15;
	}

	public int getComparatorOutput(){
		if(recievedDose < 0.01)
			return 0;
		if(recievedDose < 8)
			return 1;
		if(recievedDose < 16)
			return 2;
		if(recievedDose < 32)
			return 3;
		if(recievedDose < 64)
			return 4;
		if(recievedDose < 128)
			return 5;
		if(recievedDose < 256)
			return 6;
		if(recievedDose < 512)
			return 7;
		if(recievedDose < 1024)
			return 8;
		if(recievedDose < 2048)
			return 9;
		if(recievedDose < 4096)
			return 10;
		if(recievedDose < 8192)
			return 11;
		if(recievedDose < 16384)
			return 12;
		if(recievedDose < 32768)
			return 13;
		if(recievedDose < 65536)
			return 14;
		return 15;
	}

	public static int getRedstonePower(World world, BlockPos blockPos, EnumFacing side) {
	    BlockPos offsetPos = blockPos.offset(side);
	    int worldPower = world.getRedstonePower(offsetPos, side);
	    if (worldPower >= 15) {
	        return worldPower;
	    } else {
	        IBlockState offsetState = world.getBlockState(offsetPos);
	        if(offsetState.getBlock() instanceof BlockRedstoneWire) {
	            int wirePower = offsetState.getValue(BlockRedstoneWire.POWER);
	            return Math.max(worldPower, wirePower);
	        }
	        return worldPower;
	    }
	}
		
	@Override
	public void update() {
		if(!world.isRemote) {
            chunkRads = (ChunkRadiationManager.proxy.getRadiation(world, pos) + lastChunkRads) / 2.0;
			
			if(0 < getRedstonePower(world, pos, EnumFacing.DOWN)){
				recievedDose = 0;
			}else{
				recievedDose += chunkRads / 20.0;
			}

			redstoneOutput = getRestoneOutput();
			comparatorOutput = getComparatorOutput();

			
			if(redstoneOutput != lastRedstoneOutput || lastComparatorOutput != comparatorOutput)
				world.notifyNeighborsOfStateChange(pos, getBlockType(), true);

			lastChunkRads = chunkRads;

			lastRedstoneOutput = redstoneOutput;
			lastComparatorOutput = comparatorOutput;
		}
	}

	// opencomputers interface

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "radsensor";
	}

	@Callback(doc = "getRads(); returns the measured chunk radiation - float")
	@Optional.Method(modid = "opencomputers")
	public Object[] getRads(Context context, Arguments args) {
		return new Object[] {chunkRads};
	}

	@Callback(doc = "getDose(); returns the measured chunk radiation dose - float")
	@Optional.Method(modid = "opencomputers")
	public Object[] getDose(Context context, Arguments args) {
		return new Object[] {recievedDose};
	}

	@Callback(doc = "resetDose(); resets the radiation dose")
	@Optional.Method(modid = "opencomputers")
	public Object[] resetDose(Context context, Arguments args) {
		recievedDose = 0;
		return new Object[] {null};
	}
}
