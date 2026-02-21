package com.hbm.blocks.fluid;

import com.hbm.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

public class FluidNTM extends Fluid {
    public FluidNTM(String name, String stillName, String flowingName, Color color) {
        super(
                name,
                new ResourceLocation(Tags.MODID, "blocks/" + stillName),
                new ResourceLocation(Tags.MODID, "blocks/" + flowingName),
                color);
    }

    public FluidNTM(
            String name, ResourceLocation stillName, ResourceLocation flowingName, int color) {
        super(name, stillName, flowingName, color);
    }

    public FluidNTM(String name, String stillName, String flowingName) {
        this(name, stillName, flowingName, Color.white);
    }

    public String getUnlocalizedName() {
        return "hbmfluid." + this.getName();
    }
}
