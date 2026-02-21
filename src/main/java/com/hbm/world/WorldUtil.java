package com.hbm.world;

import com.google.common.base.Predicate;
import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.BiomeSyncPacket;
import com.hbm.util.Compat;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class WorldUtil {
    private static final MethodHandle getIntBiomeArray;
    public static final Predicate<IBlockState> STONE_PREDICATE = state -> {
        if (state != null && state.getBlock() == Blocks.STONE) {
            BlockStone.EnumType type = state.getValue(BlockStone.VARIANT);
            return type.isNatural();
        } else {
            return false;
        }
    };

    static {
        MethodHandle method = null;
        if (Compat.isIDExtensionModLoaded()) {
            //see https://github.com/TerraFirmaCraft-The-Final-Frontier/RoughlyEnoughIDs/blob/master/src/main/java/org/dimdev/jeid/mixin/core/world/MixinChunk.java
            try {
                method = MethodHandleHelper.findVirtual(Chunk.class, "getIntBiomeArray", MethodType.methodType(int[].class));
            } catch (RuntimeException ex) {
                MainRegistry.logger.error("JEID/REID/NEID is loaded but failed to find getIntBiomeArray method", ex);
            }
        }
        getIntBiomeArray = method;
    }

    public static Chunk provideChunk(WorldServer world, int chunkX, int chunkZ) {
        try {
            ChunkProviderServer provider = world.getChunkProvider();
            Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
            if(chunk != null) return chunk;
            return loadChunk(world, provider, chunkX, chunkZ);
        } catch(Throwable x) {
            return null;
        }
    }

    private static Chunk loadChunk(WorldServer world, ChunkProviderServer provider, int chunkX, int chunkZ) {
        long chunkCoord = ChunkPos.asLong(chunkX, chunkZ);
        provider.droppedChunks.remove(chunkCoord);
        Chunk chunk = provider.loadedChunks.get(chunkCoord);
        AnvilChunkLoader loader = null;

        if(provider.chunkLoader instanceof AnvilChunkLoader) {
            loader = (AnvilChunkLoader) provider.chunkLoader;
        }

        if(chunk == null && loader != null && loader.chunkExists(world, chunkX, chunkZ)) {
            chunk = ChunkIOExecutor.syncChunkLoad(world, loader, provider, chunkX, chunkZ);
        }

        return chunk;
    }

    public static void setBiome(World world, int blockX, int blockZ, Biome biome) {
        if (world == null || biome == null) return;
        final int chunkX = blockX >> 4;
        final int chunkZ = blockZ >> 4;
        final Chunk chunk = world.getChunk(chunkX, chunkZ);
        final int i = blockX & 15;
        final int j = blockZ & 15;
        final int idx = (j << 4) | i;
        final int biomeId = Biome.getIdForBiome(biome);
        boolean updated = false;
        if (Compat.isIDExtensionModLoaded()) {
            int[] arr = getIntBiomeArray(chunk);
            if (arr != null) {
                arr[idx] = biomeId;
                updated = true;
            }
        }
        if (!updated) {
            byte[] bArr = chunk.getBiomeArray();
            if (bArr.length == 256) {
                bArr[idx] = (byte) (biomeId & 0xFF);
            }
        }
        chunk.markDirty();
    }

    public static int @Nullable [] getIntBiomeArray(Chunk chunk) {
        int[] arr = null;
        if (getIntBiomeArray != null) {
            try {
                arr = (int[]) getIntBiomeArray.invokeExact(chunk);
            } catch (Throwable ex) {
                MainRegistry.logger.catching(ex);
            }
        }
        if (arr != null && arr.length == 256) {
            return arr;
        } else {
            MainRegistry.logger.error("JEID/REID/NEID is loaded, but getIntBiomeArray failed to get the correct data. This is a bug!");
            return null;
        }
    }

    public static void syncBiomeChange(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if(Compat.isIDExtensionModLoaded()) {
            PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(chunkX, chunkZ, getIntBiomeArray(chunk)), new TargetPoint(world.provider.getDimension(), chunkX << 4, 128, chunkZ << 4, 1024D));
        } else {
            PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(chunkX, chunkZ, chunk.getBiomeArray()), new TargetPoint(world.provider.getDimension(), chunkX << 4, 128, chunkZ << 4, 1024D));
        }
    }

    /**Chunkloads the chunk the entity is going to spawn in and then spawns it
     * @param entity The entity to be spawned**/

	/*fun fact: this is based off of joinEntityInSurroundings in World
	  however, since mojang is staffed by field mice, that function is client side only and half-baked
	 */
    public static void loadAndSpawnEntityInWorld(Entity entity) {

        World world = entity.world;
        int chunkX = MathHelper.floor(entity.posX / 16.0D);
        int chunkZ = MathHelper.floor(entity.posZ / 16.0D);
        byte loadRadius = 2;

        for(int k = chunkX - loadRadius; k <= chunkX + loadRadius; ++k) {
            for(int l = chunkZ - loadRadius; l <= chunkZ + loadRadius; ++l) {
                world.getChunk(k, l);
            }
        }

        if(!world.loadedEntityList.contains(entity)) {
            if(!MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, world))) {
                world.getChunk(chunkX, chunkZ).addEntity(entity);
                world.loadedEntityList.add(entity);
                world.onEntityAdded(entity);
            }
        }
    }

    // must be called server-side
    public static @NotNull List<Entity> getEntitiesInRadius(World world, double x, double y, double z, double radius) {
        AxisAlignedBB aabb = new AxisAlignedBB(x, y, z, x, y, z).grow(radius);
        return getEntitiesWithinAABBExcludingEntity((WorldServer) world, null, aabb);
    }

    /// --------------- WORLD COPY-PASTE START ---------------
    /// [Issue](https://github.com/Warfactory-Offical/Hbm-s-Nuclear-Tech-CE/issues/906)
    /// Some servers inject a check in getEntitiesWithinAABB that throws an exception if the AABB is too large
    /// It has to be bypassed, so I copied the methods here

    public static <T extends Entity> List<T> getEntitiesWithinAABB(WorldServer world, Class<? extends T> classEntity, AxisAlignedBB bb) {
        return getEntitiesWithinAABB(world, classEntity, bb, EntitySelectors.NOT_SPECTATING);
    }

    public static <T extends Entity> List<T> getEntitiesWithinAABB(WorldServer world, Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        int j2 = MathHelper.floor((aabb.minX - World.MAX_ENTITY_RADIUS) / 16.0D);
        int k2 = MathHelper.ceil((aabb.maxX + World.MAX_ENTITY_RADIUS) / 16.0D);
        int l2 = MathHelper.floor((aabb.minZ - World.MAX_ENTITY_RADIUS) / 16.0D);
        int i3 = MathHelper.ceil((aabb.maxZ + World.MAX_ENTITY_RADIUS) / 16.0D);
        List<T> list = new ArrayList<>();
        for (int j3 = j2; j3 < k2; ++j3) {
            for (int k3 = l2; k3 < i3; ++k3) {
                if (world.getChunkProvider().chunkExists(j3, k3)) {
                    world.getChunk(j3, k3).getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
                }
            }
        }
        return list;
    }

    public static List<Entity> getEntitiesWithinAABBExcludingEntity(WorldServer world, @Nullable Entity entityIn, AxisAlignedBB bb) {
        return getEntitiesInAABBexcluding(world, entityIn, bb, EntitySelectors.NOT_SPECTATING);
    }

    public static List<Entity> getEntitiesInAABBexcluding(WorldServer world, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        List<Entity> list = new ArrayList<>();
        int j2 = MathHelper.floor((boundingBox.minX - World.MAX_ENTITY_RADIUS) / 16.0D);
        int k2 = MathHelper.floor((boundingBox.maxX + World.MAX_ENTITY_RADIUS) / 16.0D);
        int l2 = MathHelper.floor((boundingBox.minZ - World.MAX_ENTITY_RADIUS) / 16.0D);
        int i3 = MathHelper.floor((boundingBox.maxZ + World.MAX_ENTITY_RADIUS) / 16.0D);
        for (int j3 = j2; j3 <= k2; ++j3) for (int k3 = l2; k3 <= i3; ++k3) {
            if (world.getChunkProvider().chunkExists(j3, k3)) {
                world.getChunk(j3, k3).getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
            }
        }
        return list;
    }
    /// --------------- WORLD COPY-PASTE END ---------------
}
