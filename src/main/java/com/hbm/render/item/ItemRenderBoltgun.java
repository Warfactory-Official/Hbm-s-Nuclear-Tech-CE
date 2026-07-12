package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "boltgun")
public class ItemRenderBoltgun extends TEISRBase {

    @Override
    public ModelBinding createModelBinding(Item item) {
        return ModelBinding.inventory(item, ItemCameraTransforms.DEFAULT);
    }

    @Override
    public boolean useIdentityTransform(Item item) {
        return true;
    }

    @Override
    public void renderByItem(ItemStack itemStackIn) {

        GlStateManager.pushMatrix();

        EntityPlayer player = Minecraft.getMinecraft().player;

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);

        switch (type) {

            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
                GlStateManager.translate(0.5, 0.35, -0.25);
                GlStateManager.rotate(15, 0, 0, 1);
                GlStateManager.rotate(80, 0, 1, 0);
                GlStateManager.scale(0.15, 0.15, 0.15);

                GlStateManager.pushMatrix();
                double[] anim = HbmAnimations.getRelevantTransformation("RECOIL", type == TransformType.FIRST_PERSON_LEFT_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                GlStateManager.translate(0, 0, -anim[0]);
                if (anim[0] != 0) player.isSwingInProgress = false;
                ResourceManager.boltgun.renderPart("Barrel");
                GlStateManager.popMatrix();
                break;

            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
            case HEAD:
                GlStateManager.multMatrix(type == TransformType.HEAD ? ItemRenderFrames17.HEAD
                        : type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_LEFT
                        : ItemRenderFrames17.THIRD_PERSON);
                GlStateManager.scale(0.25, 0.25, 0.25);
                GlStateManager.rotate(10, 0, 1, 0);
                GlStateManager.rotate(10, 0, 0, 1);
                GlStateManager.rotate(10, 1, 0, 0);
                GlStateManager.translate(1.5, -0.25, 1);
                break;

            case GROUND:
                GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
                GlStateManager.scale(0.1, 0.1, 0.1);
                break;

            case FIXED:
                GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
                GlStateManager.scale(0.1, 0.1, 0.1);
                break;

            case GUI:
                GlStateManager.enableAlpha();
                GlStateManager.enableLighting();
                GlStateManager.multMatrix(ItemRenderFrames17.GUI);
                GlStateManager.translate(7, 10, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
                GlStateManager.rotate(-135, 1, 0, 0);
                GlStateManager.scale(1.75, 1.75, -1.75);
                break;

            default:
                break;
        }

        ResourceManager.boltgun.renderPart("Gun");
        if (type != TransformType.FIRST_PERSON_RIGHT_HAND && type != TransformType.FIRST_PERSON_LEFT_HAND) {
            ResourceManager.boltgun.renderPart("Barrel");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }
}
