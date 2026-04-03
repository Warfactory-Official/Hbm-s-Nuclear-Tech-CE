package com.hbm.tileentity.machine;

import com.hbm.handler.MissileStruct;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineMissileAssembly;
import com.hbm.inventory.gui.GUIMachineMissileAssembly;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissile.FuelType;
import com.hbm.items.weapon.ItemMissile.PartType;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ItemStackHandlerWrapper;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEMissileMultipartPacket;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@AutoRegister
public class TileEntityMachineMissileAssembly extends TileEntity implements ITickable, IGUIProvider {

	private AxisAlignedBB bb;
	public ItemStackHandler inventory;

	public MissileStruct load;

	//private static final int[] access = new int[] { 0 };

	private String customName;

	public static int xCooldown = 20;
	public int cooldown = 20;
	
	public TileEntityMachineMissileAssembly() {
		inventory = new ItemStackHandler(6){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.missileAssembly";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			
			MissileStruct multipart = new MissileStruct(inventory.getStackInSlot(1), inventory.getStackInSlot(2), inventory.getStackInSlot(3), inventory.getStackInSlot(4));
			if(cooldown == 0 && world.isBlockPowered(pos)){
				construct();
				cooldown = xCooldown;
			}else{
				cooldown = Math.max(0, cooldown-1);
			}
			PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(pos, multipart), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
		}
	}
	
	public int fuselageState() {
		
		if(inventory.getStackInSlot(2).getItem() instanceof ItemMissile) {
			
			ItemMissile part = (ItemMissile)inventory.getStackInSlot(2).getItem();
			
			if(part.type == PartType.FUSELAGE)
				return 1;
		}
		
		return 0;
	}

	public int chipState() {
		
		if(inventory.getStackInSlot(0).getItem() instanceof ItemMissile) {
			
			ItemMissile part = (ItemMissile)inventory.getStackInSlot(0).getItem();
			
			if(part.type == PartType.CHIP)
				return 1;
		}
		
		return 0;
	}

	public int warheadState() {
		
		if(inventory.getStackInSlot(1).getItem() instanceof ItemMissile &&
				inventory.getStackInSlot(2).getItem() instanceof ItemMissile &&
				inventory.getStackInSlot(4).getItem() instanceof ItemMissile) {

			ItemMissile part = (ItemMissile)inventory.getStackInSlot(1).getItem();
			ItemMissile fuselage = (ItemMissile)inventory.getStackInSlot(2).getItem();
			ItemMissile thruster = (ItemMissile)inventory.getStackInSlot(4).getItem();
			if(!(part.attributes.length > 2 && thruster.attributes.length > 2))
				return 0;

			float weight = (Float)part.attributes[2];
			float thrust = (Float)thruster.attributes[2];
			
			if(part.type == PartType.WARHEAD && fuselage.type == PartType.FUSELAGE && part.bottom == fuselage.top && weight <= thrust)
				return 1;
		}
		
		return 0;
	}

	public int stabilityState() {
		
		if(inventory.getStackInSlot(3).isEmpty())
			return -1;
		
		if(inventory.getStackInSlot(3).getItem() instanceof ItemMissile && inventory.getStackInSlot(2).getItem() instanceof ItemMissile) {

			ItemMissile part = (ItemMissile)inventory.getStackInSlot(3).getItem();
			ItemMissile fuselage = (ItemMissile)inventory.getStackInSlot(2).getItem();
			
			if(part.type == PartType.FINS && fuselage.type == PartType.FUSELAGE &&
					part.top == fuselage.bottom)
				return 1;
		}
		
		return 0;
	}

	public int thrusterState() {
		
		if(inventory.getStackInSlot(4).getItem() instanceof ItemMissile && inventory.getStackInSlot(2).getItem() instanceof ItemMissile) {

			ItemMissile part = (ItemMissile)inventory.getStackInSlot(4).getItem();
			ItemMissile fuselage = (ItemMissile)inventory.getStackInSlot(2).getItem();
			
			if(part.type == PartType.THRUSTER && fuselage.type == PartType.FUSELAGE &&
					part.top == fuselage.bottom && (FuelType)part.attributes[0] == (FuelType)fuselage.attributes[0]) {
				return 1;
			}
		}
		
		return 0;
	}
	
	public boolean canBuild() {
		
		if(inventory.getStackInSlot(5).isEmpty() && chipState() == 1 && warheadState() == 1 && fuselageState() == 1 && thrusterState() == 1) {
			return stabilityState() != 0;
		}
		
		return false;
	}

	public void construct() {
		
		if(!canBuild())
			return;
		
		inventory.setStackInSlot(5, ItemCustomMissile.buildMissile(inventory.getStackInSlot(0), inventory.getStackInSlot(1), inventory.getStackInSlot(2), inventory.getStackInSlot(3), inventory.getStackInSlot(4)).copy());
		
		if(stabilityState() == 1)
			inventory.setStackInSlot(3, ItemStack.EMPTY);

		inventory.setStackInSlot(0, ItemStack.EMPTY);
		inventory.setStackInSlot(1, ItemStack.EMPTY);
		inventory.setStackInSlot(2, ItemStack.EMPTY);
		inventory.setStackInSlot(4, ItemStack.EMPTY);

		this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.missileAssembly2, SoundCategory.BLOCKS, 1F, 1F);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX() - 6, pos.getY(), pos.getZ() - 6, pos.getX() + 7, pos.getY() + 3, pos.getZ() + 7);
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(stack.getItem() instanceof ItemMissile){
			ItemMissile missilePart = (ItemMissile) stack.getItem();
			if(slot == 0){
				return missilePart.type == PartType.CHIP;
			}
			if(slot == 1){
				return missilePart.type == PartType.WARHEAD;
			}
			if(slot == 2){
				return missilePart.type == PartType.FUSELAGE;
			}
			if(slot == 3){
				return missilePart.type == PartType.FINS;
			}
			if(slot == 4){
				return missilePart.type == PartType.THRUSTER;
			}
		}
		return false;

	}

	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[]{0, 1, 2, 3, 4, 5};
	}
	
	public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return (slot == 5);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null){
			if(facing == null)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandlerWrapper(inventory, getAccessibleSlotsFromSide(facing)){
				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					if(canExtractItem(slot, inventory.getStackInSlot(slot), amount))
						return super.extractItem(slot, amount, simulate);
					return ItemStack.EMPTY;
				}
				
				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
					if(canInsertItem(slot, stack, stack.getCount()))
						return super.insertItem(slot, stack, simulate);
					return stack;
				}
			});
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) || super.hasCapability(capability, facing);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineMissileAssembly(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineMissileAssembly(player.inventory, this);
	}
}
