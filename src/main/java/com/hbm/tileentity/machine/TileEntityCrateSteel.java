package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.Tags;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class TileEntityCrateSteel extends TileEntityCrate {

    public TileEntityCrateSteel() {
        super(54, "container.crateSteel", 9, 6, 8, 18, 8, 140, 198, 176, 222, 8, 0x1C1C1C, 0x1C1C1C,
                new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_steel.png"));
    }
}
