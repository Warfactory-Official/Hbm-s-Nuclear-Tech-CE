package com.hbm.core;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;
import java.util.Collections;

public class HbmCoreModContainer extends DummyModContainer {

	public HbmCoreModContainer() {
		super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "hbmcore";
        meta.name = "NTMCore";
        meta.description = "Hbm core mod";
        //versioning scheme = MAJOR.MINOR.PATCH
        meta.version = "1.12.2-3.2.0";
        meta.authorList = Arrays.asList("Movblock","Leafia (CBR system)");
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
}
