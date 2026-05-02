package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RBMKColumnBakedModel extends AbstractRBMKLiddedBakedModel {

    protected final TextureAtlasSprite topSprite;
    protected final TextureAtlasSprite sideSprite;

    public RBMKColumnBakedModel(
            TextureAtlasSprite top, TextureAtlasSprite side,
            TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
            TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
            boolean isInventory) {
        super(ResourceManager.rbmk_element, DefaultVertexFormats.BLOCK, 1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.rbmkColumn(),
                coverTop, coverSide, glassTop, glassSide, isInventory);
        topSprite = top;
        sideSprite = side;
    }

    @Override
    protected List<BakedQuad> buildInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            addTexturedBox(quads, 0.0F, i, 0.0F, 1.0F, i + 1.0F, 1.0F, topSprite, sideSprite, topSprite);
        }
        return quads;
    }

    @Override
    protected QuadLookup buildWorldQuads(int lidType, int columnHeight) {
        List<BakedQuad> generalQuads = new ArrayList<>();
        List<BakedQuad>[] sideQuads = createSideArray();

        for (EnumFacing face : EnumFacing.VALUES) {
            addTexturedBoxFace(sideQuads[face.ordinal()], 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, face, topSprite, sideSprite, topSprite);
        }

        addLidBox(generalQuads, lidType, columnHeight);

        return freeze(generalQuads, sideQuads);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }
}
