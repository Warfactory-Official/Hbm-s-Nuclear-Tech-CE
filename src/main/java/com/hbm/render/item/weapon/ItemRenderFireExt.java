package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderFrames17;
import com.hbm.render.item.TEISRBase;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "gun_fireext")
public class ItemRenderFireExt extends TEISRBase {

    @Override
    public ModelBinding createModelBinding(Item item) {
        return ModelBinding.of(new ModelResourceLocation(item.getRegistryName(), "inventory"), ItemCameraTransforms.DEFAULT, false);
    }

    @Override
    public boolean useIdentityTransform(Item item) {
        return true;
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();
        final boolean prevCull = RenderUtil.isCullEnabled();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final int prevShade = RenderUtil.getShadeModel();
        if (!prevCull) GlStateManager.enableCull();

        ResourceLocation tex = ResourceManager.fireext_tex;
        Item item = stack.getItem();
        if (item instanceof ItemGunBaseNT gun) {
            IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
            if (mag != null) {
                if (mag.getType(stack, null) == XFactoryTool.fext_foam) tex = ResourceManager.fireext_foam_tex;
                if (mag.getType(stack, null) == XFactoryTool.fext_sand) tex = ResourceManager.fireext_sand_tex;
            }
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);

        ItemCameraTransforms.TransformType currentType = this.type;
        if (currentType == null) currentType = ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;

        switch (currentType) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                GlStateManager.multMatrix(currentType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
                double s0 = 0.35D;
                GlStateManager.rotate(25F, 0F, 0F, 1F);
                GlStateManager.translate(0.5D, -0.5D, -0.5D);
                GlStateManager.rotate(80F, 0F, 1F, 0F);
                GlStateManager.scale((float) s0, (float) s0, (float) s0);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, HEAD -> {
                GlStateManager.multMatrix(currentType == ItemCameraTransforms.TransformType.HEAD ? ItemRenderFrames17.HEAD
                        : currentType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_LEFT
                        : ItemRenderFrames17.THIRD_PERSON);
                float scale = 0.5F;
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.rotate(20F, 0F, 0F, 1F);
                GlStateManager.rotate(-5F, 0F, 1F, 1F);
                GlStateManager.rotate(10F, 0F, 1F, 0F);
                GlStateManager.rotate(15F, 1F, 0F, 0F);
                GlStateManager.translate(0.75D, -2.75D, 0.5D);
            }
            case GROUND -> {
                GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
                double s1 = 0.3D;
                GlStateManager.scale((float) s1, (float) s1, (float) s1);
            }
            case FIXED -> {
                GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
                double s1 = 0.3D;
                GlStateManager.scale((float) s1, (float) s1, (float) s1);
            }
            case GUI -> {
                GlStateManager.multMatrix(ItemRenderFrames17.GUI);
                GlStateManager.disableCull();
                GlStateManager.enableLighting();
                double s = 4.5D;
                GlStateManager.translate(2D, 14D, 0D);
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.rotate(-135F, 1F, 0F, 0F);
                GlStateManager.rotate((float) (System.currentTimeMillis() / 10 % 360), 0F, 1F, 0F);
                GlStateManager.scale((float) s, (float) s, (float) -s);
            }
            default -> {
            }
        }
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.fireext.renderAll();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
        if (!prevCull) GlStateManager.disableCull();
        if (!prevLighting) GlStateManager.disableLighting();

        GlStateManager.popMatrix();
    }
}
