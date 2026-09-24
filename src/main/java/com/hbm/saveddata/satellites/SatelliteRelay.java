package com.hbm.saveddata.satellites;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import com.hbm.main.AdvancementManager;
import com.hbm.tileentity.network.RTTYSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class SatelliteRelay extends Satellite {

	public static final String CMD_RELAY = "relay";

	/*
	 * Originally, the relay had to be set up, with the id and channel being configured using commands.
	 * While this made it feel more technical and was in line with how the other satellites work,
	 * it did mean that for channel changes, an extra tick had to be wasted since the channel can't be
	 * changed and a signal sent in the same tick. So for convenience and reliability purposes, the
	 * relay only has a single command that does all the configuring and sending at once.
	 */

	public SatelliteRelay() {
		this.satIface = Interfaces.NONE;
	}

	@Override public String getType() { return "DIMENSIONAL_RELAY"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.RELAY).getTranslationKey() + ".name")
		};
	}

	@Override
	public void onOrbit(World world, double x, double y, double z) {

		for(EntityPlayer p : world.playerEntities)
			AdvancementManager.grantAchievement(p, AdvancementManager.achFOEQ);
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_RELAY) && cmd.length > 3) {

			int dim = IRORInteractive.parseInt(cmd[1], Integer.MIN_VALUE, Integer.MAX_VALUE);
			String chan = cmd[2];

			World targetWorld = DimensionManager.getWorld(dim);

			if(targetWorld != null) {

				StringBuilder signal = new StringBuilder();
				for(int i = 3; i < cmd.length; i++) {
					if(i > 3) signal.append(" ");
					signal.append(cmd[i]);
				}

				RTTYSystem.broadcast(targetWorld, chan, signal.toString());
			}
		}
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}
}
