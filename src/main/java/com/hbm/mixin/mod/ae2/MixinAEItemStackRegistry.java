package com.hbm.mixin.mod.ae2;

import com.hbm.integration.ae2.AESharedStackInterner;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "appeng.util.item.AEItemStackRegistry", remap = false)
public abstract class MixinAEItemStackRegistry {

    @Inject(method = "getRegisteredStack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void hbm$getRegisteredStack(ItemStack stack, CallbackInfoReturnable<Object> cir) {
        if (stack.isEmpty()) return;
        Object shared = AESharedStackInterner.intern(stack);
        if (shared != null) cir.setReturnValue(shared);
    }
}
