package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.recipes.FusionRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import com.hbm.util.RenderUtil;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFusionTorus extends TileEntitySpecialRenderer<TileEntityFusionTorus>
        implements IItemRendererProvider {

    @Override
    public void render(TileEntityFusionTorus torus, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y, z + 0.5F);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fusion_torus_tex);
        ResourceManager.fusion_torus.renderPart("Torus");

        GlStateManager.pushMatrix();
        float rot = torus.prevMagnet + (torus.magnet - torus.prevMagnet) * partialTicks;
        GlStateManager.rotate(rot, 0, 1, 0);
        ResourceManager.fusion_torus.renderPart("Magnet");
        GlStateManager.popMatrix();

        if (torus.connections[0]) ResourceManager.fusion_torus.renderPart("Bolts2");
        if (torus.connections[1]) ResourceManager.fusion_torus.renderPart("Bolts4");
        if (torus.connections[2]) ResourceManager.fusion_torus.renderPart("Bolts3");
        if (torus.connections[3]) ResourceManager.fusion_torus.renderPart("Bolts1");

        FusionRecipe recipe = (FusionRecipe) torus.fusionModule.getRecipe();

        if (torus.plasmaEnergy > 0 && recipe != null) {

            long time = Clock.get_ms() + torus.timeOffset;

            float plasmaAlpha = 0.35F + (float) (Math.sin(time / 1000D) * 0.25F);

            float r = recipe.r;
            float g = recipe.g;
            float b = recipe.b;

            double mainOsc = BobMathUtil.sps(time / 1000D) % 1D;
            double glowOsc = Math.sin(time / 2000D) % 1D;
            double glowExtra = time / 10000D % 1D;
            double sparkleSpin = time / 500D * -1 % 1D;
            double sparkleOsc = Math.sin(time / 1000D) * 0.5D % 1D;

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.disableCull();

            GlStateManager.disableLighting();
            boolean prevLighting = RenderUtil.isLightingEnabled();
            if (prevLighting) GlStateManager.disableLighting();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GlStateManager.depthMask(false);

            GlStateManager.color(r, g, b, plasmaAlpha);

            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            bindTexture(ResourceManager.fusion_plasma_tex);
            GlStateManager.translate(0, mainOsc, 0);
            ResourceManager.fusion_torus.renderPart("Plasma");
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            if (MainRegistry.proxy.me().getDistanceSq(torus.getPos().getX() + 0.5, torus.getPos().getY() + 2.5, torus.getPos().getZ() + 0.5) < 100 * 100) {

                GlStateManager.color(r * 2, g * 2, b * 2, plasmaAlpha * 2);

                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                bindTexture(ResourceManager.fusion_plasma_glow_tex);
                GlStateManager.translate(0, glowOsc + glowExtra, 0);
                ResourceManager.fusion_torus.renderPart("Plasma");
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);

                GlStateManager.color(r * 2, g * 2, b * 2, 0.75F);

                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                bindTexture(ResourceManager.fusion_plasma_sparkle_tex);
                GlStateManager.translate(sparkleSpin, sparkleOsc, 0);
                ResourceManager.fusion_torus.renderPart("Plasma");
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }

            GlStateManager.enableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            if (prevLighting) GlStateManager.enableLighting();

            GlStateManager.enableCull();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.fusion_torus);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.scale(2, 2, 2);
            }

            public void renderCommon() {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fusion_torus_tex);
                ResourceManager.fusion_torus.renderPart("Torus");
                GlStateManager.pushMatrix();
                double rot = ((double) System.currentTimeMillis() / 5 % 360);
                GlStateManager.rotate(rot, 0, 1, 0);
                ResourceManager.fusion_torus.renderPart("Magnet");
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
