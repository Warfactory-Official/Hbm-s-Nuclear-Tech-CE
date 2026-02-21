package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineOrbus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderOrbus extends TileEntitySpecialRenderer<TileEntityMachineOrbus>
        implements IItemRendererProvider {

    @Override
    public boolean isGlobalRenderer(TileEntityMachineOrbus te) {
        return true;
    }

    @Override
    public void render(
            TileEntityMachineOrbus orbus,
            double x,
            double y,
            double z,
            float partialTicks,
            int destroyStage,
            float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        switch (orbus.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.translate(1F, 0F, 1F);
            case 4 -> GlStateManager.translate(1F, 0F, 0F);
            case 3 -> GlStateManager.translate(0F, 0F, 0F);
            case 5 -> GlStateManager.translate(0F, 0F, 1F);
        }

        double scale = (double) orbus.tankNew.getFill() / (double) orbus.tankNew.getMaxFill();

        if (orbus.tankNew.getFill() > 0) {
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();

            int c = orbus.tankNew.getTankType().getColor();
            float red = ((c >> 16) & 0xFF) / 255.0F;
            float green = ((c >> 8) & 0xFF) / 255.0F;
            float blue = (c & 0xFF) / 255.0F;
            GlStateManager.color(red, green, blue, 1.0F);

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    0,
                    2.5D + Math.sin(((orbus.getWorld().getTotalWorldTime() + partialTicks) * 0.1D) % (Math.PI * 2D)) * 0.125 * scale,
                    0);
            GlStateManager.scale(scale, scale, scale);

            ResourceManager.sphere_uv.renderAll();

            GlStateManager.popMatrix();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
        }

        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        bindTexture(ResourceManager.orbus_tex);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.orbus.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        if (orbus.tankNew.getFill() > 0) {
            GlStateManager.translate(0, 1, 0);

            BeamPronter.prontBeam(
                    new Vec3d(0, 3, 0),
                    EnumWaveType.SPIRAL,
                    EnumBeamType.SOLID,
                    0x101020,
                    0x101020,
                    0,
                    1,
                    0F,
                    6,
                    (float) scale * 0.5F);
            BeamPronter.prontBeam(
                    new Vec3d(0, 3, 0),
                    EnumWaveType.RANDOM,
                    EnumBeamType.SOLID,
                    0x202060,
                    0x202060,
                    (int) (orbus.getWorld().getTotalWorldTime() / 2) % 1000,
                    6,
                    (float) scale,
                    2,
                    0.0625F * (float) scale);
            BeamPronter.prontBeam(
                    new Vec3d(0, 3, 0),
                    EnumWaveType.RANDOM,
                    EnumBeamType.SOLID,
                    0x202060,
                    0x202060,
                    (int) (orbus.getWorld().getTotalWorldTime() / 4) % 1000,
                    6,
                    (float) scale,
                    2,
                    0.0625F * (float) scale);
        }

        GlStateManager.popMatrix();

    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_orbus);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -3, 0);
                GlStateManager.scale(2, 2, 2);
            }

            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.orbus_tex);
                ResourceManager.orbus.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
