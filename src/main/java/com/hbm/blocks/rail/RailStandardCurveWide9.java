package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;

import net.minecraft.world.World;

public class RailStandardCurveWide9 extends RailStandardCurveBase {

    private static final int[][] FOOTPRINT = new int[][] {
            {1, 0},
            {2, 0},
            {0, 1},
            {1, 1},
            {2, 1},
            {3, 1},
            {4, 1},
            {2, 2},
            {3, 2},
            {4, 2},
            {5, 2},
            {4, 3},
            {5, 3},
            {5, 4},
            {6, 3},
            {6, 4},
            {7, 4},
            {6, 5},
            {7, 5},
            {6, 6},
            {7, 6},
            {7, 7},
            {7, 8},
            {8, 6},
            {8, 7},
            {8, 8},
    };

    public RailStandardCurveWide9(String s) {
        super(s);
        this.width = 8;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        for(int[] array : FOOTPRINT) {
            if(!replaceable(world, x + dX * array[0] + rX * array[1], y, z + dZ * array[0] + rZ * array[1])) return false;
        }

        return true;
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

        int[][] dim = new int[][] {
                {1, 0, d},
                {2, 0, d},
                {0, 1, r},
                {1, 1, d},
                {2, 1, d},
                {3, 1, d},
                {4, 1, d},
                {2, 2, r},
                {3, 2, r},
                {4, 2, r},
                {5, 2, d},
                {4, 3, r},
                {5, 3, r},
                {5, 4, r},
                {6, 3, d},
                {6, 4, d},
                {7, 4, d},
                {6, 5, r},
                {7, 5, r},
                {6, 6, r},
                {7, 6, r},
                {7, 7, r},
                {7, 8, r},
                {8, 6, d},
                {8, 7, d},
                {8, 8, d},
        };

        for(int[] array : dim) {
            place(world, x + dX * array[0] + rX * array[1], y, z + dZ * array[0] + rZ * array[1], array[2]);
        }

        BlockDummyable.safeRem = false;
    }
}
