package com.hbm.world.feature;

import com.hbm.world.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class OreLayer3D {
    private static final int MIN_Y = 6;
    private static final int MAX_Y = 64;
    private static final int HEIGHT = MAX_Y - MIN_Y + 1;
    public static int counter = 0;
    public final int id;
    private long lastSeed = Long.MIN_VALUE;
    private NoiseGeneratorPerlin noiseX;
    private NoiseGeneratorPerlin noiseY;
    private NoiseGeneratorPerlin noiseZ;

    private double scaleH;
    private double scaleV;
    private double threshold;
    private final IBlockState oreState;
    private int dim = 0;
    private double[][] cacheX;
    private double[][] cacheZ;
    private double[][] cacheY;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public OreLayer3D(IBlockState oreState) {
        this.oreState = oreState;
        this.id = counter++;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public OreLayer3D(Block block, int meta) {
        this(block.getStateFromMeta(meta));
    }

    public OreLayer3D setDimension(int dim) {
        this.dim = dim;
        return this;
    }

    public OreLayer3D setScaleH(double scale) {
        this.scaleH = scale;
        return this;
    }

    public OreLayer3D setScaleV(double scale) {
        this.scaleV = scale;
        return this;
    }

    public OreLayer3D setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    // mlbv: that 1.7 HashSet is absolute garbage, i don't even want to explain
    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Pre event) {

        World world = event.getWorld();
        if (world == null || world.provider == null || world.provider.getDimension() != this.dim) return;
        if (world.isRemote) return;

        long seed = world.getSeed();
        if (noiseX == null || seed != lastSeed) {
            noiseX = new NoiseGeneratorPerlin(new Random(seed + 101L + id), 4);
            noiseY = new NoiseGeneratorPerlin(new Random(seed + 102L + id), 4);
            noiseZ = new NoiseGeneratorPerlin(new Random(seed + 103L + id), 4);
            lastSeed = seed;
            cacheX = new double[16][HEIGHT];
            cacheZ = new double[16][HEIGHT];
            cacheY = new double[16][16];
        }

        ChunkPos cp = event.getChunkPos();
        int cX = cp.getXStart();
        int cZ = cp.getZStart();
        int startX = cX + 8;
        int startZ = cZ + 8;
        for (int zOff = 0; zOff < 16; zOff++) {
            int worldZ = startZ + zOff;
            for (int yIndex = 0; yIndex < HEIGHT; yIndex++) {
                int y = MAX_Y - yIndex;
                cacheX[zOff][yIndex] = noiseX.getValue(y * scaleV, worldZ * scaleH);
            }
        }

        for (int xOff = 0; xOff < 16; xOff++) {
            int worldX = startX + xOff;
            for (int yIndex = 0; yIndex < HEIGHT; yIndex++) {
                int y = MAX_Y - yIndex;
                cacheZ[xOff][yIndex] = noiseZ.getValue(worldX * scaleH, y * scaleV);
            }
        }

        for (int xOff = 0; xOff < 16; xOff++) {
            int worldX = startX + xOff;
            for (int zOff = 0; zOff < 16; zOff++) {
                int worldZ = startZ + zOff;
                cacheY[xOff][zOff] = noiseY.getValue(worldX * scaleH, worldZ * scaleH);
            }
        }

        for (int xOff = 0; xOff < 16; xOff++) {
            int worldX = startX + xOff;

            for (int zOff = 0; zOff < 16; zOff++) {
                int worldZ = startZ + zOff;
                double nY = cacheY[xOff][zOff];
                for (int yIndex = 0; yIndex < HEIGHT; yIndex++) {
                    int y = MAX_Y - yIndex;
                    double nX = cacheX[zOff][yIndex];
                    double nZ = cacheZ[xOff][yIndex];
                    if (nX * nY * nZ <= threshold) continue;
                    pos.setPos(worldX, y, worldZ);
                    IBlockState state = world.getBlockState(pos);
                    Block target = state.getBlock();
                    if (target.isNormalCube(state, world, pos) && state.getMaterial() == Material.ROCK && target.isReplaceableOreGen(state, world, pos, WorldUtil.STONE_PREDICATE)) {
                        world.setBlockState(pos, oreState, 2 | 16);
                    }
                }
            }
        }
    }
}
