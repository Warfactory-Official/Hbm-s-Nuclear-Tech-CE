package com.hbm.render.entity.train;

import com.hbm.entity.train.TrainCargoTram;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderTrainCargoTram extends Render<TrainCargoTram> {

    public RenderTrainCargoTram(RenderManager manager) {
        super(manager);
    }

    @Override
    public void doRender(@NotNull TrainCargoTram train, double x, double y, double z, float swing, float interp) {
        GlStateManager.pushMatrix();

        double iX = train.prevPosX + (train.posX - train.prevPosX) * interp;
        double iY = train.prevPosY + (train.posY - train.prevPosY) * interp;
        double iZ = train.prevPosZ + (train.posZ - train.prevPosZ) * interp;
        double rX = train.lastRenderX + (train.renderX - train.lastRenderX) * interp;
        double rY = train.lastRenderY + (train.renderY - train.lastRenderY) * interp;
        double rZ = train.lastRenderZ + (train.renderZ - train.lastRenderZ) * interp;
        x -= iX - rX;
        y -= iY - rY;
        z -= iZ - rZ;

        GlStateManager.translate(x, y, z);

        float yaw = train.rotationYaw;
        float prevYaw = train.prevRotationYaw;

        if(yaw - prevYaw > 180) yaw -= 360;
        if(prevYaw - yaw > 180) prevYaw -= 360;

        float yawInterp = prevYaw + (yaw - prevYaw) * interp - 720;

        GlStateManager.rotate(-yawInterp, 0, 1, 0);

        float pitch = train.rotationPitch;
        float prevPitch = train.prevRotationPitch;
        float pitchInterp = prevPitch + (pitch - prevPitch) * interp;
        GlStateManager.rotate(-pitchInterp, 1, 0, 0);

        GlStateManager.disableCull();
        bindTexture(ResourceManager.tram_tex);
        ResourceManager.train_cargo_tram.renderAll();
        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull TrainCargoTram entity) {
        return ResourceManager.tram_tex;
    }
}
