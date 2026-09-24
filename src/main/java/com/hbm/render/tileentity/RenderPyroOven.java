package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister
public class RenderPyroOven extends TileEntitySpecialRenderer<TileEntityMachinePyroOven> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachinePyroOven tile, double x, double y, double z, float f, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch(tile.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
        }
        float anim = tile.prevAnim + (tile.anim - tile.prevAnim) * f;

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.pyrooven_tex);
        ResourceManager.pyrooven.renderPart("Oven");

        GlStateManager.pushMatrix();
        GlStateManager.translate(BobMathUtil.sps(anim * 0.125) / 2 - 0.5, 0, 0);
        ResourceManager.pyrooven.renderPart("Slider");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(1.5, 0, 1.5);
        GlStateManager.rotate((float) (anim * -15D % 360D), 0, 1, 0);
        GlStateManager.translate(-1.5, 0, -1.5);
        ResourceManager.pyrooven.renderPart("Fan");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_pyrooven);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -1, 0);
                GlStateManager.scale(3.5, 3.5, 3.5);
            }
            public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.pyrooven_tex);
                ResourceManager.pyrooven.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
