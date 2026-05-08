package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOreTE;
import com.hbm.config.WorldConfig;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemBedrockOreBase;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreType;
import com.hbm.lib.Library;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BedrockOre extends AbstractPhasedStructure {
    public static final BedrockOre OVERWORLD = new BedrockOre(new ItemStack(ModItems.bedrock_ore_base), null, 0xD78A16, 1, ModBlocks.stone_depth);
    public static final BedrockOre NETHER_GLOWSTONE = new BedrockOre(new ItemStack(Items.GLOWSTONE_DUST, 4), null, 0xF9FF4D, 1, ModBlocks.stone_depth_nether);
    public static final BedrockOre NETHER_POWDER_FIRE = new BedrockOre(new ItemStack(ModItems.powder_fire, 4), null, 0xD7341F, 1, ModBlocks.stone_depth_nether);
    public static final BedrockOre NETHER_QUARTZ = new BedrockOre(new ItemStack(Items.QUARTZ, 4), null, 0xF0EFDD, 1, ModBlocks.stone_depth_nether);

    private static final int EFFECT_RADIUS = 3;
    private static final LongArrayList CHUNK_OFFSETS = collectChunkOffsetsByRadius(EFFECT_RADIUS);

    private final ItemStack resourceStack;
    private final FluidStack acidRequirement;
    private final int color;
    private final int tier;
    private final Block depthRock;

    private BedrockOre(ItemStack stack, FluidStack acid, int color, int tier, Block depthRock) {
        this.resourceStack = stack.copy();
        this.acidRequirement = acid;
        this.color = color;
        this.tier = tier;
        this.depthRock = depthRock;
    }

    /**
     * @deprecated use the static final instances instead
     */
    @Deprecated
    public static void generate(World world, int x, int z, ItemStack stack, FluidStack acid, int color, int tier, Block depthRock) {
        BedrockOre oreTask = new BedrockOre(stack, acid, color, tier, depthRock);
        BlockPos position = new BlockPos(x, 0, z);
        oreTask.generate(world, world.rand, position);
    }

    /**
     * @deprecated use the static final instances instead when possible
     */
    @Deprecated
    public static void generate(World world, int x, int z, ItemStack stack, FluidStack acid, int color, int tier) {
        generate(world, x, z, stack, acid, color, tier, ModBlocks.stone_depth);
    }

    public static void generateAuto(World world, int x, int z) {
        double totalLevel = 0D;
        for(BedrockOreType type : BedrockOreType.VALUES) {
            totalLevel += ItemBedrockOreBase.getOreLevel(x, z, type);
        }

        totalLevel /= BedrockOreType.VALUES.length;
        BedrockOre oreTask = new BedrockOre(new ItemStack(ModItems.bedrock_ore_base), getBoreFluid(totalLevel), 0xD78A16, getTier(totalLevel), ModBlocks.stone_depth);
        oreTask.generate(world, world.rand, new BlockPos(x, 0, z));
    }

    public static final FluidStack BORE_TIER_1 = null;
    public static final FluidStack BORE_TIER_2 = new FluidStack(Fluids.WATER, 1_000);
    public static final FluidStack BORE_TIER_3 = new FluidStack(Fluids.SULFURIC_ACID, 1_000);
    public static final FluidStack BORE_TIER_4 = new FluidStack(Fluids.SOLVENT, 2_000);

    public static FluidStack getBoreFluid(double density) {
        if(density > 1.5) return BORE_TIER_4;
        if(density > 1) return BORE_TIER_3;
        if(density > 0.75) return BORE_TIER_2;
        return BORE_TIER_1;
    }

    public static int getTier(double density) {
        if(density > 1.5) return 4;
        if(density > 1) return 3;
        if(density > 0.75) return 2;
        return 1;
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
    public boolean checkSpawningConditions(@NotNull World world, long origin) {
        int ox = Library.getBlockPosX(origin);
        int oy = Library.getBlockPosY(origin);
        int oz = Library.getBlockPosZ(origin);
        return world.getBlockState(new BlockPos(ox, oy, oz)).getBlock() == Blocks.BEDROCK;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        int ox = Library.getBlockPosX(finalOrigin);
        int oy = Library.getBlockPosY(finalOrigin);
        int oz = Library.getBlockPosZ(finalOrigin);
        this.executeOriginalLogic(world, rand, new BlockPos(ox, oy, oz));
    }

    private void executeOriginalLogic(World world, Random rand, BlockPos finalOrigin) {
        int x = finalOrigin.getX();
        int z = finalOrigin.getZ();

        MutableBlockPos pos = this.mutablePos;
        for (int ix = x - 1; ix <= x + 1; ix++) {
            for (int iz = z - 1; iz <= z + 1; iz++) {
                pos.setPos(ix, 0, iz);
                IBlockState state = world.getBlockState(pos);
                Block b = state.getBlock();

                if (b == Blocks.BEDROCK) {
                    if ((ix == x && iz == z) || rand.nextBoolean()) {
                        world.setBlockState(pos, ModBlocks.ore_bedrock_block.getDefaultState(), 3);

                        TileEntity tile = world.getTileEntity(pos);
                        if (tile instanceof BlockBedrockOreTE.TileEntityBedrockOre ore) {
                            ore.resource = this.resourceStack;
                            ore.color = this.color;
                            ore.shape = rand.nextInt(10);
                            ore.acidRequirement = this.acidRequirement;
                            ore.tier = this.tier;
                            ore.markDirty();
                            world.notifyBlockUpdate(pos, state, world.getBlockState(pos), 3);
                        }
                    }
                }
            }
        }

        for (int ix = x - 3; ix <= x + 3; ix++) {
            for (int iz = z - 3; iz <= z + 3; iz++) {
                for (int iy = 1; iy < 7; iy++) {
                    pos.setPos(ix, iy, iz);
                    IBlockState state = world.getBlockState(pos);
                    Block b = state.getBlock();

                    if (iy < 3 || b == Blocks.BEDROCK) {
                        if (b == Blocks.STONE || b == Blocks.BEDROCK) {
                            world.setBlockState(pos, this.depthRock.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return CHUNK_OFFSETS;
    }

    public static BedrockOre getWeightedNetherOre(World world, int chunkX, int chunkZ) {
        final int wGlow = WorldConfig.bedrockGlowstoneSpawn;
        final int wFire = WorldConfig.bedrockPhosphorusSpawn;
        final int wQuartz = WorldConfig.bedrockQuartzSpawn;
        final int total = wGlow + wFire + wQuartz;
        if (total <= 0) return null;
        int r = Library.nextIntDeterministic(world.getSeed(), chunkX, chunkZ, total);
        if (r < wGlow) return NETHER_GLOWSTONE;
        r -= wGlow;
        if (r < wFire) return NETHER_POWDER_FIRE;
        return NETHER_QUARTZ;
    }
}
