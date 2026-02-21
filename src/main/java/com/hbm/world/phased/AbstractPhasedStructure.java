package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.RandomPool;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base class for all phased structures.
 */
public abstract class AbstractPhasedStructure extends WorldGenerator implements IPhasedStructure {
    public static final IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();
    private static final Map<Class<? extends AbstractPhasedStructure>, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>>>> STRUCTURE_CACHE = new IdentityHashMap<>();
    protected final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(); // use this, assuming no mod would ever thread worldgen

    private static long anchorKey(int anchorX, int anchorZ) {
        int ax = anchorX & 15;
        int az = anchorZ & 15;
        return (((long) ax) << 32) | (az & 0xFFFF_FFFFL);
    }

    private static Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> chunkTheLayout(Long2ObjectOpenHashMap<Object> blocks, int anchorX, int anchorZ) {
        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> chunkedMap = new Long2ObjectOpenHashMap<>();
        ObjectIterator<Long2ObjectMap.Entry<Object>> iterator = blocks.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<Object> entry = iterator.next();
            long key = entry.getLongKey();
            Object info = entry.getValue();
            int localX = anchorX + Library.getBlockPosX(key);
            int localZ = anchorZ + Library.getBlockPosZ(key);

            int relChunkX = localX >> 4;
            int relChunkZ = localZ >> 4;

            long relChunkKey = ChunkPos.asLong(relChunkX, relChunkZ);
            Long2ObjectOpenHashMap<Object> chunk = chunkedMap.get(relChunkKey);
            if (chunk == null) {
                chunk = new Long2ObjectOpenHashMap<>();
                chunkedMap.put(relChunkKey, chunk);
            }
            chunk.put(key, info);
        }
        return chunkedMap;
    }

    protected static LongArrayList collectChunkOffsetsByRadius(int horizontalRadius) {
        int chunkRadius = Math.max(0, Math.floorDiv(Math.max(0, horizontalRadius) + 15, 16));
        LongArrayList offsets = new LongArrayList((chunkRadius * 2 + 1) * (chunkRadius * 2 + 1));
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                offsets.add(ChunkPos.asLong(cx, cz));
            }
        }
        return offsets;
    }

    /**
     * Static part. Leave this empty and override {@link #useDynamicScheduler()} if the whole structure is completely dynamic.
     */
    protected void buildStructure(LegacyBuilder builder, Random rand) {
    }

    /**
     * @return false if the structure is not completely static, e.g. random is used.
     */
    protected boolean isCacheable() {
        return true;
    }

    /**
     * Override to route generation through {@link DynamicStructureDispatcher} instead of {@link PhasedStructureGenerator}.
     */
    protected boolean useDynamicScheduler() {
        return false;
    }

    protected int getGenerationHeightOffset() {
        return 0;
    }

    @Override
    public final boolean generate(World world, Random rand, BlockPos pos) {
        return generate(world, rand, pos, false);
    }

    // common logic used by a lot of legacy structures
    protected boolean locationIsValidSpawn(World world, long serialized) {
        int x = Library.getBlockPosX(serialized);
        int y = Library.getBlockPosY(serialized);
        int z = Library.getBlockPosZ(serialized);
        BlockPos.MutableBlockPos pos = mutablePos;
        IBlockState checkBlockState = world.getBlockState(pos.setPos(x, y - 1, z));
        Block checkBlock = checkBlockState.getBlock();
        IBlockState stateAbove = world.getBlockState(pos.setPos(x, y, z));
        Block blockBelow = world.getBlockState(pos.setPos(x, y - 2, z)).getBlock();

        if (!stateAbove.getBlock().isAir(stateAbove, world, pos.setPos(x, y, z))) {
            return false;
        }
        if (isValidSpawnBlock(checkBlock)) {
            return true;
        } else if (checkBlock == Blocks.SNOW_LAYER && isValidSpawnBlock(blockBelow)) {
            return true;
        } else return checkBlockState.getMaterial() == Material.PLANTS && isValidSpawnBlock(blockBelow);
    }

    protected boolean isValidSpawnBlock(Block block) {
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.STONE || block == Blocks.SAND;
    }

    //TODO: make it return false if generation failed
    public final boolean generate(World world, Random rand, BlockPos pos, boolean force) {
        WorldServer server = (WorldServer) world;
        int ox = pos.getX();
        int oy = pos.getY() + getGenerationHeightOffset();
        int oz = pos.getZ();
        long originSerialized = Library.blockPosToLong(ox, oy, oz);
        long layoutSeed = rand.nextLong();

        if (useDynamicScheduler()) {
            if (force) {
                if (GeneralConfig.enableDebugWorldGen)
                    MainRegistry.logger.info("Forcing dynamic {} generation at {}", getClass().getSimpleName(), originSerialized);
                DynamicStructureDispatcher.forceGenerate(server, rand, originSerialized, this);
            } else {
                if (GeneralConfig.enableDebugWorldGen)
                    MainRegistry.logger.info("Proposing dynamic {} generation at {}", getClass().getSimpleName(), originSerialized);
                DynamicStructureDispatcher.schedule(server, originSerialized, this, layoutSeed);
            }
            return true;
        }

        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> layout = buildLayout(originSerialized, layoutSeed);

        if (force) {
            if (GeneralConfig.enableDebugWorldGen)
                MainRegistry.logger.info("Forcing {} generation at {}", getClass().getSimpleName(), originSerialized);
            PhasedStructureGenerator.forceGenerateStructure(server, rand, originSerialized, this, layout);
        } else {
            if (GeneralConfig.enableDebugWorldGen)
                MainRegistry.logger.info("Proposing {} generation at {}", getClass().getSimpleName(), originSerialized);
            PhasedStructureGenerator.scheduleStructureForValidation(server, originSerialized, this, layout, layoutSeed);
        }
        
        logGenerationSuccess(world, originSerialized);
        return true;
    }

    protected void logGenerationSuccess(World world, long origin) {
        if (GeneralConfig.enableDebugWorldGen) {
            MainRegistry.logger.info("[PhasedGen] Structure {} scheduled for generation at BlockPos {}, {}, {} " +
                    "in dimension {} ({})", getClass().getSimpleName(), Library.getBlockPosX(origin), Library.getBlockPosY(origin),
            Library.getBlockPosZ(origin), world.provider.getDimension(), world.provider.getDimensionType().getName());
        }
    }

    final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> buildLayout(long originSerialized, long layoutSeed) {
        int anchorX = Library.getBlockPosX(originSerialized) & 15;
        int anchorZ = Library.getBlockPosZ(originSerialized) & 15;
        long aKey = anchorKey(anchorX, anchorZ);

        if (isCacheable()) {
            Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>>> byAnchor = STRUCTURE_CACHE.computeIfAbsent(getClass(), k -> new Long2ObjectOpenHashMap<>());
            return byAnchor.computeIfAbsent(aKey, _ -> {
                Random rand = RandomPool.borrow(getClass().getName().hashCode());
                try {
                    LegacyBuilder staticBuilder = new LegacyBuilder(rand);
                    buildStructure(staticBuilder, staticBuilder.rand);
                    return chunkTheLayout(staticBuilder.getBlocks(), anchorX, anchorZ);
                } finally {
                    RandomPool.recycle(rand);
                }
            });
        }

        Random rand = RandomPool.borrow(layoutSeed);
        try {
            LegacyBuilder dynamicBuilder = new LegacyBuilder(rand);
            buildStructure(dynamicBuilder, dynamicBuilder.rand);
            return chunkTheLayout(dynamicBuilder.getBlocks(), anchorX, anchorZ);
        } finally {
            RandomPool.recycle(rand);
        }
    }

    @Override
    public final void generateForChunk(World world, Random rand, long structureOrigin, Long2ObjectOpenHashMap<@NotNull Object> blocksForThisChunk) {
        if (blocksForThisChunk.isEmpty()) return;

        ObjectIterator<Long2ObjectMap.Entry<Object>> iterator = blocksForThisChunk.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<Object> entry = iterator.next();
            long key = entry.getLongKey();
            Object val = entry.getValue();
            mutablePos.setPos(Library.getBlockPosX(structureOrigin) + Library.getBlockPosX(key), Library.getBlockPosY(structureOrigin) + Library.getBlockPosY(key), Library.getBlockPosZ(structureOrigin) + Library.getBlockPosZ(key));

            if (val instanceof IBlockState state) {
                world.setBlockState(mutablePos, state, 2 | 16);
            } else if (val instanceof BlockInfo info) {
                world.setBlockState(mutablePos, info.state, 2 | 16);
                if (info.tePopulator != null) {
                    TileEntity te = world.getTileEntity(mutablePos);
                    if (te != null) {
                        try {
                            info.tePopulator.populate(world, rand, mutablePos, te);
                        } catch (ClassCastException e) {
                            MainRegistry.logger.error("WorldGen found incompatible TileEntity type in dimension {} at {}, this is a bug!", world.provider.getDimension(), mutablePos.toImmutable(), e);
                        }
                    }
                }
            }
        }
    }

    @FunctionalInterface
    public interface TileEntityPopulator {
        void populate(World worldIn, Random random, BlockPos blockPos, TileEntity chest);
    }

    /**
     * A custom block info class, containing all the information needed to generate
     * a block
     */
    public record BlockInfo(IBlockState state, @Nullable TileEntityPopulator tePopulator) {
    }

    public class LegacyBuilder {
        public final Random rand;
        private final Long2ObjectOpenHashMap<Object> blocks = new Long2ObjectOpenHashMap<>();

        public LegacyBuilder(Random rand) {
            this.rand = rand;
        }

        public void setBlockState(int x, int y, int z, IBlockState state) {
            setBlockState(x, y, z, state, null);
        }

        public void setBlockState(int x, int y, int z, IBlockState state, @Nullable TileEntityPopulator populator) {
            long key = Library.blockPosToLong(x, y, z);
            if (populator == null) {
                blocks.put(key, state);
            } else {
                blocks.put(key, new BlockInfo(state, populator));
            }
        }

        public void setBlockState(BlockPos pos, IBlockState state, int ignored) {
            setBlockState(pos, state, null);
        }

        public void setBlockState(BlockPos pos, IBlockState state) {
            setBlockState(pos, state, null);
        }

        public void setBlockState(BlockPos pos, IBlockState state, @Nullable TileEntityPopulator populator) {
            long key = Library.blockPosToLong(pos.getX(), pos.getY(), pos.getZ());
            if (populator == null) {
                blocks.put(key, state);
            } else {
                blocks.put(key, new BlockInfo(state, populator));
            }
        }

        @ApiStatus.Experimental
        public IBlockState getBlockState(int x, int y, int z) {
            long key = Library.blockPosToLong(x, y, z);
            Object val = blocks.get(key);
            if (val instanceof IBlockState state) return state;
            if (val instanceof BlockInfo info) return info.state;
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Structure {} tried to retrieve non-existent BlockState at relative ({}, {}, {})", AbstractPhasedStructure.this.getClass().getSimpleName(), x, y, z);
            }
            return AIR_DEFAULT_STATE;
        }

        @ApiStatus.Experimental
        public IBlockState getBlockState(BlockPos pos) {
            long key = Library.blockPosToLong(pos.getX(), pos.getY(), pos.getZ());
            Object val = blocks.get(key);
            if (val instanceof IBlockState state) return state;
            if (val instanceof BlockInfo info) return info.state;
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Structure {} tried to retrieve non-existent BlockState at relative {}", AbstractPhasedStructure.this.getClass().getSimpleName(), pos);
            }
            return AIR_DEFAULT_STATE;
        }

        public void setBlockToAir(int x, int y, int z) {
            setBlockState(x, y, z, AIR_DEFAULT_STATE);
        }

        public void setBlockToAir(BlockPos pos) {
            setBlockState(pos, AIR_DEFAULT_STATE);
        }

        public Random getRandom() {
            return rand;
        }

        private Long2ObjectOpenHashMap<Object> getBlocks() {
            return blocks;
        }

        public void placeDoorWithoutCheck(int x, int y, int z, EnumFacing facing, Block door, boolean isRightHinge, boolean isOpen) {
            BlockDoor.EnumHingePosition hinge = isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT;
            IBlockState baseState = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, hinge).withProperty(BlockDoor.POWERED, Boolean.FALSE).withProperty(BlockDoor.OPEN, isOpen);
            setBlockState(x, y, z, baseState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER));
            setBlockState(x, y + 1, z, baseState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
        }

        public void placeDoorWithoutCheck(int x, int y, int z, EnumFacing facing, Block door, boolean isRightHinge) {
            placeDoorWithoutCheck(x, y, z, facing, door, isRightHinge, false);
        }

        public void placeDoorWithoutCheck(BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge, boolean isOpen) {
            placeDoorWithoutCheck(pos.getX(), pos.getY(), pos.getZ(), facing, door, isRightHinge, isOpen);
        }

        public void placeDoorWithoutCheck(BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge) {
            placeDoorWithoutCheck(pos, facing, door, isRightHinge, false);
        }
    }
}
