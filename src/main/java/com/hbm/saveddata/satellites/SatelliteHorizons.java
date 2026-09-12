package com.hbm.saveddata.satellites;

import com.hbm.entity.projectile.EntityTom;
import com.hbm.items.ModItems;
import com.hbm.main.AdvancementManager;
import com.hbm.world.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Locale;

public class SatelliteHorizons extends Satellite {

	public static final String CMD_FIRE = "fire";
	public static final String CMD_CANFIRE = "canfire";

	boolean used = false;
	public long lastOp;

	public SatelliteHorizons() {
		this.satIface = Interfaces.SAT_COORD;
	}

	@Override public String getType() { return "PAYLOAD_UNKNOWN"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ModItems.sat_gerald.getTranslationKey() + ".name"),
				used ? new TextComponentTranslation("satellite.spent") : new TextComponentTranslation("satellite.ready")
		};
	}

	@Override
	public void onOrbit(World world, double x, double y, double z) {

		for(EntityPlayer p : world.playerEntities)
			AdvancementManager.grantAchievement(p, AdvancementManager.horizonsStart);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("used", used);
		nbt.setLong("lastOp", lastOp);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		used = nbt.getBoolean("used");
		lastOp = nbt.getLong("lastOp");
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_FIRE)) {
			theHorizons(world, targetX, targetZ);
			return;
		}

		if(cmd[0].equals(CMD_CANFIRE)) {
			this.tx = ("" + !used).toUpperCase(Locale.US);
		}
	}

	@Override
	public void onCoordAction(World world, EntityPlayerMP player, int x, int y, int z) {
		this.setTarget(x, z);
		this.theHorizons(world, x, z);
	}

	public void theHorizons(World world, int x, int z) {
		if(used) return;

		used = true;
		SatelliteSavedData.getData(world).markDirty();

		EntityTom tom = new EntityTom(world);
		tom.setPosition(x + 0.5, 600, z + 0.5);

		WorldUtil.loadAndSpawnEntityInWorld(tom);

		for(EntityPlayer p : world.playerEntities)
			AdvancementManager.grantAchievement(p, AdvancementManager.horizonsEnd);

		//not necessary but JUST to make sure
		if(!world.isRemote) {
			FMLCommonHandler.instance().getMinecraftServerInstance().sendMessage(new TextComponentTranslation("chat.gerald.detonated"));
		}
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}
}
