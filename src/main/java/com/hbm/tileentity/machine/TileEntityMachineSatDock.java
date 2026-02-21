package com.hbm.tileentity.machine;

import com.hbm.entity.missile.EntityMinerRocket;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineSatDock;
import com.hbm.inventory.gui.GUIMachineSatDock;
import com.hbm.itempool.ItemPool;
import com.hbm.items.ISatChip;
import com.hbm.lib.Library;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteHorizons;
import com.hbm.saveddata.satellites.SatelliteMiner;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

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

	SatelliteSavedData data = null;

	@Override
	public void update(){
		if(!world.isRemote) {

			if(data == null)
				data = (SatelliteSavedData)world.getPerWorldStorage().getOrLoadData(SatelliteSavedData.class, "satellites");

			if(data == null) {
				world.getPerWorldStorage().setData("satellites", new SatelliteSavedData());
				data = (SatelliteSavedData)world.getPerWorldStorage().getOrLoadData(SatelliteSavedData.class, "satellites");
			}
			data.markDirty();

			if(data != null && !inventory.getStackInSlot(15).isEmpty()) {
				int freq = ISatChip.getFreqS(inventory.getStackInSlot(15));

				Satellite sat = data.getSatFromFreq(freq);

				int delay = 10 * 60 * 1000; //10min

				if(sat instanceof SatelliteMiner miner) {

                    if(miner.lastOp + delay < System.currentTimeMillis()) {

						EntityMinerRocket rocket = new EntityMinerRocket(world);
						rocket.posX = pos.getX() + 0.5;
						rocket.posY = 300;
						rocket.posZ = pos.getZ() + 0.5;

                        rocket.getDataManager().set(EntityMinerRocket.SAT, freq);
						world.spawnEntity(rocket);
						miner.lastOp = System.currentTimeMillis();
						data.markDirty();
					}
				}
                // --- Start Alcater addition ---
				if(sat instanceof SatelliteHorizons gerald) {

                    if(gerald.lastOp + delay < System.currentTimeMillis()) {

						EntityMinerRocket rocket = new EntityMinerRocket(world, (byte)1);
						rocket.posX = pos.getX() + 0.5;
						rocket.posY = 300;
						rocket.posZ = pos.getZ() + 0.5;
						rocket.setRocketType((byte)1);
						world.spawnEntity(rocket);
						gerald.lastOp = System.currentTimeMillis();
						data.markDirty();
					}
				}
                // --- End Alcater addition ---
			}

			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - 0.25 + 0.5, pos.getY() + 0.75, pos.getZ() - 0.25 + 0.5, pos.getX() + 0.25 + 0.5, pos.getY() + 2, pos.getZ() + 0.25 + 0.5));

			for(Entity e : list) {

				if(e instanceof EntityMinerRocket rocket) {
                    ItemStack slot15 = inventory.getStackInSlot(15);
                    if(!slot15.isEmpty() && ISatChip.getFreqS(slot15) != rocket.getDataManager().get(EntityMinerRocket.SAT)) {
                        rocket.setDead();
                        ExplosionNukeSmall.explode(world, pos.getX()+ 0.5, pos.getY()+ 0.5, pos.getZ() + 0.5, ExplosionNukeSmall.PARAMS_TOTS);
                        break;
                    }

                    if(rocket.getDataManager().get(EntityMinerRocket.MODE) == 1 && rocket.timer == 50) {
						Satellite sat = data.getSatFromFreq(ISatChip.getFreqS(slot15));
						if (sat != null) unloadCargo((SatelliteMiner) sat);
					}
				}
			}

			ejectInto(pos.getX() + 2, pos.getY(), pos.getZ(), EnumFacing.EAST);
			ejectInto(pos.getX() - 2, pos.getY(), pos.getZ(), EnumFacing.WEST);
			ejectInto(pos.getX(), pos.getY(), pos.getZ() + 2, EnumFacing.SOUTH);
			ejectInto(pos.getX(), pos.getY(), pos.getZ() - 2, EnumFacing.NORTH);
		}
	}

    private void unloadCargo(SatelliteMiner satellite) {
		int itemAmount = world.rand.nextInt(6) + 10;

		WeightedRandomChestContentFrom1710[] cargo = ItemPool.getPool(satellite.getCargo());

		for(int i = 0; i < itemAmount; i++) {
			addToInv(ItemPool.getStack(cargo, world.rand));
		}
	}

	private void addToInv(ItemStack stack){

		for(int i = 0; i < 15; i++) {

            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if(ItemHandlerHelper.canItemStacksStack(stackInSlot, stack) && stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
                int toAdd = Math.min(stackInSlot.getMaxStackSize() - stackInSlot.getCount(), stack.getCount());
				ItemStack newStack = stackInSlot.copy();
                newStack.setCount(stackInSlot.getCount() + toAdd);
                inventory.setStackInSlot(i, newStack);
                stack.shrink(toAdd);
                if (stack.isEmpty())
				    return;
			}
		}

		for(int i = 0; i < 15; i++) {

			if(inventory.insertItem(i, stack, true).isEmpty()) {
				inventory.insertItem(i, stack, false);
				return;
			}
		}
	}

	private void ejectInto(int x, int y, int z, EnumFacing direction){
		BlockPos eject = new BlockPos(x, y, z);
        EnumFacing opposite = direction.getOpposite();
        Library.popProducts(world, eject, opposite, inventory, 0, 14);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 65536.0D;
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
}
