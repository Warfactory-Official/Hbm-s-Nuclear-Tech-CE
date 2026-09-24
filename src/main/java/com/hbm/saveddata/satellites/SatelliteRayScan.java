package com.hbm.saveddata.satellites;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

public class SatelliteRayScan extends Satellite {

	public List<RayEvent> cachedResults = new ArrayList<>();

	public static final int MAX_SCAN_RANGE = 250;

	public static final String CMD_SURVEY = "survey";
	public static final String CMD_COUNT = "count";
	public static final String CMD_GETINFO = "getinfo";
	public static final String CMD_GETPOSITION = "getposition";

	public SatelliteRayScan() {
		this.ifaceAcs.add(InterfaceActions.HAS_MAP);
		this.satIface = Interfaces.SAT_PANEL;
	}

	@Override
	public String getType() {
		return "NB_RAY_SCANNER";
	}

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.RAY_SCAN).getTranslationKey() + ".name")
		};
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_SURVEY)) {
			this.cachedResults.clear();

			for(Entry<DimPos, RayEvent> entry : rayEvent.entrySet()) {
				DimPos pos = entry.getKey();
				if(pos.dim != world.provider.getDimension()) continue;
				int dX = pos.x - this.targetX;
				int dZ = pos.z - this.targetZ;

				if(dX * dX + dZ * dZ <= MAX_SCAN_RANGE * MAX_SCAN_RANGE) {
					this.cachedResults.add(entry.getValue());
				}
			}
			return;
		}

		if(cmd[0].equals(CMD_COUNT)) {
			this.tx = "" + cachedResults.size();
			return;
		}

		if(cmd[0].equals(CMD_GETINFO) && cmd.length == 2) {
			RayEvent event = getEventFromIndex(cmd[1]);
			if(event == null) { this.tx = ""; return; }
			this.tx = "" + event.info;
			return;
		}

		if(cmd[0].equals(CMD_GETPOSITION) && cmd.length == 2) {
			RayEvent event = getEventFromIndex(cmd[1]);
			if(event == null) { this.tx = ""; return; }
			this.tx = event.x + ";" + event.z;
		}
	}

	public RayEvent getEventFromIndex(String cmd) {
		if(cachedResults.isEmpty()) return null;
		int index = IRORInteractive.parseInt(cmd, 1, cachedResults.size()) - 1;
		return cachedResults.get(index);
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.4F, 1.0F, 0.8F };
	}

	public static LinkedHashMap<DimPos, RayEvent> rayEvent = new LinkedHashMap<>();

	public static void reportEvent(World world, int x, int y, int z, String info, int lifetime) {
		if(world == null || world.isRemote) return;
		rayEvent.put(new DimPos(x, y, z, world.provider.getDimension()), new RayEvent(world, lifetime, x, z, info));
	}

	public static void updateSystem(World world) {

		rayEvent.entrySet().removeIf(entry -> world.provider.getDimension() == entry.getKey().dim && world.getTotalWorldTime() > entry.getValue().expiresOn);
	}

	public static class DimPos {

		public final int x;
		public final int y;
		public final int z;
		public final int dim;

		public DimPos(int x, int y, int z, int dim) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.dim = dim;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof DimPos)) return false;
			DimPos other = (DimPos) o;
			return x == other.x && y == other.y && z == other.z && dim == other.dim;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y, z, dim);
		}
	}

	public static class RayEvent {

		public static final String INFO_ARC_FLASH = "ARC_FLASH";
		public static final String INFO_NUCLEAR = "NEUTRON_EMISSION";
		public static final String INFO_PARTICLE = "HIGH_ENERGY_PARTICLES";
		public static final String INFO_RADAR = "RADAR_WAVES";
		public static final String INFO_RADIO = "RADIO_WAVES";

		public long expiresOn;
		public String info;
		public int x;
		public int z;

		public RayEvent(World world, int lifetime, int x, int z, String info) {
			this.expiresOn = world.getTotalWorldTime() + lifetime;
			this.x = x;
			this.z = z;
			this.info = info;
		}
	}
}
