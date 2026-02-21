package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.item.ItemRenderMissileGeneric;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;

import java.util.function.Consumer;
@AutoRegister
public class RenderLaunchPadRusted extends TileEntitySpecialRenderer<TileEntityLaunchPadRusted> implements IItemRendererProvider {

    @Override
    public void render(TileEntityLaunchPadRusted tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (tileEntity.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            case 4 -> GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            case 3 -> GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            case 5 -> GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
        }

        bindTexture(ResourceManager.missile_pad_rusted_tex);
        ResourceManager.missile_pad.renderAll();

        if (tileEntity.missileLoaded) {
            GlStateManager.translate(0.0D, 1.0D, 0.0D);
            Consumer<TextureManager> renderer = ItemRenderMissileGeneric.renderers.get(new RecipesCommon.ComparableStack(ModItems.missile_doomsday_rusted).makeSingular());
            if (renderer != null) {
                renderer.accept(this.rendererDispatcher.renderEngine);
            }
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.launch_pad_rusted);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0.0D, -1.0D, 0.0D);
                GlStateManager.scale(3.0D, 3.0D, 3.0D);
            }

            @Override
            public void renderCommon() {
                bindTexture(ResourceManager.missile_pad_rusted_tex);
                ResourceManager.missile_pad.renderAll();
            }
        };
    }
}
