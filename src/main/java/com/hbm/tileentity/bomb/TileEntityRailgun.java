package com.hbm.tileentity.bomb;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.config.RadiationConfig;
import com.hbm.entity.projectile.EntityRailgunBlast;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRailgun;
import com.hbm.inventory.gui.GUIRailgun;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Vec3dUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRailgun extends TileEntityLoadedBase implements ITickable, IEnergyReceiverMK2, IGUIProvider, IConnectionAnchors {

	private AxisAlignedBB bb;
	public ItemStackHandler inventory;
	public ICapabilityProvider specialProvider;
	
	//private static final int[] slots_top = new int[] { 0 };
	//private static final int[] slots_bottom = new int[] { 0 };
	//private static final int[] slots_side = new int[] { 0 };
	
	private long power;

	//system time for interpolation
	public long startTime;
	//system time for fire button
	public long fireTime;
	//prev pitch for interpolation
	public float lastPitch;
	//prev yaw for interpolation
	public float lastYaw;

	public static int cooldownDurationMillis = 5000;
	public static int cooldownDurationTicks = 100;
	
	public float pitch;
	public float yaw;
	//delay so the server disables fire buton while turning
	public int delay;
	//countdown to firing
	public int fireDelay;
	
	private boolean fired;
	private String customName;
	
	public TileEntityRailgun() {
		inventory = new ItemStackHandler(3){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
		specialProvider = new ICapabilityProvider(){

			@Override
			public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
				return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
			}

			@Override
			public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
				return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : null;
			}
			
		};
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.railgun";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		power = nbt.getLong("power");
		pitch = nbt.getFloat("pitch");
		yaw = nbt.getFloat("yaw");
		
		if(nbt.hasKey("inventory"))
			inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		nbt.setFloat("pitch", pitch);
		nbt.setFloat("yaw", yaw);
		
		nbt.setTag("inventory", inventory.serializeNBT());
		
		return super.writeToNBT(nbt);
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			fired = false;
			if(delay > 0) {
				delay--;
			}
			
			if(fireDelay > 0) {
				fireDelay--;
				
				if(fireDelay == 0)
					tryFire();
			}
			updateConnections();
			power = Library.chargeTEFromItems(inventory, 0, power, RadiationConfig.railgunBuffer);
			networkPackNT(100);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		buf.writeFloat(pitch);
		buf.writeFloat(yaw);
		buf.writeBoolean(fired);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		power = buf.readLong();
		pitch = buf.readFloat();
		yaw = buf.readFloat();
		if (buf.readBoolean()) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Vec3d vec = new Vec3d(5.5, 0, 0);
				vec = Vec3dUtil.rotateRoll(vec, (float) (pitch * Math.PI / 180D));
				vec = vec.rotateYaw((float) (yaw * Math.PI / 180D));

				double fX = pos.getX() + 0.5 + vec.x;
				double fY = pos.getY() + 1 + vec.y;
				double fZ = pos.getZ() + 0.5 + vec.z;

				MainRegistry.proxy.spawnSpark(world, fX, fY, fZ, vec.normalize());
			});
		}
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
		};
	}
	
	public boolean setAngles(boolean miss) {
		
		if(
				(inventory.getStackInSlot(1).getItem() == ModItems.designator || inventory.getStackInSlot(1).getItem() == ModItems.designator_range || inventory.getStackInSlot(1).getItem() == ModItems.designator_manual) &&
				inventory.getStackInSlot(1).getTagCompound() != null) {

    		int x = inventory.getStackInSlot(1).getTagCompound().getInteger("xCoord");
    		int z = inventory.getStackInSlot(1).getTagCompound().getInteger("zCoord");

    		Vec3d vec = new Vec3d(x - pos.getX(), 0, z - pos.getZ());
    		Vec3d unit = new Vec3d(1, 0, 0);
    		
    		if(miss) {
    			vec = vec.rotateYaw((float) (world.rand.nextGaussian() * Math.PI / 45));
    		}
    		
    		if(vec.length() < 1 || vec.length() > 9000)
    			return false;
    		
    		double yawUpper = vec.x * unit.x/* + vec.zCoord * unit.zCoord*/; //second side falls away since unit.z is always 0
    		double yawLower = vec.length()/* * unit.length()*/; //second side falls away since unit always has length 1
    		float yaw = (float) Math.acos(yawUpper / yawLower);
    		float pitch = (float) (Math.asin((vec.length() * 9.81) / (300 * 300)) / 2D);
			
    		float newYaw = (float) (yaw * 180D / Math.PI);
    		float newPitch = (float) (pitch * 180D / Math.PI) - 90F;
    		
    		if(vec.z > 0)
    			newYaw = 0 - (float) (yaw * 180D / Math.PI);
    		
    		if(newYaw != this.yaw || newPitch != this.pitch) {
    			this.yaw = newYaw;
    			this.pitch = newPitch;
    			this.delay = cooldownDurationTicks;
    			return true;
    		}
		}
		
		return false;
	}
	
	public boolean canFire() {
		
		int required = RadiationConfig.railgunUse;
		
		if(inventory.getStackInSlot(2).getItem() == ModItems.charge_railgun && power >= required) {
			return true;
		}
		
		return false;
	}
	
	public void tryFire() {
		
		if(canFire()) {
			fire();
			inventory.setStackInSlot(2, ItemStack.EMPTY);
			power -= RadiationConfig.railgunUse;
			if(power < 0)
				power = 0;
			fired = true;
		} else {
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.buttonNo, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}
	
	public void fire() {
		
		Vec3 vec = Vec3.createVectorHelper(6, 0, 0);
		vec.rotateAroundZ((float) (pitch * Math.PI / 180D));
		vec.rotateAroundY((float) (yaw * Math.PI / 180D));

		double fX = pos.getX() + 0.5 + vec.xCoord;
		double fY = pos.getY() + 1 + vec.yCoord;
		double fZ = pos.getZ() + 0.5 + vec.zCoord;
		
		vec = vec.normalize();
		double motionX = vec.xCoord * 15D;
		double motionY = vec.yCoord * 15D;
		double motionZ = vec.zCoord * 15D;
		
		EntityRailgunBlast fart = new EntityRailgunBlast(world);
		fart.posX = fX;
		fart.posY = fY;
		fart.posZ = fZ;
		fart.motionX = motionX;
		fart.motionY = motionY;
		fart.motionZ = motionZ;
		fart.rotation();
		world.spawnEntity(fart);
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.railgunFire, SoundCategory.BLOCKS, 100.0F, 1.0F);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX() - 6, pos.getY() - 3, pos.getZ() - 6, pos.getX() + 7, pos.getY() + 6, pos.getZ() + 7);
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return RadiationConfig.railgunBuffer;
	}
	
	public long getPowerScaled(long i) {
		//System.out.println(power * i);
		//System.out.println(MainRegistry.railgunBuffer);
		return (power * i) / RadiationConfig.railgunBuffer;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRailgun(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRailgun(player.inventory, this);
	}
}
