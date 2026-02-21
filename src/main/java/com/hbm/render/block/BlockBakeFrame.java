package com.hbm.render.block;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import static com.hbm.render.block.BlockBakeFrame.BlockForm.*;

/**
 * Flexible system for baking Block models, supporting all possible configurations (that matter)
 * All you need to do is provide its form (consult the enum on the bottom of the class), and string names of textures
 * from there it will be handled for you.
 *
 * @author MrNorwood
 */
public class BlockBakeFrame {

    public static final String ROOT_PATH = "blocks/";
    public final String[] textureArray;
    public final BlockForm blockForm;

    //Quick method for making an array of single texture ALL form blocks
    public BlockBakeFrame(String texture) {
        this.textureArray = new String[]{texture};
        this.blockForm = ALL;
    }

    public BlockBakeFrame(String topTexture, String sideTexture) {
        this.textureArray = new String[]{topTexture, sideTexture};
        this.blockForm = PILLAR;
    }

    public BlockBakeFrame(String topTexture, String sideTexture, String bottomTexture) {
        this.textureArray = new String[]{topTexture, sideTexture, bottomTexture};
        this.blockForm = PILLAR_BOTTOM;
    }

    public BlockBakeFrame(BlockForm form, @NotNull String... textures) {
        this.textureArray = textures;
        switch (textures.length) {
            case 1:
                if (form == ALL || form == ALL_UNTINTED ||
                        form == CROSS || form == CROSS_UNTINTED ||
                        form == CROP ||
                        form == LAYER) break;
            case 2:
                if (form == PILLAR || form == PILLAR_UNTINTED) break;
            case 3:
                if (form == PILLAR_BOTTOM || form == PILLAR_BOTTOM_UNTINTED) break;
            case 6:
                if (form == FULL_CUSTOM || form == FULL_CUSTOM_UNTINTED) break;
            default:
                throw new IllegalArgumentException("Amount of textures provided is invalid: " + textures.length
                        + ". The amount should be 1, 2, 3 or 6");
        }
        this.blockForm = form;
    }

    public static BlockBakeFrame[] simpleModelArray(String... texture) {
        BlockBakeFrame[] frames = new BlockBakeFrame[texture.length];
        for (int i = 0; i < texture.length; i++) {
            frames[i] = new BlockBakeFrame(texture[i]);
        }
        return frames;
    }

    public static BlockBakeFrame simpleSouthRotatable(String sides, String front) {
        return new BlockBakeFrame(FULL_CUSTOM, sides, sides, sides, front, sides, sides);
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
        for (String texture : this.textureArray) {
            ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texture);
            map.registerSprite(spriteLoc);
        }
    }

    public ResourceLocation getSpriteLoc(int index) {
        return new ResourceLocation(Tags.MODID, ROOT_PATH + textureArray[index]);
    }

    public static BlockBakeFrame bottomTop(String side, String top, String bottom) {
        return new BlockBakeFrame(FULL_CUSTOM, top, bottom, side, side, side, side);
    }

    public String getBaseModel() {
        return this.blockForm.baseBakedModel;
    }

    public void putTextures(ImmutableMap.Builder<String, String> textureMap) {
        String[] wraps = this.blockForm.textureWrap;
        AtomicInteger counter = new AtomicInteger(0);
        for (String face : wraps) {
            textureMap.put(face, getSpriteLoc(counter.getAndIncrement()).toString());
        }
        textureMap.put("particle", getSpriteLoc(0).toString());
    }

    public enum BlockForm {
        ALL("hbm:block/cube_all_tinted", 1, "all"),
        CROP("minecraft:block/crop", 1, "crop"),
        LAYER("hbm:block/block_layering", 1, "texture"),
        CROSS("hbm:block/cross_tinted", 1, "cross"),
        PILLAR("hbm:block/cube_column_tinted", 2, "end", "side"),
        PILLAR_BOTTOM("hbm:block/cube_column_tinted", 3, "end", "side", "bottom"),
        FULL_CUSTOM("hbm:block/cube_tinted", 6, "up", "down", "north", "south", "west", "east"),

        ALL_UNTINTED("minecraft:block/cube_all", 1, "all"),
        CROSS_UNTINTED("minecraft:block/cross", 1, "cross"),
        PILLAR_UNTINTED("minecraft:block/cube_column", 2, "end", "side"),
        PILLAR_BOTTOM_UNTINTED("minecraft:block/cube_bottom_top", 3, "top", "side", "bottom"),
        FULL_CUSTOM_UNTINTED("minecraft:block/cube", 6, "up", "down", "north", "south", "west", "east");

        public final String baseBakedModel;
        public final int textureNum;
        public final String[] textureWrap;

        BlockForm(String baseBakedModel, int textureNum, String... textureWrap) {
            this.baseBakedModel = baseBakedModel;
            this.textureNum = textureNum;
            this.textureWrap = textureWrap;
        }
    }
}
