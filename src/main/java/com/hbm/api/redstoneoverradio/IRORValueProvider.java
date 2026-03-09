package com.hbm.api.redstoneoverradio;


public interface IRORValueProvider extends IRORInfo {

    /** Grabs the specified value from this ROR component, operations should not cause any changes with the component itself */
    String provideRORValue(String name);
}
