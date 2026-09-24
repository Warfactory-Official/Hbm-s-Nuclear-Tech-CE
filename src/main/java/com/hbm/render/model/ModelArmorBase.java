package com.hbm.render.model;

import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
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

    /** set by LayerArmorBase#renderArmorLayer right before render(), this is the model we mirror */
    private ModelBiped sourceModel;

    public ModelArmorBase(int type) {
        this.type = type;

        //generate null defaults to prevent major breakage from using incomplete models
        head = new ModelRendererObj(null);
        body = new ModelRendererObj(null);
        leftArm = new ModelRendererObj(null).setRotationPoint(5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(null).setRotationPoint(-5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void setModelAttributes(ModelBase model) {
        super.setModelAttributes(model);

        if(model instanceof ModelBiped && model != this) this.sourceModel = (ModelBiped) model;
    }

    /**
     * basePivot is forced to the vanilla rest pivot because the OBJ geometry is authored around it.
     * rotationPoint stays live so animated parts actually translate instead of just changing their pivot.
     */
    private static void copyPart(ModelRenderer source, ModelRendererObj dest, float pivotX, float pivotY, float pivotZ) {
        dest.setBasePivot(pivotX, pivotY, pivotZ);
        dest.copyFrom(source);
    }

    private void copyPropertiesFromBiped(ModelBiped modelBiped) {
        copyPart(modelBiped.bipedHead, this.head, 0.0F, 0.0F, 0.0F);
        copyPart(modelBiped.bipedBody, this.body, 0.0F, 0.0F, 0.0F);
        copyPart(modelBiped.bipedLeftArm, this.leftArm, 5.0F, 2.0F, 0.0F);
        copyPart(modelBiped.bipedRightArm, this.rightArm, -5.0F, 2.0F, 0.0F);
        copyPart(modelBiped.bipedLeftLeg, this.leftLeg, 1.9F, 12.0F, 0.0F);
        copyPart(modelBiped.bipedRightLeg, this.rightLeg, -1.9F, 12.0F, 0.0F);
        copyPart(modelBiped.bipedLeftLeg, this.leftFoot, 1.9F, 12.0F, 0.0F);
        copyPart(modelBiped.bipedRightLeg, this.rightFoot, -1.9F, 12.0F, 0.0F);

        if(modelBiped == this) return;

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
        } else if (entityIn != null && entityIn.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F); //same offset ModelBiped#render applies
        }

        renderArmor(entityIn, scale);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();

        this.sourceModel = null;
    }


    @Override
    public void setRotationAngles(float walkCycle, float walkAmplitude, float idleCycle, float headYaw, float headPitch, float scale, Entity entity) {
        ModelBiped source = null;

        if(entity != null) {
            Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);

            if(render instanceof RenderLivingBase) {
                ModelBase mainModel = ((RenderLivingBase<?>) render).getMainModel();
                if(mainModel instanceof ModelBiped) source = (ModelBiped) mainModel;
            }
        }

        if(source == null) source = this.sourceModel;

        if(source != null && source != this) {
            this.copyPropertiesFromBiped(source);
            return;
        }

        //nothing to mirror, pose ourselves the vanilla way
        if(entity instanceof EntityArmorStand) {
            applyArmorStand((EntityArmorStand) entity);
        } else {
            this.isSneak = entity instanceof EntityPlayer && entity.isSneaking();
            super.setRotationAngles(walkCycle, walkAmplitude, idleCycle, headYaw, headPitch, scale, entity);

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
        }

        this.copyPropertiesFromBiped(this);
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