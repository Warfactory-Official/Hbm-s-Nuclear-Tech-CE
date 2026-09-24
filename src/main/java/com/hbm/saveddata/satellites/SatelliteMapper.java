package com.hbm.saveddata.satellites;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionData;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SatelliteMapper extends Satellite {

	public static final String CMD_TARGET_LOADED = "targetloaded";
	public static final String CMD_GETSMOG = "getsmog";
	public static final String CMD_SPOT_PLAYER = "spotplayers";

	public static final int SPOT_PLAYER_MAX_RANGE = 250;

	public SatelliteMapper() {
		this.ifaceAcs.add(InterfaceActions.HAS_MAP);
		this.satIface = Interfaces.SAT_PANEL;
	}

	@Override public String getType() { return "NOT_A_SPY_SATELLITE_:)"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.SPY).getTranslationKey() + ".name")
		};
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_TARGET_LOADED)) {
			this.tx = ("" + (world.getChunkProvider().getLoadedChunk(targetX >> 4, targetZ >> 4) != null)).toUpperCase(Locale.US);
			return;
		}

		if(cmd[0].equals(CMD_GETSMOG)) {

			PollutionData data = PollutionHandler.getPollutionData(world, new BlockPos(this.targetX, 255, this.targetZ));
			if(data != null) {
				float soot = data.pollution[PollutionType.SOOT.ordinal()];
				this.tx = "" + (int) Math.ceil(soot);
			}
			return;
		}

		if(cmd[0].equals(CMD_SPOT_PLAYER)) {

			List<String> names = new ArrayList<>();

			for(EntityPlayer player : world.playerEntities) {

				int x = (int) Math.floor(player.posX);
				int z = (int) Math.floor(player.posZ);

				double dX = x - targetX;
				double dZ = z - targetZ;

				if(dX * dX + dZ * dZ <= SPOT_PLAYER_MAX_RANGE * SPOT_PLAYER_MAX_RANGE) {
					int height = world.getHeight(x, z);
					if(height < player.posY + 2) names.add(player.getName());
				}
			}

			this.tx = names.isEmpty() ? "NONE" : String.join(";", names);
		}
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.538F, 1.0F, 0.523F };
	}
}
