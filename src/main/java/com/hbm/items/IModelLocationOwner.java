package com.hbm.items;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModelLocationOwner {

    @SideOnly(Side.CLIENT)
    boolean ownsModelLocation(ModelResourceLocation location);

    @SideOnly(Side.CLIENT)
    default IModel loadModel(ModelResourceLocation location) {
        return new ItemLayerModel(ImmutableList.of(new ResourceLocation(location.getNamespace(), location.getPath())));
    }
}
