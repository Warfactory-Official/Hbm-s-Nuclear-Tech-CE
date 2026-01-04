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
		BAUXITE
	}

	public enum EnumMeteorType {
		IRON,
		COPPER,
		ALUMINIUM,
		RAREEARTH,
		COBALT
	}

	public enum EnumStalagmiteType {
		SULFUR,
		ASBESTOS
	}
	
	/** DECO / STRUCTURE ENUMS */
	//i apologize in advance
	
	public enum TileType {
		LARGE,
		SMALL
	}

	public enum LightstoneType {
		UNREFINED,
		TILE,
		BRICKS,
		BRICKS_CHISELED,
		CHISELED
	}
	
	public enum DecoComputerEnum {
		IBM_300PL
	}
	
	public enum DecoCabinetEnum {
		GREEN,
		STEEL
	}

    public enum DecoCRTEnum {
        CLEAN,
        BROKEN,
        BLINKING,
        BSOD
    }

    public enum DecoToasterEnum {
        IRON,
        STEEL,
        WOOD
    }

	public enum OreType {
		EMERALD ("emerald",OreEnum.EMERALD),
		DIAMOND ("diamond", OreEnum.DIAMOND),
		RADGEM ("radgem",OreEnum.RAD_GEM),
		//URANIUM_SCORCEHD ("uranium_scorched", null),
		URANIUM ("uranium", null),
		SCHRABIDIUM ("schrabidium", null);

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


