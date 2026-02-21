package com.hbm.items.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
@Deprecated
public class ItemBattery extends Item implements IBatteryItem {

    protected long maxCharge;
    protected long chargeRate;
    protected long dischargeRate;

    public ItemBattery(long dura, long chargeRate, long dischargeRate, String s) {
        this.maxCharge = dura;
        this.chargeRate = chargeRate;
        this.dischargeRate = dischargeRate;
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        ModItems.ALL_ITEMS.add(this);
    }

    public static ItemStack getEmptyBattery(Item item) {

        if (item instanceof ItemBattery) {
            ItemStack stack = new ItemStack(item);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("charge", 0);
            //stack.setItemDamage(100);
            return stack.copy();
        }

        return null;
    }

    public static ItemStack getFullBattery(Item item) {

        if (item instanceof ItemBattery) {
            ItemStack stack = new ItemStack(item);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("charge", ((ItemBattery) item).getMaxCharge(stack));
            return stack.copy();
        }

        return new ItemStack(item);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        if (stack.getItem() == ModItems.battery_creative) return;
        long charge = maxCharge;
        if (stack.hasTagCompound()) charge = getCharge(stack);


        if (stack.getItem() != ModItems.fusion_core && stack.getItem() != ModItems.energy_core) {
            list.add("§6" + I18nUtil.resolveKey("desc.energystore") + " " + Library.getShortNumber(charge) + "/" + Library.getShortNumber(maxCharge) + "HE§r");
        } else {
            String charge1 = Library.getShortNumber((charge * 100) / this.maxCharge);
            list.add("§2" + I18nUtil.resolveKey("desc.energychargecur") + " " + charge1 + "%§r");
            list.add("(" + Library.getShortNumber(charge) + "/" + Library.getShortNumber(maxCharge) + "HE)");
        }
        list.add("§a" + I18nUtil.resolveKey("desc.energychargerate") + " " + Library.getShortNumber(chargeRate * 20) + "HE/s§r");
        list.add("§c" + I18nUtil.resolveKey("desc.energydchargerate") + " " + Library.getShortNumber(dischargeRate * 20) + "HE/s§r");
    }

    @NotNull
    @Override
    public EnumRarity getRarity(@NotNull ItemStack p_77613_1_) {

        if (this == ModItems.battery_schrabidium) {
            return EnumRarity.RARE;
        }

        if (this == ModItems.fusion_core || this == ModItems.energy_core) {
            return EnumRarity.UNCOMMON;
        }

        return EnumRarity.COMMON;
    }

    public void chargeBattery(ItemStack stack, long i) {
        if (stack.getItem() == ModItems.battery_creative) return;
        if (stack.getItem() instanceof ItemBattery) {
            if (stack.hasTagCompound()) {
                long charge = stack.getTagCompound().getLong("charge");
                stack.getTagCompound().setLong("charge", Math.min(charge + i, maxCharge));
            } else {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setLong("charge", Math.min(i, maxCharge));
            }
        }
    }

    public void setCharge(ItemStack stack, long i) {
        if (stack.getItem() == ModItems.battery_creative) return;
        if (stack.getItem() instanceof ItemBattery) {
            if (stack.hasTagCompound()) {
                stack.getTagCompound().setLong("charge", Math.min(i, maxCharge));
            } else {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setLong("charge", Math.min(i, maxCharge));
            }
        }
    }

    public void dischargeBattery(ItemStack stack, long i) {
        if (stack.getItem() == ModItems.battery_creative) return;
        if (stack.getItem() instanceof ItemBattery) {
            if (stack.hasTagCompound()) {
                long charge = stack.getTagCompound().getLong("charge");
                stack.getTagCompound().setLong("charge", Math.max(charge - i, 0));
            } else {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setLong("charge", Math.max(this.maxCharge - i, 0));
            }
        }
    }

    public long getCharge(ItemStack stack) {
        if (stack.getItem() == ModItems.battery_creative) return Long.MAX_VALUE;
        if (stack.getItem() instanceof ItemBattery) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setLong("charge", ((ItemBattery) stack.getItem()).maxCharge);
            }
            return stack.getTagCompound().getLong("charge");
        }

        return 0;
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        return maxCharge;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        return chargeRate;
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        return dischargeRate;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return stack.getItem() != ModItems.battery_creative;
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack stack) {
        return 1D - (double) getCharge(stack) / (double) getMaxCharge(stack);
    }

}
