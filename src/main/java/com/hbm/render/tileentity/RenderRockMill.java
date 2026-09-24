package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@AutoRegister
public class RenderRockMill extends TileEntitySpecialRenderer<TileEntityMachineRockMill> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineRockMill mill, double x, double y, double z, float interp, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(mill.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(0, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(270, 0F, 1F, 0F); break;
        }

        bindTexture(ResourceManager.rock_mill_tex);
        ResourceManager.rock_mill.renderPart("Base");
        if(mill.frame) ResourceManager.rock_mill.renderPart("Frame");

        float rot = mill.prevRotation + (mill.rotation - mill.prevRotation) * interp;
        GlStateManager.rotate(rot, 0, -1, 0);
        ResourceManager.rock_mill.renderPart("Wheel");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_rockmill);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {

        return new ItemRenderBase() {

            public void renderInventory() {
                GlStateManager.translate(0, -1.5, 0);
                GlStateManager.scale(3, 3, 3);
            }
            public void renderCommon(ItemStack item) {
                GlStateManager.scale(0.75, 0.75, 0.75);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.rock_mill_tex);
                ResourceManager.rock_mill.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
