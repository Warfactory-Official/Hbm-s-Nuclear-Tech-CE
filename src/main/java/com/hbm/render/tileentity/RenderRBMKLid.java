package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import com.hbm.util.ColorUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class RenderRBMKLid extends TileEntitySpecialRenderer<TileEntityRBMKRod> {

	private static final ResourceLocation TEX_FUEL = new ResourceLocation(Tags.MODID, "textures/blocks/rbmk/rbmk_element_fuel.png");
	@Override
	public void render(@NotNull TileEntityRBMKRod te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.hasRod) return;
		int offset = RBMKDials.getColumnHeight(te.getWorld());
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.pushMatrix();
        this.bindTexture(TEX_FUEL);
        GlStateManager.color(ColorUtil.fr(te.rodColor), ColorUtil.fg(te.rodColor), ColorUtil.fb(te.rodColor), 1.0F);
        for (int i = 0; i <= offset; i++) {
            ResourceManager.rbmk_element_rods_vbo.renderPart("Rods");
            GlStateManager.translate(0, 1, 0);
        }
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popMatrix();
		if (te.fluxQuantity > 5) {
			renderCherenkovEffect(0.4F, 0.9F, 1.0F, 0.1F, offset);
		}
		GlStateManager.popMatrix();
	}

	private void renderCherenkovEffect(float r, float g, float b, float a, int height) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.75, 0);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        int layerCount = height * 4 + 1;
        NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionColorQuads(layerCount);
        int packedColor = NTMBufferBuilder.packColor(r, g, b, a);

		for (float j = 0.0F; j <= height; j += 0.25F) {
            buf.appendPositionColorQuadUnchecked(
                    -0.5F, j, -0.5F,
                    -0.5F, j, 0.5F,
                    0.5F, j, 0.5F,
                    0.5F, j, -0.5F,
                    packedColor);
		}
        NTMImmediate.INSTANCE.draw();

        GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
