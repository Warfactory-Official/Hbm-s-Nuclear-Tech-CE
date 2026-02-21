package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;

public class BlockAllocatorWater implements IBlockAllocator {

    protected int resolution;

    public BlockAllocatorWater(int resolution) {
        this.resolution = resolution;
    }

    @Override
    public HashSet<BlockPos> allocate(ExplosionVNT explosion, World world, double x, double y, double z, float size) {
        HashSet<BlockPos> affectedBlocks = new HashSet<>();

        for (int i = 0; i < this.resolution; ++i) {
            for (int j = 0; j < this.resolution; ++j) {
                for (int k = 0; k < this.resolution; ++k) {
                    if (i == 0 || i == this.resolution - 1 || j == 0 || j == this.resolution - 1 || k == 0 || k == this.resolution - 1) {
                        double d0 = (float) i / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
                        double d1 = (float) j / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
                        double d2 = (float) k / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;

                        float powerRemaining = size * (0.7F + world.rand.nextFloat() * 0.6F);
                        double currentX = x;
                        double currentY = y;
                        double currentZ = z;

                        for (float stepSize = 0.3F; powerRemaining > 0.0F; powerRemaining -= stepSize * 0.75F) {
                            int blockX = MathHelper.floor(currentX);
                            int blockY = MathHelper.floor(currentY);
                            int blockZ = MathHelper.floor(currentZ);
                            BlockPos pos = new BlockPos(blockX, blockY, blockZ);

                            IBlockState state = world.getBlockState(pos);
                            Block block = state.getBlock();
                            Material material = state.getMaterial();

                            if (material != Material.AIR && !material.isLiquid()) {
                                float blockResistance = explosion.exploder != null
                                        ? explosion.exploder.getExplosionResistance(explosion.compat, world, pos, state)
                                        : block.getExplosionResistance(world, pos, explosion.exploder, explosion.compat);
                                powerRemaining -= (blockResistance + 0.3F) * stepSize;
                            }

                            if (powerRemaining > 0.0F
                                    && (explosion.exploder == null || explosion.exploder.canExplosionDestroyBlock(explosion.compat, world, pos, state, powerRemaining))
                                    && !material.isLiquid()) {
                                affectedBlocks.add(pos);
                            }

                            currentX += d0 * stepSize;
                            currentY += d1 * stepSize;
                            currentZ += d2 * stepSize;
                        }
                    }
                }
            }
        }

        return affectedBlocks;
    }
}
