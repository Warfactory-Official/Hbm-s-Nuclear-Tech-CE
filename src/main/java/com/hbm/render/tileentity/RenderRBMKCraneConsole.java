package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKCraneConsole;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderRBMKCraneConsole extends TileEntitySpecialRenderer<TileEntityRBMKCraneConsole> implements IItemRendererProvider {

    @Override
    public boolean isGlobalRenderer(TileEntityRBMKCraneConsole te) {
        return true;
    }

    @Override
    public void render(TileEntityRBMKCraneConsole console, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        int teFacing = switch (console.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> 90;
            case 4 -> 180;
            case 3 -> 270;
            default -> 0;
        };
        GlStateManager.rotate(teFacing, 0F, 1F, 0F);

        GlStateManager.translate(0.5, 0, 0);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.rbmk_crane_console_tex);
        ResourceManager.rbmk_crane_console.renderPart("Console_Coonsole");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.75, 1, 0);
        GlStateManager.rotate((float) (console.lastTiltFront + (console.tiltFront - console.lastTiltFront) * partialTicks), 0, 0, 1);
        GlStateManager.rotate((float) (console.lastTiltLeft + (console.tiltLeft - console.lastTiltLeft) * partialTicks), 1, 0, 0);
        GlStateManager.translate(-0.75, -1.015, 0);
        ResourceManager.rbmk_crane_console.renderPart("JoyStick");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.25, 0.75);
        double heat = console.loadedHeat;
        GlStateManager.rotate((float) (Math.sin(System.currentTimeMillis() * 0.01 % 360) * 180 / Math.PI * 0.05 + 135 - 270 * heat), 1, 0, 0);
        GlStateManager.translate(0, -1.25, -0.75);
        ResourceManager.rbmk_crane_console.renderPart("Meter1");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.25, 0.25);
        double enrichment = console.loadedEnrichment;
        GlStateManager.rotate((float) (Math.sin(System.currentTimeMillis() * 0.01 % 360) * 180 / Math.PI * 0.05 + 135 - 270 * enrichment), 1, 0, 0);
        GlStateManager.translate(0, -1.25, -0.25);
        ResourceManager.rbmk_crane_console.renderPart("Meter2");
        GlStateManager.popMatrix();

        // 1.12.2 exclusive decorations
        bindTexture(ResourceManager.ks23_tex);
        ResourceManager.rbmk_crane_console.renderPart("Shotgun");
        bindTexture(ResourceManager.mini_nuke_tex);
        ResourceManager.rbmk_crane_console.renderPart("MiniNuke");

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        float lastX = OpenGlHelper.lastBrightnessX;
        float lastY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        if (console.isCraneLoading()) GlStateManager.color(0.8F, 0.8F, 0F); // is the crane loading? yellow
        else if (console.hasItemLoaded()) GlStateManager.color(0F, 1F, 0F); // is the crane loaded? green
        else GlStateManager.color(0F, 0.1F, 0F); // is the crane unloaded? off
        ResourceManager.rbmk_crane_console.renderPart("Lamp1");
        if (console.isAboveValidTarget()) GlStateManager.color(0F, 1F, 0F); // valid? green
        else GlStateManager.color(1F, 0F, 0F); // not valid? red
        ResourceManager.rbmk_crane_console.renderPart("Lamp2");
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F);

        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();

        if (console.setUpCrane) {
            GlStateManager.translate(x + 0.5, y - 1, z + 0.5);
            bindTexture(ResourceManager.rbmk_crane_tex);

            int height = console.height - 6;
            double cranePosX = (console.centerX - console.getPos().getX());
            double cranePosY = (console.centerY - console.getPos().getY()) + 1;
            double cranePosZ = (console.centerZ - console.getPos().getZ());

            GlStateManager.translate(cranePosX, cranePosY, cranePosZ);
            GlStateManager.rotate(teFacing, 0F, 1F, 0F);

            double posX = (console.lastPosFront + (console.posFront - console.lastPosFront) * partialTicks);
            double posZ = (console.lastPosLeft + (console.posLeft - console.lastPosLeft) * partialTicks);

            GlStateManager.translate(-posX, 0, posZ);

            int craneRotationOffset = console.craneRotationOffset;
            GlStateManager.rotate(craneRotationOffset, 0F, 1F, 0F);
            GlStateManager.pushMatrix();
            int girderSpan = 0;

            GlStateManager.rotate(-craneRotationOffset, 0F, 1F, 0F);

            switch(craneRotationOffset) {
                case 0:
                    girderSpan = console.spanF + console.spanB + 1;
                    GlStateManager.translate(posX + console.spanB, 0, 0);
                    break;
                case 90:
                    girderSpan = console.spanL + console.spanR + 1;
                    GlStateManager.translate(0, 0, -posZ - console.spanR);
                    break;
                case 180:
                    girderSpan = console.spanF + console.spanB + 1;
                    GlStateManager.translate(posX - console.spanF, 0, 0);
                    break;
                case 270:
                    girderSpan = console.spanL + console.spanR + 1;
                    GlStateManager.translate(0, 0, -posZ + console.spanL);
                    break;
            }
            GlStateManager.rotate(craneRotationOffset, 0F, 1F, 0F);

            for (int i = 0; i < girderSpan; i++) {
                ResourceManager.rbmk_crane.renderPart("Girder");
                GlStateManager.translate(-1, 0, 0);
            }
            GlStateManager.popMatrix();

            ResourceManager.rbmk_crane.renderPart("Main");

            GlStateManager.pushMatrix();
            for (int i = 0; i < height; i++) {
                ResourceManager.rbmk_crane.renderPart("Tube");
                GlStateManager.translate(0, 1, 0);
            }
            GlStateManager.translate(0, -1, 0);
            ResourceManager.rbmk_crane.renderPart("Carriage");
            GlStateManager.popMatrix();

            GlStateManager.translate(0, -3.25 * (1 - (console.lastProgress + (console.progress - console.lastProgress) * partialTicks)), 0);
            ResourceManager.rbmk_crane.renderPart("Lift");
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.rbmk_crane_console);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -3, 0);
                GlStateManager.scale(3.5, 3.5, 3.5);
            }

            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.rbmk_crane_console_tex);
                ResourceManager.rbmk_crane_console.renderPart("Console_Coonsole");
                ResourceManager.rbmk_crane_console.renderPart("JoyStick");
                ResourceManager.rbmk_crane_console.renderPart("Meter1");
                ResourceManager.rbmk_crane_console.renderPart("Meter2");
                bindTexture(ResourceManager.ks23_tex);
                ResourceManager.rbmk_crane_console.renderPart("Shotgun");
                bindTexture(ResourceManager.mini_nuke_tex);
                ResourceManager.rbmk_crane_console.renderPart("MiniNuke");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}