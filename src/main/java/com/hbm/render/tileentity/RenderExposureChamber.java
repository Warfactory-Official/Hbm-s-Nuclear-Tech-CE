package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@AutoRegister
public class RenderExposureChamber extends TileEntitySpecialRenderer<TileEntityMachineExposureChamber> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineExposureChamber chamber, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);

        GlStateManager.disableCull();
        GlStateManager.enableLighting();

        switch (chamber.getBlockMetadata() - BlockDummyable.offset) {
            case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.exposure_chamber_tex);
        ResourceManager.exposure_chamber.renderPart("Chamber");

        double rotation = chamber.prevRotation + (chamber.rotation - chamber.prevRotation) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) rotation, 0, 1, 0);
        ResourceManager.exposure_chamber.renderPart("Magnets");
        GlStateManager.popMatrix();

        if (chamber.isOn) {
            float lastLightmapX = OpenGlHelper.lastBrightnessX;
            float lastLightmapY = OpenGlHelper.lastBrightnessY;

            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (rotation / 2D), 0, 1, 0);
            GlStateManager.translate(0, Math.sin((chamber.getWorld().getTotalWorldTime() % (Math.PI * 16D) + partialTicks) * 0.125) * 0.0625, 0);
            ResourceManager.exposure_chamber.renderPart("Core");
            GlStateManager.popMatrix();

            GlStateManager.enableCull();
            GlStateManager.shadeModel(GL11.GL_FLAT);

            int duration = 8;
            Random rand = new Random(chamber.getWorld().getTotalWorldTime() / duration);
            int chance = 2;
            int color = chamber.getWorld().getTotalWorldTime() % duration >= duration / 2 ? 0x80d0ff : 0xffffff;
            rand.nextInt(chance);

            if (rand.nextInt(chance) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 3.675, -7.5);
                BeamPronter.prontBeam(new Vec3d(0, 0, 5), EnumWaveType.RANDOM, EnumBeamType.LINE, color, 0xffffff,
                        (int) (System.currentTimeMillis() % 1000) / 50, 15, 0.125F, 1, 0);
                GlStateManager.popMatrix();
            }
            if (rand.nextInt(chance) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(1.1875, 2.5, -7.5);
                BeamPronter.prontBeam(new Vec3d(0, 0, 5), EnumWaveType.RANDOM, EnumBeamType.LINE, color, 0xffffff,
                        (int) (System.currentTimeMillis() % 1000) / 50, 15, 0.125F, 1, 0);
                GlStateManager.popMatrix();
            }
            if (rand.nextInt(chance) == 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-1.1875, 2.5, -7.5);
                BeamPronter.prontBeam(new Vec3d(0, 0, 5), EnumWaveType.RANDOM, EnumBeamType.LINE, color, 0xffffff,
                        (int) (System.currentTimeMillis() % 1000) / 50, 15, 0.125F, 1, 0);
                GlStateManager.popMatrix();
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 1.75, 0);
            BeamPronter.prontBeam(new Vec3d(0, 1.5, 0), EnumWaveType.RANDOM, EnumBeamType.LINE, 0x80d0ff, 0xffffff,
                    (int) (System.currentTimeMillis() % 1000) / 50, 10, 0.125F, 1, 0);
            BeamPronter.prontBeam(new Vec3d(0, 1.5, 0), EnumWaveType.RANDOM, EnumBeamType.LINE, 0x8080ff, 0xffffff,
                    (int) (System.currentTimeMillis() + 5 % 1000) / 50, 10, 0.125F, 1, 0);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 2.5, 0);
            BeamPronter.prontBeam(new Vec3d(0, 0, -1), EnumWaveType.SPIRAL, EnumBeamType.LINE, 0xffff80, 0xffffff,
                    (int) (System.currentTimeMillis() % 360), 15, 0.125F, 1, 0);
            BeamPronter.prontBeam(new Vec3d(0, 0, -1), EnumWaveType.SPIRAL, EnumBeamType.LINE, 0xff8080, 0xffffff,
                    (int) (System.currentTimeMillis() % 360) + 180, 15, 0.125F, 1, 0);
            GlStateManager.popMatrix();

            GlStateManager.enableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastLightmapX, lastLightmapY);
        } else {
            GlStateManager.enableCull();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_exposure_chamber);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0, -1.5, 0);
                GlStateManager.scale(3, 3, 3);
            }

            @Override
            public void renderCommon() {
                GlStateManager.translate(1.5, 0, 0);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);

                GlStateManager.disableCull();
                GlStateManager.shadeModel(GL11.GL_SMOOTH);

                bindTexture(ResourceManager.exposure_chamber_tex);
                ResourceManager.exposure_chamber.renderAll();

                GlStateManager.shadeModel(GL11.GL_FLAT);
                GlStateManager.enableCull();
            }

            @Override
            public void renderNonInv() {
                GlStateManager.rotate(-45, 0, 1, 0);
                GlStateManager.translate(-0.5F, -0.5F, 0);
            }
        };
    }
}
