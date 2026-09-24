package com.hbm.api.ntl;

import com.hbm.lib.ForgeDirection;

public interface IPneumaticConnector {

    default boolean canConnectPneumatic(ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN;
    }
}
