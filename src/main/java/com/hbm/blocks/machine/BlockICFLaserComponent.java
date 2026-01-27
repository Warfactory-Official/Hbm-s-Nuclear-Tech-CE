package com.hbm.blocks.machine;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockICFLaserComponent extends BlockEnumMeta<BlockICFLaserComponent.EnumICFPart> {

    public BlockICFLaserComponent() {
        super(Material.IRON, SoundType.METAL, "icf_laser_component", EnumICFPart.VALUES, true, true);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        EnumICFPart[] parts = EnumICFPart.VALUES;
        BlockBakeFrame[] frames = new BlockBakeFrame[parts.length];

        for (EnumICFPart part : parts) {
            switch (part) {
                case CASING -> frames[part.ordinal()] = new BlockBakeFrame("icf_casing");
                case PORT -> frames[part.ordinal()] = new BlockBakeFrame("icf_port");
                case CELL -> frames[part.ordinal()] = new BlockBakeFrame("icf_cell");
                case EMITTER -> frames[part.ordinal()] = new BlockBakeFrame("icf_emitter");
                case CAPACITOR -> frames[part.ordinal()] = new BlockBakeFrame("icf_capacitor_top", "icf_capacitor_side");
                case TURBO -> frames[part.ordinal()] = new BlockBakeFrame("icf_capacitor_top", "icf_turbocharger");
            }
        }
        return frames;
    }

    public enum EnumICFPart {
        CASING,
        PORT,
        CELL,
        EMITTER,
        CAPACITOR,
        TURBO;

        public static final EnumICFPart[] VALUES = values();
    }
}