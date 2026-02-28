package com.hbm.util;

import net.minecraft.util.EnumFacing;

public class FacingUtil {
    public static float getPitch(EnumFacing facing) {
        if (facing == EnumFacing.UP) return (float)Math.PI * 0.5F;
        if (facing == EnumFacing.DOWN) return (float)Math.PI * -0.5F;
        return 0;
    }

    public static float getYaw(EnumFacing facing) {
        if (facing == EnumFacing.NORTH) return (float)Math.PI * 0.5f;;
        if (facing == EnumFacing.SOUTH) return (float)Math.PI * -0.5f;
        if (facing == EnumFacing.WEST) return (float)Math.PI;
        return 0;
    }
}
