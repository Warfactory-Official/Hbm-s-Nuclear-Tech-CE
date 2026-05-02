package com.hbm.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelSatelliteReceiver extends ModelBase {

    public ModelRenderer Shape1;
    public ModelRenderer Shape2;
    public ModelRenderer Shape3;
    public ModelRenderer Shape4;
    public ModelRenderer Shape5;
    public ModelRenderer Shape6;
    public ModelRenderer Shape7;
    public ModelRenderer Shape8;
    public ModelRenderer Shape9;

    public ModelSatelliteReceiver() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.Shape1 = new ModelRenderer(this, 0, 0);
        this.Shape1.addBox(-6.0F, 0.0F, -6.0F, 12, 16, 12);
        this.Shape1.setRotationPoint(0.0F, 8.0F, 0.0F);
        setRotation(this.Shape1, 0F, 0F, 0F);

        float pX = 3.0F;
        float pY = 6.0F;
        float pZ = 0.0F;

        float rotX = (float) Math.toRadians(15);
        float rotY = (float) Math.toRadians(-25);
        float rotZ = 0F;

        this.Shape2 = new ModelRenderer(this, 10, 28);
        this.Shape2.addBox(-3.0F, 9.0F, -6.0F, 8, 8, 2);
        this.Shape2.setRotationPoint(pX, pY, pZ);
        this.Shape2.mirror = true;
        setRotation(this.Shape2, rotX, rotY, rotZ);

        this.Shape3 = new ModelRenderer(this, 0, 39);
        this.Shape3.addBox(-3.0F, 7.0F, -7.0F, 8, 2, 3);
        this.Shape3.setRotationPoint(pX, pY, pZ);
        this.Shape3.mirror = true;
        setRotation(this.Shape3, rotX, rotY, rotZ);

        this.Shape4 = new ModelRenderer(this, 0, 28);
        this.Shape4.addBox(-5.0F, 9.0F, -7.0F, 2, 8, 3);
        this.Shape4.setRotationPoint(pX, pY, pZ);
        this.Shape4.mirror = true;
        setRotation(this.Shape4, rotX, rotY, rotZ);

        this.Shape5 = new ModelRenderer(this, 0, 28);
        this.Shape5.addBox(5.0F, 9.0F, -7.0F, 2, 8, 3);
        this.Shape5.setRotationPoint(pX, pY, pZ);
        this.Shape5.mirror = true;
        setRotation(this.Shape5, rotX, rotY, rotZ);

        this.Shape6 = new ModelRenderer(this, 0, 39);
        this.Shape6.addBox(-3.0F, 17.0F, -7.0F, 8, 2, 3);
        this.Shape6.setRotationPoint(pX, pY, pZ);
        this.Shape6.mirror = true;
        setRotation(this.Shape6, rotX, rotY, rotZ);

        this.Shape7 = new ModelRenderer(this, 0, 44);
        this.Shape7.addBox(0.0F, 12.0F, -9.0F, 2, 2, 3);
        this.Shape7.setRotationPoint(pX, pY, pZ);
        this.Shape7.mirror = true;
        setRotation(this.Shape7, rotX, rotY, rotZ);

        this.Shape8 = new ModelRenderer(this, 0, 49);
        this.Shape8.addBox(0.5F, 12.5F, -12.0F, 1, 1, 3);
        this.Shape8.setRotationPoint(pX, pY, pZ);
        this.Shape8.mirror = true;
        setRotation(this.Shape8, rotX, rotY, rotZ);

        this.Shape9 = new ModelRenderer(this, 0, 53);
        this.Shape9.addBox(0.0F, 12.0F, -14.0F, 2, 2, 2);
        this.Shape9.setRotationPoint(pX, pY, pZ);
        this.Shape9.mirror = true;
        setRotation(this.Shape9, rotX, rotY, rotZ);
    }

    @Override
    public void render(@NotNull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        this.Shape1.render(scaleFactor);
        this.Shape2.render(scaleFactor);
        this.Shape3.render(scaleFactor);
        this.Shape4.render(scaleFactor);
        this.Shape5.render(scaleFactor);
        this.Shape6.render(scaleFactor);
        this.Shape7.render(scaleFactor);
        this.Shape8.render(scaleFactor);
        this.Shape9.render(scaleFactor);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}