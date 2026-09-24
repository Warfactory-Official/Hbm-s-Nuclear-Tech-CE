package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderMassStorage extends TileEntitySpecialRenderer<TileEntityMassStorage> {

    @Override
    public void render(@NotNull TileEntityMassStorage storage, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (storage.type == null || storage.type.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = this.getFontRenderer();
        RenderItem itemRenderer = mc.getRenderItem();
        EnumFacing facing = EnumFacing.byHorizontalIndex(storage.getBlockMetadata()).getOpposite();

        // fuck this shit, push pop the whole ass lighting state then for all I fucken care
        RenderUtil.pushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_COLOR_BUFFER_BIT);

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableNormalize();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

        GlStateManager.pushMatrix();
        {
            // align to block
            GlStateManager.translate(x, y, z);

            // align item (and flip)
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(BlockBakeFrame.getYRotationForFacing(facing), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            GlStateManager.translate(0.0F, 0.0F, -0.005F); // offset to prevent z-fighting
            GlStateManager.scale(1.0F / 16.0F, 1.0F / 16.0F, -0.0001F); // scale to block size

            GlStateManager.pushMatrix();
            {
                GlStateManager.translate(4.0F, 2.5F, 0.0F); // adjust to centered location
                GlStateManager.scale(8.0F / 16.0F, 8.0F / 16.0F, 1.0F); // scale to 8 pixels across

                if (mc.gameSettings.fancyGraphics) {
                    itemRenderer.renderItemAndEffectIntoGUI(storage.type, 0, 0);
                } else {
                    itemRenderer.renderItemIntoGUI(storage.type, 0, 0);
                }
            }
            GlStateManager.popMatrix();

            GlStateManager.color(1.0F, 1.0F, 1.0F);

            String text = getTextForCount(storage.getStockpile(), fontRenderer.getUnicodeFlag());

            int textX = 32 - fontRenderer.getStringWidth(text) / 2;
            int textY = 44;

            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            {
                GlStateManager.scale(4.0F / 16.0F, 4.0F / 16.0F, 4.0F / 16.0F);

                int fontColor = 0x00FF00;

                // funky text shadow rendering with no z-fighting and alpha testing still enabled
                fontRenderer.drawString(text, textX + 1, textY + 1, (fontColor & 16579836) >> 2 | fontColor & -16777216);
                GlStateManager.translate(0.0F, 0.0F, 1.0F);
                fontRenderer.drawString(text, textX, textY, 0x00FF00);
            }
            GlStateManager.popMatrix();

            GlStateManager.disableTexture2D();

            double fraction = (double) storage.getStockpile() / (double) storage.getCapacity();

            GlStateManager.color((float) (1.0 - fraction), (float) fraction, 0.0F, 1.0F);

            double bMinX = 2;
            double bMaxX = 2 + fraction * 12;
            double bMinY = 13.5;
            double bMaxY = 14;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferbuilder.pos(bMinX, bMaxY, 0.0D).endVertex();
            bufferbuilder.pos(bMaxX, bMaxY, 0.0D).endVertex();
            bufferbuilder.pos(bMaxX, bMinY, 0.0D).endVertex();
            bufferbuilder.pos(bMinX, bMinY, 0.0D).endVertex();
            tessellator.draw();

            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
        }
        GlStateManager.popMatrix();

        RenderUtil.popAttrib();
    }

    private String getTextForCount(int stackSize, boolean isUnicode) {
        if (stackSize >= 100000000 || (stackSize >= 1000000 && isUnicode)) return String.format("%.0fM", stackSize / 1000000f);
        if (stackSize >= 1000000) return String.format("%.1fM", stackSize / 1000000f);
        if (stackSize >= 100000 || (stackSize >= 10000 && isUnicode)) return String.format("%.0fK", stackSize / 1000f);
        if (stackSize >= 10000) return String.format("%.1fK", stackSize / 1000f);
        return String.valueOf(stackSize);
    }
}