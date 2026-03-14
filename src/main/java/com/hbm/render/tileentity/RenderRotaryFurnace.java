package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderRotaryFurnace extends TileEntitySpecialRenderer<TileEntityMachineRotaryFurnace> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineRotaryFurnace furnace, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (furnace.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.rotary_furnace_tex);
        ResourceManager.rotary_furnace.renderPart("Furnace");
        GlStateManager.pushMatrix();

        float anim = furnace.lastAnim + (furnace.anim - furnace.lastAnim) * partialTicks;

        GlStateManager.translate(0, BobMathUtil.sps((anim * 0.75) * 0.125) * 0.5 - 0.5, 0);
        ResourceManager.rotary_furnace.renderPart("Piston");
        GlStateManager.popMatrix();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_rotary_furnace);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(3.5, 3.5, 3.5);
            }

            public void renderCommon() {
                GlStateManager.scale(0.625, 0.625, 0.625);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.rotary_furnace_tex);
                ResourceManager.rotary_furnace.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
