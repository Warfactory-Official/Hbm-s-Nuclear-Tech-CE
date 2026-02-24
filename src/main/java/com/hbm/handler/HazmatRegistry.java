package com.hbm.handler;

import com.hbm.items.ModItems;
import com.hbm.items.armor.ItemModCladding;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ShadyUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class HazmatRegistry {
	private static Map<Item, Double> entries = new HashMap<>();

	public static void registerHazmat(Item item, double resistance) {

		entries.put(item, resistance);
	}

	public static double getResistance(ItemStack stack) {

		if(stack == null)
			return 0;

		float cladding = getCladding(stack);

		Double d = entries.get(stack.getItem());

		if(d != null)
			return d + cladding;

		return cladding;
	}

	public static float getCladding(ItemStack stack) {

		if (stack.hasTagCompound() && stack.getTagCompound().getFloat("hfr_cladding") > 0.0F) {
			return stack.getTagCompound().getFloat("hfr_cladding");
		} else {
			if (ArmorModHandler.hasMods(stack)) {
				ItemStack[] mods = ArmorModHandler.pryMods(stack);
				ItemStack cladding = mods[5];
				if (cladding != null && cladding.getItem() instanceof ItemModCladding) {
					return (float)((ItemModCladding)cladding.getItem()).rad;
				}
			}

			return 0.0F;
		}
	}

	public static float getResistance(EntityLivingBase player) {
		float res = 0.0F;

        if (player.getUniqueID().equals(ShadyUtil.HbMinecraft) || player.getUniqueID().equals(ShadyUtil.Drillgon) || player.getUniqueID().equals(ShadyUtil.Alcater)) {
            res += 1.0F;
        }

		for(ItemStack stack : player.getArmorInventoryList()) {
			if(!stack.isEmpty()) {
				res += getResistance(stack);
			}
		}
		PotionEffect radx = player.getActivePotionEffect(HbmPotion.radx);
		if(radx != null)
			res += 0.1F * (1+radx.getAmplifier());

		return res;
	}

	public static double fixRounding(double value){
		return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	public static void registerHazmats() {
		//assuming coefficient of 10
		//real coefficient turned out to be 5
		//oops

		double helmet = 0.2D;
		double chest = 0.4D;
		double legs = 0.3D;
		double boots = 0.1D;

		double iron = 0.0225D; // 5%
		double gold = 0.03D; // 5%
		double steel = 0.045D; // 10%
		double titanium = 0.045D; // 10%
		double alloy = 0.07D; // 15%
		double cobalt = 0.125D; // 25%

		double hazYellow = 0.6D; // 75%
		double hazRed = 1.0D; // 90%
		double hazGray = 2D; // 99%
		double liquidator = 2.4D; // 99.6%
		double paa = 3.0D; // 99.9%
		

		double t51 = 1D; // 90%
		double ajr = 1.3D; // 95%
		double hev = 1.6D; // 97.5%
		double bj = 1D; // 90%
		double rpa = 2D; // 99%
		double ncrpa = 1.7D; // 97%
        double trench = 1D; // 90%
		double fau = 4D; // 99.99%
		double dns = 6D; // 99.9999%
		double security = 0.01D; // 2.3%
		double star = 0.25D; // 44%
		double cmb = 1.3D; // 95%
		double schrab = 3D; // 99.9%
		double euph = 10D; // 99.99999999%

		HazmatRegistry.registerHazmat(ModItems.hazmat_helmet, fixRounding(hazYellow * helmet));
		HazmatRegistry.registerHazmat(ModItems.hazmat_plate, fixRounding(hazYellow * chest));
		HazmatRegistry.registerHazmat(ModItems.hazmat_legs, fixRounding(hazYellow * legs));
		HazmatRegistry.registerHazmat(ModItems.hazmat_boots, fixRounding(hazYellow * boots));

		HazmatRegistry.registerHazmat(ModItems.hazmat_helmet_red, fixRounding(hazRed * helmet));
		HazmatRegistry.registerHazmat(ModItems.hazmat_plate_red, fixRounding(hazRed * chest));
		HazmatRegistry.registerHazmat(ModItems.hazmat_legs_red, fixRounding(hazRed * legs));
		HazmatRegistry.registerHazmat(ModItems.hazmat_boots_red, fixRounding(hazRed * boots));

		HazmatRegistry.registerHazmat(ModItems.hazmat_helmet_grey, fixRounding(hazGray * helmet));
		HazmatRegistry.registerHazmat(ModItems.hazmat_plate_grey, fixRounding(hazGray * chest));
		HazmatRegistry.registerHazmat(ModItems.hazmat_legs_grey, fixRounding(hazGray * legs));
		HazmatRegistry.registerHazmat(ModItems.hazmat_boots_grey, fixRounding(hazGray * boots));

		HazmatRegistry.registerHazmat(ModItems.liquidator_helmet, fixRounding(liquidator * helmet));
		HazmatRegistry.registerHazmat(ModItems.liquidator_plate, fixRounding(liquidator * chest));
		HazmatRegistry.registerHazmat(ModItems.liquidator_legs, fixRounding(liquidator * legs));
		HazmatRegistry.registerHazmat(ModItems.liquidator_boots, fixRounding(liquidator * boots));

		HazmatRegistry.registerHazmat(ModItems.t51_helmet, fixRounding(t51 * helmet));
		HazmatRegistry.registerHazmat(ModItems.t51_plate, fixRounding(t51 * chest));
		HazmatRegistry.registerHazmat(ModItems.t51_legs, fixRounding(t51 * legs));
		HazmatRegistry.registerHazmat(ModItems.t51_boots, fixRounding(t51 * boots));

		HazmatRegistry.registerHazmat(ModItems.steamsuit_helmet, 1.3 * helmet);
		HazmatRegistry.registerHazmat(ModItems.steamsuit_plate, 1.3 * chest);
		HazmatRegistry.registerHazmat(ModItems.steamsuit_legs, 1.3 * legs);
		HazmatRegistry.registerHazmat(ModItems.steamsuit_boots, 1.3 * boots);

		HazmatRegistry.registerHazmat(ModItems.ajr_helmet, fixRounding(ajr * helmet));
		HazmatRegistry.registerHazmat(ModItems.ajr_plate, fixRounding(ajr * chest));
		HazmatRegistry.registerHazmat(ModItems.ajr_legs, fixRounding(ajr * legs));
		HazmatRegistry.registerHazmat(ModItems.ajr_boots, fixRounding(ajr * boots));

		HazmatRegistry.registerHazmat(ModItems.ajro_helmet, fixRounding(ajr * helmet));
		HazmatRegistry.registerHazmat(ModItems.ajro_plate, fixRounding(ajr * chest));
		HazmatRegistry.registerHazmat(ModItems.ajro_legs, fixRounding(ajr * legs));
		HazmatRegistry.registerHazmat(ModItems.ajro_boots, fixRounding(ajr * boots));

		HazmatRegistry.registerHazmat(ModItems.rpa_helmet, fixRounding(rpa * helmet));
		HazmatRegistry.registerHazmat(ModItems.rpa_plate, fixRounding(rpa * chest));
		HazmatRegistry.registerHazmat(ModItems.rpa_legs, fixRounding(rpa * legs));
		HazmatRegistry.registerHazmat(ModItems.rpa_boots, fixRounding(rpa * boots));

		HazmatRegistry.registerHazmat(ModItems.ncrpa_helmet, fixRounding(ncrpa * helmet));
		HazmatRegistry.registerHazmat(ModItems.ncrpa_plate, fixRounding(ncrpa * chest));
		HazmatRegistry.registerHazmat(ModItems.ncrpa_legs, fixRounding(ncrpa * legs));
		HazmatRegistry.registerHazmat(ModItems.ncrpa_boots, fixRounding(ncrpa * boots));

        HazmatRegistry.registerHazmat(ModItems.trenchmaster_helmet, trench * helmet);
        HazmatRegistry.registerHazmat(ModItems.trenchmaster_plate, trench * chest);
        HazmatRegistry.registerHazmat(ModItems.trenchmaster_legs, trench * legs);
        HazmatRegistry.registerHazmat(ModItems.trenchmaster_boots, trench * boots);

		HazmatRegistry.registerHazmat(ModItems.bj_helmet, fixRounding(bj * helmet));
		HazmatRegistry.registerHazmat(ModItems.bj_plate, fixRounding(bj * chest));
		HazmatRegistry.registerHazmat(ModItems.bj_plate_jetpack, fixRounding(bj * chest));
		HazmatRegistry.registerHazmat(ModItems.bj_legs, fixRounding(bj * legs));
		HazmatRegistry.registerHazmat(ModItems.bj_boots, fixRounding(bj * boots));

		HazmatRegistry.registerHazmat(ModItems.hev_helmet, fixRounding(hev * helmet));
		HazmatRegistry.registerHazmat(ModItems.hev_plate, fixRounding(hev * chest));
		HazmatRegistry.registerHazmat(ModItems.hev_legs, fixRounding(hev * legs));
		HazmatRegistry.registerHazmat(ModItems.hev_boots, fixRounding(hev * boots));
		
		HazmatRegistry.registerHazmat(ModItems.fau_helmet, fixRounding(fau * helmet));
		HazmatRegistry.registerHazmat(ModItems.fau_plate, fixRounding(fau * chest));
		HazmatRegistry.registerHazmat(ModItems.fau_legs, fixRounding(fau * legs));
		HazmatRegistry.registerHazmat(ModItems.fau_boots, fixRounding(fau * boots));

		HazmatRegistry.registerHazmat(ModItems.dns_helmet, fixRounding(dns * helmet));
		HazmatRegistry.registerHazmat(ModItems.dns_plate, fixRounding(dns * chest));
		HazmatRegistry.registerHazmat(ModItems.dns_legs, fixRounding(dns * legs));
		HazmatRegistry.registerHazmat(ModItems.dns_boots, fixRounding(dns * boots));

		HazmatRegistry.registerHazmat(ModItems.paa_helmet, fixRounding(paa * helmet));
		HazmatRegistry.registerHazmat(ModItems.paa_plate, fixRounding(paa * chest));
		HazmatRegistry.registerHazmat(ModItems.paa_legs, fixRounding(paa * legs));
		HazmatRegistry.registerHazmat(ModItems.paa_boots, fixRounding(paa * boots));

		HazmatRegistry.registerHazmat(ModItems.hazmat_paa_helmet, fixRounding(paa * helmet));
		HazmatRegistry.registerHazmat(ModItems.hazmat_paa_plate, fixRounding(paa * chest));
		HazmatRegistry.registerHazmat(ModItems.hazmat_paa_legs, fixRounding(paa * legs));
		HazmatRegistry.registerHazmat(ModItems.hazmat_paa_boots, fixRounding(paa * boots));

		HazmatRegistry.registerHazmat(ModItems.security_helmet, fixRounding(security * helmet));
		HazmatRegistry.registerHazmat(ModItems.security_plate, fixRounding(security * chest));
		HazmatRegistry.registerHazmat(ModItems.security_legs, fixRounding(security * legs));
		HazmatRegistry.registerHazmat(ModItems.security_boots, fixRounding(security * boots));

		HazmatRegistry.registerHazmat(ModItems.starmetal_helmet, fixRounding(star * helmet));
		HazmatRegistry.registerHazmat(ModItems.starmetal_plate, fixRounding(star * chest));
		HazmatRegistry.registerHazmat(ModItems.starmetal_legs, fixRounding(star * legs));
		HazmatRegistry.registerHazmat(ModItems.starmetal_boots, fixRounding(star * boots));

		HazmatRegistry.registerHazmat(ModItems.jackt, 0.1);
		HazmatRegistry.registerHazmat(ModItems.jackt2, 0.1);

		HazmatRegistry.registerHazmat(ModItems.gas_mask, 0.07);
		HazmatRegistry.registerHazmat(ModItems.gas_mask_m65, 0.095);

		HazmatRegistry.registerHazmat(ModItems.steel_helmet, fixRounding(steel * helmet));
		HazmatRegistry.registerHazmat(ModItems.steel_plate, fixRounding(steel * chest));
		HazmatRegistry.registerHazmat(ModItems.steel_legs, fixRounding(steel * legs));
		HazmatRegistry.registerHazmat(ModItems.steel_boots, fixRounding(steel * boots));

		HazmatRegistry.registerHazmat(ModItems.titanium_helmet, fixRounding(titanium * helmet));
		HazmatRegistry.registerHazmat(ModItems.titanium_plate, fixRounding(titanium * chest));
		HazmatRegistry.registerHazmat(ModItems.titanium_legs, fixRounding(titanium * legs));
		HazmatRegistry.registerHazmat(ModItems.titanium_boots, fixRounding(titanium * boots));

		HazmatRegistry.registerHazmat(ModItems.cobalt_helmet, fixRounding(cobalt * helmet));
		HazmatRegistry.registerHazmat(ModItems.cobalt_plate, fixRounding(cobalt * chest));
		HazmatRegistry.registerHazmat(ModItems.cobalt_legs, fixRounding(cobalt * legs));
		HazmatRegistry.registerHazmat(ModItems.cobalt_boots, fixRounding(cobalt * boots));

		HazmatRegistry.registerHazmat(Items.IRON_HELMET, fixRounding(iron * helmet));
		HazmatRegistry.registerHazmat(Items.IRON_CHESTPLATE, fixRounding(iron * chest));
		HazmatRegistry.registerHazmat(Items.IRON_LEGGINGS, fixRounding(iron * legs));
		HazmatRegistry.registerHazmat(Items.IRON_BOOTS, fixRounding(iron * boots));

		HazmatRegistry.registerHazmat(Items.GOLDEN_HELMET, fixRounding(gold * helmet));
		HazmatRegistry.registerHazmat(Items.GOLDEN_CHESTPLATE, fixRounding(gold * chest));
		HazmatRegistry.registerHazmat(Items.GOLDEN_LEGGINGS, fixRounding(gold * legs));
		HazmatRegistry.registerHazmat(Items.GOLDEN_BOOTS, fixRounding(gold * boots));

		HazmatRegistry.registerHazmat(ModItems.alloy_helmet, fixRounding(alloy * helmet));
		HazmatRegistry.registerHazmat(ModItems.alloy_plate, fixRounding(alloy * chest));
		HazmatRegistry.registerHazmat(ModItems.alloy_legs, fixRounding(alloy * legs));
		HazmatRegistry.registerHazmat(ModItems.alloy_boots, fixRounding(alloy * boots));

		HazmatRegistry.registerHazmat(ModItems.cmb_helmet, fixRounding(cmb * helmet));
		HazmatRegistry.registerHazmat(ModItems.cmb_plate, fixRounding(cmb * chest));
		HazmatRegistry.registerHazmat(ModItems.cmb_legs, fixRounding(cmb * legs));
		HazmatRegistry.registerHazmat(ModItems.cmb_boots, fixRounding(cmb * boots));

		HazmatRegistry.registerHazmat(ModItems.schrabidium_helmet, fixRounding(schrab * helmet));
		HazmatRegistry.registerHazmat(ModItems.schrabidium_plate, fixRounding(schrab * chest));
		HazmatRegistry.registerHazmat(ModItems.schrabidium_legs, fixRounding(schrab * legs));
		HazmatRegistry.registerHazmat(ModItems.schrabidium_boots, fixRounding(schrab * boots));

		HazmatRegistry.registerHazmat(ModItems.euphemium_helmet, fixRounding(euph * helmet));
		HazmatRegistry.registerHazmat(ModItems.euphemium_plate, fixRounding(euph * chest));
		HazmatRegistry.registerHazmat(ModItems.euphemium_legs, fixRounding(euph * legs));
		HazmatRegistry.registerHazmat(ModItems.euphemium_boots, fixRounding(euph * boots));
	}
}
