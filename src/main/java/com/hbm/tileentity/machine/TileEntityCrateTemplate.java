package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.Tags;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class TileEntityCrateTemplate extends TileEntityCrate {

    public TileEntityCrateTemplate() {
        super(27, "container.crateTemplate", 9, 3, 8, 18, 8, 86, 144, 176, 168, 8, 4210752, 4210752,
                new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_template.png"));
    }
}
