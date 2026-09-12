package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineSatDock;
import com.hbm.inventory.gui.GUIMachineSatDock;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMachineSatDock extends TileEntityMachineBase implements ITickable, IGUIProvider {

	private static final int[] access = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

	public TileEntityMachineSatDock(){
		super(16);
	}

	@Override
	public String getDefaultName() {
		return "container.satDock";
	}

	public boolean isUseableByPlayer(EntityPlayer player){
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	@Override
	public void update(){

		if(!world.isRemote) {

			if((world.getTotalWorldTime() + Math.abs(pos.getX() * 31 + pos.getZ())) % 20 == 0) {

				ItemStack chip = inventory.getStackInSlot(15);

				if(!chip.isEmpty() && chip.getItem() == ModItems.sat_chip) {

					SatelliteSavedData data = SatelliteSavedData.getData(world);
					Satellite sat = data.getSatFromFreq(ISatChip.getFreqS(chip));

					if(sat != null && sat.tryRequestItems(world, pos.getX(), pos.getY(), pos.getZ())) data.markDirty();
				}
			}

			ejectInto(pos.getX() + 2, pos.getY(), pos.getZ(), EnumFacing.EAST);
			ejectInto(pos.getX() - 2, pos.getY(), pos.getZ(), EnumFacing.WEST);
			ejectInto(pos.getX(), pos.getY(), pos.getZ() + 2, EnumFacing.SOUTH);
			ejectInto(pos.getX(), pos.getY(), pos.getZ() - 2, EnumFacing.NORTH);
		}
	}

	private void ejectInto(int x, int y, int z, EnumFacing direction){
		BlockPos eject = new BlockPos(x, y, z);
		Library.popProducts(world, eject, direction.getOpposite(), inventory, 0, 14);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return access;
	}

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack) {
        return this.isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 15;
    }

    @Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount){
		return slot != 15;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSatDock(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSatDock(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
