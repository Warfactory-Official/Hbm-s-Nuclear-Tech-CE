package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RBMKRodBakedModel extends AbstractRBMKLiddedBakedModel {

    private final TextureAtlasSprite sideSprite;
    private final TextureAtlasSprite innerSprite;
    private final TextureAtlasSprite capSprite;
    private final TextureAtlasSprite fuelSprite;

    public RBMKRodBakedModel(TextureAtlasSprite side,
                             TextureAtlasSprite inner, TextureAtlasSprite cap, TextureAtlasSprite fuel,
                             TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
                             TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
                             boolean isInventory) {
        super(ResourceManager.rbmk_element, isInventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK,
                isInventory ? 0.35F : 1.0F, 0.5F, isInventory ? -0.175F : 0.0F, 0.5F,
                BakedModelTransforms.isbrh(),
                coverTop, coverSide, glassTop, glassSide, isInventory);
        this.sideSprite = side;
        this.innerSprite = inner;
        this.capSprite = cap;
        this.fuelSprite = fuel;
    }

    @Override
    protected List<BakedQuad> buildInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        EnumFacing[] baseFaces = {
                EnumFacing.NORTH, EnumFacing.SOUTH,
                EnumFacing.EAST, EnumFacing.WEST
        };

        for (int i = 0; i < 4; i++) {
            quads.addAll(bakeWavefrontAtYOffset(Collections.singleton("Inner"), i, innerSprite));
            quads.addAll(bakeWavefrontAtYOffset(Collections.singleton("Cap"), i, capSprite));
            quads.addAll(bakeSimpleQuads(ResourceManager.rbmk_element_rods, Collections.singleton("Rods"),
                    0, 0, 0, true, false, fuelSprite, -1, 0.0F, i * baseScale, 0.0F, 1.0F, 1.0F));

            for (EnumFacing face : baseFaces) {
                addInventoryTexturedBoxFace(quads, 0.0F, i, 0.0F, 1.0F, i + 1.0F, 1.0F, -0.175F,
                        face, sideSprite, sideSprite, sideSprite);
            }
        }
        return quads;
    }

    @Override
    protected QuadLookup buildWorldQuads(int lidType, int columnHeight) {
        List<BakedQuad> generalQuads = new ArrayList<>();
        List<BakedQuad>[] sideQuads = createSideArray();

        generalQuads.addAll(bakeSimpleQuads(Collections.singleton("Inner"), 0, 0, 0, true, false, innerSprite));
        generalQuads.addAll(bakeSimpleQuads(Collections.singleton("Cap"), 0, 0, 0, true, false, capSprite));

        FaceBakery bakery = new FaceBakery();
        Vector3f from = new Vector3f(0, 0, 0);
        Vector3f to = new Vector3f(16, 16, 16);

        EnumFacing[] baseFaces = {
                EnumFacing.NORTH, EnumFacing.SOUTH,
                EnumFacing.EAST, EnumFacing.WEST
        };

        for (EnumFacing face : baseFaces) {
            BlockFaceUV uv = makeFaceUV(face, from, to);
            BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
            BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sideSprite, face,
                    TRSRTransformation.identity(), null, true, true);
            sideQuads[face.ordinal()].add(quad);
        }

        addLidBox(generalQuads, lidType, columnHeight);

        return freeze(generalQuads, sideQuads);
    }

    private List<BakedQuad> bakeWavefrontAtYOffset(Set<String> parts, float yOffsetBlocks, TextureAtlasSprite sprite) {
        return bakeSimpleQuads(parts, 0, 0, 0, true, false, sprite, -1, 0.0F,
                yOffsetBlocks * baseScale, 0.0F);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }
}
