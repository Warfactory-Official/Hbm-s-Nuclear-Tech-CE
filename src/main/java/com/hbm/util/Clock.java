package com.hbm.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Clock {
    private static long time_ms;

    public static void update() {
        time_ms = System.currentTimeMillis();
    }

    public static long get_ms() {
        return time_ms;
    }
}
