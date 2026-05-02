package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderHeatBoiler extends TileEntitySpecialRenderer<TileEntityHeatBoiler> implements IItemRendererProvider {
    @Override
    public void render(TileEntityHeatBoiler boiler, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();

        switch (boiler.getBlockMetadata() - BlockDummyable.offset) {
            case 3:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
        }
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.boiler_tex);
        if(!boiler.hasExploded) {

            if(boiler.tanks[1].getFill() > boiler.tanks[1].getMaxFill() * 0.9) {
                double sine = Math.sin(System.currentTimeMillis() / 50D % (Math.PI * 2));
                sine *= 0.01D;
                GlStateManager.scale(1 - sine, 1 + sine, 1 - sine);
            }

            GlStateManager.enableCull();
            ResourceManager.boiler.renderAll();
        } else {
            GlStateManager.disableCull();
            ResourceManager.boiler_burst.renderAll();
            GlStateManager.enableCull();
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_boiler);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2.55, 0);
                GlStateManager.scale(3.05, 3.05, 3.05);
            }

            public void renderCommon(ItemStack stack) {
                GlStateManager.scale(1, 1, 1);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.boiler_tex);
                if(stack.getItemDamage() == 1)
                    ResourceManager.boiler_burst.renderAll();
                else
                    ResourceManager.boiler.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
