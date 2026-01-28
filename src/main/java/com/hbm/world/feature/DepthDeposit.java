package com.hbm.world.feature;

import com.google.common.base.Predicate;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.BufferUtil;
import com.hbm.world.phased.AbstractPhasedStructure;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DepthDeposit extends AbstractPhasedStructure {
    private static final Predicate<IBlockState> BEDROCK_MATCHER = BlockMatcher.forBlock(Blocks.BEDROCK);
    private final int size;
    private final double fill;
    private final Block oreBlock;
    private final Block filler;
    private final Block genTarget;
    private final Predicate<IBlockState> matcher;
    private final LongArrayList chunkOffsets;

    private DepthDeposit(int size, double fill, Block oreBlock, Block genTarget, Block filler) {
        this.size = size;
        this.fill = fill;
        this.oreBlock = oreBlock;
        this.genTarget = genTarget;
        this.filler = filler;
        this.chunkOffsets = collectChunkOffsetsByRadius(size + 8);
        this.matcher = BlockMatcher.forBlock(genTarget);
    }

    public static void generateConditionOverworld(World world, int x, int yMin, int yDev, int z, int size, double fill, Block block, Random rand,
                                                  int chance) {
        if (rand.nextInt(chance) != 0) return;

        int cx = x + rand.nextInt(16);
        int cy = yMin + rand.nextInt(yDev);
        int cz = z + rand.nextInt(16);

        DepthDeposit deposit = new DepthDeposit(size, fill, block, Blocks.STONE, ModBlocks.stone_depth);
        deposit.generate(world, rand, deposit.mutablePos.setPos(cx, cy, cz));
    }

    public static void generateConditionNether(World world, int x, int yMin, int yDev, int z, int size, double fill, Block block, Random rand,
                                               int chance) {
        if (rand.nextInt(chance) != 0) return;

        int cx = x + rand.nextInt(16);
        int cy = yMin + rand.nextInt(yDev);
        int cz = z + rand.nextInt(16);

        DepthDeposit deposit = new DepthDeposit(size, fill, block, Blocks.NETHERRACK, ModBlocks.stone_depth_nether);
        deposit.generate(world, rand, deposit.mutablePos.setPos(cx, cy, cz));
    }

    public static void generateCondition(World world, int x, int yMin, int yDev, int z, int size, double fill, Block block, Random rand, int chance,
                                         Block genTarget, Block filler) {
        if (rand.nextInt(chance) != 0) return;

        int cx = x + rand.nextInt(16);
        int cy = yMin + rand.nextInt(yDev);
        int cz = z + rand.nextInt(16);

        DepthDeposit deposit = new DepthDeposit(size, fill, block, genTarget, filler);
        deposit.generate(world, rand, deposit.mutablePos.setPos(cx, cy, cz));
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        generateSphere(world, Library.getBlockPosX(finalOrigin), Library.getBlockPosY(finalOrigin), Library.getBlockPosZ(finalOrigin), rand);
    }

    private void generateSphere(World world, int cx, int cy, int cz, Random rand) {
        if (world.isRemote) return;
        MutableBlockPos pos = this.mutablePos;

        for (int ix = cx - size; ix <= cx + size; ix++) {
            int dx = ix - cx;
            for (int jy = cy - size; jy <= cy + size; jy++) {
                if (jy < 1 || jy > 126) continue;

                int dy = jy - cy;
                for (int kz = cz - size; kz <= cz + size; kz++) {
                    int dz = kz - cz;

                    pos.setPos(ix, jy, kz);

                    IBlockState state = world.getBlockState(pos);
                    Block current = state.getBlock();

                    //yes you've heard right, bedrock
                    if (!current.isReplaceableOreGen(state, world, pos, BEDROCK_MATCHER) && !current.isReplaceableOreGen(state, world, pos, matcher)) {
                        continue;
                    }

                    double len = Math.sqrt(dx * (double) dx + dy * (double) dy + dz * (double) dz);

                    if (len + rand.nextInt(2) < size * fill) {
                        world.setBlockState(pos, oreBlock.getDefaultState(), 2 | 16);
                    } else if (len + rand.nextInt(2) <= size) {
                        world.setBlockState(pos, filler.getDefaultState(), 2 | 16);
                    }
                }
            }
        }
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return chunkOffsets;
    }

    
    public void writeToBuf(@NotNull ByteBuf out) {
        BufferUtil.writeVarInt(out, size);
        out.writeDouble(fill);
        BufferUtil.writeString(out, oreBlock.getRegistryName().toString());
        BufferUtil.writeString(out, genTarget.getRegistryName().toString());
        BufferUtil.writeString(out, filler.getRegistryName().toString());
    }

    @Nullable
    public static DepthDeposit readFromBuf(@NotNull ByteBuf in) {
        int size;
        double fill;
        String oreS;
        String targetS;
        String fillerS;
        try {
            size = BufferUtil.readVarInt(in);
            fill = in.readDouble();
            oreS = BufferUtil.readString(in, 256);
            targetS = BufferUtil.readString(in, 256);
            fillerS = BufferUtil.readString(in, 256);
        } catch (Exception ex) {
            MainRegistry.logger.warn("[DepthDeposit] Failed to read from buffer", ex);
            return null;
        }

        Block oreBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(oreS));
        Block genTarget = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(targetS));
        Block filler = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(fillerS));
        if (oreBlock == null || genTarget == null || filler == null) return null;
        return new DepthDeposit(size, fill, oreBlock, genTarget, filler);
    }
}
