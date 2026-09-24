package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTapeDrive;
import com.hbm.inventory.gui.GUITapeDrive;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrive.EnumDriveType;
import com.hbm.lib.ForgeDirection;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TileEntityProxyBase;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_tape_drive")
public class TileEntityMachineTapeDrive extends TileEntityMachineBase implements ITickable, IGUIProvider {

	public byte[] tapes = new byte[12];
	public static final byte SLOT_EMPTY			= 0;
	public static final byte SLOT_ANY			= 1;
	public static final byte SLOT_EMPTY_TAPE	= 2;
	public static final byte SLOT_FILLED_TAPE	= 3;

	public TileEntityMachineTapeDrive() {
		super(12, 1);
	}

	@Override
	public String getDefaultName() {
		return "container.machineTapeDrive";
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			if(world.getTotalWorldTime() % 10 == 0) {

				EnumFacing facing = world.getBlockState(pos).getValue(net.minecraft.block.BlockHorizontal.FACING).getOpposite();
				TileEntity connected = Compat.getTileStandard(world, pos.getX() + facing.getXOffset(), pos.getY(), pos.getZ() + facing.getZOffset());

				if(connected instanceof TileEntityProxyBase) {
					connected = ((TileEntityProxyBase) connected).getTE();
				}

				if(connected instanceof TileEntityMachineSatLink) {
					TileEntityMachineSatLink link = (TileEntityMachineSatLink) connected;
					if(link.connected) {

						SatelliteSavedData dat = SatelliteSavedData.getData(world);
						Satellite satellite = dat.sats.get(link.freq);

						if(satellite != null && satellite.hasData(world)) {

							for(int i = 0; i < 12; i++) {
								ItemStack stack = inventory.getStackInSlot(i);
								if(stack.isEmpty() || stack.getItem() != ModItems.drive) continue;
								EnumDriveType type = EnumUtil.grabEnumSafely(EnumDriveType.VALUES, stack.getItemDamage());
								EnumDriveType ret = satellite.getOutputData(type);

								if(ret != null) {
									satellite.consumeData();
									inventory.setStackInSlot(i, new ItemStack(ModItems.drive, 1, ret.ordinal()));
									dat.markDirty();
									break;
								}
							}
						}
					}
				}
			}

			this.networkPackNT(50);
		}
	}

	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return stack.getItem() == ModItems.drive; }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		for(int i = 0; i < 12; i++) {

			byte type = SLOT_EMPTY;
			ItemStack stack = inventory.getStackInSlot(i);

			if(!stack.isEmpty()) {
				type = SLOT_ANY;

				if(stack.getItem() == ModItems.drive) {
					int meta = stack.getItemDamage();
					if(meta == EnumDriveType.DISK_EMPTY.ordinal() || meta == EnumDriveType.FLASH_EMPTY.ordinal()) {
						type = SLOT_EMPTY_TAPE;
					} else if(meta == EnumDriveType.DISK_BROKEN.ordinal() || meta == EnumDriveType.FLASH_BROKEN.ordinal()) {
						type = SLOT_ANY;
					} else {
						type = SLOT_FILLED_TAPE;
					}
				}
			}

			buf.writeByte(type);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		for(int i = 0; i < 12; i++) this.tapes[i] = buf.readByte();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTapeDrive(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITapeDrive(player.inventory, this);
	}
}
