package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRBMKHeater;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.DataValueString;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.gui.GUIRBMKHeater;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityRBMKHeater extends TileEntityRBMKSlottedBase implements IFluidStandardTransceiver, IGUIProvider, SimpleComponent, CompatHandler.OCComponent {

	public FluidTankNTM feed;
	public FluidTankNTM steam;
	
	public TileEntityRBMKHeater() {
		super(1);
		this.feed = new FluidTankNTM(Fluids.COOLANT, 16_000);
		this.steam = new FluidTankNTM(Fluids.COOLANT_HOT, 16_000);
	}

	@Override
	public String getName() {
		return "container.rbmkHeater";
	}
	
	@Override
	public void update() {

		if(!world.isRemote) {

			feed.setType(0, inventory);

			if(feed.getTankType().hasTrait(FT_Heatable.class)) {
				FT_Heatable trait = feed.getTankType().getTrait(FT_Heatable.class);
				FT_Heatable.HeatingStep step = trait.getFirstStep();
				steam.setTankType(step.typeProduced);
				double tempRange = this.heat - steam.getTankType().temperature;
				double eff = trait.getEfficiency(FT_Heatable.HeatingType.HEATEXCHANGER);

				if(tempRange > 0 && eff > 0) {
					double TU_PER_DEGREE = 2_000D * eff; //based on 1mB of water absorbing 200 TU as well as 0.1°C from an RBMK column
					int inputOps = feed.getFill() / step.amountReq;
					int outputOps = (steam.getMaxFill() - steam.getFill()) / step.amountProduced;
					int tempOps = (int) Math.floor((tempRange * TU_PER_DEGREE) / step.heatReq);
					int ops = Math.min(inputOps, Math.min(outputOps, tempOps));

					feed.setFill(feed.getFill() - step.amountReq * ops);
					steam.setFill(steam.getFill() + step.amountProduced * ops);
					this.heat -= (step.heatReq * ops / TU_PER_DEGREE) * trait.getEfficiency(FT_Heatable.HeatingType.HEATEXCHANGER);
				}

			} else {
				steam.setTankType(Fluids.NONE);
			}

			this.trySubscribe(feed.getTankType(), world, pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y);
			for(DirPos pos : getOutputPos()) {
				if(this.steam.getFill() > 0) this.sendFluid(steam, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}
		}
		
		super.update();
	}

	protected DirPos[] getOutputPos() {

		if(world.getBlockState(pos.add(0, -1, 0)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), Library.NEG_Y)
			};
		} else if(world.getBlockState(pos.add(0, -2, 0)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), Library.NEG_Y)
			};
		} else {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y)
			};
		}
	}

	public void getDiagData(NBTTagCompound nbt) {
		this.writeToNBT(nbt);
		nbt.removeTag("jumpheight");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		feed.readFromNBT(nbt, "feed");
		steam.readFromNBT(nbt, "steam");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		feed.writeToNBT(nbt, "feed");
		steam.writeToNBT(nbt, "steam");
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		this.feed.serialize(buf);
		this.steam.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.feed.deserialize(buf);
		this.steam.deserialize(buf);
	}
	
	@Override
	public void onMelt(int reduce) {
		
		int count = 1 + world.rand.nextInt(2);
		
		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}
		
		super.onMelt(reduce);
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.HEATEX;
	}

	@Override
	public RBMKColumn getConsoleData() {
		RBMKColumn.HeaterColumn data = (RBMKColumn.HeaterColumn) super.getConsoleData();
		data.water = this.feed.getFill();
		data.maxWater = this.feed.getMaxFill();
		data.steam = this.steam.getFill();
		data.maxSteam = this.steam.getMaxFill();
		data.coldType = (short)this.feed.getTankType().getID();
		data.hotType = (short)this.steam.getTankType().getID();
		return data;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {feed, steam};
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {steam};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {feed};
	}

	// control panel
	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();

		data.put("t0_fluidType", new DataValueString(feed.getTankType().getName()));
		data.put("t0_fluidAmount", new DataValueFloat((float) feed.getFill()));
		data.put("t1_fluidType", new DataValueString(steam.getTankType().getName()));
		data.put("t1_fluidAmount", new DataValueFloat((float) steam.getFill()));

		return data;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "rbmk_heater";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[] {heat};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getFill(Context context, Arguments args) {
		return new Object[] {feed.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getFillMax(Context context, Arguments args) {
		return new Object[] {feed.getMaxFill()};
	}
	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getExport(Context context, Arguments args) {
		return new Object[] {steam.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getExportMax(Context context, Arguments args) {
		return new Object[] {steam.getMaxFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getFillType(Context context, Arguments args) {
		return new Object[] {feed.getTankType().getName()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getExportType(Context context, Arguments args) {
		return new Object[] {steam.getTankType().getName()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {heat, feed.getFill(), feed.getMaxFill(), steam.getFill(), steam.getMaxFill(), feed.getTankType().getName(), steam.getTankType().getName(), pos.getX(), pos.getY(), pos.getZ()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {pos.getX(), pos.getY(), pos.getZ()};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKHeater(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKHeater(player.inventory, this);
	}
}
