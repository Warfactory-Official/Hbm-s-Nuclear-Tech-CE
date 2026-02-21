package com.hbm.tileentity.machine.rbmk;

import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
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
public abstract class TileEntityRBMKControl extends TileEntityRBMKSlottedBase implements SimpleComponent, CompatHandler.OCComponent {

	@SideOnly(Side.CLIENT)
	public double lastLevel;
	public double level;
	public static final double speed = 0.00277D; // it takes around 18 seconds for the thing to fully extend
	public double targetLevel;

	public TileEntityRBMKControl() {
		super(0);
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

			if(level < targetLevel) {

				level += speed * RBMKDials.getControlSpeed(world);

				if(level > targetLevel)
					level = targetLevel;
			}

			if(level > targetLevel) {

				level -= speed * RBMKDials.getControlSpeed(world);

				if(level < targetLevel)
					level = targetLevel;
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
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.level = buf.readDouble();
		this.targetLevel = buf.readDouble();
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
