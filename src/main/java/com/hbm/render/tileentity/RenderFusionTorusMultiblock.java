package com.hbm.render.tileentity;

import com.hbm.blocks.machine.fusion.MachineFusionTorus;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.NTMRenderHelper;
import com.hbm.tileentity.machine.TileEntityFusionTorusStruct;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;

import static com.hbm.render.util.SmallBlockPronter.renderSimpleBlockAt;
import static com.hbm.render.util.SmallBlockPronter.startDrawing;

@AutoRegister
public class RenderFusionTorusMultiblock extends TileEntitySpecialRenderer<TileEntityFusionTorusStruct> {

    public static final TextureAtlasSprite[] componentSprites = new TextureAtlasSprite[7];

    @Override
    public void render(@NotNull TileEntityFusionTorusStruct te,
                       double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        startDrawing();
        NTMRenderHelper.bindBlockTexture();
        NTMRenderHelper.startDrawingTexturedQuads();

        for(int iy = 0; iy < 5; iy++) {
            for(int ix = 0; ix < MachineFusionTorus.layout[0].length; ix++) {
                for(int iz = 0; iz < MachineFusionTorus.layout[0][0].length; iz++) {

                    int ly = iy > 2 ? 4 - iy : iy;
                    int meta = MachineFusionTorus.layout[ly][ix][iz];
                    if (meta == 0) continue;

                    TextureAtlasSprite sprite = componentSprites[meta];
                    if (sprite != null) {
                        renderSimpleBlockAt(sprite,
                                ix - 7,
                                iy,
                                iz - 7);
                    }
                }
            }
        }

        NTMRenderHelper.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
