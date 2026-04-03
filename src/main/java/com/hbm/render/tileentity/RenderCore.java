package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.RenderSparks;
import com.hbm.tileentity.machine.TileEntityCore;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Random;
@AutoRegister
public class RenderCore extends TileEntitySpecialRenderer<TileEntityCore> {
    @Override
    public void render(TileEntityCore core, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (core.heat == 0) {
            renderStandby(core, x, y, z);
        } else {

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            if (core.meltdownTick)
                renderFlare(core);
            else
                renderOrb(core, 0, 0, 0);
            GlStateManager.popMatrix();
        }
    }

    public void renderStandby(TileEntityCore core, double x, double y, double z) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableTexture2D();

        GlStateManager.scale(0.25F, 0.25F, 0.25F);
        GlStateManager.color(0.5F, 0.5F, 0.5F);
        ResourceManager.sphere_uv.renderAll();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.scale(1.25F, 1.25F, 1.25F);
        GlStateManager.color(0.1F, 0.2F, 0.4F);
        ResourceManager.sphere_uv.renderAll();
        GlStateManager.disableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        if (core.getWorld().rand.nextInt(50) == 0) {
            for (int i = 0; i < 3; i++) {
                RenderSparks.renderSpark((int) System.currentTimeMillis() / 100 + i * 10000, 0, 0, 0, 1.5F, 5, 10, 0x00FFFF, 0xFFFFFF);
                RenderSparks.renderSpark((int) System.currentTimeMillis() / 50 + i * 10000, 0, 0, 0, 3F, 5, 10, 0x00FFFF, 0xFFFFFF);
            }
        }
        GlStateManager.color(1F, 1F, 1F);
        GlStateManager.popMatrix();
    }

    public void renderOrb(TileEntityCore core, double x, double y, double z) {
        GlStateManager.pushMatrix();

        int color = core.color;

        float r = ((color & 0xFF0000) >> 16) / 256F;
        float g = ((color & 0x00FF00) >> 8) / 256F;
        float b = ((color & 0x0000FF)) / 256F;
        float mod = 0.4F;
        GlStateManager.color(r * mod, g * mod, b * mod);

        int tot = core.tanks[0].getMaxFill() + core.tanks[1].getMaxFill();
        int fill = core.tanks[0].getFill() + core.tanks[1].getFill();

        float scale = 4.5F * fill / tot + 0.5F;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.enableCull();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        GlStateManager.scale(0.25F, 0.25F, 0.25F);
        ResourceManager.sphere_ruv.renderAll();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        double ix = (core.getWorld().getTotalWorldTime() * 0.1D) % (Math.PI * 2D);
        double t = 0.8F;
        float pulse = (float) ((1D / t) * Math.atan((t * Math.sin(ix)) / (1 - t * Math.cos(ix))));

        pulse += 1D;
        pulse /= 2D;

        for (int i = 0; i <= 16; i++) {
            GlStateManager.pushMatrix();

            float s = 1F + 0.25F * i;
            s += (pulse * (20 - i)) * 0.125F;

            GlStateManager.scale(s, s, s);
            ResourceManager.sphere_ruv.renderAll();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }


    public void renderFlare(TileEntityCore core) {
        int color = core.color;
        float r = ((color & 0xFF0000) >> 16) / 255F;
        float g = ((color & 0x00FF00) >> 8) / 255F;
        float b = ((color & 0x0000FF)) / 255F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderHelper.disableStandardItemLighting();

        float f1 = core.getWorld().getTotalWorldTime() / 200.0F;
        float f2 = 0.0F;

        Random random = new Random(432L);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();

        double ix = (core.getWorld().getTotalWorldTime() * 0.2D) % (Math.PI * 2D);
        double t = 0.8F;
        float pulse = (float) ((1D / t) * Math.atan((t * Math.sin(ix)) / (1 - t * Math.cos(ix))));
        pulse += 1D;
        pulse /= 2D;

        float s = 0.875F + pulse * 0.125F;
        GlStateManager.scale(s, s, s);

        int count = 150;
        for (int i = 0; i < count; i++) {
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);

            float f3 = random.nextFloat() * 2.0F + 5.0F + f2 * 10F;
            float f4 = random.nextFloat() * 1.0F + 1.0F + f2 * 2.0F;

            buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(0.0D, 0.0D, 0.0D).color(r, g, b, 1.0F).endVertex();
            buffer.pos(-0.866D * f4, f3, -0.5F * f4).color(r, g, b, 0.0F).endVertex();
            buffer.pos(0.866D * f4, f3, -0.5F * f4).color(r, g, b, 0.0F).endVertex();
            buffer.pos(0.0D, f3, 1.0F * f4).color(r, g, b, 0.0F).endVertex();
            buffer.pos(-0.866D * f4, f3, -0.5F * f4).color(r, g, b, 0.0F).endVertex();
            tessellator.draw();

            GlStateManager.scale(0.999F, 0.999F, 0.999F);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        RenderHelper.enableStandardItemLighting();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
