package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;

import java.nio.FloatBuffer;

@AutoRegister(item = "boltgun")
public class ItemRenderBoltgun extends TEISRBase {

    ViewModelPositonDebugger offsets = new ViewModelPositonDebugger();
//            .get(TransformType.GUI)
//            .setScale(0.11f).setPosition(-4.15, 3.30, -3.35).setRotation(0, 135, -90)
//            .getHelper()
//            .get(TransformType.FIRST_PERSON_RIGHT_HAND)
//            .setPosition(-6.75, 0.55, 2.25).setRotation(80, 5, -180)
//            .getHelper()
//            .get(TransformType.FIRST_PERSON_LEFT_HAND)
//            .setPosition(-10.5, -1, 0).setRotation(180, 165, -180)
//            .getHelper()
//            .get(TransformType.THIRD_PERSON_RIGHT_HAND)
//            .setScale(0.1f).setPosition(-4.25, 5, -5.5).setRotation(-5, 90, 0)
//            .getHelper()
//            .get(TransformType.THIRD_PERSON_LEFT_HAND)
//            .setScale(1.03f).setPosition(-0.75, -0.25, 0.25).setRotation(5, 0, 0)
//            .getHelper()
//            .get(TransformType.GROUND)
//            .setPosition(-10, 10, -10).setRotation(0, 0, 0).setScale(0.05f)
//            .getHelper();
//    //Norwood: This is great and all but eulerian angles' order of rotation is important. You should probably use quaternions instead but I'm too lazy to do that.
//    //For now, just queue multiple rotations in the correct order. //TODO: Make angles use quaternions
//    ViewModelPositonDebugger.offset corrections = new ViewModelPositonDebugger.offset(offsets)
//            .setRotation(0, 5, 0);


    @Override
    public void renderByItem(ItemStack itemStackIn) {

        GlStateManager.pushMatrix();
        // Begin debug group
        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPushDebugGroup(
                    KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                    1,
                    "Boltgun render; 1.12.2; type = " + type
            );
        }


        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);

        // Track cumulative transforms
        double cumRotX = 0, cumRotY = 0, cumRotZ = 0;
        double cumTransX = 0, cumTransY = 0, cumTransZ = 0;
        double cumScaleX = 1, cumScaleY = 1, cumScaleZ = 1;

        EntityPlayer player = Minecraft.getMinecraft().player;

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);
        switch (type) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
//                offsets.apply(type);


                double s0 = 0.15D;

                FloatBuffer buf = BufferUtils.createFloatBuffer(16);
                Matrix4f FPE17 = new Matrix4f(
                        -0.048f, -0.253f,  -0.542f,  0.000f,
                        -0.022f,  0.544f, -0.252f,  0.000f,
                        0.598f,  0.000f, -0.053f,  0.000f,
                        0.606f, -0.436f, -0.198f,  1.000f
                );
                FPE17.translate(0.5F, 0.35F, -0.25F);
                FPE17.rotate((float)Math.toRadians(15), 0f, 0f, 1f);
                FPE17.rotate((float)Math.toRadians(80), 0f, 1f, 0f);
                FPE17.scale((float) s0);
                FPE17.get(buf);
                GlStateManager.loadIdentity();
                GlStateManager.multMatrix(buf);



//                GlStateManager.translate(0.5F, 0.35F, -0.25F);
//                GlStateManager.rotate(15F, 0F, 0F, 1F);
//                GlStateManager.rotate(80F, 0F, 1F, 0F);
//                GlStateManager.scale((float) s0, (float) s0, (float) s0);
                cumScaleX *= s0; cumScaleY *= s0; cumScaleZ *= s0;

                if(GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glDebugMessageInsert(
                            KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                            KHRDebug.GL_DEBUG_TYPE_MARKER,
                            1001,
                            KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                            String.format(
                                    "FP setup cumulative transforms: Trans=(%.2f,%.2f,%.2f) Rot=(%.2f,%.2f,%.2f) Scale=(%.2f,%.2f,%.2f)",
                                    cumTransX, cumTransY, cumTransZ,
                                    cumRotX, cumRotY, cumRotZ,
                                    cumScaleX, cumScaleY, cumScaleZ
                            )
                    );
                }

                GlStateManager.pushMatrix();
                double[] anim = HbmAnimations.getRelevantTransformation("RECOIL", type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                GlStateManager.translate(0F, 0F, (float) -anim[0]);
                if (anim[0] != 0)
                    player.isSwingInProgress = false;


                if(GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glDebugMessageInsert(
                            KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                            KHRDebug.GL_DEBUG_TYPE_MARKER,
                            1004,
                            KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                            String.format(
                                    "FP Barrel translation due to RECOIL: Trans=(%.2f,%.2f,%.2f)",
                                    cumTransX, cumTransY, cumTransZ
                            )
                    );
                }

                ResourceManager.boltgun.renderPart("Barrel");

                GlStateManager.popMatrix();
            }


            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
//                offsets.apply(type);
                double scale = 0.1D;
                GlStateManager.scale((float) scale, (float) scale, (float) scale);
                GlStateManager.rotate(10F, 0F, 1F, 0F);
                GlStateManager.rotate(10F, 0F, 0F, 1F);
                GlStateManager.rotate(10F, 1F, 0F, 0F);
                GlStateManager.translate(1.5F, -0.25F, 1F);
                cumScaleX *= scale; cumScaleY *= scale; cumScaleZ *= scale;

                if(GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glDebugMessageInsert(
                            KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                            KHRDebug.GL_DEBUG_TYPE_MARKER,
                            1005,
                            KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                            String.format(
                                    "Entity cumulative transforms: Trans=(%.2f,%.2f,%.2f) Rot=(%.2f,%.2f,%.2f) Scale=(%.2f,%.2f,%.2f)",
                                    cumTransX, cumTransY, cumTransZ,
                                    cumRotX, cumRotY, cumRotZ,
                                    cumScaleX, cumScaleY, cumScaleZ
                            )
                    );
                }

            }
            case GROUND -> {
//                offsets.apply(type);
                double s1 = 0.1D;
                GlStateManager.scale((float) s1, (float) s1, (float) s1);
                cumScaleX *= s1; cumScaleY *= s1; cumScaleZ *= s1;

                if(GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glDebugMessageInsert(
                            KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                            KHRDebug.GL_DEBUG_TYPE_MARKER,
                            1005,
                            KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                            String.format(
                                    "Entity cumulative transforms: Trans=(%.2f,%.2f,%.2f) Rot=(%.2f,%.2f,%.2f) Scale=(%.2f,%.2f,%.2f)",
                                    cumTransX, cumTransY, cumTransZ,
                                    cumRotX, cumRotY, cumRotZ,
                                    cumScaleX, cumScaleY, cumScaleZ
                            )
                    );
                }
            }
            case GUI, FIXED -> {
//                offsets.apply(type);
                GlStateManager.enableAlpha();
                GlStateManager.enableLighting();

                double s = 1.75D;
                GlStateManager.translate(7F, 10F, 0F);
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.rotate(-135F, 1F, 0F, 0F);
                GlStateManager.scale((float) s, (float) s, (float) -s);
                cumScaleX *= s; cumScaleY *= s; cumScaleZ *= -s;

                if(GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glDebugMessageInsert(
                            KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                            KHRDebug.GL_DEBUG_TYPE_MARKER,
                            1003,
                            KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                            String.format(
                                    "Inventory cumulative transforms: Trans=(%.2f,%.2f,%.2f) Rot=(%.2f,%.2f,%.2f) Scale=(%.2f,%.2f,%.2f)",
                                    cumTransX, cumTransY, cumTransZ,
                                    cumRotX, cumRotY, cumRotZ,
                                    cumScaleX, cumScaleY, cumScaleZ
                            )
                    );
                }

            }
            default -> {
            }
        }


        ResourceManager.boltgun.renderPart("Gun");
        if (type != type.FIRST_PERSON_RIGHT_HAND && type != type.FIRST_PERSON_LEFT_HAND) {
            ResourceManager.boltgun.renderPart("Barrel");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
//        ViewModelPositonDebugger.renderGizmo(4f, 3f);
        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPopDebugGroup();
        }

        GlStateManager.popMatrix();
    }


}
