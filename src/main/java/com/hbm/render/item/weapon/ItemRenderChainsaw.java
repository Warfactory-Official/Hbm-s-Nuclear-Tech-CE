package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.tool.ItemToolAbilityFueled;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.TEISRBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

@AutoRegister(item = "chainsaw")
public class ItemRenderChainsaw extends TEISRBase {
    @Override
    public void renderByItem(@NotNull ItemStack itemStackIn) {
        this.renderByItem(itemStackIn, 1.0F);
    }

    @Override
    public void renderByItem(@NotNull ItemStack item, float partialTicks) {
        GlStateManager.pushMatrix();

        EntityPlayer player = Minecraft.getMinecraft().player;

        GlStateManager.enableCull();
        bindTexture(ResourceManager.chainsaw_tex);

        switch (type) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND:
                player.isSwingInProgress = false;
                player.swingProgress = 0.0f;

                EnumHand hand = type == TransformType.FIRST_PERSON_RIGHT_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                GlStateManager.scale(0.56, 0.56, 0.56);

                GlStateManager.rotate(-95, 0, 1, 0);
                GlStateManager.rotate(-45, 1, 0, 0);

                GlStateManager.rotate(5, 0, 0, 1);
                GlStateManager.translate(0.66, 0.07, 0.13);

                if (!player.isActiveItemStackBlocking()) {
                    double[] sRot = HbmAnimations.getRelevantTransformation("SWING_ROT", hand);
                    double[] sTrans = HbmAnimations.getRelevantTransformation("SWING_TRANS", hand);
                    GlStateManager.translate(sTrans[0], sTrans[1], sTrans[2]);
                    GlStateManager.rotate(sRot[2], 1, 0, 0);
                    GlStateManager.rotate(sRot[1], 0, 0, 1);
                    GlStateManager.rotate(sRot[0], 0, 1, 0);
                }
                break;

            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND:
                GlStateManager.scale(0.56, 0.56, 0.56);
                GlStateManager.rotate(-180, 0, 1, 0);
                GlStateManager.translate(-1, -1.3, -1.1);
                break;

            case GROUND:
                GlStateManager.translate(0.5, 0.3, 0.5);
                GlStateManager.scale(0.5, 0.5, 0.5);
                break;

            case GUI:
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GlStateManager.enableLighting();

                double s = 0.25D;
                GlStateManager.translate(0.52, 0.36, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
                GlStateManager.rotate(-45, 1, 0, 0);
                GlStateManager.scale(s, s, s);
                break;

            default:
                break;
        }

        ResourceManager.chainsaw.renderPart("Saw");

        for (int i = 0; i < 20; i++) {

            double run = ((ItemToolAbilityFueled) item.getItem()).canOperate(item) ? System.currentTimeMillis() % 100D * 0.25D / 100D : 0.0625D;
            double forward = i * 0.25 + (run) - 2.0625;

            GlStateManager.pushMatrix();

            GlStateManager.translate(0, 0, 1.9375);
            GlStateManager.translate(0, 0.375, 0.5625);
            double angle = MathHelper.clamp(forward, 0, 0.25 * Math.PI);
            GlStateManager.rotate(angle * 180D / (Math.PI * 0.25), 1, 0, 0);
            GlStateManager.translate(0, -0.375, -0.5625);
            if (forward < 0) GlStateManager.translate(0, 0, forward);
            if (forward > Math.PI * 0.25) GlStateManager.translate(0, 0, forward - Math.PI * 0.25);
            ResourceManager.chainsaw.renderPart("Tooth");
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
