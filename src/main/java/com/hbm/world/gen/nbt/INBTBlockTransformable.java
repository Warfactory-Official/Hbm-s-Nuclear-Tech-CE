package com.hbm.world.gen.nbt;

import net.minecraft.block.Block;

/**
 * Copy-pasted from 1.7 master rev 84601f685fea0a92d11f1a28e0b1e0a1742a27c2.
 * Consider changing coordBaseMode's type to EnumFacing if we are really going to port the whole NBT-based worldgen.
 *
 * @author George Paton
 */
public interface INBTBlockTransformable {

    /**
     * Defines this block as something that has a rotation or some other blockstate
     * which needs transformations applied when building from an .nbt structure file
     */

    // Takes the block current meta and translates it into a rotated meta
    int transformMeta(int meta, int coordBaseMode);

    // Takes the block and turns it into a different block entirely, to turn off lights, shit like that
    default Block transformBlock(Block block) {
        return block;
    }


    /**
     * A fair few blocks have generalized rotations so, since we have all this space, put em here
     */

    static int transformMetaDeco(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        switch(coordBaseMode) {
            case 1: //West
                switch (meta) {
                    case 2 -> {
                        return 5;
                    }
                    case 3 -> {
                        return 4;
                    }
                    case 4 -> {
                        return 2;
                    }
                    case 5 -> {
                        return 3;
                    }
                }
            case 2: //North
                switch (meta) {
                    case 2 -> {
                        return 3;
                    }
                    case 3 -> {
                        return 2;
                    }
                    case 4 -> {
                        return 5;
                    }
                    case 5 -> {
                        return 4;
                    }
                }
            case 3: //East
                switch (meta) {
                    case 2 -> {
                        return 4;
                    }
                    case 3 -> {
                        return 5;
                    }
                    case 4 -> {
                        return 3;
                    }
                    case 5 -> {
                        return 2;
                    }
                }
        }
        return meta;
    }

    static int transformMetaDecoModelHigh(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        int rot = (meta >> 2) & 3;
        int type = meta & 3;

        for(int i = 0; i < coordBaseMode; i++) {
            // CCW Rotation: S(0) -> E(3), E(3) -> N(1), N(1) -> W(2), W(2) -> S(0)
            if(rot == 0) rot = 3;
            else if(rot == 3) rot = 1;
            else if(rot == 1) rot = 2;
            else rot = 0;
        }

        return (rot << 2) | type;
    }

    static int transformMetaDecoModelLow(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        int rot = meta & 3;
        int type = meta & 12;

        rot = switch (coordBaseMode) {
            case 1 -> (rot + 1) % 4; // West
            case 2 -> (rot + 2) % 4; // North
            case 3 -> (rot + 3) % 4; // East
            default -> rot;
        };

        return type | rot;
    }

    static int transformMetaStairs(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        switch (coordBaseMode) {
            case 1 -> { //West
                if ((meta & 3) < 2) //Flip second bit for E/W
                    meta = meta ^ 2;
                else
                    meta = meta ^ 3; //Flip both bits for N/S
            }
            case 2 -> //North
                    meta = meta ^ 1; //Flip first bit
            case 3 -> { //East
                if ((meta & 3) < 2) //Flip both bits for E/W
                    meta = meta ^ 3;
                else //Flip second bit for N/S
                    meta = meta ^ 2;
            }
        }
        return meta;
    }

    // what in the FUCK mojangles
    // same as stairs but 1 & 3 flipped
    static int transformMetaTrapdoor(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        switch (coordBaseMode) {
            case 1 -> { //West
                if ((meta & 3) < 2)
                    meta = meta ^ 3;
                else
                    meta = meta ^ 2;
            }
            case 2 -> //North
                    meta = meta ^ 1; //Flip first bit
            case 3 -> { //East
                if ((meta & 3) < 2)
                    meta = meta ^ 2;
                else
                    meta = meta ^ 3;
            }
        }
        return meta;
    }

    static int transformMetaPillar(int meta, int coordBaseMode) {
        if(coordBaseMode == 0 || coordBaseMode == 2) return meta;
        int type = meta & 3;
        int rot = meta & 12;

        if(rot == 4) return type | 8;
        if(rot == 8) return type | 4;

        return meta;
    }

    static int transformMetaDirectional(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        int rot = meta & 3;
        int other = meta & 12;

        switch (coordBaseMode) {
            case 1 -> //W
                    rot = (rot + 1) % 4;
            case 2 -> //N
                    rot ^= 2;
            case 3 -> //E
                    rot = (rot + 3) % 4;
            default -> {
            } //S
        }

        return other | rot;
    }

    static int transformMetaTorch(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        switch(coordBaseMode) {
            case 1: //West
                switch (meta) {
                    case 1 -> {
                        return 3;
                    }
                    case 2 -> {
                        return 4;
                    }
                    case 3 -> {
                        return 2;
                    }
                    case 4 -> {
                        return 1;
                    }
                }
            case 2: //North
                switch (meta) {
                    case 1 -> {
                        return 2;
                    }
                    case 2 -> {
                        return 1;
                    }
                    case 3 -> {
                        return 4;
                    }
                    case 4 -> {
                        return 3;
                    }
                }
            case 3: //East
                switch (meta) {
                    case 1 -> {
                        return 4;
                    }
                    case 2 -> {
                        return 3;
                    }
                    case 3 -> {
                        return 1;
                    }
                    case 4 -> {
                        return 2;
                    }
                }
        }
        return meta;
    }

    static int transformMetaDoor(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        if(meta == 8 || meta == 9) return meta; // ignore top parts

        return transformMetaDirectional(meta, coordBaseMode);
    }

    static int transformMetaLever(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        if(meta <= 0 || meta >= 7) { //levers suck ass
            switch (coordBaseMode) {
                case 1, 3 -> //west / east
                        meta ^= 0b111;
            }
        } else if(meta >= 5) {
            switch (coordBaseMode) {
                case 1, 3 -> //west / east
                        meta = (meta + 1) % 2 + 5;
            }
        } else {
            meta = transformMetaTorch(meta, coordBaseMode);
        }

        return meta;
    }

    static int transformMetaVine(int meta, int coordBaseMode) { //Sloppppp coddee aa
        int result = 0;

        for (int i = 0; i < 4; i++) {
            int bit = 1 << i;
            if ((meta & bit) != 0) {
                result |= rotateVineBit(bit, coordBaseMode);
            }
        }

        return result;
    }

    static int rotateVineBit(int bit, int coordBaseMode) {
        int index = -1;

        switch (bit) {
            case 1 -> index = 0;
            // south
            case 2 -> index = 1;
            // west
            case 4 -> index = 2;
            // north
            case 8 -> index = 3;
            // east
            default -> {
                return 0;
            }
        }

        int rotated = switch (coordBaseMode) {
            case 1 -> (index + 1) % 4; // 90°
            case 2 -> (index + 2) % 4; // 180°
            case 3 -> (index + 3) % 4;
            default -> index; // 270°
            // case 0: vines work ughhggh (im dragging it)
        };

        return switch (rotated) {
            case 0 -> 1; // south
            case 1 -> 2; // west
            case 2 -> 4; // north
            case 3 -> 8;
            default -> // east
                    0;
        };

    }
}
