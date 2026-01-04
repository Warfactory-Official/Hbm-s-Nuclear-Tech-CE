package com.hbm.items.machine;

import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;
import java.util.Set;

public class ItemMachineUpgrade extends ItemBakedBase {
	public UpgradeType type;
	public int tier;

	public ItemMachineUpgrade(String s) {
		this(s, UpgradeType.SPECIAL, 0);
	}

	public ItemMachineUpgrade(String s, UpgradeType type) {
		this(s, type, 0);
	}

	public ItemMachineUpgrade(String s, UpgradeType type, int tier) {
		super(s);
		this.type = type;
		this.tier = tier;
	}

	public int getSpeed(){
		if(this == ModItems.upgrade_speed_1) return 1;
		if(this == ModItems.upgrade_speed_2) return 2;
		if(this == ModItems.upgrade_speed_3) return 3;
		if(this == ModItems.upgrade_overdrive_1) return 4;
		if(this == ModItems.upgrade_overdrive_2) return 6;
		if(this == ModItems.upgrade_overdrive_3) return 8;
		if(this == ModItems.upgrade_screm) return 10;
		return 0;
	}

	public static int getSpeed(ItemStack stack){
		if(stack == null || stack.isEmpty()) return 0;
		Item upgrade = stack.getItem();
		if(upgrade == ModItems.upgrade_speed_1) return 1;
		if(upgrade == ModItems.upgrade_speed_2) return 2;
		if(upgrade == ModItems.upgrade_speed_3) return 3;
		if(upgrade == ModItems.upgrade_overdrive_1) return 4;
		if(upgrade == ModItems.upgrade_overdrive_2) return 6;
		if(upgrade == ModItems.upgrade_overdrive_3) return 8;
		if(upgrade == ModItems.upgrade_screm) return 10;
		return 0;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		GuiScreen open = Minecraft.getMinecraft().currentScreen;

		if (open instanceof GuiContainer guiContainer) {
			Container container = guiContainer.inventorySlots;
			if (!container.inventorySlots.isEmpty()) {
				Slot first = container.getSlot(0);

				IItemHandler handler = null;

				if (first instanceof SlotItemHandler) {
					handler = ((SlotItemHandler) first).getItemHandler();
				} else if (first.inventory instanceof ICapabilityProvider capProv) {
					if (capProv.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
						handler = capProv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					}
				}

				if (handler instanceof IUpgradeInfoProvider provider) {
					boolean advanced = flagIn.isAdvanced();
					if (provider.canProvideInfo(this.type, this.tier, advanced)) {
						provider.provideInfo(this.type, this.tier, list, advanced);
						return;
					}
				}
			}
		}

		if(this == ModItems.upgrade_radius)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade7"));
			list.add(" "+I18nUtil.resolveKey("desc.upgraderd"));
			list.add("");
			list.add(" "+I18nUtil.resolveKey("desc.upgradestack"));
		}

		if(this == ModItems.upgrade_health)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade8"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeht"));
			list.add("");
			list.add(" "+I18nUtil.resolveKey("desc.upgradestack"));
		}
		
		if(this == ModItems.upgrade_smelter)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade12"));
		}

		if(this == ModItems.upgrade_shredder)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade13"));
		}

		if(this == ModItems.upgrade_centrifuge)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade21"));
		}

		if(this == ModItems.upgrade_crystallizer)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade14"));
		}

		if(this == ModItems.upgrade_screm)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade15"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade16"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade17"));
		}
		
		if(this == ModItems.upgrade_nullifier)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade10"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade19"));
		}
		// I'm not translating this shit for now
		if(this == ModItems.upgrade_gc_speed) {
			list.add(TextFormatting.RED + "Gas Centrifuge Upgrade");
			list.add("Allows for total isotopic separation of HEUF6");
			list.add(TextFormatting.YELLOW + "also your centrifuge goes sicko mode");
		}
	}

	public static final Set<Item> scrapItems = Sets.newHashSet(Item.getItemFromBlock(Blocks.GRASS),
			Item.getItemFromBlock(Blocks.DIRT),
			Item.getItemFromBlock(Blocks.STONE),
			Item.getItemFromBlock(Blocks.COBBLESTONE),
			Item.getItemFromBlock(Blocks.SAND),
			Item.getItemFromBlock(Blocks.SANDSTONE),
			Item.getItemFromBlock(Blocks.GRAVEL),
			Item.getItemFromBlock(Blocks.NETHERRACK),
			Item.getItemFromBlock(Blocks.END_STONE),
			Item.getItemFromBlock(ModBlocks.stone_gneiss),
			Items.FLINT,
			Items.SNOWBALL,
			Items.WHEAT_SEEDS,
			Items.STICK);

	public enum UpgradeType {
		SPEED,
		EFFECT,
		POWER,
		FORTUNE,
		AFTERBURN,
		OVERDRIVE,
		NULLIFIER,
		SCREAM,
		SPECIAL
	}
}
