package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.BufferUtil;
import com.hbm.world.phased.AbstractPhasedStructure;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OilBubble extends AbstractPhasedStructure {
    public final int radius;
    private final LongArrayList chunkOffsets;

    public OilBubble(int radius) {
        this.radius = radius;
        this.chunkOffsets = collectChunkOffsetsByRadius(radius);
    }

    public static OilBubble readFromBuf(@NotNull ByteBuf in) {
        int radius;
        try {
            radius = BufferUtil.readVarInt(in);
        } catch (Exception ex) {
            MainRegistry.logger.warn("[OilBubble] Failed to read from buffer", ex);
            return null;
        }
        return new OilBubble(radius);
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return chunkOffsets;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        int ox = Library.getBlockPosX(finalOrigin);
        int oz = Library.getBlockPosZ(finalOrigin);
        int oy = Library.getBlockPosY(finalOrigin);
        this.spawnOil(world, ox, oy, oz, this.radius);

        this.addSurfaceSpot(world, rand, ox, oz);
    }

    private void spawnOil(World world, int x, int y, int z, int radius) {
        int r2 = radius * radius;
        int r22 = r2 / 2;

        MutableBlockPos pos = mutablePos;
        for (int xx = -radius; xx < radius; xx++) {
            int X = xx + x;
            int XX = xx * xx;
            for (int yy = -radius; yy < radius; yy++) {
                int Y = yy + y;
                int YY = XX + yy * yy * 3;
                for (int zz = -radius; zz < radius; zz++) {
                    int Z = zz + z;
                    int ZZ = YY + zz * zz;
                    if (ZZ < r22) {
                        pos.setPos(X, Y, Z);
                        if (world.getBlockState(pos).getBlock() == Blocks.STONE)
                            world.setBlockState(pos, ModBlocks.ore_oil.getDefaultState(), 2 | 16);
                    }
                }
            }
        }
    }

    public void spawnOil(World world, int x, int y, int z, int radius, Block block, int meta, Block target) {
        int r2 = radius * radius;
        int r22 = r2 / 2;

        MutableBlockPos pos = mutablePos;

        for (int xx = -radius; xx < radius; xx++) {
            int X = xx + x;
            int XX = xx * xx;
            for (int yy = -radius; yy < radius; yy++) {
                int Y = yy + y;
                int YY = XX + yy * yy * 3;
                for (int zz = -radius; zz < radius; zz++) {
                    int Z = zz + z;
                    int ZZ = YY + zz * zz;
                    if (ZZ < r22) {
                        pos.setPos(X, Y, Z);
                        if (world.getBlockState(pos).getBlock() == target)
                            world.setBlockState(pos, block.getDefaultState(), 2 | 16);
                    }
                }
            }
        }
    }

    protected void addSurfaceSpot(World world, Random rand, int xCoord, int zCoord) {
        int spotCount = 150;
        int spotWidth = 7;
        MutableBlockPos pos = this.mutablePos;

        for (int i = 0; i < spotCount; i++) {
            int offX = (int) (rand.nextGaussian() * spotWidth);
            int offZ = (int) (rand.nextGaussian() * spotWidth);
            int absX = xCoord + offX;
            int absZ = zCoord + offZ;

            for (int y = 127; y >= 0; y--) {
                pos.setPos(absX, y, absZ);
                IBlockState state = world.getBlockState(pos);

                if (state.isFullCube()) {
                    for (int oy = 1; oy > -3; oy--) {
                        BlockPos subPos = pos.add(0, oy, 0);
                        IBlockState subState = world.getBlockState(subPos);
                        Block b = subState.getBlock();

                        int distSq = offX * offX + offZ * offZ;
                        boolean inner = distSq < (spotWidth / 2) * (spotWidth / 2);

                        if (b == Blocks.GRASS || b == Blocks.DIRT) {
                            world.setBlockState(subPos, inner ? ModBlocks.dirt_oily.getDefaultState() : ModBlocks.dirt_dead.getDefaultState(), 2 | 16);

                            if (!inner && oy == 0 && rand.nextInt(20) == 0) {
                                IBlockState deadPlant = ModBlocks.plant_dead.getDefaultState();
                                world.setBlockState(subPos.up(), deadPlant, 2 | 16);
                            }
                            break;
                        } else if (b == Blocks.SAND || b == ModBlocks.ore_oil_sand) {
                            if (b == Blocks.SAND && subState.getValue(net.minecraft.block.BlockSand.VARIANT) == net.minecraft.block.BlockSand.EnumType.RED_SAND) {
                                world.setBlockState(subPos, ModBlocks.sand_dirty_red.getDefaultState(), 2 | 16);
                            } else {
                                world.setBlockState(subPos, ModBlocks.sand_dirty.getDefaultState(), 2 | 16);
                            }
                            break;
                        } else if (b == Blocks.STONE) {
                            world.setBlockState(subPos, ModBlocks.stone_cracked.getDefaultState(), 2 | 16);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        for (int i = 1; i < 6; i++) {
            EnumFacing facing = EnumFacing.byIndex(i);
            int x = xCoord + facing.getXOffset();
            int z = zCoord + facing.getZOffset();
            int solids = 0;

            for (int y = 127; y >= 0; y--) {
                pos.setPos(x, y, z);
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() == Blocks.AIR) continue;
                if (state.getMaterial().isLiquid()) break;

                if (state.isFullCube()) {
                    solids++;

                    if (i > 1) {
                        world.setBlockState(pos, ModBlocks.stone_cracked.getDefaultState(), 2 | 16);
                        if (solids >= 4) break;
                    } else {
                        if (solids < 3) world.setBlockToAir(pos);
                        if (solids == 3) world.setBlockState(pos, ModBlocks.oil_spill.getDefaultState(), 2 | 16);
                        if (solids > 3 && solids < 7)
                            world.setBlockState(pos, ModBlocks.stone_cracked.getDefaultState(), 2 | 16);
                        if (solids == 7) break;
                    }
                }
            }
        }
    }

    public void writeToBuf(@NotNull ByteBuf out) {
        BufferUtil.writeVarInt(out, radius);
    }
}
