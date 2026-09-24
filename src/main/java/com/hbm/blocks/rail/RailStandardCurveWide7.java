package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;

import net.minecraft.world.World;

public class RailStandardCurveWide7 extends RailStandardCurveBase {

    public RailStandardCurveWide7(String s) {
        super(s);
        this.width = 6;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        return replaceable(world, x + dX, y, z + dZ) &&
                replaceable(world, x + rX, y, z + rZ) &&
                replaceable(world, x + dX + rX, y, z + dZ + rZ) &&
                replaceable(world, x + dX + rX * 2, y, z + dZ + rZ * 2) &&
                replaceable(world, x + dX * 2 + rX, y, z + dZ * 2 + rZ) &&
                replaceable(world, x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2) &&
                replaceable(world, x + dX * 3 + rX, y, z + dZ * 3 + rZ) &&
                replaceable(world, x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2) &&
                replaceable(world, x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3) &&
                replaceable(world, x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3) &&
                replaceable(world, x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3) &&
                replaceable(world, x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4) &&
                replaceable(world, x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4);
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {

        BlockDummyable.safeRem = true;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        int d = dir.ordinal();
        int r = rot.ordinal();

        place(world, x + dX, y, z + dZ, d);
        place(world, x + dX * 2, y, z + dZ * 2, d);
        place(world, x + rX, y, z + rZ, r);
        place(world, x + dX + rX, y, z + dZ + rZ, r);
        place(world, x + dX * 2 + rX, y, z + dZ * 2 + rZ, r);
        place(world, x + dX * 3 + rX, y, z + dZ * 3 + rZ, d);
        place(world, x + dX * 4 + rX, y, z + dZ * 4 + rZ, d);
        place(world, x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2, r);
        place(world, x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2, d);
        place(world, x + dX * 4 + rX * 2, y, z + dZ * 4 + rZ * 2, d);
        place(world, x + dX * 5 + rX * 2, y, z + dZ * 5 + rZ * 2, d);
        place(world, x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3, r);
        place(world, x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3, d);
        place(world, x + dX * 5 + rX * 3, y, z + dZ * 5 + rZ * 3, d);
        place(world, x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4, r);
        place(world, x + dX * 5 + rX * 4, y, z + dZ * 5 + rZ * 4, d);
        place(world, x + dX * 6 + rX * 4, y, z + dZ * 6 + rZ * 4, d);
        place(world, x + dX * 5 + rX * 5, y, z + dZ * 5 + rZ * 5, r);
        place(world, x + dX * 5 + rX * 6, y, z + dZ * 5 + rZ * 6, r);
        place(world, x + dX * 6 + rX * 5, y, z + dZ * 6 + rZ * 5, r);
        place(world, x + dX * 6 + rX * 6, y, z + dZ * 6 + rZ * 6, r);

        BlockDummyable.safeRem = false;
    }
}
