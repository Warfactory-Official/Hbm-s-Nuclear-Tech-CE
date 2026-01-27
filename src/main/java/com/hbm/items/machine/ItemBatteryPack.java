package com.hbm.items.machine;

import com.hbm.Tags;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.IMetaItemTesr;
import com.hbm.main.MainRegistry;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemBatteryPack extends ItemEnumMulti<ItemBatteryPack.EnumBatteryPack> implements IBatteryItem, IMetaItemTesr {

    public ItemBatteryPack(String s) {
        super(s, EnumBatteryPack.VALUES, true, false);
        this.setMaxStackSize(1);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setHasSubtypes(true);
        IMetaItemTesr.INSTANCES.add(this);
    }

    public static ItemStack makeEmptyBattery(ItemStack stack) {
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setLong("charge", 0);
        return stack;
    }

    public static ItemStack makeFullBattery(ItemStack stack) {
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setLong("charge", ((ItemBatteryPack) stack.getItem()).getMaxCharge(stack));
        return stack;
    }

    @Override
    public void chargeBattery(ItemStack stack, long i) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") + i);
        } else {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("charge", i);
        }
    }

    @Override
    public void setCharge(ItemStack stack, long i) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setLong("charge", i);
        } else {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("charge", i);
        }
    }

    @Override
    public void dischargeBattery(ItemStack stack, long i) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") - i);
        } else {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("charge", 0);
        }
    }

    @Override
    public long getCharge(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("charge", 0);
        }
        return stack.getTagCompound().getLong("charge");
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        EnumBatteryPack pack = EnumUtil.grabEnumSafely(EnumBatteryPack.VALUES, stack.getItemDamage());
        return pack.capacity;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        EnumBatteryPack pack = EnumUtil.grabEnumSafely(EnumBatteryPack.VALUES, stack.getItemDamage());
        return pack.chargeRate;
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        EnumBatteryPack pack = EnumUtil.grabEnumSafely(EnumBatteryPack.VALUES, stack.getItemDamage());
        return pack.dischargeRate;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) != 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - (double) getCharge(stack) / (double) getMaxCharge(stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        long maxCharge = this.getMaxCharge(stack);
        long chargeRate = this.getChargeRate(stack);
        long dischargeRate = this.getDischargeRate(stack);
        long charge = maxCharge;

        if (stack.hasTagCompound()) charge = getCharge(stack);

        tooltip.add(TextFormatting.GREEN + "Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE (" + (charge * 1000 / maxCharge / 10D) + "%)");
        tooltip.add(TextFormatting.YELLOW + "Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t");
        tooltip.add(TextFormatting.YELLOW + "Discharge rate: " + BobMathUtil.getShortNumber(dischargeRate) + "HE/t");
        tooltip.add(TextFormatting.GOLD + "Time for full charge: " + (maxCharge / chargeRate / 20 / 60D) + "min");
        tooltip.add(TextFormatting.GOLD + "Charge lasts for: " + (maxCharge / dischargeRate / 20 / 60D) + "min");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            /* mlbv: code below is useless since EnumBatteryPack doesn't implement IOrderedEnum
            Enum[] order = theEnum.getEnumConstants();
            if (order[0] instanceof IOrderedEnum) order = ((IOrderedEnum) order[0]).getOrder();
            */
            for (EnumBatteryPack anEnum : theEnum) {
                items.add(makeEmptyBattery(new ItemStack(this, 1, anEnum.ordinal())));
                items.add(makeFullBattery(new ItemStack(this, 1, anEnum.ordinal())));
            }
        }
    }

    public enum EnumBatteryPack {
        BATTERY_REDSTONE("battery_redstone", 100L, false),
        BATTERY_LEAD("battery_lead", 1_000L, false),
        BATTERY_LITHIUM("battery_lithium", 10_000L, false),
        BATTERY_SODIUM("battery_sodium", 50_000L, false),
        BATTERY_SCHRABIDIUM("battery_schrabidium", 250_000L, false),
        BATTERY_QUANTUM("battery_quantum", 1_000_000L, 20 * 60 * 60),

        CAPACITOR_COPPER("capacitor_copper", 1_000L, true),
        CAPACITOR_GOLD("capacitor_gold", 10_000L, true),
        CAPACITOR_NIOBIUM("capacitor_niobium", 100_000L, true),
        CAPACITOR_TANTALUM("capacitor_tantalum", 500_000L, true),
        CAPACITOR_BISMUTH("capacitor_bismuth", 2_500_000L, true),
        CAPACITOR_SPARK("capacitor_spark", 10_000_000L, true);

        public final ResourceLocation texture;
        public final long capacity;
        public final long chargeRate;
        public final long dischargeRate;
        public static final EnumBatteryPack[] VALUES = values();

        EnumBatteryPack(String tex, long dischargeRate, boolean capacitor) {
            this(tex,
                    capacitor ? (dischargeRate * 20 * 30) : (dischargeRate * 20 * 60 * 15),
                    capacitor ? dischargeRate : dischargeRate * 10,
                    dischargeRate);
        }

        EnumBatteryPack(String tex, long dischargeRate, long duration) {
            this(tex, dischargeRate * duration, dischargeRate * 10, dischargeRate);
        }

        EnumBatteryPack(String tex, long capacity, long chargeRate, long dischargeRate) {
            this.texture = new ResourceLocation(Tags.MODID, "textures/models/machines/" + tex + ".png");
            this.capacity = capacity;
            this.chargeRate = chargeRate;
            this.dischargeRate = dischargeRate;
        }

        public boolean isCapacitor() {
            return this.ordinal() > BATTERY_QUANTUM.ordinal();
        }

        public ItemStack stack() {
            return new ItemStack(ModItems.battery_pack, 1, this.ordinal());
        }
    }

    @Override
    public String getResourceLocationAsString() {
        return getRegistryName().toString();
    }

    @Override
    public int getSubitemCount() {
        return EnumBatteryPack.VALUES.length;
    }

    // Th3_Sl1ze: why from i = 1, mov?
    @Override
    public void redirectModel() {
        for (int i = 0; i < getSubitemCount(); i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(getResourceLocationAsString(), "inventory"));
        }
    }
}
