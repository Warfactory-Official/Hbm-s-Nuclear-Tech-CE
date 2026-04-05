package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.DataValueString;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AutoRegister
@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityRBMKCooler extends TileEntityRBMKBase implements IFluidStandardTransceiverMK2, SimpleComponent, CompatHandler.OCComponent {

	protected int timer = 0;
	public int lastCooled;
	private final FluidTankNTM[] tanks;
	protected TileEntityRBMKBase[] neighborCache = new TileEntityRBMKBase[25];

	public TileEntityRBMKCooler() {
		super();
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.PERFLUOROMETHYL_COLD, 4_000);
		tanks[1] = new FluidTankNTM(Fluids.PERFLUOROMETHYL, 4_000);
	}

	public void getDiagData(NBTTagCompound nbt) {
		diag = true;
		writeToNBT(nbt);
		diag = false;
		nbt.removeTag("jumpheight");
	}

	@Override
	public void update() {
		if(!world.isRemote) {

			if(timer <= 0) {
				timer = 60;

				for(int i = 0; i < 25; i++) {
					int x = pos.getX() - 2 + i / 5;
					int z = pos.getZ() - 2 + i % 5;
					if(Compat.getTileStandard(world, x, pos.getY(), z) instanceof TileEntityRBMKBase tile) {
						neighborCache[i] = tile;
					} else {
						neighborCache[i] = null;
					}
				}

			} else {
				timer--;
			}

			if(tanks[0].getFill() >= 50 && tanks[1].getMaxFill() - tanks[1].getFill() >= 50) {
				tanks[0].setFill(tanks[0].getFill() - 50);
				tanks[1].setFill(tanks[1].getFill() + 50);

				int cooled = 0;
				for(TileEntityRBMKBase neighbor : neighborCache) {
					if(neighbor != null && !neighbor.isInvalid()) {
						double before = neighbor.heat;
						neighbor.heat -= 200;
						if(neighbor.heat < 20) neighbor.heat = 20;
						int delta = (int)(before - neighbor.heat);
						if(delta > 0) {
							cooled += delta;
							neighbor.markDirty();
						}
					}
				}
				lastCooled = cooled;
			} else {
				lastCooled = 0;
			}

			trySubscribe(tanks[0].getTankType(), world, pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y);

			if(tanks[1].getFill() > 0) for(DirPos pos : getOutputPos()) {
				tryProvide(tanks[1], world, pos);
			}

		}

		super.update();
	}

	protected DirPos[] getOutputPos() {

		if(world.getBlockState(pos.down()).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(pos.getX(), pos.getY() + RBMKDials.getColumnHeight(world) + 1, pos.getZ(), Library.POS_Y),
					new DirPos(pos.getX() + 1, pos.getY() - 1, pos.getZ(), Library.POS_X),
					new DirPos(pos.getX() - 1, pos.getY() - 1, pos.getZ(), Library.NEG_X),
					new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1, Library.POS_Z),
					new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() - 1, Library.NEG_Z),
					new DirPos(pos.getX(), pos.getY() - 2, pos.getZ(), Library.NEG_Y)
			};
		} else if(world.getBlockState(pos.down(2)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(pos.getX(), pos.getY() + RBMKDials.getColumnHeight(world) + 1, pos.getZ(), Library.POS_Y),
					new DirPos(pos.getX() + 1, pos.getY() - 2, pos.getZ(), Library.POS_X),
					new DirPos(pos.getX() - 1, pos.getY() - 2, pos.getZ(), Library.NEG_X),
					new DirPos(pos.getX(), pos.getY() - 2, pos.getZ() + 1, Library.POS_Z),
					new DirPos(pos.getX(), pos.getY() - 2, pos.getZ() - 1, Library.NEG_Z),
					new DirPos(pos.getX(), pos.getY() - 3, pos.getZ(), Library.NEG_Y)
			};
		} else {
			return new DirPos[] {
					new DirPos(pos.getX(), pos.getY() + RBMKDials.getColumnHeight(world) + 1, pos.getZ(), Library.POS_Y)
			};
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		tanks[0].writeToNBT(nbt, "t0");
		tanks[1].writeToNBT(nbt, "t1");
		return nbt;
	}

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
    }

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.COOLER;
	}

	@Override
	public RBMKColumn getConsoleData() {
		RBMKColumn.CoolerColumn data = (RBMKColumn.CoolerColumn) super.getConsoleData();
		data.cooled = lastCooled;
		data.cryo = tanks[0].getFill();
		data.maxCryo = tanks[0].getMaxFill();
		data.hot = tanks[1].getFill();
		data.maxHot = tanks[1].getMaxFill();
		data.coldType = (short) tanks[0].getTankType().getID();
		data.hotType = (short) tanks[1].getTankType().getID();
		return data;
	}

	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();
		data.put("t0_fluidType", new DataValueString(tanks[0].getTankType().getName()));
		data.put("t0_fluidAmount", new DataValueFloat(tanks[0].getFill()));
		data.put("t1_fluidType", new DataValueString(tanks[1].getTankType().getName()));
		data.put("t1_fluidAmount", new DataValueFloat(tanks[1].getFill()));
		return data;
	}

	@Override public FluidTankNTM[] getAllTanks() { return tanks; }
	@Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tanks[0]}; }
	@Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {tanks[1]}; }

	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "rbmk_cooler";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[]{heat};
	}
	
	// Th3_Sl1ze: I'm trying to predict for now
	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCryo(Context context, Arguments args) {
		return new Object[]{tanks[0].getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCryoMax(Context context, Arguments args) {
		return new Object[]{tanks[0].getMaxFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {pos.getX(), pos.getY(), pos.getZ()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[]{heat, tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill(), pos.getX(), pos.getY(), pos.getZ()};
	}
}
