package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityConveyorPress;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;

@AutoRegister
public class RenderConveyorPress extends TileEntitySpecialRenderer<TileEntityConveyorPress> implements IItemRendererProvider {

    @Override
    public void render(TileEntityConveyorPress te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        correctRotation(te);

        bindTexture(ResourceManager.conveyor_press_tex);
        ResourceManager.conveyor_press.renderPart("Press");
        if (te.syncStack != null && !te.syncStack.isEmpty()) {
            GlStateManager.pushMatrix();
            double piston = te.lastPress + (te.renderPress - te.lastPress) * partialTicks;
            GlStateManager.translate(0D, -piston * 0.75D, 0D);
            ResourceManager.conveyor_press.renderPart("Piston");
            GlStateManager.popMatrix();
        }
        bindTexture(ResourceManager.conveyor_press_belt_tex);
        try {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            long time = 0L;
            if(te.getWorld() != null) time = te.getWorld().getTotalWorldTime();
            int ticks = (int) (time % 16L) - 2;
            GlStateManager.translate(0D, ticks / 16D, 0D);
        } finally {
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        ResourceManager.conveyor_press.renderPart("Belt");

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
    }

    @Override
    public void correctRotation(TileEntity te) {
        switch (te.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(90F, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(180F, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(270F, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(0F, 0F, 1F, 0F); break;
        }
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_conveyor_press);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase( ) {
            public void renderInventory() {
                GlStateManager.translate(0, -4, 0);
                GL11.glScaled(4.5, 4.5, 4.5);
            }
            public void renderCommon() {
                bindTexture(ResourceManager.conveyor_press_tex);
                ResourceManager.conveyor_press.renderPart("Press");
                ResourceManager.conveyor_press.renderPart("Piston");
                bindTexture(ResourceManager.conveyor_press_belt_tex);
                ResourceManager.conveyor_press.renderPart("Belt");
            }};
    }
}
