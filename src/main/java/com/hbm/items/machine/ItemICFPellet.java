package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ItemICFPellet extends Item {
    public static HashMap<FluidType, EnumICFFuel> fluidMap = new HashMap<>();
    public static HashMap<NTMMaterial, EnumICFFuel> materialMap = new HashMap<>();
    public ItemICFPellet(String s) {
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModItems.ALL_ITEMS.add(this);
    }

    public static void init() {
        if (!fluidMap.isEmpty() && !materialMap.isEmpty()) return;
        fluidMap.put(Fluids.HYDROGEN, EnumICFFuel.HYDROGEN);
        fluidMap.put(Fluids.DEUTERIUM, EnumICFFuel.DEUTERIUM);
        fluidMap.put(Fluids.TRITIUM, EnumICFFuel.TRITIUM);
        fluidMap.put(Fluids.HELIUM3, EnumICFFuel.HELIUM3);
        fluidMap.put(Fluids.HELIUM4, EnumICFFuel.HELIUM4);
        materialMap.put(Mats.MAT_LITHIUM, EnumICFFuel.LITHIUM);
        materialMap.put(Mats.MAT_BERYLLIUM, EnumICFFuel.BERYLLIUM);
        materialMap.put(Mats.MAT_BORON, EnumICFFuel.BORON);
        materialMap.put(Mats.MAT_GRAPHITE, EnumICFFuel.CARBON);
        fluidMap.put(Fluids.OXYGEN, EnumICFFuel.OXYGEN);
        materialMap.put(Mats.MAT_SODIUM, EnumICFFuel.SODIUM);
        fluidMap.put(Fluids.CHLORINE, EnumICFFuel.CHLORINE);
        materialMap.put(Mats.MAT_CALCIUM, EnumICFFuel.CALCIUM);
    }

    @Contract(pure = true)
    public static long getMaxDepletion(ItemStack stack) {
        double base = 50_000_000_000L;
        base /= getType(stack, true).depletionSpeed;
        base /= getType(stack, false).depletionSpeed;
        return (long) base;
    }

    @Contract(pure = true)
    public static long getFusingDifficulty(ItemStack stack) {
        double base = 10_000_000L;
        base *= getType(stack, true).fusingDifficulty * getType(stack, false).fusingDifficulty;
        if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("muon")) base /= 4;
        return (long) base;
    }

    @Contract(pure = true)
    public static long getDepletion(ItemStack stack) {
        if (!stack.hasTagCompound()) return 0L;
        return stack.getTagCompound().getLong("depletion");
    }

    public static long react(ItemStack stack, long heat) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = stack.getTagCompound();
        nbt.setLong("depletion", nbt.getLong("depletion") + heat);
        return (long) (heat * getType(stack, true).reactionMult * getType(stack, false).reactionMult);
    }

    public static ItemStack setup(EnumICFFuel type1, EnumICFFuel type2, boolean muon) {
        return setup(new ItemStack(ModItems.icf_pellet), type1, type2, muon);
    }

    public static ItemStack setup(ItemStack stack, EnumICFFuel type1, EnumICFFuel type2, boolean muon) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = stack.getTagCompound();
        nbt.setByte("type1", (byte) type1.ordinal());
        nbt.setByte("type2", (byte) type2.ordinal());
        nbt.setBoolean("muon", muon);
        return stack;
    }

    @Contract(pure = true)
    public static EnumICFFuel getType(ItemStack stack, boolean first) {
        if (!stack.hasTagCompound()) return first ? EnumICFFuel.DEUTERIUM : EnumICFFuel.TRITIUM;
        return EnumUtil.grabEnumSafely(EnumICFFuel.VALUES, stack.getTagCompound().getByte("type" + (first ? 1 : 2)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(setup(EnumICFFuel.DEUTERIUM, EnumICFFuel.TRITIUM, false));
            items.add(setup(EnumICFFuel.HELIUM3, EnumICFFuel.HELIUM4, false));
            items.add(setup(EnumICFFuel.LITHIUM, EnumICFFuel.OXYGEN, false));
            items.add(setup(EnumICFFuel.SODIUM, EnumICFFuel.CHLORINE, true));
            items.add(setup(EnumICFFuel.BERYLLIUM, EnumICFFuel.CALCIUM, true));
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) > 0D;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return (double) getDepletion(stack) / (double) getMaxDepletion(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        boolean muon = stack.hasTagCompound() && stack.getTagCompound().getBoolean("muon");
        tooltip.add(TextFormatting.GREEN + "Depletion: " + String.format(Locale.US, "%.1f", getDurabilityForDisplay(stack) * 100D) + "%");
        tooltip.add(TextFormatting.YELLOW + "Fuel: " + I18nUtil.resolveKey("icffuel." + getType(stack, true).name().toLowerCase(Locale.US)) +
                " / " + I18nUtil.resolveKey("icffuel." + getType(stack, false).name().toLowerCase(Locale.US)));
        tooltip.add(TextFormatting.YELLOW + "Heat required: " + BobMathUtil.getShortNumber(getFusingDifficulty(stack)) + "TU");
        tooltip.add(TextFormatting.YELLOW + "Reactivity multiplier: x" + (int) (getType(stack, true).reactionMult * getType(stack, false).reactionMult * 100) / 100D);
        if (muon) tooltip.add(TextFormatting.DARK_AQUA + "Muon catalyzed!");
    }

    public enum EnumICFFuel {

        HYDROGEN(0x4040FF, 1.00D, 0.85D, 1.00D),
        DEUTERIUM(0x2828CB, 1.25D, 1.00D, 1.00D),
        TRITIUM(0x000092, 1.50D, 1.00D, 1.05D),
        HELIUM3(0xFFF09F, 1.75D, 1.00D, 1.25D),
        HELIUM4(0xFF9B60, 2.00D, 1.00D, 1.50D),
        LITHIUM(0xE9E9E9, 1.25D, 0.85D, 2.00D),
        BERYLLIUM(0xA79D80, 2.00D, 1.00D, 2.50D),
        BORON(0x697F89, 3.00D, 0.50D, 3.50D),
        CARBON(0x454545, 2.00D, 1.00D, 5.00D),
        OXYGEN(0xB4E2FF, 1.25D, 1.50D, 7.50D),
        SODIUM(0xDFE4E7, 3.00D, 0.75D, 8.75D),
        //aluminium, silicon, phosphorus
        CHLORINE(0xDAE598, 2.50D, 1.00D, 10.0D),
        CALCIUM(0xD2C7A9, 3.00D, 1.00D, 12.5D),
        //titanium
        ;

        public static final EnumICFFuel[] VALUES = values();

        public final int color;
        public final double reactionMult;
        public final double depletionSpeed;
        public final double fusingDifficulty;

        EnumICFFuel(int color, double react, double depl, double laser) {
            this.color = color;
            this.reactionMult = react;
            this.depletionSpeed = depl;
            this.fusingDifficulty = laser;
        }
    }
}
