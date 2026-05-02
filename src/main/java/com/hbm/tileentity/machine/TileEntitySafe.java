package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.Tags;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class TileEntitySafe extends TileEntityCrate {
    public TileEntitySafe() {
        super(15, "container.safe", 5, 3, 44, 18, 8, 86, 144, 176, 168, 8, 4210752, 4210752,
                new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_safe.png"));
    }
}
