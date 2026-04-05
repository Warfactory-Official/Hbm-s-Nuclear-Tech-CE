package com.hbm.render.chunk;

import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

public interface IRenderExtentsOverride {

    int @Nullable [] getRenderExtentsOverride(IBlockState state);
}
