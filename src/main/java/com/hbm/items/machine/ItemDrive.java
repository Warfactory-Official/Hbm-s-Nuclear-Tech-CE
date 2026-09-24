package com.hbm.items.machine;

public class ItemDrive {

	public enum EnumDriveType {
		FLASH_EMPTY,
		DISK_EMPTY,
		FLASH_BROKEN,
		DISK_BROKEN,

		FLASH_FLIGHTSIM,			// precalc for spaceflight
		FLASH_PARTICLESIM,			// precalc for fusion

		DISK_FLIGHTDATA,			// raw data from satellite
		DISK_FLIGHTDATA_PROCESSED,	// processed data from satellite
		DISK_ORBITDATA,				// raw sensor relay data
		DISK_ORBITDATA_PROCESSED,	// processed data from sensor relay

		KLAUS;						// kkklanker

		public static final EnumDriveType[] VALUES = values();
	}
}
