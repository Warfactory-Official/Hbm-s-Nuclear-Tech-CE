package com.hbm.render.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

public class SimpleStateMapper extends StateMapperBase {
    private final ModelResourceLocation location;

    public SimpleStateMapper(ModelResourceLocation location) {
        this.location = location;
    }

    public SimpleStateMapper(ResourceLocation rl) {
        this(new ModelResourceLocation(rl, "normal"));
    }

    public SimpleStateMapper(ResourceLocation rl, String variant) {
        this(new ModelResourceLocation(rl, variant));
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        return location;
    }
}
