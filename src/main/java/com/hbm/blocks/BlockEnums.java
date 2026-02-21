package com.hbm.blocks;

import com.hbm.items.ModItems;
import net.minecraft.item.Item;

import javax.annotation.Nullable;

import static com.hbm.blocks.OreEnumUtil.OreEnum;

public class BlockEnums {

	public enum EnumStoneType {
		SULFUR,
		ASBESTOS,
		HEMATITE,
		MALACHITE,
		LIMESTONE,
		BAUXITE;

		public static final EnumStoneType[] VALUES = values();
	}

	public enum EnumMeteorType {
		IRON,
		COPPER,
		ALUMINIUM,
		RAREEARTH,
		COBALT;

		public static final EnumMeteorType[] VALUES = values();
	}

	public enum EnumStalagmiteType {
		SULFUR,
		ASBESTOS;

		public static final EnumStalagmiteType[] VALUES = values();
	}

	/** DECO / STRUCTURE ENUMS */
	//i apologize in advance

	public enum TileType {
		LARGE,
		SMALL;

		public static final TileType[] VALUES = values();
	}

	public enum LightstoneType {
		UNREFINED,
		TILE,
		BRICKS,
		BRICKS_CHISELED,
		CHISELED;

		public static final LightstoneType[] VALUES = values();
	}

	public enum DecoComputerEnum {
		IBM_300PL;

		public static final DecoComputerEnum[] VALUES = values();
	}

	public enum DecoCabinetEnum {
		GREEN,
		STEEL;

		public static final DecoCabinetEnum[] VALUES = values();
	}

    public enum DecoCRTEnum {
        CLEAN,
        BROKEN,
        BLINKING,
        BSOD;

		public static final DecoCRTEnum[] VALUES = values();
    }

    public enum DecoToasterEnum {
        IRON,
        STEEL,
        WOOD;

		public static final DecoToasterEnum[] VALUES = values();
    }

	public enum OreType {
		EMERALD ("emerald",OreEnum.EMERALD),
		DIAMOND ("diamond", OreEnum.DIAMOND),
		RADGEM ("radgem",OreEnum.RAD_GEM),
		//URANIUM_SCORCEHD ("uranium_scorched", null),
		URANIUM ("uranium", null),
		SCHRABIDIUM ("schrabidium", null);

		public static final OreType[] VALUES = values();

		public final String overlayTexture;
		public final OreEnum oreEnum;

		public String getName(){
			return overlayTexture;
		}

		OreType(String overlayTexture, @Nullable OreEnum oreEnum) {
			this.overlayTexture = overlayTexture;
			this.oreEnum = oreEnum;

		}
	}


	public enum EnumBasaltOreType {
		SULFUR,
		FLUORITE,
		ASBESTOS,
		GEM,
		MOLYSITE;

		public static final EnumBasaltOreType[] VALUES = values();

		public Item getDrop() {
			return switch (this) {
                 case SULFUR -> ModItems.sulfur;
                 case FLUORITE -> ModItems.fluorite;
                 case ASBESTOS -> ModItems.ingot_asbestos;
                 case GEM -> ModItems.gem_volcanic;
                 case MOLYSITE -> ModItems.powder_molysite;
			};
		}

		public int getDropCount(int rand){
			return rand + 1;
		}
    }

	public enum EnumBlockCapType {
		NUKA,
		QUANTUM,
		RAD,
		SPARKLE,
		KORL,
		FRITZ,
		SUNSET,
		STAR;

		public static final EnumBlockCapType[] VALUES = values();

		public Item getDrop() {
			return switch (this) {
                 case NUKA -> ModItems.cap_nuka;
                 case QUANTUM -> ModItems.cap_quantum;
                 case RAD -> ModItems.cap_rad;
                 case SPARKLE -> ModItems.cap_sparkle;
                 case KORL -> ModItems.cap_korl;
                 case FRITZ -> ModItems.cap_fritz;
                 case SUNSET -> ModItems.cap_sunset;
                 case STAR -> ModItems.cap_star;
			};
		}

		public int getDropCount(){
			return 128;
		}
	}
}
