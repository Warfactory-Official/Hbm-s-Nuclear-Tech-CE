package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.item.ItemStack;

public interface IPAMelee {

    void setupFirstPerson(ItemStack stack);
    void renderFirstPerson(ItemStack stack);

    BusAnimationSedna playAnim(ItemStack stack, HbmAnimationsSedna.GunAnimation type);
    void orchestra(ItemStack stack, ItemGunBaseNT.LambdaContext ctx);

    void clickPrimary(ItemStack stack, ItemGunBaseNT.LambdaContext ctx);
    void clickSecondary(ItemStack stack, ItemGunBaseNT.LambdaContext ctx);
}