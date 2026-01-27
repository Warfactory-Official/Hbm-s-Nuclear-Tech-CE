package com.hbm.lib;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

public enum ForgeDirection {

	//Drillgon200: Screw it, copy paste ForgeDirection from 1.7.10 because EnumFacing doesn't have UNKNOWN
	
	/** -Y */
    DOWN(0, -1, 0),

    /** +Y */
    UP(0, 1, 0),

    /** -Z */
    NORTH(0, 0, -1),

    /** +Z */
    SOUTH(0, 0, 1),

    /** -X */
    WEST(-1, 0, 0),

    /** +X */
    EAST(1, 0, 0),

    /**
     * Used only by getOrientation, for invalid inputs
     */
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;
    public static final ForgeDirection[] VALUES = values();
    public static final ForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    // Left hand rule rotation matrix for all possible axes of rotation
    public static final int[][] ROTATION_MATRIX = {
        {0, 1, 4, 5, 3, 2, 6},
        {0, 1, 5, 4, 2, 3, 6},
    	{5, 4, 2, 3, 0, 1, 6},
    	{4, 5, 2, 3, 1, 0, 6},
    	{2, 3, 1, 0, 4, 5, 6},
    	{3, 2, 0, 1, 4, 5, 6},
    	{0, 1, 2, 3, 4, 5, 6},
    };

    ForgeDirection(int x, int y, int z)
    {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        flag = 1 << ordinal();
    }

    public static ForgeDirection getOrientation(EnumFacing e){
        return VALID_DIRECTIONS[e.ordinal()];
    }

    public static ForgeDirection getOrientation(int id)
    {
        if (id >= 0 && id < VALID_DIRECTIONS.length)
        {
            return VALID_DIRECTIONS[id];
        }
        return UNKNOWN;
    }

    public ForgeDirection getOpposite()
    {
        return getOrientation(ordinal() ^ 1);
    }

    public ForgeDirection getRotation(ForgeDirection axis)
    {
    	return getOrientation(ROTATION_MATRIX[axis.ordinal()][ordinal()]);
    }
    
    public Rotation getBlockRotation(){
        return switch (this) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public EnumFacing toEnumFacing() {
        return this == UNKNOWN ? null : EnumFacing.VALUES[ordinal()];
    }
    
    public float getRotationDegrees(){
        return switch (this.ordinal()) {
            case 4 -> 90;
            case 3 -> 180;
            case 5 -> 270;
            default -> 0;
        };
    }
    
    public float getRotationRadians(){
    	return (float) Math.toRadians(getRotationDegrees());
    }
}
