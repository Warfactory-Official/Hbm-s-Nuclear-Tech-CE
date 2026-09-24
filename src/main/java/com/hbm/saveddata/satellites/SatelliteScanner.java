package com.hbm.saveddata.satellites;

import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SatelliteScanner extends Satellite {

	public SatelliteScanner() {
		this.ifaceAcs.add(InterfaceActions.HAS_ORES);
		this.satIface = Interfaces.SAT_PANEL;
	}

	@Override public String getType() { return "DEPTH_SCANNER"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.SCANNER).getTranslationKey() + ".name")
		};
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.544F, 0.680F, 1.0F };
	}
}
