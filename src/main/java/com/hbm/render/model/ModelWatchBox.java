package com.hbm.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelWatchBox extends ModelBase {
    public final ModelRenderer box;

    public ModelWatchBox() {
        this.textureWidth = 16;
        this.textureHeight = 16;
        this.box = new ModelRenderer(this, 0, 0);
        this.box.addBox(-2.0F, -2.0F, -0.5F, 4, 4, 1);
        this.box.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.box.setTextureSize(16, 16);
    }
}
