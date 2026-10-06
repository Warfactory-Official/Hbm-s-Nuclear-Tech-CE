package com.hbm.mixin.mod.litematica;

import com.hbm.integration.litematica.LitematicaCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "fi.dy.masa.litematica.util.WorldUtils", remap = false)
public class MixinWorldUtils {

    @Inject(method = "doEasyPlaceAction", at = @At("HEAD"), cancellable = true, remap = false)
    private static void hbm$easyPlaceMultiblock(Minecraft mc, CallbackInfoReturnable<EnumActionResult> cir) {
        EnumActionResult result = LitematicaCompat.easyPlace(mc);
        if (result != null) cir.setReturnValue(result);
    }

    @Inject(method = "doEasyPlaceAction", at = @At("RETURN"), remap = false)
    private static void hbm$afterEasyPlace(Minecraft mc, CallbackInfoReturnable<EnumActionResult> cir) {
        LitematicaCompat.afterEasyPlace(cir.getReturnValue());
    }
}
