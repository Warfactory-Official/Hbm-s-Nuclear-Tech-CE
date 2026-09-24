package com.hbm.render.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.utils.IOUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class TextureAtlasSpritePadded extends TextureAtlasSprite implements ICustomizableSprite {
    private static final String BASE_PATH = "textures";
    private final String sourceTexturePath;

    public TextureAtlasSpritePadded(String spriteName, String sourceTexturePath) {
        super(spriteName);
        this.sourceTexturePath = sourceTexturePath;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        ResourceLocation sourceLocation = completeResourceLocation(new ResourceLocation(location.getNamespace(), sourceTexturePath));
        IResource resource = null;

        try {
            resource = manager.getResource(sourceLocation);
            if (resource.getMetadata("animation") != null) {
                throw new IOException("Animated padded atlas sprites are unsupported: " + sourceLocation);
            }

            BufferedImage image = TextureUtil.readBufferedImage(resource.getInputStream());
            //noinspection ConstantValue
            if (image == null) {
                throw new IOException("Failed to decode texture: " + sourceLocation);
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int mipLevels = Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels();
            int requiredMultiple = mipLevels <= 0 ? 1 : 1 << mipLevels;
            int size = Math.max(width, height);
            int remainder = size % requiredMultiple;
            if (remainder != 0) {
                size += requiredMultiple - remainder;
            }

            this.animationMetadata = null;
            this.clearFramesTextureData();
            this.frameCounter = 0;
            this.tickCounter = 0;
            this.width = size;
            this.height = size;

            int[][] frameData = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
            frameData[0] = new int[size * size];

            for (int y = 0; y < size; y++) {
                int sourceY = Math.min(y, height - 1);
                for (int x = 0; x < size; x++) {
                    int sourceX = Math.min(x, width - 1);
                    frameData[0][y * size + x] = image.getRGB(sourceX, sourceY);
                }
            }

            this.framesTextureData.add(frameData);
        } catch (RuntimeException | IOException e) {
            FMLClientHandler.instance().trackBrokenTexture(sourceLocation, e.getMessage());
            return true;
        } finally {
            IOUtils.closeQuietly(resource);
        }
        return false;
    }

    private ResourceLocation completeResourceLocation(ResourceLocation loc) {
        return new ResourceLocation(loc.getNamespace(), String.format("%s/%s%s", BASE_PATH, loc.getPath(), ".png"));
    }
}
