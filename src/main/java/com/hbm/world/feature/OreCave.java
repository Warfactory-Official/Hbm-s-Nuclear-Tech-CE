package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockStalagmite;
import com.hbm.world.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class OreCave {

    private NoiseGeneratorPerlin noise;
    private long lastSeed = Long.MIN_VALUE;

    private final IBlockState ore;
    private final int oreId;

    /** The number that is being deducted flat from the result of the perlin noise before all other processing. Increase this to make strata rarer. */
    private double threshold = 2D;
    /** The mulitplier for the remaining bit after the threshold has been deducted. Increase to make strata wavier. */
    private int rangeMult = 3;
    /** The maximum range after multiplying - anything above this will be subtracted from (maxRange * 2) to yield the proper range. Increase this to make strata thicker. */
    private int maxRange = 4;
    /** The y-level around which the stratum is centered. */
    private int yLevel = 30;

    private IBlockState fluid;
    private int dim = 0;
    private final IBlockState stalactiteState;
    private final IBlockState stalagmiteState;

    public OreCave(Block oreBlock) {
        this(oreBlock, 0);
    }

    public OreCave(Block oreBlock, int meta) {
        this.ore = oreBlock.getStateFromMeta(meta);
        this.oreId = Block.getIdFromBlock(oreBlock);
        final int resMeta = BlockStalagmite.getMetaFromResource(meta);
        this.stalactiteState = ModBlocks.stalactite.getStateFromMeta(resMeta);
        this.stalagmiteState = ModBlocks.stalagmite.getStateFromMeta(resMeta);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public OreCave setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public OreCave setRangeMult(int rangeMult) {
        this.rangeMult = rangeMult;
        return this;
    }

    public OreCave setMaxRange(int maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    public OreCave setYLevel(int yLevel) {
        this.yLevel = yLevel;
        return this;
    }

    public OreCave withFluid(Block fluidBlock) {
        this.fluid = (fluidBlock != null) ? fluidBlock.getDefaultState() : null;
        return this;
    }

    public OreCave withFluid(IBlockState fluidState) {
        this.fluid = fluidState;
        return this;
    }

    public OreCave setDimension(int dim) {
        this.dim = dim;
        return this;
    }

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Pre event) {

        World world = event.getWorld();
        if (world == null || world.provider == null || world.provider.getDimension() != this.dim) return;
        if (world.isRemote) return;

        long seed = world.getSeed();
        if (this.noise == null || this.lastSeed != seed) {
            this.noise = new NoiseGeneratorPerlin(new Random(seed + (oreId * 31L) + yLevel), 2);
            this.lastSeed = seed;
        }

        final Random rand = event.getRand();
        final Block fluidBlock = (fluid != null) ? fluid.getBlock() : null;

        ChunkPos chunkPos = event.getChunkPos();
        int cX = chunkPos.getXStart();
        int cZ = chunkPos.getZStart();

        double scale = 0.01D;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos npos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos downPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos clPos = new BlockPos.MutableBlockPos();

        final boolean canDecorateSpikes = (stalactiteState != null && stalagmiteState != null);

        for (int x = cX + 8; x < cX + 24; x++) {
            for (int z = cZ + 8; z < cZ + 24; z++) {

                double n = noise.getValue(x * scale, z * scale);
                if (n <= threshold) continue;

                int range = (int) ((n - threshold) * rangeMult);
                if (range > maxRange) range = (maxRange * 2) - range;
                if (range < 0) continue;

                for (int y = yLevel - range; y <= yLevel + range; y++) {
                    pos.setPos(x, y, z);
                    IBlockState genState = world.getBlockState(pos);
                    Block genBlock = genState.getBlock();
                    Material mat = genState.getMaterial();
                    if (genBlock.isNormalCube(genState, world, pos) && (mat == Material.ROCK || mat == Material.GROUND) && genBlock.isReplaceableOreGen(genState, world, pos, WorldUtil.STONE_PREDICATE)) {
                        boolean shouldGen = false;
                        boolean canGenFluid = rand.nextBoolean();

                        for (EnumFacing dir : EnumFacing.VALUES) {
                            npos.setPos(pos).move(dir);

                            IBlockState neighborState = world.getBlockState(npos);
                            Block neighborBlock = neighborState.getBlock();
                            if (neighborState.getMaterial() == Material.AIR || neighborBlock instanceof BlockStalagmite) {
                                shouldGen = true;
                            }

                            if (shouldGen && (fluid == null || !canGenFluid)) {
                                break;
                            }

                            if (fluid != null) {
                                switch (dir) {
                                    case UP:
                                        if (neighborState.getMaterial() != Material.AIR && !(neighborBlock instanceof BlockStalagmite)) {
                                            canGenFluid = false;
                                        }
                                        break;
                                    case DOWN:
                                        if (!neighborBlock.isNormalCube(neighborState, world, npos)) {
                                            canGenFluid = false;
                                        }
                                        break;
                                    case NORTH:
                                    case SOUTH:
                                    case EAST:
                                    case WEST:
                                        if (!neighborBlock.isNormalCube(neighborState, world, npos) && neighborBlock != fluidBlock) {
                                            canGenFluid = false;
                                        }
                                        break;
                                }
                            }
                        }

                        if (fluid != null && canGenFluid) {
                            world.setBlockState(pos, fluid, 2 | 16);

                            downPos.setPos(pos).move(EnumFacing.DOWN);
                            world.setBlockState(downPos, ore, 2 | 16);

                            for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                                clPos.setPos(pos).move(dir);

                                IBlockState neighborState = world.getBlockState(clPos);
                                Block neighborBlock = neighborState.getBlock();

                                if (neighborBlock.isNormalCube(neighborState, world, clPos)) {
                                    world.setBlockState(clPos, ore, 2 | 16);
                                }
                            }
                        } else if (shouldGen) {
                            world.setBlockState(pos, ore, 2 | 16);
                        }

                        continue;
                    }

                    if (canDecorateSpikes && (mat == Material.AIR || !genBlock.isNormalCube(genState, world, pos)) && rand.nextInt(5) == 0 && !mat.isLiquid()) {
                        if (ModBlocks.stalactite.canPlaceBlockAt(world, pos)) {
                            world.setBlockState(pos, stalactiteState, 2 | 16);
                        } else if (ModBlocks.stalagmite.canPlaceBlockAt(world, pos)) {
                            world.setBlockState(pos, stalagmiteState, 2 | 16);
                        }
                    }
                }
            }
        }
    }
}
