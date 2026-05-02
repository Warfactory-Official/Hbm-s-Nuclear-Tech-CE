package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockConcreteColoredExt extends BlockEnumMeta<BlockConcreteColoredExt.EnumConcreteType> {

    public BlockConcreteColoredExt(Material material, SoundType type, String name, EnumConcreteType[] enumValues, boolean multiName, boolean multiTex) {
        super(material, type, name, enumValues, multiName, multiTex);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        BlockBakeFrame[] frames = new BlockBakeFrame[EnumConcreteType.VALUES.length];
        for (EnumConcreteType type : EnumConcreteType.VALUES) {
            String name = registryName + "." + type.name().toLowerCase(Locale.US);
            if (type == EnumConcreteType.MACHINE_STRIPE) {
                String machine = registryName + "." + EnumConcreteType.MACHINE.name().toLowerCase(Locale.US);
                frames[type.ordinal()] = BlockBakeFrame.cube(
                        machine, // up
                        machine, // down
                        name,  // north
                        name,  // south
                        name,  // west
                        name   // east
                );
            } else {
                frames[type.ordinal()] = BlockBakeFrame.cubeAll(name);
            }
        }
        return frames;
    }

    public enum EnumConcreteType {
        MACHINE,
        MACHINE_STRIPE,
        INDIGO,
        PURPLE,
        PINK,
        HAZARD,
        SAND,
        BRONZE;

        public static final EnumConcreteType[] VALUES = values();
    }
}
