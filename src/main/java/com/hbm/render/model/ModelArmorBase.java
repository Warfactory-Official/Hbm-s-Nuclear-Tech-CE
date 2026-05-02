package com.hbm.render.model;

import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public abstract class ModelArmorBase extends ModelBiped {

    private static final float DEG_TO_RAD = (float) Math.PI / 180F;
    int type;

    ModelRendererObj head;
    ModelRendererObj body;
    ModelRendererObj leftArm;
    ModelRendererObj rightArm;
    ModelRendererObj leftLeg;
    ModelRendererObj rightLeg;
    ModelRendererObj leftFoot;
    ModelRendererObj rightFoot;

    public ModelArmorBase(int type) {
        this.type = type;

        //generate null defaults to prevent major breakage from using incomplete models
        head = new ModelRendererObj(null);
        body = new ModelRendererObj(null);
        leftArm = new ModelRendererObj(null).setRotationPoint(-5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(null).setRotationPoint(5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    private static void copyModelAngles(ModelRenderer source, ModelRendererObj dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
    }

    private void copyPropertiesFromBiped(ModelBiped modelBiped) {
        copyModelAngles(modelBiped.bipedHead, this.head);
        copyModelAngles(modelBiped.bipedBody, this.body);
        copyModelAngles(modelBiped.bipedLeftArm, this.leftArm);
        copyModelAngles(modelBiped.bipedRightArm, this.rightArm);
        copyModelAngles(modelBiped.bipedLeftLeg, this.leftLeg);
        copyModelAngles(modelBiped.bipedRightLeg, this.rightLeg);
        copyModelAngles(modelBiped.bipedLeftLeg, this.leftFoot);
        copyModelAngles(modelBiped.bipedRightLeg, this.rightFoot);

        this.swingProgress = modelBiped.swingProgress;
        this.isSneak = modelBiped.isSneak;
        this.isRiding = modelBiped.isRiding;
        this.isChild = modelBiped.isChild;
        this.leftArmPose = modelBiped.leftArmPose;
        this.rightArmPose = modelBiped.rightArmPose;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                       float scale) {

        this.setVisible(false); //Prevents zfighting with skin layers
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        if (this.isChild) {
            GlStateManager.scale(0.75F, 0.75F, 0.75F);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
        }

        renderArmor(entityIn, scale);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }


    @Override
    public void setRotationAngles(float walkCycle, float walkAmplitude, float idleCycle, float headYaw, float headPitch, float scale, Entity entity) {
        boolean copied = false;
        Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);

        if(render instanceof RenderPlayer) {
            this.copyPropertiesFromBiped(((RenderPlayer) render).getMainModel());
            copied = true;
        } else if(render instanceof RenderLivingBase) {
            ModelBase mainModel = ((RenderLivingBase<?>) render).getMainModel();
            if(mainModel instanceof ModelBiped) {
                this.copyPropertiesFromBiped((ModelBiped) mainModel);
                copied = true;
            }
        }

        if(!copied) {
            if(entity instanceof EntityArmorStand) {
                applyArmorStand((EntityArmorStand) entity);
            } else {
                super.setRotationAngles(walkCycle, walkAmplitude, idleCycle, headYaw, headPitch, scale, entity);

                this.isSneak = entity instanceof EntityPlayer && entity.isSneaking();

                if(entity instanceof EntityZombie zombie) {
                    boolean armsRaised = zombie.isArmsRaised();
                    float armYaw = 8F * DEG_TO_RAD;
                    this.bipedLeftArm.rotateAngleY = armYaw;
                    this.bipedRightArm.rotateAngleY = -armYaw;

                    if(armsRaised) {
                        float raisedAngle = -120F * DEG_TO_RAD;
                        this.bipedLeftArm.rotateAngleX = raisedAngle;
                        this.bipedRightArm.rotateAngleX = raisedAngle;
                    }
                }

                copyModelAngles(this.bipedHead, this.head);
                copyModelAngles(this.bipedBody, this.body);
                copyModelAngles(this.bipedLeftArm, this.leftArm);
                copyModelAngles(this.bipedRightArm, this.rightArm);
                copyModelAngles(this.bipedLeftLeg, this.leftLeg);
                copyModelAngles(this.bipedRightLeg, this.rightLeg);
                copyModelAngles(this.bipedLeftLeg, this.leftFoot);
                copyModelAngles(this.bipedRightLeg, this.rightFoot);
            }
        }

        if(this.isSneak) {
            applySneakOffset();
        } else {
            resetOffsets();
        }
    }

    private void applySneakOffset() {
        this.head.offsetY = 4.24F;
        this.head.rotationPointY -= 1.045F;
        this.body.offsetY = 3.425F;
        this.rightArm.offsetY = 3.425F;
        this.leftArm.offsetY = 3.25F;

        this.rightFoot.offsetZ = this.rightLeg.offsetZ = 4F;
        this.leftFoot.offsetZ = this.leftLeg.offsetZ = 4F;

        this.rightFoot.rotationPointY = this.rightLeg.rotationPointY = 12F;
        this.leftFoot.rotationPointY = this.leftLeg.rotationPointY = 12F;

        this.rightFoot.rotationPointZ = this.rightLeg.rotationPointZ = -1F;
        this.leftFoot.rotationPointZ = this.leftLeg.rotationPointZ = -1F;
    }

    private void resetOffsets() {
        this.head.offsetY = 0F;
        this.body.offsetY = 0F;
        this.rightArm.offsetY = 0F;
        this.leftArm.offsetY = 0F;

        this.rightFoot.offsetZ = this.rightLeg.offsetZ = 0F;
        this.leftFoot.offsetZ = this.leftLeg.offsetZ = 0F;
    }

    private void applyArmorStand(EntityArmorStand armorStand) {
        this.bipedHead.rotateAngleX = armorStand.getHeadRotation().getX() * DEG_TO_RAD;
        this.bipedHead.rotateAngleY = armorStand.getHeadRotation().getY() * DEG_TO_RAD;
        this.bipedHead.rotateAngleZ = armorStand.getHeadRotation().getZ() * DEG_TO_RAD;
        this.bipedHead.setRotationPoint(0.0F, 1.0F, 0.0F);

        this.bipedBody.rotateAngleX = armorStand.getBodyRotation().getX() * DEG_TO_RAD;
        this.bipedBody.rotateAngleY = armorStand.getBodyRotation().getY() * DEG_TO_RAD;
        this.bipedBody.rotateAngleZ = armorStand.getBodyRotation().getZ() * DEG_TO_RAD;

        this.bipedLeftArm.rotateAngleX = armorStand.getLeftArmRotation().getX() * DEG_TO_RAD;
        this.bipedLeftArm.rotateAngleY = armorStand.getLeftArmRotation().getY() * DEG_TO_RAD;
        this.bipedLeftArm.rotateAngleZ = armorStand.getLeftArmRotation().getZ() * DEG_TO_RAD;

        this.bipedRightArm.rotateAngleX = armorStand.getRightArmRotation().getX() * DEG_TO_RAD;
        this.bipedRightArm.rotateAngleY = armorStand.getRightArmRotation().getY() * DEG_TO_RAD;
        this.bipedRightArm.rotateAngleZ = armorStand.getRightArmRotation().getZ() * DEG_TO_RAD;

        this.bipedLeftLeg.rotateAngleX = armorStand.getLeftLegRotation().getX() * DEG_TO_RAD;
        this.bipedLeftLeg.rotateAngleY = armorStand.getLeftLegRotation().getY() * DEG_TO_RAD;
        this.bipedLeftLeg.rotateAngleZ = armorStand.getLeftLegRotation().getZ() * DEG_TO_RAD;
        this.bipedLeftLeg.setRotationPoint(1.9F, 11.0F, 0.0F);

        this.bipedRightLeg.rotateAngleX = armorStand.getRightLegRotation().getX() * DEG_TO_RAD;
        this.bipedRightLeg.rotateAngleY = armorStand.getRightLegRotation().getY() * DEG_TO_RAD;
        this.bipedRightLeg.rotateAngleZ = armorStand.getRightLegRotation().getZ() * DEG_TO_RAD;
        this.bipedRightLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);

        this.isSneak = false;
    }


    protected abstract void renderArmor(Entity entity, float scale);
}
