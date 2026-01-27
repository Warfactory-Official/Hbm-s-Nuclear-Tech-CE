package com.hbm.items.weapon;

import com.hbm.items.ItemAmmoEnums;
import com.hbm.items.ItemAmmoEnums.IAmmoItemEnum;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class ItemAmmo<E extends Enum<E> & IAmmoItemEnum> extends ItemEnumMulti<E> {

	public enum AmmoItemTrait {
		CON_ACCURACY2,
		CON_DAMAGE,
		CON_HEAVY_WEAR,
		CON_LING_FIRE,
		CON_NN,
		CON_NO_DAMAGE,
		CON_NO_EXPLODE1,
		CON_NO_EXPLODE2,
		CON_NO_EXPLODE3,
		CON_NO_FIRE,
		CON_NO_MIRV,
		CON_NO_PROJECTILE,
		CON_PENETRATION,
		CON_RADIUS,
		CON_RANGE2,
		CON_SING_PROJECTILE,
		CON_SPEED,
		CON_SUPER_WEAR,
		CON_WEAR,
		NEU_40MM,
		NEU_BLANK,
		NEU_BOAT,
		NEU_BOXCAR,
		NEU_BUILDING,
		NEU_CHLOROPHYTE,
		NEU_ERASER,
		NEU_FUN,
		NEU_HEAVY_METAL,
		NEU_HOMING,
		NEU_JOLT,
		NEU_LESS_BOUNCY,
		NEU_MASKMAN_FLECHETTE,
		NEU_MASKMAN_METEORITE,
		NEU_MORE_BOUNCY,
		NEU_NO_BOUNCE,
		NEU_NO_CON,
		NEU_STARMETAL,
		NEU_TRACER,
		NEU_UHH,
		NEU_LEADBURSTER,
		NEU_WARCRIME1,
		NEU_WARCRIME2,
		PRO_ACCURATE1,
		PRO_ACCURATE2,
		PRO_BALEFIRE,
		PRO_BOMB_COUNT,
		PRO_CAUSTIC,
		PRO_CHAINSAW,
		PRO_CHLORINE,
		PRO_DAMAGE,
		PRO_DAMAGE_SLIGHT,
		PRO_EMP,
		PRO_EXPLOSIVE,
		PRO_FALLOUT,
		PRO_FIT_357,
		PRO_FLAMES,
		PRO_GRAVITY,
		PRO_HEAVY_DAMAGE,
		PRO_INCENDIARY,
		PRO_LUNATIC,
		PRO_MARAUDER,
		PRO_MINING,
		PRO_NO_GRAVITY,
		PRO_NUCLEAR,
		PRO_PENETRATION,
		PRO_PERCUSSION,
		PRO_PHOSPHORUS,
		PRO_PHOSPHORUS_SPLASH,
		PRO_POISON_GAS,
		PRO_RADIUS,
		PRO_RADIUS_HIGH,
		PRO_RANGE,
		PRO_ROCKET,
		PRO_ROCKET_PROPELLED,
		PRO_SHRAPNEL,
		PRO_SPEED,
		PRO_STUNNING,
		PRO_TOXIC,
		PRO_WEAR,
		PRO_WITHERING;

		public String key = "desc.item.ammo.";

		private AmmoItemTrait() {
			key += this.toString().toLowerCase(Locale.US);
		}
	}

	private final String altName;

	public ItemAmmo(String s, E[] clazz) {
		this(s, clazz, "");
	}

	public ItemAmmo(String s, E[] clazz, String altName) {
		super(s, clazz, true, true);
		this.setCreativeTab(null);
		this.altName = altName;

		this.textures = Arrays.stream(theEnum)
				.sorted(Comparator.comparing(Enum::ordinal))
				.map(IAmmoItemEnum::getInternalName)
				.toArray(String[]::new);
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		ItemAmmoEnums.IAmmoItemEnum num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return "item." + num.getInternalName();
	}

	@Override
	public ItemAmmo<E> setCreativeTab(CreativeTabs tab) {
        return (ItemAmmo<E>) super.setCreativeTab(tab);
	}
}
