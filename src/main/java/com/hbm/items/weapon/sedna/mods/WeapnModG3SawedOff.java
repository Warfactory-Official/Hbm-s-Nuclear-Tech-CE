package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.XFactory556mm;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna.GunAnimation;
import net.minecraft.item.ItemStack;

import java.util.Objects;
import java.util.function.BiFunction;

public class WeapnModG3SawedOff extends WeaponModBase {

	public WeapnModG3SawedOff(int id) {
		super(id, "SHIELD");
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {
		if(Objects.equals(key, GunConfig.I_DRAWDURATION)) return cast(5, base);
		if(Objects.equals(key, GunConfig.FUN_ANIMNATIONS)) return (T) LAMBDA_G3_ANIMS;
		return base;
	}
	
	@SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, GunAnimation, BusAnimationSedna> LAMBDA_G3_ANIMS = (stack, type) -> {
		if (Objects.requireNonNull(type) == GunAnimation.EQUIP) {
			return new BusAnimationSedna().addBus("EQUIP", new BusAnimationSequenceSedna().addPos(45, 0, 0, 0).addPos(0, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_FULL));
		}
		return XFactory556mm.LAMBDA_G3_ANIMS.apply(stack, type);
	};
}
