package com.hbm.render.item;

import net.minecraft.item.Item;

public abstract class ItemRenderBaseFMM extends ItemRenderBase {

    @Override
    public boolean useFMMPerspective(Item item) {
        return true;
    }
}
