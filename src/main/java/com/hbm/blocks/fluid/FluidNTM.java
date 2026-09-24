package com.hbm.blocks.fluid;

import com.hbm.Tags;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;

public class FluidNTM extends Fluid {

    public FluidType type;

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

    public FluidNTM(String name, ResourceLocation stillName, ResourceLocation flowingName, int color, FluidType type) {
        super(name, stillName, flowingName, color);
        this.type = type;
    }

    @Override
    public String getUnlocalizedName() {
        if (this.type != null) {
            return this.type.getTranslationKey();
        }
        return "hbmfluid." + this.getName();
    }

    @Override
    public String getLocalizedName(FluidStack stack) {
        if (this.type != null) {
            String name = this.type.getConditionalName();
            if (!name.equals(this.type.getTranslationKey())) {
                return name;
            }
        }
        return super.getLocalizedName(stack);
    }
}