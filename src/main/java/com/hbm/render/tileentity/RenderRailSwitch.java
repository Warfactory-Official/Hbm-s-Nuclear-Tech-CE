package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.rail.RailStandardSwitchFlipped;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.tileentity.rail.TileEntityRailSwitch;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class RenderRailSwitch extends TileEntitySpecialRenderer<TileEntityRailSwitch> implements IItemRendererProvider {

    @Override
    public void render(@NotNull TileEntityRailSwitch tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        int meta = tileEntity.getBlockMetadata();
        if(meta < 12) return;

        boolean flipped = tileEntity.getWorld().getBlockState(tileEntity.getPos()).getBlock() instanceof RailStandardSwitchFlipped;
        WaveFrontObjectVAO model = flipped ? ResourceManager.rail_standard_switch_flipped : ResourceManager.rail_standard_switch;
        ResourceLocation signTex = flipped ? ResourceManager.rail_switch_sign_flipped_tex : ResourceManager.rail_switch_sign_tex;

        GlStateManager.pushMatrix();

        if(meta == 12) GlStateManager.translate(0.5, 0, 0);
        if(meta == 13) GlStateManager.translate(-0.5, 0, 0);
        if(meta == 14) GlStateManager.translate(0, 0, -0.5);
        if(meta == 15) GlStateManager.translate(0, 0, 0.5);

        GlStateManager.translate(x + 0.5, y, z + 0.5);

        float rotation = 0;
        if(meta == 15) rotation = 90F;
        if(meta == 12) rotation = 180F;
        if(meta == 14) rotation = 270F;
        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);

        bindTexture(ResourceManager.rail_standard_tex);
        model.renderPart("Rail");

        bindTexture(signTex);
        model.renderPart(tileEntity.isSwitched ? "SignTurn" : "SignStraight");

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(@NotNull TileEntityRailSwitch te) {
        return true;
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.rail_large_switch);
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] {
                Item.getItemFromBlock(ModBlocks.rail_large_switch),
                Item.getItemFromBlock(ModBlocks.rail_large_switch_flipped)
        };
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {

        boolean flipped = item == Item.getItemFromBlock(ModBlocks.rail_large_switch_flipped);
        WaveFrontObjectVAO model = flipped ? ResourceManager.rail_standard_switch_flipped : ResourceManager.rail_standard_switch;

        return new ItemRenderBase() {

            public void renderInventory() {
                GlStateManager.translate(0, -0.8, 0);
                GlStateManager.scale(0.8, 0.8, 0.8);
            }

            public void renderCommon(ItemStack itemStack) {
                GlStateManager.translate(2.0, 0, 0);
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                bindTexture(ResourceManager.rail_standard_tex);
                model.renderPart("Rail");
            }
        };
    }
}
