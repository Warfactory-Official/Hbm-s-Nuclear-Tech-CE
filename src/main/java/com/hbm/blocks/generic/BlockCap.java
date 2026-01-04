package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.BlockEnums;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockCap extends BlockEnumMeta {

    public BlockCap(String registryName) {
        super(Material.IRON, SoundType.METAL, registryName, BlockEnums.EnumBlockCapType.class, true, true);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        return Arrays.stream(blockEnum.getEnumConstants())
                .sorted(Comparator.comparing(Enum::ordinal))
                .map(Enum::name)
                .map(name -> registryName + "_" + name.toLowerCase(Locale.US))
                .map(texture -> new BlockBakeFrame(texture + "_top", texture))
                .toArray(BlockBakeFrame[]::new);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int meta = state.getValue(META);
        BlockEnums.EnumBlockCapType oreType = (BlockEnums.EnumBlockCapType) this.blockEnum.getEnumConstants()[meta];

        return Collections.singletonList(new ItemStack(oreType.getDrop(), oreType.getDropCount()));
    }
}
