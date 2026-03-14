package com.hbm.render.block;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describes how a baked block model should be textured.
 *
 * <p>Prefer the named factory methods such as {@link #cubeAll(String)} or
 * {@link #tintedCubeAll(String)} over the constructors. The factories make both
 * the shape and tint behavior explicit, which avoids the old "constructor arity
 * decides everything" ambiguity.
 */
public class BlockBakeFrame {

    public static final String ROOT_PATH = "blocks/";

    private final BlockForm blockForm;
    private final String[] textures;

    /**
     * Prefer {@link #cubeAll(String)}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public BlockBakeFrame(String texture) {
        this(BlockForm.ALL, texture);
    }

    /**
     * Prefer {@link #column(String, String)}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public BlockBakeFrame(String topTexture, String sideTexture) {
        this(BlockForm.COLUMN, topTexture, sideTexture);
    }

    /**
     * Prefer {@link #cubeBottomTop(String, String, String)}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public BlockBakeFrame(String topTexture, String sideTexture, String bottomTexture) {
        this(BlockForm.BOTTOM_TOP, topTexture, sideTexture, bottomTexture);
    }

    /**
     * Prefer the named factory methods unless you are implementing a custom layout.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public BlockBakeFrame(BlockForm form, @NotNull String... textures) {
        validateTextureCount(form, textures.length);
        this.blockForm = form;
        this.textures = textures;
    }

    public static BlockBakeFrame cubeAll(String texture) {
        return new BlockBakeFrame(BlockForm.ALL, texture);
    }

    public static BlockBakeFrame tintedCubeAll(String texture) {
        return new BlockBakeFrame(BlockForm.ALL_TINTED, texture);
    }

    public static BlockBakeFrame cross(String texture) {
        return new BlockBakeFrame(BlockForm.CROSS, texture);
    }

    public static BlockBakeFrame tintedCross(String texture) {
        return new BlockBakeFrame(BlockForm.CROSS_TINTED, texture);
    }

    public static BlockBakeFrame column(String topTexture, String sideTexture) {
        return new BlockBakeFrame(BlockForm.COLUMN, topTexture, sideTexture);
    }

    public static BlockBakeFrame tintedColumn(String topTexture, String sideTexture) {
        return new BlockBakeFrame(BlockForm.COLUMN_TINTED, topTexture, sideTexture);
    }

    public static BlockBakeFrame cubeBottomTop(String topTexture, String sideTexture, String bottomTexture) {
        return new BlockBakeFrame(BlockForm.BOTTOM_TOP, topTexture, sideTexture, bottomTexture);
    }

    public static BlockBakeFrame tintedCubeBottomTop(String topTexture, String sideTexture, String bottomTexture) {
        return new BlockBakeFrame(BlockForm.BOTTOM_TOP_TINTED, topTexture, sideTexture, bottomTexture);
    }

    public static BlockBakeFrame cube(@NotNull String... textures) {
        return new BlockBakeFrame(BlockForm.CUBE, textures);
    }

    public static BlockBakeFrame tintedCube(@NotNull String... textures) {
        return new BlockBakeFrame(BlockForm.CUBE_TINTED, textures);
    }

    public static BlockBakeFrame layer(String texture) {
        return new BlockBakeFrame(BlockForm.LAYER, texture);
    }

    public static BlockBakeFrame crop(String texture) {
        return new BlockBakeFrame(BlockForm.CROP, texture);
    }

    /**
     * Reuses the same texture across arbitrary texture keys.
     * Intended for callers that provide the base model elsewhere, such as stairs.
     */
    public static BlockBakeFrame singleTexture(String texture, String... textureKeys) {
        return new BlockBakeFrame(BlockForm.ALL, texture) {
            @Override
            public void putTextures(ImmutableMap.Builder<String, String> builder) {
                String sprite = getTextureLocation(0).toString();
                for (String textureKey : textureKeys) {
                    builder.put(textureKey, sprite);
                }
                builder.put("particle", sprite);
            }
        };
    }

    public static BlockBakeFrame[] simpleModelArray(String... textures) {
        BlockBakeFrame[] frames = new BlockBakeFrame[textures.length];
        for (int i = 0; i < textures.length; i++) {
            frames[i] = cubeAll(textures[i]);
        }
        return frames;
    }

    public static BlockBakeFrame southFacingCube(String sideTexture, String frontTexture) {
        return cube(sideTexture, sideTexture, sideTexture, frontTexture, sideTexture, sideTexture);
    }

    /**
     * @deprecated Prefer {@link #southFacingCube(String, String)}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public static BlockBakeFrame simpleSouthRotatable(String sides, String front) {
        return southFacingCube(sides, front);
    }

    public static BlockBakeFrame sideTopBottom(String sideTexture, String topTexture, String bottomTexture) {
        return cubeBottomTop(topTexture, sideTexture, bottomTexture);
    }

    /**
     * @deprecated Prefer {@link #sideTopBottom(String, String, String)} or
     * {@link #cubeBottomTop(String, String, String)}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public static BlockBakeFrame bottomTop(String side, String top, String bottom) {
        return sideTopBottom(side, top, bottom);
    }

    private static void validateTextureCount(BlockForm form, int textureCount) {
        if (textureCount != form.textureCount) {
            throw new IllegalArgumentException(
                    "Invalid texture count for " + form + ": expected " + form.textureCount + ", got " + textureCount
            );
        }
    }

    public static int getYRotationForFacing(EnumFacing facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    public static int getXRotationForFacing(EnumFacing facing) {
        return switch (facing) {
            case UP -> 90;
            case DOWN -> 270;
            default -> 0;
        };
    }

    public void registerBlockTextures(TextureMap map) {
        Set<String> uniqueTextures = new LinkedHashSet<>();
        for (String texture : this.textures) {
            uniqueTextures.add(texture);
        }
        for (String texture : uniqueTextures) {
            map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texture));
        }
    }

    public ResourceLocation getBaseModelLocation() {
        return new ResourceLocation(this.blockForm.baseModel);
    }

    /**
     * @deprecated Prefer {@link #getBaseModelLocation()}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public String getBaseModel() {
        return this.blockForm.baseModel;
    }

    public String getTexturePath(int index) {
        return this.textures[index];
    }

    public String getPrimaryTexturePath() {
        return getTexturePath(0);
    }

    public ResourceLocation getTextureLocation(int index) {
        return new ResourceLocation(Tags.MODID, ROOT_PATH + getTexturePath(index));
    }

    /**
     * @deprecated Prefer {@link #getTextureLocation(int)}.
     */
    @Deprecated(forRemoval = true, since = "2.2.0.0")
    public ResourceLocation getSpriteLoc(int index) {
        return getTextureLocation(index);
    }

    public void putTextures(ImmutableMap.Builder<String, String> textureMap) {
        String[] textureKeys = this.blockForm.textureKeys;
        AtomicInteger counter = new AtomicInteger(0);
        for (String textureKey : textureKeys) {
            textureMap.put(textureKey, getTextureLocation(counter.getAndIncrement()).toString());
        }
        textureMap.put("particle", getTextureLocation(0).toString());
    }

    public enum BlockForm {
        ALL("minecraft:block/cube_all", 1, "all"),
        CROSS("minecraft:block/cross", 1, "cross"),
        COLUMN("minecraft:block/cube_column", 2, "end", "side"),
        BOTTOM_TOP("minecraft:block/cube_bottom_top", 3, "top", "side", "bottom"),
        CUBE("minecraft:block/cube", 6, "up", "down", "north", "south", "west", "east"),

        ALL_TINTED("hbm:block/cube_all_tinted", 1, "all"),
        CROSS_TINTED("hbm:block/cross_tinted", 1, "cross"),
        COLUMN_TINTED("hbm:block/cube_column_tinted", 2, "end", "side"),
        BOTTOM_TOP_TINTED("hbm:block/cube_column_tinted", 3, "end", "side", "bottom"),
        CUBE_TINTED("hbm:block/cube_tinted", 6, "up", "down", "north", "south", "west", "east"),

        CROP("minecraft:block/crop", 1, "crop"),
        LAYER("hbm:block/block_layering", 1, "texture");

        private final String baseModel;
        private final int textureCount;
        private final String[] textureKeys;

        BlockForm(String baseModel, int textureCount, String... textureKeys) {
            this.baseModel = baseModel;
            this.textureCount = textureCount;
            this.textureKeys = textureKeys;
        }
    }
}
