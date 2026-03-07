package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.lib.ForgeDirection;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public abstract class TileEntityRBMKControl extends TileEntityRBMKSlottedBase implements SimpleComponent, CompatHandler.OCComponent, IEnergyReceiverMK2 {

	@SideOnly(Side.CLIENT)
	public double lastLevel;
	public double level;
	public static final double speed = 0.00277D; // it takes around 18 seconds for the thing to fully extend
	public double targetLevel;

	public boolean hasPower = false;;
	public long power;
	public static final long consumption = 5_000;
	public static final long maxPower = consumption * 10; // enough buffer for half a second of movement

	public TileEntityRBMKControl() {
		super(0);
	}

	public boolean isPowered() {
		return this.getBlockType() == ModBlocks.rbmk_control_reasim || this.getBlockType() == ModBlocks.rbmk_control_reasim_auto;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return isPowered() ? this.maxPower : 0; }

	@Override public boolean canConnect(ForgeDirection dir) {
		return isPowered() ? dir == ForgeDirection.DOWN : false;
	}

	@Override public ConnectionPriority getPriority() {
		return ConnectionPriority.LOW; // high would make more sense, but i am a sadistic asshole
	}

	@Override
	public boolean isLidRemovable() {
		return false;
	}
	
	@Override
	public void update() {
		
		if(world.isRemote) {
			
			this.lastLevel = this.level;
		
		} else {

			this.hasPower = true;

			if(this.isPowered()) {
				this.trySubscribe(world, pos.getX(), pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN);
				if(this.power < consumption) this.hasPower = false;
			}

			this.lastLevel = this.level;

			if(this.hasPower) {
				if(level < targetLevel) {
					level += speed * RBMKDials.getControlSpeed(world);
					if(level > targetLevel) level = targetLevel;
				}

				if(level > targetLevel) {
					level -= speed * RBMKDials.getControlSpeed(world);
					if(level < targetLevel) level = targetLevel;
				}

				if(this.isPowered() && level != lastLevel) {
					this.power -= this.consumption;
				}
			}
		}
		
		super.update();
	}
	
	public void setTarget(double target) {
		this.targetLevel = target;
	}
	
	public double getMult() {
		return this.level;
	}
	
	@Override
	public int trackingRange() {
		return 100;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.level = nbt.getDouble("level");
		this.targetLevel = nbt.getDouble("targetLevel");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setDouble("level", this.level);
		nbt.setDouble("targetLevel", this.targetLevel);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeDouble(this.level);
		buf.writeDouble(this.targetLevel);
		buf.writeLong(this.power);
		buf.writeBoolean(this.hasPower);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.level = buf.readDouble();
		this.targetLevel = buf.readDouble();
		this.power = buf.readLong();
		this.hasPower = buf.readBoolean();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
	
	@Override
	public void onMelt(int reduce) {

		if(this.isModerated()) {

			int count = 2 + world.rand.nextInt(2);

			for(int i = 0; i < count; i++) {
				spawnDebris(DebrisType.GRAPHITE);
			}
		}

		int count = 2 + world.rand.nextInt(2);

		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.ROD);
		}
		
		this.standardMelt(reduce);
	}

	@Override
	public RBMKNeutronHandler.RBMKType getRBMKType() {
		return RBMKNeutronHandler.RBMKType.CONTROL_ROD;
	}

	@Override
	public RBMKColumn getConsoleData() {
		RBMKColumn.ControlColumn data = (RBMKColumn.ControlColumn) super.getConsoleData();
		data.level = this.level;
		return data;
	}

	// control panel
	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();

		data.put("level", new DataValueFloat((float) this.level*100));
		data.put("targetLevel", new DataValueFloat((float) this.targetLevel*100));

		return data;
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "rbmk_control_rod";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getLevel(Context context, Arguments args) {
		return new Object[] {getMult() * 100};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getTargetLevel(Context context, Arguments args) {
		return new Object[] {targetLevel * 100};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {pos.getX(), pos.getY(), pos.getZ()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[] {heat};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {heat, getMult() * 100, targetLevel * 100, pos.getX(), pos.getY(), pos.getZ()};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "opencomputers")
	public Object[] setLevel(Context context, Arguments args) {
		double newLevel = args.checkDouble(0)/100.0;
		targetLevel = MathHelper.clamp(newLevel, 0, 1);
		return new Object[] {};
	}
}
