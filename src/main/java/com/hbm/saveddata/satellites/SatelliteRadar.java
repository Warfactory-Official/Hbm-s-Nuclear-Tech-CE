package com.hbm.saveddata.satellites;

import com.hbm.api.entity.IRadarDetectableNT.RadarScanParams;
import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Basically the AUTOCAL is now mandatory lol lmao
 */
public class SatelliteRadar extends Satellite {

	public static final int MAX_SCAN_RANGE = 1_000;
	public static RadarScanParams scanParams = new RadarScanParams(true, true, true, false);

	public static final String CMD_SURVEY = "survey";
	public static final String CMD_FILTER = "filter";
	public static final String CMD_COUNT = "count";
	public static final String CMD_GETTARGETID = "gettargetid";
	public static final String CMD_GETPOSITION = "getposition";
	public static final String CMD_GETNAME = "getname";

	public List<Entity> cachedRadarResults = new ArrayList<>();
	public List<Entity> filteredRadarResults = new ArrayList<>();

	public SatelliteRadar() {
		this.ifaceAcs.add(InterfaceActions.HAS_MAP);
		this.ifaceAcs.add(InterfaceActions.HAS_RADAR);
		this.satIface = Interfaces.SAT_PANEL;
	}

	@Override public String getType() { return "LEO_RADAR"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.RADAR).getTranslationKey() + ".name")
		};
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_SURVEY)) {

			cachedRadarResults.clear();

			for(Entity entity : TileEntityMachineRadarNT.matchingEntities) {
				if(entity.dimension != world.provider.getDimension()) continue;

				int x = (int) Math.floor(entity.posX);
				int z = (int) Math.floor(entity.posZ);

				double dX = x - targetX;
				double dZ = z - targetZ;

				if(dX * dX + dZ * dZ <= (double) MAX_SCAN_RANGE * MAX_SCAN_RANGE) {
					cachedRadarResults.add(entity);
				}
			}

			filteredRadarResults = new ArrayList<>(cachedRadarResults);
			return;
		}

		if(cmd[0].equals(CMD_FILTER) && cmd.length == 2) {

			filteredRadarResults.clear();
			String filter = cmd[1].toLowerCase(Locale.US);

			for(Entity entity : cachedRadarResults) {
				if(entity.isDead) continue;
				String classname = entity.getClass().getSimpleName().toLowerCase(Locale.US);
				if(classname.contains(filter)) {
					filteredRadarResults.add(entity);
				}
			}
			return;
		}

		if(cmd[0].equals(CMD_COUNT)) {
			this.tx = "" + filteredRadarResults.size();
			return;
		}

		if(cmd[0].equals(CMD_GETTARGETID) && cmd.length == 2) {
			Entity target = getTargetFromIndex(cmd[1]);
			this.tx = target == null ? "" : "" + target.getEntityId();
			return;
		}

		if(cmd[0].equals(CMD_GETPOSITION) && cmd.length == 2) {
			Entity target = getTargetFromIndex(cmd[1]);
			this.tx = target == null ? "" : (int) Math.floor(target.posX) + ";" + (int) Math.floor(target.posY) + ";" + (int) Math.floor(target.posZ);
			return;
		}

		if(cmd[0].equals(CMD_GETNAME) && cmd.length == 2) {
			Entity target = getTargetFromIndex(cmd[1]);
			this.tx = target == null ? "" : target.getClass().getSimpleName().toLowerCase(Locale.US);
		}
	}

	public Entity getTargetFromIndex(String cmd) {
		if(filteredRadarResults.isEmpty()) return null;
		int index = IRORInteractive.parseInt(cmd, 1, filteredRadarResults.size()) - 1;
		Entity target = filteredRadarResults.get(index);
		if(target.isDead) return null;
		return target;
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.134F, 1.0F, 0.134F };
	}
}
