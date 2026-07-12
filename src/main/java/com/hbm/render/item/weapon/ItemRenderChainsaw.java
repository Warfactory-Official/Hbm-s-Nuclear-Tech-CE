package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.tool.ItemToolAbilityFueled;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.ItemRenderFrames17;
import com.hbm.render.item.TEISRBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

@AutoRegister(item = "chainsaw")
public class ItemRenderChainsaw extends TEISRBase {

    // 1.7 chainsaw is a full IItemRenderer (3D in inventory too), so use the boltgun pattern: 3D GUI via the
    // TEISR with identity binding + ItemRenderFrames17 frames. The old per-context ops were compensating for
    // the pre-e6fceb4a9 non-identity TEISR binding.
    @Override
    public ModelBinding createModelBinding(Item item) {
        return ModelBinding.inventory(item, ItemCameraTransforms.DEFAULT);
    }

    @Override
    public boolean useIdentityTransform(Item item) {
        return true;
    }

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

        // ItemRenderFrames17 base frame + verbatim 1.7 ItemRenderChainsaw body.
        switch (type) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND: {
                if (player != null) {
                    player.isSwingInProgress = false;
                    player.swingProgress = 0.0f;
                }
                EnumHand hand = type == TransformType.FIRST_PERSON_RIGHT_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
                // 1.7 EQUIPPED_FIRST_PERSON
                GlStateManager.translate(0.5, 0.25, -0.25);
                GlStateManager.rotate(45, 0, 0, 1);
                GlStateManager.rotate(80, 0, 1, 0);
                GlStateManager.scale(0.35, 0.35, 0.35);
                if (player != null && !player.isActiveItemStackBlocking()) {
                    double[] sRot = HbmAnimations.getRelevantTransformation("SWING_ROT", hand);
                    double[] sTrans = HbmAnimations.getRelevantTransformation("SWING_TRANS", hand);
                    GlStateManager.translate(sTrans[0], sTrans[1], sTrans[2]);
                    GlStateManager.rotate(sRot[2], 0, 0, 1);
                    GlStateManager.rotate(sRot[1], 0, 1, 0);
                    GlStateManager.rotate(sRot[0], 1, 0, 0);
                }
                break;
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, HEAD:
                GlStateManager.multMatrix(type == TransformType.HEAD ? ItemRenderFrames17.HEAD
                        : type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_FULL3D_LEFT
                        : ItemRenderFrames17.THIRD_PERSON_FULL3D);
                // 1.7 EQUIPPED
                GlStateManager.scale(-0.375, -0.375, -0.375);
                GlStateManager.rotate(85, 0, 1, 0);
                GlStateManager.rotate(135, 1, 0, 0);
                GlStateManager.translate(-0.125, -2.0, 1.75);
                break;
            case GROUND:
                GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
                GlStateManager.scale(0.5, 0.5, 0.5);
                break;
            case FIXED:
                GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
                GlStateManager.scale(0.5, 0.5, 0.5);
                break;
            case GUI:
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GlStateManager.enableLighting();
                GlStateManager.multMatrix(ItemRenderFrames17.GUI);
                // 1.7 INVENTORY
                GlStateManager.translate(8, 10, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
                GlStateManager.rotate(-135, 1, 0, 0);
                GlStateManager.scale(4, 4, -4);
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
