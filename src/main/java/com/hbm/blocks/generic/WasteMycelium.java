package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// foont: todo:  should be modified when plantableOn is implemented
public class WasteMycelium extends WasteEarth {

    public WasteMycelium(Material materialIn, SoundType type, boolean tick, String s) {
        super(materialIn, type, tick, s);
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        if (GeneralConfig.enableMycelium) {
            this.spread(world, pos, rand);
        }
        super.updateTick(world, pos, state, rand);
    }

    public void spread(World world, BlockPos pos, Random rand) {
        List<BlockPos> validPositions = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    BlockPos adjacentBlockPos = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    Block adjB = world.getBlockState(adjacentBlockPos).getBlock();
                    IBlockState aboveAdjacentBlockState = world.getBlockState(adjacentBlockPos.up());
                    if (!aboveAdjacentBlockState.isOpaqueCube() &&
                            (adjB == Blocks.DIRT || adjB == Blocks.GRASS || adjB == Blocks.MYCELIUM || adjB == ModBlocks.waste_earth)) {
                        validPositions.add(adjacentBlockPos);
                    }
                }
            }
        }
        if (!validPositions.isEmpty()) {
            BlockPos targetPos = validPositions.get(rand.nextInt(validPositions.size()));
            world.setBlockState(targetPos, this.getDefaultState());
        }
    }
}
