package com.hbm.saveddata.satellites;

import com.hbm.entity.logic.EntityDeathBlast;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Locale;

public class SatelliteLaser extends Satellite {

	public static final String CMD_FIRE = "fire";
	public static final String CMD_CANFIRE = "canfire";

	public static final int CHARGE_TIME = 5 * 60 * 20;

	public long lastOp;

	public SatelliteLaser() {
		this.ifaceAcs.add(InterfaceActions.HAS_MAP);
		this.ifaceAcs.add(InterfaceActions.SHOW_COORDS);
		this.ifaceAcs.add(InterfaceActions.CAN_CLICK);
		this.satIface = Interfaces.SAT_PANEL;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("lastOp", lastOp);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		lastOp = nbt.getLong("lastOp");
	}

	@Override
	public ITextComponent[] getInfo(World world) {

		boolean canFire = lastOp + CHARGE_TIME < world.getTotalWorldTime();
		int cooldown = (int) ((lastOp + CHARGE_TIME) - world.getTotalWorldTime());

		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.DEATH_RAY).getTranslationKey() + ".name"),
				canFire ? new TextComponentTranslation("satellite.ready") : new TextComponentTranslation("satellite.cooldown", cooldown / 20 + "s")
		};
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_FIRE)) {
			deathBlast(world, targetX, targetZ, null);
			return;
		}

		if(cmd[0].equals(CMD_CANFIRE)) {
			this.tx = ("" + (lastOp + CHARGE_TIME < world.getTotalWorldTime())).toUpperCase(Locale.US);
		}
	}

	@Override
	public void onClick(World world, EntityPlayerMP player, int x, int z) {
		deathBlast(world, x, z, player);
	}

	public void deathBlast(World world, int x, int z, EntityPlayerMP detonator) {

		if(lastOp + CHARGE_TIME < world.getTotalWorldTime()) {
			lastOp = world.getTotalWorldTime();

			int y = world.getHeight(x, z);

			EntityDeathBlast blast = new EntityDeathBlast(world);
			blast.posX = x;
			blast.posY = y;
			blast.posZ = z;
			blast.detonator = detonator;
			world.spawnEntity(blast);
			this.markDirty();
		}
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.221F, 0.663F, 1.0F };
	}
}
