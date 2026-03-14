package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.factory.Orchestras;
import com.hbm.items.weapon.sedna.factory.XFactory44;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna.GunAnimation;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class WeaponModCarbineBayonet extends WeaponModBase {

	public WeaponModCarbineBayonet(int id) {
		super(id, "BAYONET");
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {
		if(Objects.equals(key, GunConfig.FUN_ANIMNATIONS)) return (T) LAMBDA_CARBINE_ANIMS;
		if(Objects.equals(key, GunConfig.I_INSPECTDURATION)) return cast(30, base);
		if(Objects.equals(key, GunConfig.CON_ONPRESSSECONDARY)) return (T) XFactory44.SMACK_A_FUCKER;
		if(Objects.equals(key, GunConfig.CON_ORCHESTRA)) return (T) ORCHESTRA_CARBINE;
		if(Objects.equals(key, GunConfig.I_INSPECTCANCEL)) return cast(false, base);
		return base;
	}
	
	public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_CARBINE = (stack, ctx) -> {
		EntityLivingBase entity = ctx.entity;
		if(entity.world.isRemote) return;
		GunAnimation type = ItemGunBaseNT.getLastAnim(stack, ctx.configIndex);
		int timer = ItemGunBaseNT.getAnimTimer(stack, ctx.configIndex);

		if(type == GunAnimation.INSPECT) {
			
			if(timer == 15 && ctx.getPlayer() != null) {
				RayTraceResult mop = EntityDamageUtil.getMouseOver(ctx.getPlayer(), 3.0D);
				if(mop != null) {
					if(mop.typeOfHit == mop.typeOfHit.ENTITY) {
						float damage = 15F;
						mop.entityHit.attackEntityFrom(DamageSource.causePlayerDamage(ctx.getPlayer()), damage);
						mop.entityHit.motionX *= 2;
						mop.entityHit.motionZ *= 2;
						// TODO SOUND
						entity.world.playSound(null, mop.entityHit.posX, mop.entityHit.posY, mop.entityHit.posZ, HBMSoundHandler.fireStab, SoundCategory.PLAYERS, 1F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
					}
					if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
						Block b = entity.world.getBlockState(mop.getBlockPos()).getBlock();
						entity.world.playSound(null, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, b.getSoundType().getStepSound(), SoundCategory.BLOCKS, 2F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
					}
				}
			}
			return;
		}
		
		Orchestras.ORCHESTRA_CARBINE.accept(stack, ctx);
	};

	@SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, GunAnimation, BusAnimationSedna> LAMBDA_CARBINE_ANIMS = (stack, type) -> {
		if (Objects.requireNonNull(type) == GunAnimation.INSPECT) {
			return new BusAnimationSedna()
					.addBus("STAB", new BusAnimationSequenceSedna().addPos(0, 1, -2, 250, IType.SIN_DOWN).hold(250).addPos(0, 1, 5, 250, IType.SIN_UP).hold(250).addPos(0, 0, 0, 500, IType.SIN_FULL));
		}
		
		return XFactory762mm.LAMBDA_CARBINE_ANIMS.apply(stack, type);
	};
}
