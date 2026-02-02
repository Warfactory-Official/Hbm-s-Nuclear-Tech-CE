package com.hbm.tileentity.machine.rbmk;

import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRBMKStorage;
import com.hbm.inventory.gui.GUIRBMKStorage;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityRBMKStorage extends TileEntityRBMKSlottedBase implements IRBMKLoadable, IGUIProvider {


	public TileEntityRBMKStorage() {
		super(12);
	}

	@Override
	public String getName() {
		return "container.rbmkStorage";
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			int freeSlot = 0;
			for(int i = 0; i < 12; i++){
				if(inventory.getStackInSlot(i).isEmpty()){
					continue;
				}else{
					if(inventory.getStackInSlot(freeSlot).isEmpty()){
						moveItem(i, freeSlot);
					}
					freeSlot++;
				}
			}
		}
		super.update();
	}

	public void moveItem(int fromSlot, int toSlot){
		inventory.setStackInSlot(toSlot, inventory.getStackInSlot(fromSlot).copy());
		inventory.setStackInSlot(fromSlot, ItemStack.EMPTY);
	}

    @Override
    public RBMKNeutronHandler.RBMKType getRBMKType() {
        return RBMKNeutronHandler.RBMKType.OTHER;
    }

    @Override
	public ColumnType getConsoleType() {
		return ColumnType.STORAGE;
	}


	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return true;
	}

	@Override
	public boolean canLoad(ItemStack toLoad) {
		return toLoad != null && inventory.getStackInSlot(11).isEmpty();
	}

	@Override
	public void load(ItemStack toLoad) {
		inventory.setStackInSlot(11, toLoad.copy());
		this.markDirty();
	}

	@Override
	public boolean canUnload() {
		return !inventory.getStackInSlot(0).isEmpty();
	}

	@Override
	public ItemStack provideNext() {
		return inventory.getStackInSlot(0);
	}

	@Override
	public void unload() {
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		this.markDirty();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKStorage(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKStorage(player.inventory, this);
	}
}
