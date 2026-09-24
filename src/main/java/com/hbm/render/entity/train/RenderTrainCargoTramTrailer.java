package com.hbm.render.entity.train;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.train.TrainCargoTramTrailer;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class RenderTrainCargoTramTrailer extends Render<TrainCargoTramTrailer> {

    private static final double[][][] CRATE_STACKS = {
            {{0.0D, 0.375D, 0.0D}},
            {{0.1D, 0.375D, 0.25D}, {-0.1D, 0.375D, -0.25D}},
            {{0.1D, 0.375D, 0.0D}, {-0.1D, 0.375D, 0.375D}, {-0.1D, 0.375D, -0.375D}},
            {{0.2D, 0.375D, 0.3D}, {0.2D, 0.375D, -0.2D}, {-0.2D, 0.375D, 0.2D}, {-0.2D, 0.375D, -0.3D}},
            {{0.2D, 0.375D, 0.6D}, {0.2D, 0.375D, 0.0D}, {0.2D, 0.375D, -0.5D}, {-0.2D, 0.375D, 0.2D}, {-0.2D, 0.375D, -0.3D}},
            {{0.2D, 0.375D, 0.6D}, {0.2D, 0.375D, 0.0D}, {0.2D, 0.375D, -0.5D}, {-0.2D, 0.375D, 0.5D}, {-0.2D, 0.375D, -0.1D}, {-0.2D, 0.375D, -0.6D}},
            {{0.2D, 0.375D, 0.4D}, {0.2D, 0.375D, 0.0D}, {0.2D, 0.375D, -0.4D}, {-0.2D, 0.375D, 0.3D}, {-0.2D, 0.375D, -0.1D}, {-0.2D, 0.375D, -0.5D}, {0.0D, 0.6875D, -0.25D}},
            {{0.2D, 0.375D, 0.4D}, {0.2D, 0.375D, 0.0D}, {0.2D, 0.375D, -0.4D}, {-0.2D, 0.375D, 0.3D}, {-0.2D, 0.375D, -0.1D}, {-0.2D, 0.375D, -0.5D}, {0.0D, 0.6875D, -0.25D}, {0.0D, 0.6875D, 0.15D}},
            {{0.2D, 0.375D, 0.4D}, {0.2D, 0.375D, 0.0D}, {0.2D, 0.375D, -0.4D}, {-0.2D, 0.375D, 0.3D}, {-0.2D, 0.375D, -0.1D}, {-0.2D, 0.375D, -0.5D}, {0.0D, 0.6875D, -0.25D}, {0.0D, 0.6875D, 0.15D}, {-0.1D, 0.375D, 0.8D}}
    };

    public RenderTrainCargoTramTrailer(RenderManager manager) {
        super(manager);
    }

    @Override
    public void doRender(@NotNull TrainCargoTramTrailer train, double x, double y, double z, float swing, float interp) {
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
        bindTexture(ResourceManager.tram_trailer_tex);
        ResourceManager.train_cargo_tram_trailer.renderAll();
        GlStateManager.enableCull();

        int slots = train.getOccupiedSlots();

        if(slots > 0) {

            EntityItem dummy = new EntityItem(train.world, 0, 0, 0, new ItemStack(ModBlocks.crate));
            dummy.hoverStart = 0.0F;

            double scale = 2;
            GlStateManager.scale(scale, scale, scale);

            int tier = Math.min((slots - 1) / 5, CRATE_STACKS.length - 1);

            for(double[] offset : CRATE_STACKS[tier]) {
                this.renderManager.renderEntity(dummy, offset[0], offset[1], offset[2], 0.0F, interp, false);
            }
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull TrainCargoTramTrailer entity) {
        return ResourceManager.tram_trailer_tex;
    }
}
