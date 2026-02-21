package com.hbm.blocks;

public class PlantEnums {

    public enum EnumDeadPlantType {
        GENERIC,
        GRASS,
        FLOWER,
        BIG_FLOWER,
        FERN;

        public static final EnumDeadPlantType[] VALUES = values();
    }

    public enum EnumFlowerPlantType {
        FOXGLOVE,
        HEMP,
        MUSTARD_WILLOW_0(true),
        MUSTARD_WILLOW_1(true),
        NIGHTSHADE,
        TOBACCO,
        ;

        public static final EnumFlowerPlantType[] VALUES = values();

        public final boolean needsOil;
        EnumFlowerPlantType(boolean needsOil){ this.needsOil = needsOil;}
        EnumFlowerPlantType(){ this.needsOil = false;}
    }

    public enum EnumTallPlantType {
        HEMP_LOWER(false),
        HEMP_UPPER(false),
        MUSTARD_WILLOW_2_LOWER,
        MUSTARD_WILLOW_2_UPPER,
        MUSTARD_WILLOW_3_LOWER,
        MUSTARD_WILLOW_3_UPPER,
        MUSTARD_WILLOW_4_LOWER,
        MUSTARD_WILLOW_4_UPPER,
        ;
        public static final EnumTallPlantType[] VALUES = values();
        public final boolean needsOil;
        EnumTallPlantType(boolean needsOil){ this.needsOil = needsOil;}
        EnumTallPlantType(){ this.needsOil = true;}
    }

}
