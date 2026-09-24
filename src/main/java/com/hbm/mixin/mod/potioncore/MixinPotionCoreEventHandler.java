package com.hbm.mixin.mod.potioncore;

import com.hbm.main.MainRegistry;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.tmtravlr.potioncore.PotionCoreEventHandler", remap = false)
public class MixinPotionCoreEventHandler {

    @WrapMethod(method = "onLivingHurt", remap = false)
    private static void hbm$guardOnLivingHurt(LivingHurtEvent event, Operation<Void> original) {
        try {
            original.call(event);
        } catch (Exception e) {
            MainRegistry.logger.error("Potion Core's onLivingHurt threw for '" + event.getSource().getDamageType() + "' damage; suppressing so the hit still applies", e);
        }
    }
}
