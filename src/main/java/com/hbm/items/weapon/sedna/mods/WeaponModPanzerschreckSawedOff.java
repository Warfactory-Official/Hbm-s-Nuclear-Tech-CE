package com.hbm.items.weapon.sedna.mods;

import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.Lego;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna.GunAnimation;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.item.ItemStack;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class WeaponModPanzerschreckSawedOff extends WeaponModBase {

	public WeaponModPanzerschreckSawedOff(int id) {
		super(id, "SHIELD");
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {
		if(Objects.equals(key, GunConfig.I_DRAWDURATION)) return cast(5, base);
		if(Objects.equals(key, Receiver.CON_ONFIRE)) { return (T) LAMBDA_FIRE; }
		return base;
	}

	@SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, GunAnimation, BusAnimationSedna> LAMBDA_PANZERSCHRECK_ANIMS = (stack, type) -> {
		if (Objects.requireNonNull(type) == GunAnimation.EQUIP) {
			return new BusAnimationSedna().addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_DOWN));
		}
		return XFactoryRocket.LAMBDA_PANZERSCHRECK_ANIMS.apply(stack, type);
	};
	
	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_FIRE = (stack, ctx) -> {
		Lego.LAMBDA_STANDARD_FIRE.accept(stack, ctx);
		if(ctx.entity != null) {
			HbmLivingCapability.IEntityHbmProps props = HbmLivingProps.getData(ctx.entity);
			props.setFire(props.getFire() + 100);
			EntityDamageUtil.attackEntityFromNT(ctx.entity, BulletConfig.getDamage(ctx.entity, ctx.entity, DamageClass.FIRE), 4F, true, false, 0F, 0F, 0F);
		}
	};
}
