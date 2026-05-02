package com.hbm.handler;

import com.hbm.api.item.IGasMask;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import com.hbm.util.Tuple;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArmorUtil {

	public static List<Tuple.Pair<Item, HazardClass[]>> external = new ArrayList<>();

	public static HazardClass[] FULL_NO_LIGHT = new HazardClass[] {HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.SAND};
	public static HazardClass[] FULL_PACKAGE = new HazardClass[] {HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND};

	public static void register() {
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_mono, HazardClass.PARTICLE_COARSE, HazardClass.GAS_MONOXIDE);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_combo, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_rag, HazardClass.PARTICLE_COARSE);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_piss, HazardClass.PARTICLE_COARSE, HazardClass.GAS_LUNG);

		ArmorRegistry.registerHazard(ModItems.gas_mask, HazardClass.SAND, HazardClass.LIGHT);
		ArmorRegistry.registerHazard(ModItems.gas_mask_m65, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.mask_rag, HazardClass.PARTICLE_COARSE);
		ArmorRegistry.registerHazard(ModItems.mask_piss, HazardClass.PARTICLE_COARSE, HazardClass.GAS_LUNG);

		ArmorRegistry.registerHazard(ModItems.goggles, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.ashglasses, HazardClass.LIGHT, HazardClass.SAND);

		ArmorRegistry.registerHazard(ModItems.attachment_mask, HazardClass.SAND);

		ArmorRegistry.registerHazard(ModItems.asbestos_helmet, HazardClass.SAND, HazardClass.LIGHT);
		ArmorRegistry.registerHazard(ModItems.hazmat_helmet, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.hazmat_helmet_red, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.hazmat_helmet_grey, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.hazmat_paa_helmet, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.liquidator_helmet, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.schrabidium_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.euphemium_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);

		for(Tuple.Pair<Item, HazardClass[]> pair : external) {
			ArmorRegistry.registerHazard(pair.getKey(), pair.getValue());
		}
		
		//Ob ihr wirklich richtig steht, seht ihr wenn das Licht angeht!
		registerIfExists("gregtech", "gt.armor.hazmat.universal.head", HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		registerIfExists("gregtech", "gt.armor.hazmat.biochemgas.head", HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		registerIfExists("gregtech", "gt.armor.hazmat.radiation.head", HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
	}
	
	private static void registerIfExists(String domain, String name, HazardClass... classes) {
		Item item = Compat.tryLoadItem(domain, name);
		if(item != null)
			ArmorRegistry.registerHazard(item, classes);
	}
	
	public static boolean checkForFaraday(EntityPlayer player) {
		
		NonNullList<ItemStack> armor = player.inventory.armorInventory;
		
		if(armor.get(0).isEmpty() || armor.get(1).isEmpty() || armor.get(2).isEmpty() || armor.get(3).isEmpty()) return false;

        return ArmorUtil.isFaradayArmor(armor.get(0)) &&
                ArmorUtil.isFaradayArmor(armor.get(1)) &&
                ArmorUtil.isFaradayArmor(armor.get(2)) &&
                ArmorUtil.isFaradayArmor(armor.get(3));
    }

	public static boolean isFaradayArmor(ItemStack item) {
		
		String name = item.getTranslationKey();

		for(String metal : metals) if(name.toLowerCase(Locale.US).contains(metal)) return true;
		if(HazmatRegistry.getCladding(item) > 0) return true;

		return false;
	}
	
	public static boolean checkArmorNull(EntityLivingBase player, EntityEquipmentSlot slot) {
		return player.getItemStackFromSlot(slot).isEmpty();
	}

	public static final String[] metals = new String[] {
			"chainmail",
			"iron",
			"silver",
			"gold",
			"platinum",
			"tin",
			"lead",
			"liquidator",
			"schrabidium",
			"euphemium",
			"steel",
			"cmb",
			"titanium",
			"alloy",
			"copper",
			"bronze",
			"electrum",
			"t45",
			"bj",
			"starmetal",
			"hazmat", //also count because rubber is insulating
			"rubber",
			"hev",
			"ajr",
			"rpa",
			"spacesuit"
	};

	public static void damageSuit(EntityPlayer player, int slot, int amount) {
	
		if(player.inventory.armorInventory.get(slot).isEmpty())
			return;
	
		int j = player.inventory.armorInventory.get(slot).getItemDamage();
		player.inventory.armorInventory.get(slot).setItemDamage(j += amount);
	
		if(player.inventory.armorInventory.get(slot).getItemDamage() >= player.inventory.armorInventory.get(slot).getMaxDamage()) {
			player.inventory.armorInventory.set(slot, ItemStack.EMPTY);
		}
	}

	public static void resetFlightTime(EntityPlayer player) {
		if(player instanceof EntityPlayerMP mp) {
            mp.connection.floatingTickCount = 0;
		}
	}

	public static boolean checkForFiend2(EntityPlayer player) {
		
		return ArmorUtil.checkArmorPiece(player, ModItems.jackt2, 2) && Library.checkForHeld(player, ModItems.shimmer_axe);
	}

	public static boolean checkForFiend(EntityPlayer player) {
		
		return ArmorUtil.checkArmorPiece(player, ModItems.jackt, 2) && Library.checkForHeld(player, ModItems.shimmer_sledge);
	}

	// Drillgon200: Is there a reason for this method? I don't know and I don't
	// care to find out.
	// Alcater: Looks like some kind of hazmat tier 2 check
	public static boolean checkForHaz2(EntityLivingBase player) {

        return checkArmor(player, ModItems.hazmat_paa_helmet, ModItems.hazmat_paa_plate, ModItems.hazmat_paa_legs, ModItems.hazmat_paa_boots) ||
                checkArmor(player, ModItems.liquidator_helmet, ModItems.liquidator_plate, ModItems.liquidator_legs, ModItems.liquidator_boots) ||
                checkArmor(player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots) ||
                checkArmor(player, ModItems.rpa_helmet, ModItems.rpa_plate, ModItems.rpa_legs, ModItems.rpa_boots) ||
                checkArmor(player, ModItems.fau_helmet, ModItems.fau_plate, ModItems.fau_legs, ModItems.fau_boots) ||
                checkArmor(player, ModItems.dns_helmet, ModItems.dns_plate, ModItems.dns_legs, ModItems.dns_boots);
    }

	public static boolean checkForHazmatOnly(EntityLivingBase player) {
        return checkArmor(player, ModItems.hazmat_helmet, ModItems.hazmat_plate, ModItems.hazmat_legs, ModItems.hazmat_boots) ||
                checkArmor(player, ModItems.hazmat_helmet_red, ModItems.hazmat_plate_red, ModItems.hazmat_legs_red, ModItems.hazmat_boots_red) ||
                checkArmor(player, ModItems.hazmat_helmet_grey, ModItems.hazmat_plate_grey, ModItems.hazmat_legs_grey, ModItems.hazmat_boots_grey) ||
                checkArmor(player, ModItems.hazmat_paa_helmet, ModItems.hazmat_paa_plate, ModItems.hazmat_paa_legs, ModItems.hazmat_paa_boots) ||
                checkArmor(player, ModItems.liquidator_helmet, ModItems.liquidator_plate, ModItems.liquidator_legs, ModItems.liquidator_boots);
    }

	@Deprecated public static boolean checkForHazmat(EntityLivingBase player) {
		if(checkArmor(player, ModItems.hazmat_helmet, ModItems.hazmat_plate, ModItems.hazmat_legs, ModItems.hazmat_boots) ||
				checkArmor(player, ModItems.hazmat_helmet_red, ModItems.hazmat_plate_red, ModItems.hazmat_legs_red, ModItems.hazmat_boots_red) ||
				checkArmor(player, ModItems.hazmat_helmet_grey, ModItems.hazmat_plate_grey, ModItems.hazmat_legs_grey, ModItems.hazmat_boots_grey) ||
				checkArmor(player, ModItems.schrabidium_helmet, ModItems.schrabidium_plate, ModItems.schrabidium_legs, ModItems.schrabidium_boots) ||
				checkForHaz2(player)) {
	
			return true;
		}

        return player.isPotionActive(HbmPotion.mutation);
    }

	public static boolean checkForAsbestos(EntityLivingBase player) {
        return ArmorUtil.checkArmor(player, ModItems.asbestos_helmet, ModItems.asbestos_plate, ModItems.asbestos_legs, ModItems.asbestos_boots);
    }

	public static boolean checkArmor(EntityLivingBase player, Item helm, Item chest, Item leg, Item shoe) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == shoe &&
                player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == leg &&
                player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == chest &&
                player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == helm;
    }

	public static boolean checkArmorPiece(EntityPlayer player, Item armor, int slot) {
        return !player.inventory.armorInventory.get(slot).isEmpty() && player.inventory.armorInventory.get(slot).getItem() == armor;
    }
	
	/*
	 * Default implementations for IGasMask items
	 */
	public static final String FILTERK_KEY = "hfrFilter";
	
	public static void damageGasMaskFilter(EntityLivingBase entity, int damage) {
		
		ItemStack mask = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		
		if(mask.isEmpty())
			return;
		
		if(!(mask.getItem() instanceof IGasMask)) {
			
			if(ArmorModHandler.hasMods(mask)) {
				
				ItemStack[] mods = ArmorModHandler.pryMods(mask);
				
				if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() instanceof IGasMask)
					mask = mods[ArmorModHandler.helmet_only];
			}
		}
		
		if(mask != null)
			damageGasMaskFilter(mask, damage);
	}
	
	public static void damageGasMaskFilter(ItemStack mask, int damage) {
		ItemStack filter = getGasMaskFilter(mask);
		
		if(filter.isEmpty()) {
			if(ArmorModHandler.hasMods(mask)) {
				ItemStack[] mods = ArmorModHandler.pryMods(mask);
				
				if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() instanceof IGasMask)
					filter = getGasMaskFilter(mods[ArmorModHandler.helmet_only]);
			}
		}
		
		if(filter.isEmpty() || filter.getMaxDamage() == 0)
			return;
		
		filter.setItemDamage(filter.getItemDamage() + damage);
		
		if(filter.getItemDamage() > filter.getMaxDamage()){
			removeFilter(mask);
		}
		else{
			installGasMaskFilter(mask, filter);
		}
	}
	
	public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
		
		if(mask == null || filter == null || filter.isEmpty())
			return;
		
		if(!mask.hasTagCompound())
			mask.setTagCompound(new NBTTagCompound());
		
		ItemStack copy = filter.copy();
		copy.setCount(1);
		NBTTagCompound attach = new NBTTagCompound();
		copy.writeToNBT(attach);
		
		mask.getTagCompound().setTag(FILTERK_KEY, attach);
	}
	
	public static void removeFilter(ItemStack mask) {
		
		if(mask == null)
			return;
		
		if(!mask.hasTagCompound())
			return;
		
		mask.getTagCompound().removeTag(FILTERK_KEY);
		if(mask.getTagCompound().isEmpty())
			mask.setTagCompound(null);
	}

	@NotNull
	public static ItemStack getGasMaskFilter(ItemStack mask) {
		
		if(mask == null)
			return ItemStack.EMPTY;
		
		if(!mask.hasTagCompound())
			return ItemStack.EMPTY;
		
		NBTTagCompound attach = mask.getTagCompound().getCompoundTag(FILTERK_KEY);
		ItemStack filter = new ItemStack(attach);
		if(filter.isEmpty())
			return ItemStack.EMPTY;
		return filter;
	}
	
	public static boolean checkForDigamma(EntityPlayer player) {
		
		if(checkArmor(player, ModItems.fau_helmet, ModItems.fau_plate, ModItems.fau_legs, ModItems.fau_boots))
			return true;

		if(checkArmor(player, ModItems.dns_helmet, ModItems.dns_plate, ModItems.dns_legs, ModItems.dns_boots))
			return true;

        return player.isPotionActive(HbmPotion.stability);
    }

	/**
	 * Grabs the installed filter or the filter of the attachment, used for attachment rendering
	 * @param mask
	 * @param entity
	 * @return
	 */
	public static ItemStack getGasMaskFilterRecursively(ItemStack mask) {
		
		ItemStack filter = getGasMaskFilter(mask);
		
		if((filter.isEmpty() || filter.isEmpty()) && ArmorModHandler.hasMods(mask)) {
			
			ItemStack[] mods = ArmorModHandler.pryMods(mask);
			
			if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() instanceof IGasMask)
				filter = ((IGasMask)mods[ArmorModHandler.helmet_only].getItem()).getFilter(mods[ArmorModHandler.helmet_only]);
		}
		
		return filter;
	}

	public static void addGasMaskTooltip(ItemStack mask, World world, List<String> list, ITooltipFlag flagIn){
		
		if(mask == null || !(mask.getItem() instanceof IGasMask))
			return;
		
		ItemStack filter = ((IGasMask)mask.getItem()).getFilter(mask);
		
		if(filter.isEmpty()) {
			list.add("§c" + I18nUtil.resolveKey("desc.nofilter"));
			return;
		}
		
		list.add("§6" + I18nUtil.resolveKey("desc.infilter"));
		
		int meta = filter.getItemDamage();
		int max = filter.getMaxDamage();
		
		String append = "";
		
		if(max > 0) {
			append = " (" + Library.getPercentage((max - meta) / (double)max) + "%) "+(max-meta)+"/"+max;
		}
		
		List<String> lore = new ArrayList<>();
		list.add("  " + filter.getDisplayName() + append);
		filter.getItem().addInformation(filter, world, lore, flagIn);
		ForgeEventFactory.onItemTooltip(filter, null, lore, flagIn);
		lore.forEach(x -> list.add("§e  " + x));
	}
}
