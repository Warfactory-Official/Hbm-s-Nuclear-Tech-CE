package com.hbm.hazard;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.type.IHazardType;

import java.util.ArrayList;
import java.util.List;

public class HazardData {
	
	/*
	 * Purges all previously loaded data when read, useful for when specific items should fully override ore dict data.
	 */
	boolean doesOverride = false;
	/*
	 * MUTEX, even more precise to make only specific entries mutually exclusive, for example oredict aliases such as plutonium238 and pu238.
	 * Does the opposite of overrides, if a previous entry collides with this one, this one will yield.
	 * 
	 * RESERVED BITS (please keep this up to date)
	 * -1: oredict ("ingotX")
	 */
	int mutexBits = 0b0000_0000_0000_0000_0000_0000_0000_0000;

    //mlbv: made public so that modders can modify the hazards in the map after registration
	public List<HazardEntry> entries = new ArrayList<>();
	
	public HazardData addEntry(final IHazardType hazard) {
        return this.addEntry(hazard, 1D, false);
	}

    public HazardData addEntry(final IHazardType hazard, final double level) {
		if(hazard == HazardRegistry.CONTAMINATING && !RadiationConfig.enableContaminationOnGround) return this;
		return this.addEntry(hazard, level, false);
	}

    public HazardData addEntry(final IHazardType hazard, final double level, final boolean override) {
		this.entries.add(new HazardEntry(hazard, level));
		this.doesOverride = override;
		return this;
	}
	
	public HazardData addEntry(final HazardEntry entry) {
		this.entries.add(entry);
		return this;
	}
	
	public HazardData setMutex(final int mutex) {
		this.mutexBits = mutex;
		return this;
	}

	public HazardData setOverride(final boolean override) {
		this.doesOverride = override;
		return this;
	}

	public int getMutex() {
		return mutexBits;
	}
}
