package com.hbm.mixin;

import com.hbm.render.chunk.ISectionGeometryState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockStateContainer.StateImplementation.class)
public abstract class MixinStateImplementation implements ISectionGeometryState {

    @Unique
    private boolean hbm$sectioned;

    @Override
    public boolean hbm$sectioned() {
        return hbm$sectioned;
    }

    @Override
    public void hbm$sectioned(boolean sectioned) {
        hbm$sectioned = sectioned;
    }

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private EnumBlockRenderType hbm$sectionedRenderType(EnumBlockRenderType original) {
        return hbm$sectioned ? EnumBlockRenderType.INVISIBLE : original;
    }
}
