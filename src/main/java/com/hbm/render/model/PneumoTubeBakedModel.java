package com.hbm.render.model;

import com.hbm.lib.ForgeDirection;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

import static com.hbm.blocks.network.PneumoTube.*;

@MethodsReturnNonnullByDefault
@SideOnly(Side.CLIENT)
public class PneumoTubeBakedModel extends AbstractBakedModel {

    private static final int INVENTORY_KEY = -1;
    private final StampedLock lock = new StampedLock();
    private final Int2ObjectOpenHashMap<List<BakedQuad>> cache = new Int2ObjectOpenHashMap<>();

    public PneumoTubeBakedModel() {
        super(BakedModelTransforms.standardBlock());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        boolean inventory = state == null, pX = false, nX = false, pY = false, nY = false, pZ = false, nZ = false;
        ForgeDirection outDir = ForgeDirection.UNKNOWN, inDir = ForgeDirection.UNKNOWN;
        int connectorMask = 0;
        int key;

        if (inventory) {
            key = INVENTORY_KEY;
        } else {
            try {
                IExtendedBlockState ext = (IExtendedBlockState) state;
                outDir = ext.getValue(OUT_DIR);
                inDir = ext.getValue(IN_DIR);
                connectorMask = ext.getValue(CONNECTOR_MASK);
                nZ = ext.getValue(CONN_NORTH);
                pZ = ext.getValue(CONN_SOUTH);
                nX = ext.getValue(CONN_WEST);
                pX = ext.getValue(CONN_EAST);
                nY = ext.getValue(CONN_DOWN);
                pY = ext.getValue(CONN_UP);
            } catch (Exception _) { pX = true; nX = true; }

            int mask = (pX ? 32 : 0) | (nX ? 16 : 0) | (pY ? 8 : 0) | (nY ? 4 : 0) | (pZ ? 2 : 0) | (nZ ? 1 : 0);
            key = mask | (outDir.ordinal() << 6) | (inDir.ordinal() << 9) | (connectorMask << 12);
        }

        List<BakedQuad> quads;
        long stamp = lock.tryOptimisticRead();
        quads = cache.get(key);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                quads = cache.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        if (quads != null) return quads;

        stamp = lock.writeLock();
        try {
            quads = cache.get(key);
            if (quads == null) {
                quads = inventory
                        ? generateInventoryQuads()
                        : generateWorldQuads(pX, nX, pY, nY, pZ, nZ, outDir, inDir, connectorMask);
                cache.put(key, quads);
            }
        } finally {
            lock.unlockWrite(stamp);
        }

        return quads;
    }

    private List<BakedQuad> generateInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        addLegacyBox(
                quads,
                5.0F, 5.0F, 0.0F,
                11.0F, 11.0F, 16.0F,
                new TextureAtlasSprite[]{iconStraight, iconStraight, iconConnector, iconConnector, iconStraight, iconStraight},
                LEGACY_ALL_FACES,
                new int[]{1, 2, 0, 0, 0, 0});
        return Collections.unmodifiableList(quads);
    }

    private List<BakedQuad> generateWorldQuads(boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ,
                                               ForgeDirection outDir, ForgeDirection inDir, int connectorMask) {
        List<BakedQuad> quads = new ArrayList<>();
        int mask = (pX ? 32 : 0) | (nX ? 16 : 0) | (pY ? 8 : 0) | (nY ? 4 : 0) | (pZ ? 2 : 0) | (nZ ? 1 : 0);
        int count = Integer.bitCount(mask);
        boolean straightX = (mask & 0b001111) == 0 && mask > 0;
        boolean straightY = (mask & 0b110011) == 0 && mask > 0;
        boolean straightZ = (mask & 0b111100) == 0 && mask > 0;
        boolean hasConnections = outDir != ForgeDirection.UNKNOWN || inDir != ForgeDirection.UNKNOWN || connectorMask != 0;

        if (straightX && count > 1 && !hasConnections) {
            addLegacyBox(
                    quads,
                    0.0F, 5.0F, 5.0F,
                    16.0F, 11.0F, 11.0F,
                    iconStraight,
                    legacyVisibility(true, true, true, true, false, false),
                    LEGACY_NO_ROTATION);
        } else if (straightZ && count > 1 && !hasConnections) {
            addLegacyBox(
                    quads,
                    5.0F, 5.0F, 0.0F,
                    11.0F, 11.0F, 16.0F,
                    iconStraight,
                    legacyVisibility(true, true, false, false, true, true),
                    new int[]{1, 2, 0, 0, 0, 0});
        } else if (straightY && count > 1 && !hasConnections) {
            addLegacyBox(
                    quads,
                    5.0F, 0.0F, 5.0F,
                    11.0F, 16.0F, 11.0F,
                    iconStraight,
                    legacyVisibility(false, false, true, true, true, true),
                    new int[]{0, 0, 2, 2, 2, 2});
        } else {
            addLegacyBox(
                    quads,
                    5.0F, 5.0F, 5.0F,
                    11.0F, 11.0F, 11.0F,
                    iconBase,
                    legacyVisibility(!nY, !pY, !nZ, !pZ, !nX, !pX),
                    LEGACY_NO_ROTATION);

            if (nY) addTubeArm(quads, ForgeDirection.DOWN, iconBase);
            if (pY) addTubeArm(quads, ForgeDirection.UP, iconBase);
            if (nX) addTubeArm(quads, ForgeDirection.WEST, iconBase);
            if (pX) addTubeArm(quads, ForgeDirection.EAST, iconBase);
            if (nZ) addTubeArm(quads, ForgeDirection.NORTH, iconBase);
            if (pZ) addTubeArm(quads, ForgeDirection.SOUTH, iconBase);

            if (outDir != ForgeDirection.UNKNOWN) addConnector(quads, outDir, iconOut);
            if (inDir != ForgeDirection.UNKNOWN) addConnector(quads, inDir, iconIn);
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if ((connectorMask & (1 << dir.ordinal())) != 0) {
                    addConnector(quads, dir, iconConnector);
                }
            }
        }

        return Collections.unmodifiableList(quads);
    }

    private void addTubeArm(List<BakedQuad> quads, ForgeDirection dir, TextureAtlasSprite sprite) {
        switch (dir) {
            case EAST:
                addLegacyBox(quads, 11.0F, 5.0F, 5.0F, 16.0F, 11.0F, 11.0F, sprite, legacyVisibility(true, true, true, true, false, false), LEGACY_NO_ROTATION);
                break;
            case WEST:
                addLegacyBox(quads, 0.0F, 5.0F, 5.0F, 5.0F, 11.0F, 11.0F, sprite, legacyVisibility(true, true, true, true, false, false), LEGACY_NO_ROTATION);
                break;
            case UP:
                addLegacyBox(quads, 5.0F, 11.0F, 5.0F, 11.0F, 16.0F, 11.0F, sprite, legacyVisibility(false, false, true, true, true, true), LEGACY_NO_ROTATION);
                break;
            case DOWN:
                addLegacyBox(quads, 5.0F, 0.0F, 5.0F, 11.0F, 5.0F, 11.0F, sprite, legacyVisibility(false, false, true, true, true, true), LEGACY_NO_ROTATION);
                break;
            case SOUTH:
                addLegacyBox(quads, 5.0F, 5.0F, 11.0F, 11.0F, 11.0F, 16.0F, sprite, legacyVisibility(true, true, false, false, true, true), LEGACY_NO_ROTATION);
                break;
            case NORTH:
                addLegacyBox(quads, 5.0F, 5.0F, 0.0F, 11.0F, 11.0F, 5.0F, sprite, legacyVisibility(true, true, false, false, true, true), LEGACY_NO_ROTATION);
                break;
            default:
                break;
        }
    }

    private void addConnector(List<BakedQuad> quads, ForgeDirection dir, TextureAtlasSprite nozzleSprite) {
        switch (dir) {
            case EAST:
                addLegacyBox(quads, 11.0F, 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, iconBase, legacyVisibility(true, true, true, true, false, false), LEGACY_NO_ROTATION);
                addLegacyBox(quads, 12.0F, 4.0F, 4.0F, 16.0F, 12.0F, 12.0F, nozzleSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
                break;
            case WEST:
                addLegacyBox(quads, 4.0F, 5.0F, 5.0F, 5.0F, 11.0F, 11.0F, iconBase, legacyVisibility(true, true, true, true, false, false), LEGACY_NO_ROTATION);
                addLegacyBox(quads, 0.0F, 4.0F, 4.0F, 4.0F, 12.0F, 12.0F, nozzleSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
                break;
            case UP:
                addLegacyBox(quads, 5.0F, 11.0F, 5.0F, 11.0F, 12.0F, 11.0F, iconBase, legacyVisibility(false, false, true, true, true, true), LEGACY_NO_ROTATION);
                addLegacyBox(quads, 4.0F, 12.0F, 4.0F, 12.0F, 16.0F, 12.0F, nozzleSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
                break;
            case DOWN:
                addLegacyBox(quads, 5.0F, 4.0F, 5.0F, 11.0F, 5.0F, 11.0F, iconBase, legacyVisibility(false, false, true, true, true, true), LEGACY_NO_ROTATION);
                addLegacyBox(quads, 4.0F, 0.0F, 4.0F, 12.0F, 4.0F, 12.0F, nozzleSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
                break;
            case SOUTH:
                addLegacyBox(quads, 5.0F, 5.0F, 11.0F, 11.0F, 11.0F, 12.0F, iconBase, legacyVisibility(true, true, false, false, true, true), LEGACY_NO_ROTATION);
                addLegacyBox(quads, 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 16.0F, nozzleSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
                break;
            case NORTH:
                addLegacyBox(quads, 5.0F, 5.0F, 4.0F, 11.0F, 11.0F, 5.0F, iconBase, legacyVisibility(true, true, false, false, true, true), LEGACY_NO_ROTATION);
                addLegacyBox(quads, 4.0F, 4.0F, 0.0F, 12.0F, 12.0F, 4.0F, nozzleSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
                break;
            default:
                break;
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return iconBase;
    }
}
