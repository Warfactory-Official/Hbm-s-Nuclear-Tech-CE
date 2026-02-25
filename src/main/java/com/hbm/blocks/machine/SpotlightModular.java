package com.hbm.blocks.machine;

import com.hbm.blocks.BlockEnums;
import net.minecraft.block.material.Material;

public class SpotlightModular extends Spotlight {

    public SpotlightModular(String name, Material mat, int beamLength, BlockEnums.LightType type, boolean isOn) {
        super(name, mat, beamLength, type, isOn);
    }

    @Override
    public String getPartName(int connectionCount) {
        if (connectionCount == 0) return "FluoroSingle";
        if (connectionCount == 1) return "FluoroCap";
        return "FluoroMid";
    }
}
