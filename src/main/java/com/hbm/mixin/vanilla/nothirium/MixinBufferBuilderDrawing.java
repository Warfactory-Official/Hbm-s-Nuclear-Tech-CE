package com.hbm.mixin.vanilla.nothirium;

import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface MixinBufferBuilderDrawing {

    @Accessor("isDrawing")
    boolean hbm$isDrawing();
}
