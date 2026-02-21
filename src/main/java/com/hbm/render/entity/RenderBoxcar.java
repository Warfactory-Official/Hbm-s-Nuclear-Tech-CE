package com.hbm.render.entity;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityBoxcar;
import com.hbm.entity.projectile.EntityTorpedo;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(entity = EntityBoxcar.class, factory = "FACTORY")
@AutoRegister(entity = EntityTorpedo.class, factory = "FACTORY")
public class RenderBoxcar extends Render<Entity> implements IItemRendererProvider {

    public static final IRenderFactory<Entity> FACTORY = RenderBoxcar::new;

    protected RenderBoxcar(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        final boolean prevCull = RenderUtil.isCullEnabled();
        final boolean prevLighting = RenderUtil.isLightingEnabled();

        GlStateManager.translate((float) x, (float) y, (float) z);
        if (!prevCull) GlStateManager.enableCull();
        if (!prevLighting) GlStateManager.enableLighting();

        if (entity instanceof EntityBoxcar) {
            GlStateManager.translate(0F, 0F, -1.5F);
            GlStateManager.rotate(180F, 0F, 0F, 1F);
            GlStateManager.rotate(90F, 1F, 0F, 0F);

            bindTexture(ResourceManager.boxcar_tex);
            ResourceManager.boxcar.renderAll();
        }

        if (entity instanceof EntityTorpedo) {
            final float f = entity.ticksExisted + partialTicks;
            GlStateManager.rotate(Math.min(85F, f * 3F), 1F, 0F, 0F);

            final int prevShade = RenderUtil.getShadeModel();
            if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);

            bindTexture(ResourceManager.torpedo_tex);
            ResourceManager.torpedo.renderAll();

            if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
        }
        if (!prevLighting) GlStateManager.disableLighting();
        if (!prevCull) GlStateManager.disableCull();

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return ResourceManager.boxcar_tex;
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.boxcar);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                GlStateManager.translate(0F, -1F, 0F);
                GlStateManager.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon() {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.boxcar_tex);
                ResourceManager.boxcar.renderAll();
            }
        };
    }
}
