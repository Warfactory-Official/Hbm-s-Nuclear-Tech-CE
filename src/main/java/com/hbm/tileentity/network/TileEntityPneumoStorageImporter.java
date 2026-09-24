package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPneumoStorageImporter;
import com.hbm.inventory.gui.GUIPneumoStorageImporter;
import com.hbm.lib.ForgeDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_pneumatic_storage_importer")
public class TileEntityPneumoStorageImporter extends TileEntityPneumaticMachineBase {

	public int[] delay = new int[9];
	public int[] SLOT_ACCESS = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};

	public TileEntityPneumoStorageImporter() {
		super(9);
	}

	@Override
	public String getDefaultName() {
		return "container.pneumoStorageImporter";
	}

	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
	@Override public int[] getAccessibleSlotsFromSide(EnumFacing side) { return SLOT_ACCESS; }

	@Override
	public void update() {
		super.update();

		if(!world.isRemote) {

			if(this.cache != null && !this.cache.hasExpired) for(int i = 0; i < 9; i++) {
				if(this.delay[i] > 0) {
					this.delay[i]--;
					continue;
				}
				ItemStack stack = inventory.getStackInSlot(i);
				if(stack.isEmpty()) continue;

				int leftover = (int) this.cache.addItemsAndReturnQuantity(stack, stack.getCount());
				if(leftover == stack.getCount()) {
					this.delay[i] = 100;
				} else {
					inventory.extractItem(i, stack.getCount() - leftover, false);
				}
			}
		}
	}

	@Override
	public boolean canConnectPneumatic(ForgeDirection dir) {
		return true;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerPneumoStorageImporter(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIPneumoStorageImporter(player.inventory, this);
	}
}
