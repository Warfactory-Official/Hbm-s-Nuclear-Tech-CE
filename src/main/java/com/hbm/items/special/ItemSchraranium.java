package com.hbm.items.special;

import com.hbm.config.GeneralConfig;
import com.hbm.items.ItemBase;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ItemSchraranium extends ItemBase {

    public ItemSchraranium(String s) {
        super(s);
        this.addPropertyOverride(new ResourceLocation("nikonium"), (_, _, _) -> GeneralConfig.enableLBSM && GeneralConfig.enableLBSMFullSchrab ? 1 : 0);
    }

    @Override
    public @NotNull String getItemStackDisplayName(ItemStack stack) {
        if (GeneralConfig.enableLBSM && GeneralConfig.enableLBSMFullSchrab) return I18nUtil.resolveKey("item.ingot_nikonium.name");
        else return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flagIn) {
        if (GeneralConfig.enableLBSM && GeneralConfig.enableLBSMFullSchrab) list.add("pankæk");
    }
}
