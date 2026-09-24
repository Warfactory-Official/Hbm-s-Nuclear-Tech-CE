package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockSkeletonHolder;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

@AutoRegister
public class RenderSkeletonHolderTile extends TileEntitySpecialRenderer<BlockSkeletonHolder.TileEntitySkeletonHolder> {

    @Override
    public void render(BlockSkeletonHolder.TileEntitySkeletonHolder holder, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (holder.item == null || holder.item.isEmpty()) return;
        World world = holder.getWorld();
        EnumFacing facing = world.getBlockState(holder.getPos()).getValue(BlockSkeletonHolder.FACING);
        float yaw = ((5 - facing.getHorizontalIndex()) & 3) * 90.0F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);

        ItemStack stack = holder.item.copy();
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);

        if (!(stack.getItem() instanceof ItemBlock)) {
            GlStateManager.scale(1.5F, 1.5F, 1.5F);
        }
        GlStateManager.translate(0.0F, 0.125F, 0.0F);

        EntityItem dummy = new EntityItem(world, 0, 0, 0, stack);
        dummy.hoverStart = 0.0F;
        Minecraft.getMinecraft().getRenderManager().renderEntity(dummy, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
        GlStateManager.popMatrix();
    }
}
