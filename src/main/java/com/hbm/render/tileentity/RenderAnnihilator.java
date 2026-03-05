package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.TileEntityMachineAnnihilator;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@AutoRegister
public class RenderAnnihilator extends TileEntitySpecialRenderer<TileEntityMachineAnnihilator> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineAnnihilator tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F); break;
            case 4: GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F); break;
            case 3: GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); break;
            case 5: GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F); break;
        }

        bindTexture(ResourceManager.annihilator_tex);
        ResourceManager.annihilator.renderPart("Annihilator");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0, 1.75, 0.0);
        GlStateManager.rotate((float)(System.currentTimeMillis() * 0.15 % 360), 0.0F, 0.0F, -1.0F);
        GlStateManager.translate(0.0, -1.75, 0.0);
        ResourceManager.annihilator.renderPart("Roller");
        GlStateManager.popMatrix();

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        bindTexture(ResourceManager.annihilator_belt_tex);
        GlStateManager.translate(-System.currentTimeMillis() / 3000D % 1D, 0.0, 0.0);
        ResourceManager.annihilator.renderPart("Belt");
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_annihilator);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {

        return new ItemRenderBase() {

            public void renderInventory() {
                GlStateManager.translate(0.0, -3.0, 0.0);
                GlStateManager.scale(2.75, 2.75, 2.75);
            }
            public void renderCommon(ItemStack itemStack) {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.annihilator_tex);
                ResourceManager.annihilator.renderPart("Annihilator");
                ResourceManager.annihilator.renderPart("Roller");
                bindTexture(ResourceManager.annihilator_belt_tex);
                ResourceManager.annihilator.renderPart("Belt");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}