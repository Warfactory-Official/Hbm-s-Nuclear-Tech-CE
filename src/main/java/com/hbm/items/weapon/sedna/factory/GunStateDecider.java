package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

public class GunStateDecider {
    /**
     * The meat and bones of the gun system's state machine.
     * This standard decider can handle guns with an automatic primary receiver, as well as one receiver's reloading state.
     * It supports draw delays as well as semi and auto fire
     */
    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_STANDARD_DECIDER = (stack, ctx) -> {
        int index = ctx.configIndex;
        ItemGunBaseNT.GunState lastState = ItemGunBaseNT.getState(stack, index);
        deciderStandardFinishDraw(stack, lastState, index);
        deciderStandardClearJam(stack, lastState, index);
        deciderStandardReload(stack, ctx, lastState, 0, index);
        deciderAutoRefire(stack, ctx, lastState, 0, index, () -> { return ItemGunBaseNT.getPrimary(stack, index) && ItemGunBaseNT.getMode(stack, ctx.configIndex) == 0; });
    };

    /** Transitions the gun from DRAWING to IDLE */
    public static void deciderStandardFinishDraw(ItemStack stack, ItemGunBaseNT.GunState lastState, int index) {

        //transition to idle
        if(lastState == ItemGunBaseNT.GunState.DRAWING) {
            ItemGunBaseNT.setState(stack, index, ItemGunBaseNT.GunState.IDLE);
            ItemGunBaseNT.setTimer(stack, index, 0);
        }
    }

    /** Transitions the gun from DRAWING to IDLE */
    public static void deciderStandardClearJam(ItemStack stack, ItemGunBaseNT.GunState lastState, int index) {

        //transition to idle
        if(lastState == ItemGunBaseNT.GunState.JAMMED) {
            ItemGunBaseNT.setState(stack, index, ItemGunBaseNT.GunState.IDLE);
            ItemGunBaseNT.setTimer(stack, index, 0);
        }
    }

    /** Triggers a reload action on the first receiver. If the mag is not full and reloading is still possible, set to RELOADING, otherwise IDLE */
    public static void deciderStandardReload(ItemStack stack, ItemGunBaseNT.LambdaContext ctx, ItemGunBaseNT.GunState lastState, int recIndex, int gunIndex) {

        if(lastState == ItemGunBaseNT.GunState.RELOADING) {

            EntityLivingBase entity = ctx.entity;
            EntityPlayer player = ctx.getPlayer();
            GunConfig cfg = ctx.config;
            Receiver rec = cfg.getReceivers(stack)[recIndex];
            IMagazine mag = rec.getMagazine(stack);

            mag.reloadAction(stack, ctx.inventory);
            boolean cancel = ItemGunBaseNT.getReloadCancel(stack);

            //if after reloading the gun can still reload, assume a tube mag and resume reloading
            if(!cancel && mag.canReload(stack, ctx.inventory)) {
                ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.RELOADING);
                ItemGunBaseNT.setTimer(stack, gunIndex, rec.getReloadCycleDuration(stack));
                ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.GunAnimation.RELOAD_CYCLE, gunIndex);
                //if no more reloading can be done, go idle
            } else {

                if(getStandardJamChance(stack, cfg, gunIndex) > entity.getRNG().nextFloat()) {
                    ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.JAMMED);
                    ItemGunBaseNT.setTimer(stack, gunIndex, rec.getJamDuration(stack));
                    ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.GunAnimation.JAMMED, gunIndex);
                } else {
                    ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.DRAWING);
                    int duration = rec.getReloadEndDuration(stack) + (mag.getAmountBeforeReload(stack) <= 0 ? rec.getReloadCockOnEmptyPost(stack) : 0);
                    ItemGunBaseNT.setTimer(stack, gunIndex, duration);
                    ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.GunAnimation.RELOAD_END, gunIndex);
                }

                ItemGunBaseNT.setReloadCancel(stack, false);
            }

            mag.setAmountAfterReload(stack, mag.getAmount(stack, ctx.inventory));
        }
    }

    public static float getStandardJamChance(ItemStack stack, GunConfig config, int index) {
        float percent = (float) ItemGunBaseNT.getWear(stack, index) / config.getDurability(stack);
        if(percent < 0.66F) return 0F;
        return Math.min((percent - 0.66F) * 4F, 1F);
    }

    /** Triggers a re-fire of the primary if the fire delay has expired, the left mouse button is down and re-firing is enabled, otherwise switches to IDLE */
    public static void deciderAutoRefire(ItemStack stack, ItemGunBaseNT.LambdaContext ctx, ItemGunBaseNT.GunState lastState, int recIndex, int gunIndex, BooleanSupplier refireCondition) {

        if(lastState == ItemGunBaseNT.GunState.COOLDOWN) {

            EntityLivingBase entity = ctx.entity;
            EntityPlayer player = ctx.getPlayer();
            GunConfig cfg = ctx.config;
            Receiver rec = cfg.getReceivers(stack)[recIndex];

            //if the gun supports re-fire (i.e. if it's an auto)
            if(rec.getRefireOnHold(stack) && refireCondition.getAsBoolean()) {
                //if there's a bullet loaded, fire again
                if(rec.getCanFire(stack).apply(stack, ctx)) {
                    rec.getOnFire(stack).accept(stack, ctx);
                    ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.COOLDOWN);
                    ItemGunBaseNT.setTimer(stack, gunIndex, rec.getDelayAfterFire(stack));

                    if(rec.getFireSound(stack) != null) entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, rec.getFireSound(stack), SoundCategory.PLAYERS, rec.getFireVolume(stack), rec.getFirePitch(stack));

                    int remaining = rec.getRoundsPerCycle(stack) - 1;
                    for(int i = 0; i < remaining; i++) if(rec.getCanFire(stack).apply(stack, ctx)) rec.getOnFire(stack).accept(stack, ctx);
                    //if not, check if dry firing is allowed for refires
                } else if(rec.getDoesDryFireAfterAuto(stack)) {
                    //if refire after dry is allowed, switch to COOLDOWN which will trigger a refire, otherwise switch to DRAWING
                    ItemGunBaseNT.setState(stack, gunIndex, rec.getRefireAfterDry(stack) ? ItemGunBaseNT.GunState.COOLDOWN : ItemGunBaseNT.GunState.DRAWING);
                    ItemGunBaseNT.setTimer(stack, gunIndex, rec.getDelayAfterDryFire(stack));
                    ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.GunAnimation.CYCLE_DRY, gunIndex);
                    //if not, revert to idle
                } else {
                    ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.IDLE);
                    ItemGunBaseNT.setTimer(stack, gunIndex, 0);
                }
                //if not, go idle
            } else {

                //reload on empty, only for non-refiring guns
                if(rec.getReloadOnEmpty(stack) && rec.getMagazine(stack).getAmount(stack, ctx.inventory) <= 0) {
                    ItemGunBaseNT.setIsAiming(stack, false);
                    IMagazine mag = rec.getMagazine(stack);

                    if(mag.canReload(stack, ctx.inventory)) {
                        int loaded = mag.getAmount(stack, ctx.inventory);
                        mag.setAmountBeforeReload(stack, loaded);
                        ItemGunBaseNT.setState(stack, ctx.configIndex, ItemGunBaseNT.GunState.RELOADING);
                        ItemGunBaseNT.setTimer(stack, ctx.configIndex, rec.getReloadBeginDuration(stack) + (loaded <= 0 ? rec.getReloadCockOnEmptyPre(stack) : 0));
                        ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.GunAnimation.RELOAD, ctx.configIndex);
                    } else {
                        ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.IDLE);
                        ItemGunBaseNT.setTimer(stack, gunIndex, 0);
                    }

                } else {
                    ItemGunBaseNT.setState(stack, gunIndex, ItemGunBaseNT.GunState.IDLE);
                    ItemGunBaseNT.setTimer(stack, gunIndex, 0);
                }
            }
        }
    }
}
