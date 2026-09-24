package com.hbm.render.loader;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelRendererObj {

    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public float offsetX;
    public float offsetY;
    public float offsetZ;

    /** the pivot the OBJ geometry is actually authored around, animation never touches this */
    public float basePivotX;
    public float basePivotY;
    public float basePivotZ;

    /** mirrors ModelRenderer#offsetX/Y/Z, applied unscaled exactly like vanilla */
    public float partOffsetX;
    public float partOffsetY;
    public float partOffsetZ;

    public boolean showModel = true;
    public boolean isHidden = false;

    String[] parts;
    IModelCustom model;

    public ModelRendererObj(IModelCustom model, String... parts) {
        this.model = model;
        this.parts = parts;
    }

    public ModelRendererObj setPosition(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public ModelRendererObj setRotationPoint(float x, float y, float z) {
        this.rotationPointX = x;
        this.rotationPointY = y;
        this.rotationPointZ = z;
        return this.setBasePivot(x, y, z);
    }

    public ModelRendererObj setBasePivot(float x, float y, float z) {
        this.basePivotX = x;
        this.basePivotY = y;
        this.basePivotZ = z;
        return this;
    }

    /** pulls the live transform off a vanilla part, visibility is deliberately not copied */
    public void copyFrom(ModelRenderer source) {
        this.rotateAngleX = source.rotateAngleX;
        this.rotateAngleY = source.rotateAngleY;
        this.rotateAngleZ = source.rotateAngleZ;
        this.rotationPointX = source.rotationPointX;
        this.rotationPointY = source.rotationPointY;
        this.rotationPointZ = source.rotationPointZ;
        this.partOffsetX = source.offsetX;
        this.partOffsetY = source.offsetY;
        this.partOffsetZ = source.offsetZ;
    }

    public void copyTo(ModelRendererObj obj) {
        obj.offsetX = offsetX;
        obj.offsetY = offsetY;
        obj.offsetZ = offsetZ;
        obj.partOffsetX = partOffsetX;
        obj.partOffsetY = partOffsetY;
        obj.partOffsetZ = partOffsetZ;
        obj.rotateAngleX = rotateAngleX;
        obj.rotateAngleY = rotateAngleY;
        obj.rotateAngleZ = rotateAngleZ;
        obj.rotationPointX = rotationPointX;
        obj.rotationPointY = rotationPointY;
        obj.rotationPointZ = rotationPointZ;
        obj.basePivotX = basePivotX;
        obj.basePivotY = basePivotY;
        obj.basePivotZ = basePivotZ;
        obj.showModel = showModel;
        obj.isHidden = isHidden;
    }


    @SideOnly(Side.CLIENT)
    public void render(float scale) {
        if(this.model == null || this.isHidden || !this.showModel) return;

        GlStateManager.pushMatrix();

        this.applyTransform(scale);
        GlStateManager.scale(scale, scale, scale);

        if (parts != null && parts.length > 0) for (String part : parts)
            model.renderPart(part);
        else model.renderAll();

        GlStateManager.popMatrix();
    }

    /**
     * applies this part's full transform to the matrix stack without drawing anything.
     * use this to hang extra geometry off a bone (fans, wings, backpacks) so it inherits
     * whatever the source model did to that bone.
     */
    @SideOnly(Side.CLIENT)
    public void applyTransform(float scale) {
        GlStateManager.translate(this.partOffsetX, this.partOffsetY, this.partOffsetZ);
        GlStateManager.translate(this.offsetX * scale, this.offsetY * scale, this.offsetZ * scale);

        GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

        if (this.rotateAngleZ != 0.0F)
        {
            GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
        }

        if (this.rotateAngleY != 0.0F)
        {
            GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
        }

        if (this.rotateAngleX != 0.0F)
        {
            GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
        }

        GlStateManager.translate(-this.basePivotX * scale, -this.basePivotY * scale, -this.basePivotZ * scale);
    }
}