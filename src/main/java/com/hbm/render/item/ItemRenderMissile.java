package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePronter;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;

@AutoRegister(item = "missile_custom")
public class ItemRenderMissile extends TEISRBase {

    @Override
    public void renderByItem(ItemStack item) {
        MissileMultipart missile = MissileMultipart.loadFromStruct(ItemCustomMissile.getStruct(item));
        if (missile == null)
            return;
        GlStateManager.pushMatrix();
        switch (type) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {

                double s = 0.2;
                GL11.glScaled(s, s, s);
                GlStateManager.translate(2, 0, 0);
                MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);
            }
            case HEAD, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                double s = 0.2;
                GL11.glScaled(s, s, s);
                double height = missile.getHeight();


                GlStateManager.translate(2, -(height * 0.25), 2);
                MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);

            }

            case GROUND -> {
                double s = 0.2;
                GL11.glScaled(s, s, s);

                GlStateManager.translate(2.5, 0, 2.5);
                MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);

            }

            case  FIXED  -> {
                double height = missile.getHeight();

                if (height == 0D)
                    height = 4D;

                double size = 20;
                double scale = size / height;

                GlStateManager.translate(2.5,(height / 2) * scale, 2.5);

                GL11.glScaled(scale / 14.285714285714285, scale / 14.285714285714285, scale / 14.285714285714285);


                GlStateManager.translate(2.5, 0, 2.5);
                MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);

            }
            case GUI -> {

                double height = missile.getHeight();

                if (height == 0D)
                    height = 4D;

                double size = 20;
                double scale = size / height;

                GlStateManager.translate(height / 2 * scale, 0, 0);
                GlStateManager.translate(-9.2, 0.2, 0);
                GL11.glRotated(45, 0, 0, 1);
                GL11.glRotated(45, 1, 0, 0);

                GL11.glScaled(scale / 14.285714285714285, scale / 14.285714285714285, scale / 14.285714285714285);

                GlStateManager.rotate((float) System.currentTimeMillis() / 25 % 360, 0, -1, 0);
                MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);
            }
            default -> {
            }
        }

        GlStateManager.popMatrix();
    }
}
