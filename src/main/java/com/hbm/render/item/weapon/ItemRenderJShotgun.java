package com.hbm.render.item.weapon;

import com.hbm.animloader.AnimatedModel.IAnimatedModelCallback;
import com.hbm.animloader.AnimationWrapper;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemGunBase;
import com.hbm.items.weapon.ItemGunJShotty;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.TEISRBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;
@AutoRegister(item = "jshotgun")
public class ItemRenderJShotgun extends TEISRBase {

	public static float[] firstPersonFlashlightPos = null;
	public static Vec3d flashlightDirection;

	@Override
	public void renderByItem(ItemStack stack) {
		boolean prevBlend = RenderUtil.isBlendEnabled();
		int prevSrc = RenderUtil.getBlendSrcFactor();
		int prevDst = RenderUtil.getBlendDstFactor();
		int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
		int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();
        try {
            GlStateManager.translate(0.5, 0.5, 0.5);
            int mag = ItemGunBase.getMag(stack);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.jshotgun_tex);

            switch(type) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                EnumHand hand = type == TransformType.FIRST_PERSON_LEFT_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                AnimationWrapper reload = HbmAnimations.getRelevantBlenderAnim(hand);
                float time = HbmAnimations.getTimeDifference("JS_RECOIL", hand);
                float timeMax = 0.0375F;
                if(time > 0 && time < timeMax){
                    ResourceManager.flash_lmap.use();
                    GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
                    Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.jshotgun_lmap);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                    ResourceManager.flash_lmap.uniform1i("flash_map", 3);
                    GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
                }
                double[] recoil = HbmAnimations.getRelevantTransformation("JS_RECOIL", hand);
                GL11.glScaled(2.5, 2.5, 2.5);
                if(type == TransformType.FIRST_PERSON_RIGHT_HAND) {
                    GL11.glRotated(270 + recoil[0] * 10, 0, 0, 1);
                    GL11.glRotated(95, 0, 1, 0);
                    GlStateManager.translate(0.2, -1.9, -1);
                }
                GlStateManager.translate(0, 0, -recoil[0] * 1.25F);
                if(reload != null) {
                    ResourceManager.jshotgun.controller.setAnim(reload);
                    //ResourceManager.arm_rig.controller.setAnim(reload);
                    //Minecraft.getMinecraft().getTextureManager().bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
                    //ResourceManager.arm_rig.renderAnimated(System.currentTimeMillis());
                    //Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.jshotgun_tex);
                } else {
                    ResourceManager.jshotgun.controller.setAnim(AnimationWrapper.EMPTY);
                }
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                ResourceManager.jshotgun.renderAnimated(System.currentTimeMillis(), new IAnimatedModelCallback() {
                    @Override
                    public boolean onRender(int prevFrame, int currentFrame, int model, float diffN, String modelName) {
                        //Sounds
                        if(prevFrame == 9 && currentFrame == 10){
                            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(HBMSoundHandler.jsg_reload0, 1F, 0.15F));
                        }
                        if(prevFrame == 45 && currentFrame == 46){
                            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(HBMSoundHandler.jsg_reload1, 1F, 0.15F));
                        }
                        if(modelName.startsWith("Main")){
                            firstPersonFlashlightPos = NTMRenderHelper.project(1.31674F, -8.20808F, -1.57076F);
                            flashlightDirection = BobMathUtil.viewFromLocal(new Vector4f(0, -1, 0, 0))[0];
                        }
                        //Render
                        if(modelName.startsWith("Flash")){
                            if(time > 0 && time < timeMax){
                                boolean left = mag == 1 || mag == 2;
                                if((left && modelName.equals("Flash1")) || (!left && modelName.equals("Flash2"))){
                                    Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.mflash);
                                    HbmShaderManager2.releaseShader();
                                    GlStateManager.enableBlend();
                                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
                                    float prevLx = OpenGlHelper.lastBrightnessX;
                                    float prevLy = OpenGlHelper.lastBrightnessY;
                                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                                    GlStateManager.disableLighting();
                                    GlStateManager.depthMask(false);
                                    GlStateManager.disableCull();
                                    GL11.glCallList(model);
                                    GlStateManager.enableCull();
                                    GlStateManager.enableLighting();
                                    GlStateManager.depthMask(true);
                                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLx, prevLy);
                                    GlStateManager.disableBlend();
                                    ResourceManager.flash_lmap.use();
                                    ResourceManager.flash_lmap.uniform1i("flash_map", 3);
                                }
                            }
                            return true;
                        }
                        if(modelName.startsWith("rightArm") || modelName.startsWith("leftArm")) {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
                        } else {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.jshotgun_tex);
                        }
                        boolean done = false;
                        if(reload == null) {
                            done = true;
                        } else {
                            int diff = (int) (System.currentTimeMillis() - reload.startTime);
                            diff *= reload.speedScale;
                            if(diff > reload.anim.length)
                                done = true;
                        }
                        if(done && (modelName.startsWith("rightArm") || modelName.startsWith("leftArm") || modelName.startsWith("Boolet")))
                            return true;
                        return false;
                    }
                });
                if(time > 0 && time < timeMax){
                    HbmShaderManager2.releaseShader();
                }
                GlStateManager.shadeModel(GL11.GL_FLAT);
                return;
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
            case HEAD:
            case FIXED:
            case GROUND:
                GL11.glScaled(0.75, 0.75, 0.75);
                GlStateManager.translate(-0.5, -1.5, 0.8);
                GL11.glRotated(180, 0, 1, 0);
                break;
            case GUI:
                GlStateManager.translate(-0.75, 0, -0.2);
                GL11.glScaled(0.45, 0.45, 0.45);
                GL11.glRotated(180, 0, 1, 0);
                GL11.glRotated(45, 0, 0, 1);
                GL11.glRotated(-45, 1, 0, 0);
                break;
            case NONE:
                break;
            }
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            boolean flash = ItemGunJShotty.getDelay(stack) == ((ItemGunJShotty)stack.getItem()).mainConfig.rateOfFire && MainRegistry.proxy.partialTicks() < 0.75F;
            ResourceManager.jshotgun.render(new IAnimatedModelCallback() {
                @Override
                public boolean onRender(int prevFrame, int currentFrame, int model, float diffN, String modelName) {
                    if(modelName.startsWith("rightArm") || modelName.startsWith("leftArm") || modelName.startsWith("Boolet"))
                        return true;
                    if(modelName.startsWith("Flash")){
                        if(flash && ((mag == 1 && modelName.equals("Flash1")) || (mag == 0 && modelName.equals("Flash2")))){
                            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.mflash);
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
                            float prevLx = OpenGlHelper.lastBrightnessX;
                            float prevLy = OpenGlHelper.lastBrightnessY;
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                            GlStateManager.disableLighting();
                            GlStateManager.depthMask(false);
                            GlStateManager.disableCull();
                            GL11.glCallList(model);
                            GlStateManager.enableCull();
                            GlStateManager.enableLighting();
                            GlStateManager.depthMask(true);
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLx, prevLy);
                            GlStateManager.disableBlend();
                        }
                        return true;
                    }
                    return false;
                }
            });
            GlStateManager.shadeModel(GL11.GL_FLAT);
        } finally {
            GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
            if (prevBlend) {
                GlStateManager.enableBlend();
            } else {
                GlStateManager.disableBlend();
            }
        }
    }
}
