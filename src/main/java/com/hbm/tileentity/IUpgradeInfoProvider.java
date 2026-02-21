package com.hbm.tileentity;

import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.List;

public interface IUpgradeInfoProvider {

    /** If any of the automated display stuff should be applied for this upgrade. A level of 0 is used by the GUI's indicator, as opposed to the item tooltips */
    boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo);
    void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo);
    HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades();

    static String getStandardLabel(Block block) {
        return TextFormatting.YELLOW + ">>> " + I18nUtil.resolveKey(block.getTranslationKey() + ".name") + " <<<";
    }

    String KEY_ACID = "upgrade.acid";
    String KEY_BURN = "upgrade.burn";
    String KEY_CONSUMPTION = "upgrade.consumption";
    String KEY_COOLANT_CONSUMPTION = "upgrade.coolantConsumption";
    String KEY_DELAY = "upgrade.delay";
    String KEY_SPEED = "upgrade.speed";
    String KEY_EFFICIENCY = "upgrade.efficiency";
    String KEY_PRODUCTIVITY = "upgrade.productivity";
    String KEY_FORTUNE = "upgrade.fortune";
    String KEY_RANGE = "upgrade.range";
}
