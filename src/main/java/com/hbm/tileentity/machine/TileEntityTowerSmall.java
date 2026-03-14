package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.ClientConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IConfigurableMachine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AutoRegister
public class TileEntityTowerSmall extends TileEntityCondenser {

	//Configurable values
	public static int inputTankSizeTS = 1_000;
	public static int outputTankSizeTS = 1_000;
	
	public TileEntityTowerSmall() {
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.SPENTSTEAM, inputTankSizeTS);
		tanks[1] = new FluidTankNTM(Fluids.WATER, outputTankSizeTS);
	}

	@Override
	public String getConfigName() {
		return "condenserTowerSmall";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSizeTS = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeTS);
		outputTankSizeTS = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeTS);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inputTankSize").value(inputTankSizeTS);
		writer.name("I:outputTankSize").value(outputTankSizeTS);
	}
	
	@Override
	public void update() {
		super.update();
		
		if(world.isRemote) {

			if(ClientConfig.COOLING_TOWER_PARTICLES.get() && (this.waterTimer > 0 && this.world.getTotalWorldTime() % 2 == 0)) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 1F);
				data.setFloat("base", 0.5F);
				data.setFloat("max", 4F);
				data.setInteger("life", 250 + world.rand.nextInt(250));
	
				data.setDouble("posX", pos.getX() + 0.5);
				data.setDouble("posZ", pos.getZ() + 0.5);
				data.setDouble("posY", pos.getY() + 18);
				
				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {
		this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX() + 3, pos.getY(), pos.getZ(), Library.POS_X);
		this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX() - 3, pos.getY(), pos.getZ(), Library.NEG_X);
		this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX(), pos.getY(), pos.getZ() + 3, Library.POS_Z);
		this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX(), pos.getY(), pos.getZ() - 3, Library.NEG_Z);
	}

	@Override
	public void sendFluidToAll(FluidTankNTM tank, TileEntity te) {
		this.sendFluid(this.tanks[1], world, pos.getX() + 3, pos.getY(), pos.getZ(), Library.POS_X);
		this.sendFluid(this.tanks[1], world, pos.getX() - 3, pos.getY(), pos.getZ(), Library.NEG_X);
		this.sendFluid(this.tanks[1], world, pos.getX(), pos.getY(), pos.getZ() + 3, Library.POS_Z);
		this.sendFluid(this.tanks[1], world, pos.getX(), pos.getY(), pos.getZ() - 3, Library.NEG_Z);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 2,
					pos.getY(),
					pos.getZ() - 2,
					pos.getX() + 3,
					pos.getY() + 20,
					pos.getZ() + 3
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}