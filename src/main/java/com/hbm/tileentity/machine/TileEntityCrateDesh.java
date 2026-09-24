package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.Tags;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class TileEntityCrateDesh extends TileEntityCrate {

    public TileEntityCrateDesh() {
        super(104, "container.crateDesh", 13, 8, 8, 18, 44, 174, 232, 248, 256, 44, 0x3F1515, 0x3F1515,
                new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_desh.png"));
    }
}
