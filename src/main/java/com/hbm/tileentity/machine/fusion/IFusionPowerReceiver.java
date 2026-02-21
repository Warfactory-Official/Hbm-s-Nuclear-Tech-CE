package com.hbm.tileentity.machine.fusion;

public interface IFusionPowerReceiver {

    /**
     * Returns true if this receives output fusion power which it shares with all other connected devices,
     * or false if it only uses neutron power which is not shared.
     */
    boolean receivesFusionPower();

    /**
     * fusionPower is the per-port output adjusted for the amount of connected receivers (i.e. when boilers share output energy)
     * neutronPower is a fixed value provided by the recipe
     */
    void receiveFusionPower(long fusionPower, double neutronPower);
}
