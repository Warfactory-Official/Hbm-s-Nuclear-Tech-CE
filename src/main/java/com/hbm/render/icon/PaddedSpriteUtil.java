package com.hbm.render.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public final class PaddedSpriteUtil {

    private PaddedSpriteUtil() {
    }

    public static TextureInfo inspectTexture(ResourceLocation textureLocation) {
        return inspectTexture(textureResource(textureLocation), textureLocation);
    }

    public static TextureInfo inspectTexture(ResourceLocation sourceTextureResource, ResourceLocation textureLocation) {
        try {
            BufferedImage image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(sourceTextureResource).getInputStream());
            if (image == null) {
                return TextureInfo.original(textureLocation);
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int requiredMultiple = mipRequirement();
            int size = paddedSpriteSize(width, height, requiredMultiple);
            boolean generated = width != size || height != size;

            return new TextureInfo(
                    textureLocation,
                    generated ? new ResourceLocation(textureLocation.getNamespace(), "generated_atlas/" + textureLocation.getPath()) : textureLocation,
                    width / (float) size,
                    height / (float) size,
                    generated
            );
        } catch (IOException e) {
            return TextureInfo.original(textureLocation);
        }
    }

    public static void register(TextureMap map, TextureInfo textureInfo) {
        if (textureInfo.generated) {
            map.setTextureEntry(new TextureAtlasSpritePadded(
                    textureInfo.spriteLocation.toString(), textureInfo.textureLocation.getPath()));
        } else {
            map.registerSprite(textureInfo.spriteLocation);
        }
    }

    public static TextureAtlasSprite sprite(TextureMap atlas, TextureInfo textureInfo) {
        String spriteName = textureInfo.spriteLocation.toString();
        TextureAtlasSprite sprite = atlas.getTextureExtry(spriteName);
        return sprite != null ? sprite : atlas.getAtlasSprite(spriteName);
    }

    private static ResourceLocation textureResource(ResourceLocation textureLocation) {
        return new ResourceLocation(textureLocation.getNamespace(), "textures/" + textureLocation.getPath() + ".png");
    }

    private static int mipRequirement() {
        int mipLevels = Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels();
        return mipLevels <= 0 ? 1 : 1 << mipLevels;
    }

    private static int paddedSpriteSize(int width, int height, int requiredMultiple) {
        int size = Math.max(width, height);
        int remainder = size % requiredMultiple;
        if (remainder != 0) {
            size += requiredMultiple - remainder;
        }
        return size;
    }

    public static final class TextureInfo {
        public final ResourceLocation textureLocation;
        public final ResourceLocation spriteLocation;
        public final float uScale;
        public final float vScale;
        public final boolean generated;

        public TextureInfo(ResourceLocation textureLocation, ResourceLocation spriteLocation, float uScale,
                           float vScale, boolean generated) {
            this.textureLocation = textureLocation;
            this.spriteLocation = spriteLocation;
            this.uScale = uScale;
            this.vScale = vScale;
            this.generated = generated;
        }

        public static TextureInfo original(ResourceLocation textureLocation) {
            return new TextureInfo(textureLocation, textureLocation, 1.0F, 1.0F, false);
        }
    }
}
