package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineUF6Tank;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineUF6Tank;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineUF6Tank extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiver, IGUIProvider {
	public FluidTankNTM tank;
	public byte lastRedstone = 0;

	//private static final int[] slots_top = new int[] {0};
	//private static final int[] slots_bottom = new int[] {1, 3};
	//private static final int[] slots_side = new int[] {2};

	public TileEntityMachineUF6Tank() {
		super(4, true, false);
		tank = new FluidTankNTM(Fluids.UF6, 64000);
	}

	@Override
	public String getDefaultName() {
		return "container.uf6_tank";
	}


	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		tank.readFromNBT(compound, "tank");
	}

	@NotNull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		tank.writeToNBT(compound, "tank");
		return super.writeToNBT(compound);
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			tank.loadTank(0, 1, inventory);
			tank.unloadTank(2, 3, inventory);

			// Redstone Comparator Check
			byte comp = tank.getRedstoneComparatorPower();
			if(comp != this.lastRedstone) {
				this.markDirty();
				world.updateComparatorOutputLevel(pos, getBlockType());
				world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
			}
			this.lastRedstone = comp;
		}
	}

	@Override
	public void serialize(ByteBuf buf){
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf){
		tank.deserialize(buf);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos, pos.add(1, 2, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineUF6Tank(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineUF6Tank(player.inventory, this);
	}
}
