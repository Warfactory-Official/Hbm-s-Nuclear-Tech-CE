package com.hbm.render.loader;

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
        return this;
    }

    public void copyTo(ModelRendererObj obj) {
        obj.offsetX = offsetX;
        obj.offsetY = offsetY;
        obj.offsetZ = offsetZ;
        obj.rotateAngleX = rotateAngleX;
        obj.rotateAngleY = rotateAngleY;
        obj.rotateAngleZ = rotateAngleZ;
        obj.rotationPointX = rotationPointX;
        obj.rotationPointY = rotationPointY;
        obj.rotationPointZ = rotationPointZ;
    }

    @SideOnly(Side.CLIENT)
    public void render(float scale) {
        GlStateManager.pushMatrix();

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

        GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);

        GlStateManager.scale(scale, scale, scale);

        if (parts != null && parts.length > 0) for (String part : parts)
            model.renderPart(part);
        else model.renderAll();


        GlStateManager.popMatrix();

    }
}
