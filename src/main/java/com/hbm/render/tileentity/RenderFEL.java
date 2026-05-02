package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityFEL;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

import java.awt.*;
@AutoRegister
public class RenderFEL extends TileEntitySpecialRenderer<TileEntityFEL>
        implements IItemRendererProvider {
    @Override
    public void render(
            TileEntityFEL fel,
            double x,
            double y,
            double z,
            float partialTicks,
            int destroyStage,
            float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        switch (fel.getBlockMetadata() - BlockDummyable.offset) {
            case 4 -> GlStateManager.rotate(90F, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(180F, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(270F, 0F, 1F, 0F);
            // case 2 rotation by 0F omitted as it's a no-op
        }

        bindTexture(ResourceManager.fel_tex);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.fel.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        int color;
        if (fel.mode.renderedBeamColor == 0) {
            color = Color.HSBtoRGB(fel.getWorld().getTotalWorldTime() / 2000.0F, 0.5F, 0.1F) & 0xFFFFFF;
        } else {
            color = fel.mode.renderedBeamColor;
        }

        int length = fel.distance - 3;
        GlStateManager.translate(0, 1.5, -1.5);

        if (fel.power > TileEntityFEL.powerReq * Math.pow(4, fel.mode.ordinal())
                && fel.isOn
                && fel.mode != EnumWavelengths.NULL
                && length > 0) {

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            BeamPronter.prontBeam(
                    Vec3.createVectorHelper(0, 0, -length - 1).toVec3d(),
                    EnumWaveType.STRAIGHT,
                    EnumBeamType.SOLID,
                    color,
                    0xFFFFFF,
                    0,
                    1,
                    0,
                    3,
                    0.0625F);
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_fel);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -1, 0);
                GlStateManager.scale(2, 2, 2);
            }

            public void renderCommon() {
                GlStateManager.translate(1, 0, 0);
                GlStateManager.rotate(90, 0, -1, 0);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fel_tex);
                ResourceManager.fel.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
