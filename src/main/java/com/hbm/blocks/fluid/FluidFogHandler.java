package com.hbm.blocks.fluid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraft.client.renderer.GlStateManager.FogMode.EXP;

public class FluidFogHandler {
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new FluidFogHandler());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        Entity entity = event.getEntity();
        IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(
                entity.world, entity, (float) event.getRenderPartialTicks());

        if (state.getBlock() instanceof IFluidFog fogBlock) {
            GlStateManager.setFog(EXP);
            event.setDensity(fogBlock.getFogDensity());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        Entity entity = event.getEntity();
        IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(
                entity.world, entity, (float) event.getRenderPartialTicks());

        if (state.getBlock() instanceof IFluidFog fogBlock) {
            int color = fogBlock.getFogColor();
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            event.setRed(red);
            event.setGreen(green);
            event.setBlue(blue);
        }
    }

}
