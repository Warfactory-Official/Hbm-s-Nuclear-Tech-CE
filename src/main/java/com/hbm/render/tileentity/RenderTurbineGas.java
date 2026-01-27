package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderTurbineGas extends TileEntitySpecialRenderer<TileEntityMachineTurbineGas>
        implements IItemRendererProvider {

    @Override
    public void render(
            TileEntityMachineTurbineGas turbineGas,
            double x,
            double y,
            double z,
            float f,
            int destroyStage,
            float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        switch (turbineGas.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
        }

        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.turbine_gas_tex);
        ResourceManager.turbine_gas.renderAll();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();

    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_turbinegas);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(-0.7, -1, 1.5);//small X change to fit in slot`
                GlStateManager.scale(2.5, 2.5, 2.5);
            }

            public void renderCommon() {
                GlStateManager.disableCull();
                GlStateManager.scale(0.75, 0.75, 0.75);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.turbine_gas_tex);
                ResourceManager.turbine_gas.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
