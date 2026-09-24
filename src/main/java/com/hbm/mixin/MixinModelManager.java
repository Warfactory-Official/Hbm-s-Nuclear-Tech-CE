package com.hbm.mixin;

import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.resources.IResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelManager.class)
public abstract class MixinModelManager {

    @Shadow
    public abstract BlockModelShapes getBlockModelShapes();

    // RETURN: after reloadModels, so other mods' bake-event wrappers are captured too.
    @Inject(method = "onResourceManagerReload", at = @At("RETURN"))
    private void hbm$publishSectionGeometry(IResourceManager manager, CallbackInfo ci) {
        SectionGeometry.modelsReloaded(getBlockModelShapes());
    }
}
