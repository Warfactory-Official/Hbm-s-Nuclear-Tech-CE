package com.hbm.interfaces;

import com.hbm.items.IDynamicModels;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHasCustomModel extends IDynamicModels {

    public ModelResourceLocation getResourceLocation();


    //Note: this was never used of anything but items
    @SideOnly(Side.CLIENT)
    default void registerModel() {
        if (this instanceof Item item)
            ModelLoader.setCustomModelResourceLocation(item, 0, getResourceLocation());
    }

    ;

}
