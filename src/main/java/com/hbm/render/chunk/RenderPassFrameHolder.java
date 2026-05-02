package com.hbm.render.chunk;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderPassFrameHolder {

    public static int currentTerrainFrame = Integer.MIN_VALUE;
    public static int currentShadowTerrainFrame = Integer.MIN_VALUE;

    private RenderPassFrameHolder() {
    }
}
