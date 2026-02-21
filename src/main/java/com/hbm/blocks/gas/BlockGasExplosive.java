package com.hbm.blocks.gas;

import com.hbm.config.GeneralConfig;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class BlockGasExplosive extends BlockGasFlammable {
    private static final ThreadLocal<Boolean> isCombusting = ThreadLocal.withInitial(() -> false);

    public BlockGasExplosive(String s) {
        super(s);
    }

    @Override
    protected void combust(World world, BlockPos startPos) {
        if (isCombusting.get() || !GeneralConfig.enableExplosiveGas) return;
        isCombusting.set(true);
        try {
            Queue<BlockPos> processQueue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();

            if (world.getBlockState(startPos).getBlock() == this) {
                processQueue.offer(startPos);
                visited.add(startPos);
            }

            int processedCount = 0;
            while (!processQueue.isEmpty() && processedCount < 128) {
                BlockPos currentPos = processQueue.poll();
                processedCount++;
                world.setBlockState(currentPos, Blocks.FIRE.getDefaultState(), 2);
                world.newExplosion(null, currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5, 3F, true, false);
                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos neighborPos = currentPos.offset(facing);
                    if (!visited.contains(neighborPos)) {
                        if (world.getBlockState(neighborPos).getBlock() == this) {
                            visited.add(neighborPos);
                            processQueue.offer(neighborPos);
                        }
                    }
                }
            }
        } finally {
            isCombusting.set(false);
        }
    }
}