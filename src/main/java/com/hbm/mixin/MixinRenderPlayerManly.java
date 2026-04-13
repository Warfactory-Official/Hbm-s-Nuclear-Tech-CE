package com.hbm.mixin;

import com.hbm.packet.PermaSyncHandler;
import com.hbm.render.model.ModelMan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderPlayer.class)
// note: the erasure must match RenderLivingBase or the @Override trick won't work due to specialization
public abstract class MixinRenderPlayerManly extends RenderLivingBase<EntityLivingBase> {

    @Shadow
    protected abstract void setModelVisibilities(AbstractClientPlayer clientPlayer);

    @Shadow
    public abstract ModelPlayer getMainModel();

    @Unique
    private final ModelMan hbm$manlyModel = new ModelMan();

    protected MixinRenderPlayerManly(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Override
    protected void renderModel(EntityLivingBase player, float limbSwing, float limbSwingAmount, float ageInTicks,
                               float netHeadYaw, float headPitch, float scaleFactor) {
        if (!hbm$isManly((AbstractClientPlayer) player)) {
            super.renderModel(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            return;
        }

        boolean visible = this.isVisible(player);
        boolean translucent = !visible && !player.isInvisibleToPlayer(Minecraft.getMinecraft().player);

        if (!visible && !translucent) {
            return;
        }

        if (translucent) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        }

        hbm$manlyModel.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

        if (translucent) {
            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderLayers(EntityLivingBase player, float limbSwing, float limbSwingAmount, float partialTicks,
                                float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
        if (!hbm$isManly((AbstractClientPlayer) player)) {
            super.renderLayers(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);
            return;
        }

        // I hate myself for writing this, yes this is type safe
        for (LayerRenderer layer : this.layerRenderers) {
            if (layer instanceof LayerCape || layer instanceof LayerDeadmau5Head) {
                continue;
            }

            boolean brightness = this.setBrightness(player, partialTicks, layer.shouldCombineTextures());
            layer.doRenderLayer(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);

            if (brightness) {
                this.unsetBrightness();
            }
        }
    }

    @Inject(method = "renderRightArm", at = @At("HEAD"), cancellable = true, require = 1)
    private void hbm$renderRightArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (!hbm$isManly(clientPlayer)) return;

        ModelPlayer modelPlayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        modelPlayer.swingProgress = 0.0F;
        modelPlayer.isSneak = false;
        modelPlayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        hbm$manlyModel.renderRightArm(clientPlayer, 0.0625F);
        GlStateManager.disableBlend();
        ci.cancel();
    }

    @Inject(method = "renderLeftArm", at = @At("HEAD"), cancellable = true, require = 1)
    private void hbm$renderLeftArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (!hbm$isManly(clientPlayer)) return;

        ModelPlayer modelPlayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        modelPlayer.swingProgress = 0.0F;
        modelPlayer.isSneak = false;
        modelPlayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        hbm$manlyModel.renderLeftArm(clientPlayer, 0.0625F);
        GlStateManager.disableBlend();
        ci.cancel();
    }

    @Unique
    private static boolean hbm$isManly(AbstractClientPlayer player) {
        return !PermaSyncHandler.boykissers.isEmpty() && PermaSyncHandler.boykissers.contains(player.getEntityId());
    }
}
