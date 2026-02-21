package com.hbm.interfaces;

import com.hbm.config.MachineConfig;
import net.minecraft.tileentity.TileEntity;

public interface IDoor {

    void open();

    void close();

    DoorState getState();

    void toggle();

    default boolean setTexture(String tex) {
        return false;
    }

    default void setTextureState(byte tex) {
    }

    boolean getRedstoneOnly();

    default Mode getConfiguredMode() {
        String name = ((TileEntity) this).getBlockType().getRegistryName().toString();
        if (MachineConfig.doorConf.containsKey(name)) {
            return MachineConfig.doorConf.get(name);
        }
        return MachineConfig.doorConf.getOrDefault("ALL", Mode.DEFAULT);
    }

    default boolean isRedstoneOnly() {
        Mode mode = getConfiguredMode();
        if (mode == Mode.REDSTONE) return true;
        if (mode == Mode.DEFAULT) return false;
        return getRedstoneOnly();
    }

    void setRedstoneOnly(boolean redstoneOnly);

    default void toggleRedstoneMode() {
        this.setRedstoneOnly(!this.getRedstoneOnly());
        ((TileEntity) this).markDirty();
    }

    enum DoorState {
        CLOSED, OPEN, CLOSING, OPENING;

        public boolean isStationaryState() {
            return (this == CLOSED || this == OPEN);
        }

        public boolean isMovingState() {
            return (this == CLOSING || this == OPENING);
        }
    }

    enum Mode {
        DEFAULT,
        TOOLABLE,
        REDSTONE
    }
}
