package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.PlantEnums;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDeadPlant extends BlockPlantEnumMeta {

    public BlockDeadPlant(String registryName) {
        super(registryName, PlantEnums.EnumDeadPlantType.class);

    }

    public static void initPlacables() {
        PLANTABLE_BLOCKS.add(ModBlocks.waste_earth);
        PLANTABLE_BLOCKS.add(ModBlocks.dirt_oily);
        PLANTABLE_BLOCKS.add(ModBlocks.dirt_dead);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

}