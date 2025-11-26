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
    public final boolean tinted;

    //Quick method for making an array of single texture ALL form blocks
    public BlockBakeFrame(String texture, boolean tinted) {
        this.textureArray = new String[]{texture};
        this.blockForm = ALL;
        this.tinted = tinted;
    }

    public BlockBakeFrame(String texture) {
        this.textureArray = new String[]{texture};
        this.blockForm = ALL;
        this.tinted = false;
    }

    public BlockBakeFrame(String topTexture, String sideTexture) {
        this.textureArray = new String[]{topTexture, sideTexture};
        this.blockForm = PILLAR;
        tinted = false;
    }

    public BlockBakeFrame(String topTexture, String sideTexture, boolean tinted) {
        this.textureArray = new String[]{topTexture, sideTexture};
        this.blockForm = PILLAR;
        this.tinted =tinted;
    }

    public BlockBakeFrame(String topTexture, String sideTexture, String bottomTexture) {
        this.textureArray = new String[]{topTexture, sideTexture, bottomTexture};
        this.blockForm = PILLAR_BOTTOM;
        tinted = false;
    }

    public BlockBakeFrame(String topTexture, String sideTexture, String bottomTexture, boolean tinted) {
        this.textureArray = new String[]{topTexture, sideTexture, bottomTexture};
        this.blockForm = PILLAR_BOTTOM;
        this.tinted = tinted ;
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
        this.tinted =false;
    }

    public BlockBakeFrame(BlockForm form, boolean tinted,@NotNull String... textures ) {
        this.textureArray = textures;
        this.tinted = tinted ;
        switch (textures.length) {
            case 1:
                if (form == ALL || form == CROSS || form == CROP || form == LAYER) break;
            case 2:
                if (form == PILLAR) break;
            case 3:
                if (form == PILLAR_BOTTOM) break;
            case 6:
                if (form == FULL_CUSTOM) break;
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
        return tinted ? this.blockForm.baseBakedModelTined : this.blockForm.baseBakedModel;
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
        ALL("hbm:block/cube_all_tinted", "minecraft:block/cube_all", 1, new String[]{"all"}),
        CROP( "minecraft:block/crop", "minecraft:block/crop", 1, new String[]{"crop"}),
        LAYER("hbm:block/block_layering_tinted", "minecraft:block/layer", 1, new String[]{"texture"}),
        CROSS("hbm:block/cross_tinted", "minecraft:block/cross", 1, new String[]{"cross"}),
        PILLAR("hbm:block/cube_column_tinted", "minecraft:block/cube_column", 2, new String[]{"end", "side"}),
        PILLAR_BOTTOM("hbm:block/cube_column_tinted", "minecraft:block/cube_column", 3, new String[]{"end", "side", "bottom"}),
        FULL_CUSTOM("hbm:block/cube_tinted", "minecraft:block/cube", 6, new String[]{"up","down","north","south","west","east"});

        public final String baseBakedModelTined;
        public final String baseBakedModel;
        public final int textureNum;
        public final String[] textureWrap;

        BlockForm(String baseBakedModel, String baseBakedModel1, int textureNum, String[] textureWrap) {
            this.baseBakedModelTined = baseBakedModel;
            this.baseBakedModel = baseBakedModel1;
            this.textureNum = textureNum;
            this.textureWrap = textureWrap;
        }
    }
}
