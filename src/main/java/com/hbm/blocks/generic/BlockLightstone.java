package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

import java.util.Locale;
// TODO: lightstone slabs/stairs
public class BlockLightstone<E extends Enum<E>> extends BlockEnumMeta<E> {

    public BlockLightstone(Material mat, SoundType type, String registryName, E[] blockEnum, boolean multiName, boolean multiTexture) {
        super(mat, type, registryName, blockEnum, multiName, multiTexture);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        E[] values = this.blockEnum;
        BlockBakeFrame[] frames = new BlockBakeFrame[values.length];

        for (E e : values) {
            int i = e.ordinal();
            String base = registryName + "." + e.name().toLowerCase(Locale.US);

            if (i >= 3) {
                frames[i] = new BlockBakeFrame(BlockBakeFrame.BlockForm.FULL_CUSTOM,
                        base + ".top", // up
                        base + ".top", // down
                        base,          // north
                        base,          // south
                        base,          // west
                        base           // east
                );
            } else {
                frames[i] = new BlockBakeFrame(base);
            }
        }
        return frames;
    }
}
