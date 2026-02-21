package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockConcreteColored extends BlockEnumMeta<EnumDyeColor> {
    public BlockConcreteColored() {
        super(Material.ROCK, SoundType.STONE, "concrete_colored", EnumDyeColor.META_LOOKUP, true, true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        BlockBakeFrame[] frames = new BlockBakeFrame[16];
        for (int meta = 0; meta < 16; meta++) {
            String color = EnumDyeColor.byMetadata(meta).getName();
            frames[meta] = new BlockBakeFrame("concrete_" + color);
        }
        return frames;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int meta = this.getMetaFromState(state);
        return MapColor.BLOCK_COLORS[meta];
    }
}
