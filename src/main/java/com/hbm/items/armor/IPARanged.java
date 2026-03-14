package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import net.minecraft.item.ItemStack;

public interface IPARanged {

    public void clickPrimary(ItemStack stack, ItemGunBaseNT.LambdaContext ctx);
    public void clickSecondary(ItemStack stack, ItemGunBaseNT.LambdaContext ctx);
}
