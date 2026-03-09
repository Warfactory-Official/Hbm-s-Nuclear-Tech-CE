package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.ClientConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
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
public class TileEntityTowerLarge extends TileEntityCondenser {

	//Configurable values
	public static int inputTankSizeTL = 10_000;
	public static int outputTankSizeTL = 10_000;
	
	public TileEntityTowerLarge() {
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.SPENTSTEAM, inputTankSizeTL);
		tanks[1] = new FluidTankNTM(Fluids.WATER, outputTankSizeTL);
	}

	@Override
	public String getConfigName() {
		return "condenserTowerLarge";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSizeTL = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeTL);
		outputTankSizeTL = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeTL);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inputTankSize").value(inputTankSizeTL);
		writer.name("I:outputTankSize").value(outputTankSizeTL);
	}
	
	@Override
	public void update() {
		super.update();
		
		if(world.isRemote) {

			if(ClientConfig.COOLING_TOWER_PARTICLES.get() && (this.waterTimer > 0 && this.world.getTotalWorldTime() % 4 == 0)) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 0.5F);
				data.setFloat("base", 1F);
				data.setFloat("max", 10F);
				data.setInteger("life", 750 + world.rand.nextInt(250));
	
				data.setDouble("posX", pos.getX() + 0.5 + world.rand.nextDouble() * 3 - 1.5);
				data.setDouble("posZ", pos.getZ() + 0.5 + world.rand.nextDouble() * 3 - 1.5);
				data.setDouble("posY", pos.getY() + 1);
				
				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {

		for(int i = 2; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX() + dir.offsetX * 5, pos.getY(), pos.getZ() + dir.offsetZ * 5, dir);
			this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX() + dir.offsetX * 5 + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * 3, dir);
			this.trySubscribe(this.tanks[0].getTankType(),world,  pos.getX() + dir.offsetX * 5 + rot.offsetX * -3, pos.getY(), pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * -3, dir);
		}
	}

	@Override
	public void sendFluidToAll(FluidTankNTM tank, TileEntity te) {

		for(int i = 2; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			this.sendFluid(this.tanks[1], world, pos.getX() + dir.offsetX * 5, pos.getY(), pos.getZ() + dir.offsetZ * 5, dir);
			this.sendFluid(this.tanks[1], world, pos.getX() + dir.offsetX * 5 + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * 3, dir);
			this.sendFluid(this.tanks[1], world,  pos.getX() + dir.offsetX * 5 + rot.offsetX * -3, pos.getY(), pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * -3, dir);
		}
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 4,
					pos.getY(),
					pos.getZ() - 4,
					pos.getX() + 5,
					pos.getY() + 13,
					pos.getZ() + 5
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