package com.hbm.items;

public class ItemEnums {

  public enum EnumCokeType {
    COAL,
    LIGNITE,
    PETROLEUM;

    public static final EnumCokeType[] VALUES = values();
  }

  public enum EnumTarType {
    CRUDE,
    CRACK,
    COAL,
    WOOD,
    WAX,
    PARAFFIN;

    public static final EnumTarType[] VALUES = values();
  }

  public enum EnumBriquetteType {
    COAL,
    LIGNITE,
    WOOD;

    public static final EnumBriquetteType[] VALUES = values();
  }

  public enum SoyuzSkinType {
    NORMAL,
    LUNAR,
    POST_WAR;

    public static final SoyuzSkinType[] VALUES = values();
  }

  public enum EnumLegendaryType {
    TIER1,
    TIER2,
    TIER3;

    public static final EnumLegendaryType[] VALUES = values();
  }

  public enum EnumAshType {
    WOOD,
    COAL,
    MISC,
    FLY,
    SOOT,
    FULLERENE;

    public static final EnumAshType[] VALUES = values();
  }

  public enum EnumPlantType {
    TOBACCO,
    ROPE,
    MUSTARDWILLOW;

    public static final EnumPlantType[] VALUES = values();
  }

  public enum EnumChunkType {
    RARE,
    MALACHITE,
    CRYOLITE;

    public static final EnumChunkType[] VALUES = values();
  }

  public enum EnumAchievementType {
    GOFISH,
    ACID,
    BALLS,
    DIGAMMASEE,
    DIGAMMAFEEL,
    DIGAMMAKNOW,
    DIGAMMAKAUAIMOHO,
    DIGAMMAUPONTOP,
    DIGAMMAFOROURRIGHT,
    QUESTIONMARK;

    public static final EnumAchievementType[] VALUES = values();
  }

  public enum EnumFuelAdditive {
    ANTIKNOCK,
    DEICER;

    public static final EnumFuelAdditive[] VALUES = values();
  }

  public enum EnumPages {
    PAGE1,
    PAGE2,
    PAGE3,
    PAGE4,
    PAGE5,
    PAGE6,
    PAGE7,
    PAGE8;

    public static final EnumPages[] VALUES = values();
  }

  public static enum EnumSecretType {
    CANISTER, CONTROLLER, SELENIUM_STEEL, ABERRATOR, FOLLY;

    public static final EnumSecretType[] VALUES = values();
  }

  public enum EnumCasingType {
    SMALL, LARGE, SMALL_STEEL, LARGE_STEEL, SHOTSHELL, BUCKSHOT, BUCKSHOT_ADVANCED;

    public static final EnumCasingType[] VALUES = values();
  }

  public enum EnumCircuitType {
    VACUUM_TUBE,
    CAPACITOR,
    CAPACITOR_TANTALIUM,
    PCB,
    SILICON,
    CHIP,
    CHIP_BISMOID,
    ANALOG,
    BASIC,
    ADVANCED,
    CAPACITOR_BOARD,
    BISMOID,
    CONTROLLER_CHASSIS,
    CONTROLLER,
    CONTROLLER_ADVANCED,
    QUANTUM,
    CHIP_QUANTUM,
    CONTROLLER_QUANTUM,
    ATOMIC_CLOCK,
    NUMITRON;

    public static final EnumCircuitType[] VALUES = values();
  }

  public enum EnumDepletedRTGMaterial {
    BISMUTH,
    MERCURY,
    NEPTUNIUM,
    LEAD,
    ZIRCONIUM,
    NICKEL;

    public static final EnumDepletedRTGMaterial[] VALUES = values();
  }

  public enum EnumPartType {
    PISTON_PNEUMATIC("piston_pneumatic"),
    PISTON_HYDRAULIC("piston_hydraulic"),
    PISTON_ELECTRIC("piston_electric"),
    LDE("low_density_element"),
    HDE("heavy_duty_element"),
    GLASS_POLARIZED("glass_polarized");

    public static final EnumPartType[] VALUES = values();

    final String texName;

    EnumPartType(String texName) {
      this.texName = texName;
    }
  }

  public enum ScrapType {
    //GENERAL BOARD
    BOARD_BLANK,
    BOARD_TRANSISTOR,
    BOARD_CONVERTER,

    //CHIPSET
    BRIDGE_NORTH,
    BRIDGE_SOUTH,
    BRIDGE_IO,
    BRIDGE_BUS,
    BRIDGE_CHIPSET,
    BRIDGE_CMOS,
    BRIDGE_BIOS,

    //CPU
    CPU_REGISTER,
    CPU_CLOCK,
    CPU_LOGIC,
    CPU_CACHE,
    CPU_EXT,
    CPU_SOCKET,

    //RAM
    MEM_SOCKET,
    MEM_16K_A,
    MEM_16K_B,
    MEM_16K_C,
    MEM_16K_D,

    //EXTENSION CARD
    CARD_BOARD,
    CARD_PROCESSOR;

    public static final ScrapType[] VALUES = values();
  }

  public enum CircuitComponentType {
    CHIPSET,
    CPU,
    RAM,
    CARD;

    public static final CircuitComponentType[] VALUES = values();
  }
  public enum EnumExpensiveType {
    STEEL_PLATING, HEAVY_FRAME, CIRCUIT, LEAD_PLATING, FERRO_PLATING, COMPUTER, BRONZE_TUBES, PLASTIC;

    public static final EnumExpensiveType[] VALUES = values();
  }

  public enum EnumDrillType {
    STEEL			(1.0D, 1, 0, false, false),
    STEEL_DIAMOND	(1.0D, 1, 2, false, true),
    HSS				(1.2D, 2, 0, true, false),
    HSS_DIAMOND		(1.2D, 2, 3, true, true),
    DESH			(1.5D, 3, 1, true, true),
    DESH_DIAMOND	(1.5D, 3, 4, true, true),
    TCALLOY			(2.0D, 4, 1, true, true),
    TCALLOY_DIAMOND	(2.0D, 4, 4, true, true),
    FERRO			(2.5D, 5, 1, true, true),
    FERRO_DIAMOND	(2.5D, 5, 4, true, true);

    public static final EnumDrillType[] VALUES = values();

    public double speed;
    public int tier;
    public int fortune;
    public boolean vein;
    public boolean silk;

    private EnumDrillType(double speed, int tier, int fortune, boolean vein, boolean silk) {
      this.speed = speed;
      this.tier = tier;
      this.fortune = fortune;
      this.vein = vein;
      this.silk = silk;
    }
  }
}

