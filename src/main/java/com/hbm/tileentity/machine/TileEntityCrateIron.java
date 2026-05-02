package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.Tags;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class TileEntityCrateIron extends TileEntityCrate {

    public TileEntityCrateIron() {
        super(36, "container.crateIron", 9, 4, 8, 18, 8, 104, 162, 176, 186, 8, 4210752, 4210752,
                new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_iron.png"));
    }
}
