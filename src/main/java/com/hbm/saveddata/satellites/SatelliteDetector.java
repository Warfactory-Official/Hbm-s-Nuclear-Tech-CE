package com.hbm.saveddata.satellites;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SatelliteDetector extends Satellite {

	public List<RadiationBurst> cachedResults = new ArrayList<>();

	public static final String CMD_SURVEY = "survey";
	public static final String CMD_COUNT = "count";
	public static final String CMD_GETTYPE = "gettype";
	public static final String CMD_GETPOSITION = "getposition";

	public SatelliteDetector() { }

	@Override
	public String getType() {
		return "UWB_EMISSION_DETECTOR";
	}

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.DETECTOR).getTranslationKey() + ".name")
		};
	}

	@Override
	public void onCommandImpl(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_SURVEY)) {
			cachedResults.clear();

			for(RadiationBurst burst : bursts) {
				if(world.provider.getDimension() == burst.dimension) cachedResults.add(burst);
			}
			return;
		}

		if(cmd[0].equals(CMD_COUNT)) {
			this.tx = "" + cachedResults.size();
			return;
		}

		if(cmd[0].equals(CMD_GETTYPE) && cmd.length == 2) {
			RadiationBurst burst = getBurstFromIndex(cmd[1]);
			if(burst == null) { this.tx = ""; return; }
			this.tx = "" + burst.intensity.name();
			return;
		}

		if(cmd[0].equals(CMD_GETPOSITION) && cmd.length == 2) {
			RadiationBurst burst = getBurstFromIndex(cmd[1]);
			if(burst == null) { this.tx = ""; return; }
			this.tx = burst.x + ";" + burst.z;
		}
	}

	public RadiationBurst getBurstFromIndex(String cmd) {
		if(cachedResults.isEmpty()) return null;
		int index = IRORInteractive.parseInt(cmd, 1, cachedResults.size()) - 1;
		return cachedResults.get(index);
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.8F, 0.4F, 1.0F };
	}

	public static List<RadiationBurst> bursts = new ArrayList<>();

	public static final int DURATION_LOW = 15 * 20;
	public static final int DURATION_MEDIUM = 20 / 2;
	public static final int DURATION_HIGH = 60 * 20;

	public static final double INACCURACY_LOW = 10_000;
	public static final double INACCURACY_MEDIUM = 2_500;
	public static final double INARRCURACY_HIGH = 500;

	public static void reportEvent(World world, int lifetime, BurstIntensity intensity, double x, double z) {
		if(world == null || world.isRemote) return;
		bursts.add(new RadiationBurst(world, lifetime, intensity, (int) Math.floor(x), (int) Math.floor(z)));
	}

	public static void updateSystem(World world) {

		bursts.removeIf(b -> world.provider.getDimension() == b.dimension && world.getTotalWorldTime() > b.expiresOn);
	}

	public static class RadiationBurst {

		public int dimension;
		public long expiresOn;
		public BurstIntensity intensity;
		public int x;
		public int z;

		public RadiationBurst(World world, int lifetime, BurstIntensity intensity, int x, int z) {
			this.dimension = world.provider.getDimension();
			this.expiresOn = world.getTotalWorldTime() + lifetime;
			this.intensity = intensity;
			this.x = x;
			this.z = z;

			double inaccuracy =
					intensity == BurstIntensity.LOW ? INACCURACY_LOW :
					intensity == BurstIntensity.MEDIUM ? INACCURACY_MEDIUM :
						INARRCURACY_HIGH;

            this.x = (int) (this.x + MathHelper.clamp(world.rand.nextGaussian(), -1, 1) * inaccuracy);
            this.z = (int) (this.z + MathHelper.clamp(world.rand.nextGaussian(), -1, 1) * inaccuracy);
		}
	}

	public enum BurstIntensity {
		LOW,
		MEDIUM,
		HIGH
	}
}
