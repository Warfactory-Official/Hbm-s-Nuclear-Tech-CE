package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityCoin;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;

@AutoRegister(entity = EntityCoin.class, factory = "FACTORY")
public class RenderCoin extends Render<EntityCoin> {

    public static final IRenderFactory<EntityCoin> FACTORY = RenderCoin::new;

    public RenderCoin(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityCoin entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate((entity.ticksExisted + partialTicks) * 45.0F, 0.0F, 0.0F, 1.0F);

        double scale = 0.125D;
        GlStateManager.scale(scale, scale, scale);

        this.bindEntityTexture(entity);
        ResourceManager.coin.renderAll();

        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull EntityCoin entity) {
        return ResourceManager.coin_tex;
    }
}
