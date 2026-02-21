package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockPedestal;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@AutoRegister
public class RenderPedestalTile extends TileEntitySpecialRenderer<BlockPedestal.TileEntityPedestal> {
    @Override
    public void render(BlockPedestal.TileEntityPedestal pedestal, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1, z + 0.5);
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();

        if(pedestal.item != null) {

            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack stack = pedestal.item.copy();
            GlStateManager.scale(1.5, 1.5, 1.5);

            if(!(stack.getItem() instanceof ItemBlock)) {
                GlStateManager.translate(0, 0.125, 0);
                GlStateManager.rotate((player.ticksExisted + partialTicks) % 360, 0.0F, -1.0F, 0.0F);

                GlStateManager.translate(0, Math.sin((player.ticksExisted + partialTicks) * 0.1) * 0.0625, 0);
            } else {
                GlStateManager.translate(0, Math.sin((player.ticksExisted + partialTicks) * 0.1) * 0.0625, 0);
            }

            EntityItem dummy = new EntityItem(pedestal.getWorld(), 0, 0, 0, stack);
            dummy.hoverStart = 0.0F;

            Minecraft.getMinecraft().getRenderManager().renderEntity(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, false);
        }

        GlStateManager.popMatrix();
    }
}
