package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.*;
import com.hbm.util.ColorUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderRBMKLid extends TileEntitySpecialRenderer<TileEntityRBMKBase> {

	private static final ResourceLocation TEX_FUEL = new ResourceLocation(Tags.MODID, "textures/blocks/rbmk/rbmk_element_fuel.png");
	private static final ResourceLocation TEX_CONTROL = new ResourceLocation(Tags.MODID, "textures/blocks/rbmk/rbmk_element_control.png");

	@Override
	public boolean isGlobalRenderer(@NotNull TileEntityRBMKBase te) {
		return true;
	}

	@Override
	public void render(@NotNull TileEntityRBMKBase te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		int offset = 1;
		for (int o = 1; o < 16; o++) {
			IBlockState stateAbove = te.getWorld().getBlockState(te.getPos().up(o));
			if (stateAbove.getBlock() == te.getBlockType()) {
				offset = o;
				int meta = stateAbove.getBlock().getMetaFromState(stateAbove);
				if (meta > 5 && meta < 12) break;
			} else break;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);

		if (te instanceof TileEntityRBMKRod rodTe) {
			boolean hasRod = rodTe.hasRod;

			if (hasRod) {
				GlStateManager.pushMatrix();
				this.bindTexture(TEX_FUEL);
				GlStateManager.color(ColorUtil.fr(rodTe.rodColor), ColorUtil.fg(rodTe.rodColor), ColorUtil.fb(rodTe.rodColor), 1.0F);

				for (int i = 0; i <= offset; i++) {
					ResourceManager.rbmk_element_rods_vbo.renderPart("Rods");
					GlStateManager.translate(0, 1, 0);
				}

				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.popMatrix();
			}

			if (rodTe.fluxQuantity > 5) {
				renderCherenkovEffect(0.4F, 0.9F, 1.0F, 0.1F, offset + 1);
			}
		}

		if (te instanceof TileEntityRBMKControl) {
			this.bindTexture(TEX_CONTROL);
			GlStateManager.pushMatrix();
			ResourceManager.rbmk_element_rods.renderPart("Control");
			GlStateManager.popMatrix();
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
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		for (double j = 0; j <= height; j += 0.25) {
			buf.pos(-0.5, j, -0.5).color(r, g, b, a).endVertex();
			buf.pos(-0.5, j, 0.5).color(r, g, b, a).endVertex();
			buf.pos(0.5, j, 0.5).color(r, g, b, a).endVertex();
			buf.pos(0.5, j, -0.5).color(r, g, b, a).endVertex();
		}
		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
