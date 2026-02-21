package com.hbm.explosion.vanillant.standard;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;
import com.hbm.interfaces.Untested;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;

public class BlockAllocatorGlyphidDig implements IBlockAllocator {
    protected double maximum;
    protected int resolution;

    public BlockAllocatorGlyphidDig(double maximum) {
        this(maximum, 16);
    }

    public BlockAllocatorGlyphidDig(double maximum, int resolution) {
        this.resolution = resolution;
        this.maximum = maximum;
    }

    @Override
    @Untested
    public HashSet<BlockPos> allocate(ExplosionVNT explosion, World world, double x, double y, double z, float size) {

        HashSet<BlockPos> affectedBlocks = new HashSet<>();

        for(int i = 0; i < this.resolution; ++i) {
            for(int j = 0; j < this.resolution; ++j) {
                for(int k = 0; k < this.resolution; ++k) {

                    if(i == 0 || i == this.resolution - 1 || j == 0 || j == this.resolution - 1 || k == 0 || k == this.resolution - 1) {

                        double d0 = (float) i / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
                        double d1 = (float) j / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
                        double d2 = (float) k / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;

                        double currentX = x;
                        double currentY = y;
                        double currentZ = z;

                        double dist = 0;

                        for(float stepSize = 0.3F; dist <= explosion.size;) {

                            double deltaX = currentX - x;
                            double deltaY = currentY - y;
                            double deltaZ = currentZ - z;
                            dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                            BlockPos affectedPos = new BlockPos( MathHelper.floor(currentX), MathHelper.floor(currentY), MathHelper.floor(currentZ));//Refactored to use BlockPos, Blockstate
                            IBlockState block = world.getBlockState(affectedPos);

                            if(block.getMaterial() != Material.AIR) {
                                float blockResistance =  block.getBlock().getExplosionResistance(explosion.exploder);//Using a shortcut, other vars are disregarded either way, Entity specific method got removed
                                if(this.maximum < blockResistance || block.getBlock() == ModBlocks.glyphid_spawner) {
                                    break;
                                }
                            }

                            if(explosion.exploder == null || block.getBlock().canEntityDestroy(block, world, affectedPos, explosion.exploder)) {//Another removed method. May cause differences in behavior
                                affectedBlocks.add(affectedPos);
                            }

                            currentX += d0 * (double) stepSize;
                            currentY += d1 * (double) stepSize;
                            currentZ += d2 * (double) stepSize;
                        }
                    }
                }
            }
        }

        return affectedBlocks;
    }
}
