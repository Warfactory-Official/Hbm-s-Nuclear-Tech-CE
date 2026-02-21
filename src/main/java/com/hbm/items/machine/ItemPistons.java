package com.hbm.items.machine;

import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPistons extends ItemEnumMulti<ItemPistons.EnumPistonType> {

  public ItemPistons(String name) {
    super(name, EnumPistonType.VALUES, true, true);
  }

  @Override
  public void addInformation(
      ItemStack stack,
      @Nullable World worldIn,
      List<String> tooltip,
      @NotNull ITooltipFlag flagIn) {
    EnumPistonType type = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());

    tooltip.add(TextFormatting.YELLOW + "Fuel efficiency:");
    for (int i = 0; i < type.eff.length; i++) {
      tooltip.add(
          TextFormatting.YELLOW
              + "-"
              + I18nUtil.resolveKey(FT_Combustible.FuelGrade.VALUES[i].getGrade())
              + ": "
              + TextFormatting.RED
              + (int) (type.eff[i] * 100)
              + "%");
    }
  }

  public enum EnumPistonType {
    STEEL(1.00, 0.75, 0.25, 0.00, 0.00),
    DURA(0.50, 1.00, 0.90, 0.50, 0.00),
    DESH(0.00, 0.50, 1.00, 0.75, 0.00),
    STARMETAL(0.50, 0.75, 1.00, 0.90, 0.50);

    public static final EnumPistonType[] VALUES = values();

    public final double[] eff;

    EnumPistonType(double... eff) {
      this.eff = new double[Math.min(FT_Combustible.FuelGrade.VALUES.length, eff.length)];
      System.arraycopy(eff, 0, this.eff, 0, eff.length);
    }
  }
}
