package com.hbm.render.model;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RBMKControlBakedModel extends AbstractRBMKLiddedBakedModel {

    private final TextureAtlasSprite baseTopSprite;
    private TextureAtlasSprite baseBottomSprite;
    private final TextureAtlasSprite baseSideSprite;

    private final TextureAtlasSprite pipeTopSprite;
    private final TextureAtlasSprite pipeSideSprite;

    private final TextureAtlasSprite lidSprite;

    public RBMKControlBakedModel(TextureAtlasSprite baseTop, TextureAtlasSprite baseSide,
                                 TextureAtlasSprite pipeTop, TextureAtlasSprite pipeSide,
                                 TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
                                 TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
                                 TextureAtlasSprite lidSprite,
                                 boolean isInventory) {
        super((HFRWavefrontObject) ResourceManager.rbmk_rods,
                isInventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK,
                1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.rbmkColumn(),
                coverTop, coverSide, glassTop, glassSide, isInventory);
        this.baseTopSprite = baseTop;
        this.baseSideSprite = baseSide;
        this.baseBottomSprite = baseTop;

        this.pipeTopSprite = pipeTop;
        this.pipeSideSprite = pipeSide;

        this.lidSprite = lidSprite;
    }

    public RBMKControlBakedModel setBottomSprite(TextureAtlasSprite baseBottom) {
        this.baseBottomSprite = baseBottom;
        return this;
    }

    @Override
    protected List<BakedQuad> buildInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            addTexturedBox(quads, 0.0F, i, 0.0F, 1.0F, i + 1.0F, 1.0F, baseTopSprite, baseSideSprite, baseBottomSprite);
        }

        addPipes(quads, 4.0F);

        if (lidSprite != null) {
            quads.addAll(bakeWavefrontAtYOffset(Collections.singleton("Lid"), 3.0F, lidSprite));
        }

        return quads;
    }

    @Override
    protected QuadLookup buildWorldQuads(int lidType, int columnHeight) {
        List<BakedQuad> generalQuads = new ArrayList<>();
        List<BakedQuad>[] sideQuads = createSideArray();

        for (EnumFacing face : EnumFacing.VALUES) {
            addTexturedBoxFace(sideQuads[face.ordinal()], 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, face, baseTopSprite,
                    baseSideSprite, baseBottomSprite);
        }

        if (lidType != RBMKBase.LID_NONE && lidType != RBMKBase.LID_NULL) {
            addLidBox(generalQuads, lidType, columnHeight);
        } else if (lidType != RBMKBase.LID_NULL) {
            addPipes(generalQuads, columnHeight);
        }

        return freeze(generalQuads, sideQuads);
    }

    private void addPipes(List<BakedQuad> quads, float yBase) {
        addTexturedBox(quads, 0.0625F, yBase, 0.0625F, 0.4375F, yBase + 0.125F, 0.4375F, pipeTopSprite, pipeSideSprite,
                pipeTopSprite);
        addTexturedBox(quads, 0.0625F, yBase, 0.5625F, 0.4375F, yBase + 0.125F, 0.9375F, pipeTopSprite, pipeSideSprite,
                pipeTopSprite);
        addTexturedBox(quads, 0.5625F, yBase, 0.5625F, 0.9375F, yBase + 0.125F, 0.9375F, pipeTopSprite, pipeSideSprite,
                pipeTopSprite);
        addTexturedBox(quads, 0.5625F, yBase, 0.0625F, 0.9375F, yBase + 0.125F, 0.4375F, pipeTopSprite, pipeSideSprite,
                pipeTopSprite);
    }

    private List<BakedQuad> bakeWavefrontAtYOffset(Set<String> parts, float yOffsetBlocks, TextureAtlasSprite sprite) {
        return bakeSimpleQuads(parts, 0, 0, 0, true, false, sprite, -1, 0.0F, yOffsetBlocks, 0.0F);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return baseSideSprite;
    }
}
