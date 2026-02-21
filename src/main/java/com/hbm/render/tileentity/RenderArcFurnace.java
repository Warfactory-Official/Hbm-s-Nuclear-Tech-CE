package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderArcFurnace extends TileEntitySpecialRenderer<TileEntityMachineArcFurnaceLarge> implements IItemRendererProvider {

    private static float lastX;
    private static float lastY;
    private static boolean lastLighting;
    private static boolean lastCull;

    public static void fullbright(boolean on) {
        if (on) {
            lastX = OpenGlHelper.lastBrightnessX;
            lastY = OpenGlHelper.lastBrightnessY;
            lastLighting = RenderUtil.isLightingEnabled();
            lastCull = RenderUtil.isCullEnabled();

            if (lastLighting) GlStateManager.disableLighting();
            if (lastCull) GlStateManager.disableCull();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        } else {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
            if (lastLighting) GlStateManager.enableLighting();
            if (lastCull) GlStateManager.enableCull();
        }
    }

    @Override
    public void render(TileEntityMachineArcFurnaceLarge tile, double x, double y, double z, float interp, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (tile.getBlockMetadata() - BlockDummyable.offset) {
            case 2:
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(180F, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(270F, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(0F, 0F, 1F, 0F);
                break;
        }

        float lift = tile.prevLid + (tile.lid - tile.prevLid) * interp;

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.arc_furnace_tex);
        ResourceManager.arc_furnace.renderPart("Furnace");

        if (!tile.liquids.isEmpty()) {
            fullbright(true);
            GlStateManager.translate(0, -1.75 + TileEntityMachineArcFurnaceLarge.getStackAmount(tile.liquids) * 1.75 / TileEntityMachineArcFurnaceLarge.maxLiquid, 0);
            ResourceManager.arc_furnace.renderPart("ContentsHot");
            fullbright(false);
        } else if (tile.hasMaterial) {
            ResourceManager.arc_furnace.renderPart("ContentsCold");
        }

        GlStateManager.translate(0, 2 * lift, 0);
        if (tile.isProgressing)
            GlStateManager.translate(0F, 0F, (float) Math.sin((tile.getWorld().getTotalWorldTime() + interp)) * 0.005F);
        ResourceManager.arc_furnace.renderPart("Lid");
        if (tile.electrodes[0] != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) ResourceManager.arc_furnace.renderPart("Ring1");
        if (tile.electrodes[1] != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) ResourceManager.arc_furnace.renderPart("Ring2");
        if (tile.electrodes[2] != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) ResourceManager.arc_furnace.renderPart("Ring3");
        if (tile.electrodes[0] == TileEntityMachineArcFurnaceLarge.ELECTRODE_FRESH) ResourceManager.arc_furnace.renderPart("Electrode1");
        if (tile.electrodes[1] == TileEntityMachineArcFurnaceLarge.ELECTRODE_FRESH) ResourceManager.arc_furnace.renderPart("Electrode2");
        if (tile.electrodes[2] == TileEntityMachineArcFurnaceLarge.ELECTRODE_FRESH) ResourceManager.arc_furnace.renderPart("Electrode3");

        fullbright(true);
        if (tile.electrodes[0] == TileEntityMachineArcFurnaceLarge.ELECTRODE_USED) ResourceManager.arc_furnace.renderPart("Electrode1Hot");
        if (tile.electrodes[1] == TileEntityMachineArcFurnaceLarge.ELECTRODE_USED) ResourceManager.arc_furnace.renderPart("Electrode2Hot");
        if (tile.electrodes[2] == TileEntityMachineArcFurnaceLarge.ELECTRODE_USED) ResourceManager.arc_furnace.renderPart("Electrode3Hot");
        if (tile.electrodes[0] == TileEntityMachineArcFurnaceLarge.ELECTRODE_DEPLETED) ResourceManager.arc_furnace.renderPart("Electrode1Short");
        if (tile.electrodes[1] == TileEntityMachineArcFurnaceLarge.ELECTRODE_DEPLETED) ResourceManager.arc_furnace.renderPart("Electrode2Short");
        if (tile.electrodes[2] == TileEntityMachineArcFurnaceLarge.ELECTRODE_DEPLETED) ResourceManager.arc_furnace.renderPart("Electrode3Short");
        fullbright(false);

        if (tile.electrodes[0] != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 5.5, 0.5);
            if (tile.isProgressing)
                GlStateManager.rotate((float) (Math.sin((tile.getWorld().getTotalWorldTime() + interp) / 2) * 30.0), 1F, 0F, 0F);
            GlStateManager.translate(0, -5.5, -0.5);
            ResourceManager.arc_furnace.renderPart("Cable1");
            GlStateManager.popMatrix();
        }
        if (tile.electrodes[1] != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 5.5, 0);
            if (tile.isProgressing)
                GlStateManager.rotate((float) (Math.sin((tile.getWorld().getTotalWorldTime() + interp) / 2) * 30.0), 1F, 0F, 0F);
            GlStateManager.translate(0, -5.5, 0);
            ResourceManager.arc_furnace.renderPart("Cable2");
            GlStateManager.popMatrix();
        }
        if (tile.electrodes[2] != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 5.5, -0.5);
            if (tile.isProgressing)
                GlStateManager.rotate((float) (Math.sin((tile.getWorld().getTotalWorldTime() + interp) / 2) * 30.0), 1F, 0F, 0F);
            GlStateManager.translate(0, -5.5, 0.5);
            ResourceManager.arc_furnace.renderPart("Cable3");
            GlStateManager.popMatrix();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_arc_furnace);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0F, -3F, 0F);
                GlStateManager.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon() {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.arc_furnace_tex);
                ResourceManager.arc_furnace.renderPart("Furnace");
                ResourceManager.arc_furnace.renderPart("Lid");
                ResourceManager.arc_furnace.renderPart("Ring1");
                ResourceManager.arc_furnace.renderPart("Ring2");
                ResourceManager.arc_furnace.renderPart("Ring3");
                ResourceManager.arc_furnace.renderPart("Electrode1");
                ResourceManager.arc_furnace.renderPart("Electrode2");
                ResourceManager.arc_furnace.renderPart("Electrode3");
                ResourceManager.arc_furnace.renderPart("Cable1");
                ResourceManager.arc_furnace.renderPart("Cable2");
                ResourceManager.arc_furnace.renderPart("Cable3");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
