package com.hbm.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IClaimedModelLocation extends IModelLocationOwner {

    @SideOnly(Side.CLIENT)
    static boolean isInventoryLocation(ModelResourceLocation location, ResourceLocation resourceLocation) {
        return location.equals(new ModelResourceLocation(resourceLocation, "inventory"));
    }
}
