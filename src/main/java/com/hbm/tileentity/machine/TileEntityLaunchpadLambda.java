package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.entity.missile.EntityRocketLambda;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerLaunchpadLambda;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUILaunchpadLambda;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "tileentity_launchpad_lambda")
public class TileEntityLaunchpadLambda extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardReceiverMK2, IGUIProvider, IControlReceiver {

	public long power;
	public static final long maxPower = 1_000_000;
	public static final long CONSUMPTION = 10_000;
	public FluidTankNTM[] tanks;

	public static final int INDEX_DOORS		= 0;
	public static final int INDEX_ERECTOR	= 1;
	public static final int INDEX_ROTOR		= 2;
	public static final int INDEX_CLAMPS	= 3;
	public static final int INDEX_PISTONS	= 4;

	public float[] positions		= new float[5];
	public float[] prevPositions	= new float[5];
	public float[] speed			= new float[5];
	public float[] target			= new float[5];
	public float[] syncPositions	= new float[5];

	protected int turnProgress;

	public boolean erected = false;
	public boolean erecting = false;

	public int animationDelay = 0;

	public boolean autolaunch = false;

	private AudioWrapper audio;

	public static final int COUNTDOWN_DURATION = 200;
	public int countdown;

	public TileEntityLaunchpadLambda() {
		super(7);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.GASOLINE_LEADED, 64_000);
		tanks[1] = new FluidTankNTM(Fluids.PEROXIDE, 64_000);
	}

	@Override
	public String getDefaultName() {
		return "container.launchpadLambda";
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			this.power = Library.chargeTEFromItems(inventory, 6, power, maxPower);

			tanks[0].loadTank(2, 3, inventory);
			tanks[1].loadTank(4, 5, inventory);

			if(!this.hasRocketLoaded()) {
				this.erected = false;
				this.erecting = false;
				this.countdown = 0;
			}

			if(this.power >= CONSUMPTION) {
				this.updateStates();
				this.move();
				this.power -= CONSUMPTION;

				if(this.autolaunch && this.erected && this.canLaunch() && this.countdown <= 0) {
					this.countdown = COUNTDOWN_DURATION;
				}
			}

			this.networkPackNT(300);

		} else {

			for(int i = 0; i < this.positions.length; i++) {

				this.prevPositions[i] = this.positions[i];

				if(this.turnProgress > 0) {
					this.positions[i] = this.positions[i] + ((this.syncPositions[i] - this.positions[i]) / (float) this.turnProgress);
					--this.turnProgress;
				} else {
					this.positions[i] = this.syncPositions[i];
				}
			}

			if(this.countdown > 0) {

				if(this.audio != null && !this.audio.isPlaying()) {
					this.audio.stopSound();
					this.audio = null;
				}
				if(this.audio == null) {
					this.audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.alarmRegular, SoundCategory.BLOCKS, pos.getX() + 0.5F, pos.getY() + 3F, pos.getZ() + 0.5F, 10F, 50F, 1F, 20);
					this.audio.startSound();
				}
				this.audio.keepAlive();

			} else {
				if(this.audio != null) {
					this.audio.stopSound();
					this.audio = null;
				}
			}

			if(this.erected && this.hasOxidizer()) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 0F);
				data.setFloat("base", 0.5F);
				data.setFloat("max", 2F);
				data.setInteger("life", 70 + world.rand.nextInt(30));
				data.setDouble("posX", pos.getX() + 0.5 + world.rand.nextGaussian() * 0.25);
				data.setDouble("posZ", pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.25);
				data.setDouble("posY", pos.getY() + 2);
				data.setBoolean("noWind", true);
				data.setFloat("alphaMod", 2F);
				data.setFloat("strafe", 0.075F);
				for(int i = 0; i < 3; i++) MainRegistry.proxy.effectNT(data);
			}
		}
	}

	public void updateStates() {

		if(finishedAllMoving() && this.erecting && !this.erected) {

			if(this.erectorDown() && this.doorsClosed()) {
				this.setTarget(INDEX_DOORS, 3F, 3F, 60);
				this.setTarget(INDEX_PISTONS, 0F, 1F, 1);
				this.setTarget(INDEX_CLAMPS, 0F, 1F, 1);
				this.setTarget(INDEX_ROTOR, 180F, 180F, 1);
			} else if(this.erectorDown() && this.doorsOpen()) {
				this.animationDelay = 20;
				this.setTarget(INDEX_ERECTOR, 27F, 27F, 100);
			} else if(this.erectorUp() && this.rotorDown()) {
				this.animationDelay = 20;
				this.setTarget(INDEX_ROTOR, 0F, 180F, 60);
				this.setTarget(INDEX_DOORS, 0F, 3F, 60);
			} else if(this.erectorUp() && this.rotorUp() && this.doorsClosed()) {
				this.animationDelay = 20;
				this.setTarget(INDEX_ERECTOR, 25F, 2F, 60);
			} else if(this.erectorCenter() && this.rotorUp() && this.doorsClosed()) {
				this.erected = true;
			} else {
				this.erecting = false;
			}
		}

		if(this.erected && this.countdown <= 20 && this.countdown > 0) {
			this.erecting = false;
		}

		if(finishedAllMoving() && !this.erecting) {

			this.setTarget(INDEX_DOORS, 0F, 3F, 60);

			if(this.clampsOn()) {
				this.setTarget(INDEX_PISTONS, 0.75F, 0.75F, 20);
			} else if(this.clampsOff() && this.clampsDown()) {
				this.animationDelay = 10;
				this.setTarget(INDEX_CLAMPS, 90F, 90F, 40);
			} else if(this.clampsUp() && !this.erectorDown()) {
				this.animationDelay = 20;
				this.setTarget(INDEX_ERECTOR, 0F, 25F, 100);
			} else if(this.erectorDown() && this.doorsClosed()) {

				if(this.hasRocketLoaded()) {
					this.erecting = true;
				}
			}
		}

		if(this.erected && this.countdown > 0) {
			this.countdown--;

			if(this.countdown <= 0) {
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.soyuzTakeOff, SoundCategory.BLOCKS, 100F, 1.1F);
				this.liftOff();
			}
		}
	}

	public void liftOff() {

		double x = pos.getX() + 0.5;
		double y = pos.getY() + 2;
		double z = pos.getZ() + 0.5;

		EntityRocketLambda rocket = new EntityRocketLambda(world);
		rocket.setLocationAndAngles(x, y, z, 0, 0);
		world.spawnEntity(rocket);

		tanks[0].setFill(tanks[0].getFill() - 24_000);
		tanks[1].setFill(tanks[1].getFill() - 24_000);

		rocket.setSat(inventory.getStackInSlot(1).copy());

		inventory.setStackInSlot(0, ItemStack.EMPTY);
		inventory.setStackInSlot(1, ItemStack.EMPTY);

		this.markChanged();
	}

	public boolean doorsClosed() { return this.positions[INDEX_DOORS] == 0F; }
	public boolean doorsOpen() { return this.positions[INDEX_DOORS] == 3F; }
	public boolean erectorDown() { return this.positions[INDEX_ERECTOR] == 0F; }
	public boolean erectorCenter() { return this.positions[INDEX_ERECTOR] == 25F; }
	public boolean erectorUp() { return this.positions[INDEX_ERECTOR] == 27F; }
	public boolean rotorUp() { return this.positions[INDEX_ROTOR] == 0F; }
	public boolean rotorDown() { return this.positions[INDEX_ROTOR] == 180F; }
	public boolean clampsOn() { return this.positions[INDEX_PISTONS] == 0F; }
	public boolean clampsOff() { return this.positions[INDEX_PISTONS] == 0.75F; }
	public boolean clampsUp() { return this.positions[INDEX_CLAMPS] == 90F; }
	public boolean clampsDown() { return this.positions[INDEX_CLAMPS] == 0F; }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		buf.writeLong(power);
		buf.writeBoolean(erecting);
		buf.writeBoolean(erected);
		buf.writeBoolean(autolaunch);
		buf.writeInt(countdown);

		for(int i = 0; i < this.positions.length; i++) {
			buf.writeFloat(this.positions[i]);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		this.power = buf.readLong();
		this.erecting = buf.readBoolean();
		this.erected = buf.readBoolean();
		this.autolaunch = buf.readBoolean();
		this.countdown = buf.readInt();

		for(int i = 0; i < this.positions.length; i++) {
			float newSync = buf.readFloat();
			if(this.syncPositions[i] != newSync) {
				this.syncPositions[i] = newSync;
				this.turnProgress = 3;
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.erected = nbt.getBoolean("erected");
		this.erecting = nbt.getBoolean("erecting");
		this.autolaunch = nbt.getBoolean("autolaunch");
		this.countdown = nbt.getInteger("countdown");
		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");

		for(int i = 0; i < this.positions.length; i++) {
			this.positions[i] = nbt.getFloat("pos" + i);
			this.target[i] = nbt.getFloat("tar" + i);
			this.speed[i] = nbt.getFloat("spd" + i);
		}
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		nbt.setBoolean("erected", erected);
		nbt.setBoolean("erecting", erecting);
		nbt.setBoolean("autolaunch", autolaunch);
		nbt.setInteger("countdown", countdown);
		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");

		for(int i = 0; i < this.positions.length; i++) {
			nbt.setFloat("pos" + i, this.positions[i]);
			nbt.setFloat("tar" + i, this.target[i]);
			nbt.setFloat("spd" + i, this.speed[i]);
		}

		return super.writeToNBT(nbt);
	}

	public void setTarget(int index, float target, float span, int duration) {
		if(span <= 0) span = 1F;
		this.target[index] = target;
		this.speed[index] = span / duration;
	}

	public void move() {

		if(this.animationDelay > 0) this.animationDelay--;

		for(int i = 0; i < this.positions.length; i++) {

			this.prevPositions[i] = this.positions[i];
			if(this.animationDelay > 0) continue;

			if(Math.abs(this.positions[i] - this.target[i]) <= this.speed[i]) {
				this.positions[i] = this.target[i];
			} else if(this.positions[i] < this.target[i]) {
				this.positions[i] += this.speed[i];
			} else {
				this.positions[i] -= this.speed[i];
			}
		}
	}

	public boolean canLaunch() {
		if(!this.hasRocketLoaded()) return false;
		if(!this.hasAllFuel()) return false;
		if(this.power < CONSUMPTION) return false;

		ItemStack sat = inventory.getStackInSlot(1);
		if(sat.isEmpty()) return false;
		if(!this.isItemValidForSlot(1, sat)) return false;
		if(TileEntityLaunchpadSoyuz.needsOrbiter(sat)) return false;

		return true;
	}

	public boolean hasRocketLoaded() {
		ItemStack stack = inventory.getStackInSlot(0);
		return !stack.isEmpty() && stack.getItem() == ModItems.missile_lambda;
	}

	public boolean finishedMoving(int index) { return this.positions[index] == this.target[index]; }

	public boolean finishedAllMoving() {
		for(int i = 0; i < this.positions.length; i++) if(!finishedMoving(i)) return false;
		return true;
	}

	public float getInterpPos(int index, float interp) {
		return prevPositions[index] + (positions[index] - prevPositions[index]) * interp;
	}

	public boolean hasAllFuel() {
		return hasJetFuel() && hasOxidizer();
	}

	public boolean hasJetFuel() { return this.tanks[0].getFill() >= 24_000; }
	public boolean hasOxidizer() { return this.tanks[1].getFill() >= 24_000; }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return stack.getItem() == ModItems.missile_lambda;
		if(slot == 1) return stack.getItem() instanceof ISatChip;
		if(slot == 6) return stack.getItem() instanceof IBatteryItem;
		return true;
	}

	@Override public long getPower() { return this.power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTankNTM[] getReceivingTanks() { return tanks; }
	@Override public FluidTankNTM[] getAllTanks() { return tanks; }

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerLaunchpadLambda(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUILaunchpadLambda(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 7,
					pos.getY(),
					pos.getZ() - 7,
					pos.getX() + 8,
					pos.getY() + 30,
					pos.getZ() + 8);
		}

		return bb;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {

		if(data.hasKey("auto")) {
			this.autolaunch = data.getBoolean("auto");
			this.markChanged();
		}

		if(data.hasKey("launch")) {

			if(canLaunch()) {
				this.countdown = COUNTDOWN_DURATION;
			}
		}
	}
}
