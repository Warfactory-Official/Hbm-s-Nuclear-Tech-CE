package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePronter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "missile_custom")
public class ItemRenderMissile extends TEISRBase {

    // 1.7-exact frames need identity binding (see ItemRenderFrames17); the old per-context height offsets
    // were compensating for the pre-e6fceb4a9 non-identity TEISR binding.
    @Override
    public ModelBinding createModelBinding(Item item) {
        return ModelBinding.inventory(item, ItemCameraTransforms.DEFAULT);
    }

    @Override
    public boolean useIdentityTransform(Item item) {
        return true;
    }

    @Override
    public void renderByItem(ItemStack item) {
        MissileMultipart missile = MissileMultipart.loadFromStruct(ItemCustomMissile.getStruct(item));
        if (missile == null)
            return;
        GlStateManager.pushMatrix();
        // ItemRenderFrames17 base frame for the context + verbatim 1.7 ItemRenderMissile body. 1.7 shared one
        // body for EQUIPPED/EQUIPPED_FIRST_PERSON/ENTITY (scale(0.2)·translate(2,0,0)); INVENTORY is separate.
        switch (type) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND ->
                    GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
                    GlStateManager.multMatrix(type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_LEFT : ItemRenderFrames17.THIRD_PERSON);
            case HEAD -> GlStateManager.multMatrix(ItemRenderFrames17.HEAD);
            case GROUND -> GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
            case FIXED -> GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
            case GUI -> GlStateManager.multMatrix(ItemRenderFrames17.GUI);
            default -> {
                GlStateManager.popMatrix();
                return;
            }
        }

        if (type == TransformType.GUI) {
            // 1.7 INVENTORY
            double height = missile.getHeight();
            if (height == 0D)
                height = 4D;
            double size = 20;
            double scale = size / height;
            GL11.glTranslated(height / 2 * scale, 0, 0);
            GL11.glRotated(135, 0, 0, 1);
            GL11.glRotated(215, 1, 0, 0);
            GL11.glTranslated(7, 14, 0);
            GL11.glScaled(-scale, -scale, -scale);
            GL11.glRotatef(System.currentTimeMillis() / 25 % 360, 0, -1, 0);
        } else {
            // 1.7 EQUIPPED / EQUIPPED_FIRST_PERSON / ENTITY (shared body)
            double s = 0.2;
            GL11.glScaled(s, s, s);
            GL11.glTranslated(2, 0, 0);
        }
        MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);

        GlStateManager.popMatrix();
    }
}
