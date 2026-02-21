package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderRadGen extends TileEntitySpecialRenderer<TileEntityMachineRadGen> implements IItemRendererProvider {

    @Override
    public boolean isGlobalRenderer(@NotNull TileEntityMachineRadGen te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineRadGen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        switch (te.getBlockMetadata() - BlockDummyable.offset) {
            case 2:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.radgen_body_tex);

        ResourceManager.radgen_body.renderPart("Base");

        GlStateManager.pushMatrix();
        if (te.isOn) {
            GlStateManager.translate(0, 1.5, 0);
            GlStateManager.rotate((System.currentTimeMillis() % 3600) * -0.1F, 1F, 0F, 0F);
            GlStateManager.translate(0, -1.5, 0);
        }
        ResourceManager.radgen_body.renderPart("Rotor");
        GlStateManager.popMatrix();

        GlStateManager.disableTexture2D();

        boolean prevLighting = RenderUtil.isLightingEnabled();
        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        if (te.isOn) GlStateManager.color(0F, 1F, 0F);
        else GlStateManager.color(0F, 0.1F, 0F);
        ResourceManager.radgen_body.renderPart("Light");
        GlStateManager.color(1F, 1F, 1F);
        if (prevLighting) GlStateManager.enableLighting();

        int brightness = te.getWorld().getCombinedLight(new BlockPos(te.getPos()), 0);
        int lX = brightness % 65536;
        int lY = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lX, (float) lY);

        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(false);

        GlStateManager.color(0.5F, 0.75F, 1F, 0.3F);
        ResourceManager.radgen_body.renderPart("Glass");
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.depthMask(true);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();

        GlStateManager.enableTexture2D();

        ResourceManager.radgen_body.renderPart("Glass");

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_radgen);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -1, 0);
                GlStateManager.scale(4.5, 4.5, 4.5);
            }

            public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.translate(0.5, 0, 0);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.radgen_body_tex);
                ResourceManager.radgen_body.renderPart("Base");
                ResourceManager.radgen_body.renderPart("Rotor");
                GlStateManager.disableTexture2D();
                GlStateManager.color(0F, 1F, 0F);
                ResourceManager.radgen_body.renderPart("Light");
                GlStateManager.color(1F, 1F, 1F);
                GlStateManager.enableTexture2D();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
