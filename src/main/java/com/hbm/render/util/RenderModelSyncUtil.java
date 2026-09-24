package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;

public class RenderModelSyncUtil {

    private RenderModelSyncUtil() { }

    /** the ModelBiped actually driving this entity's animation, or null if there isn't one */
    public static ModelBiped getSourceModel(Entity entity, ModelBiped self) {
        if(entity == null) return null;

        Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);

        if(render instanceof RenderLivingBase) {
            ModelBase main = ((RenderLivingBase<?>) render).getMainModel();
            if(main instanceof ModelBiped && main != self) return (ModelBiped) main;
        }

        return null;
    }

    /** full transform copy - everything vanilla ModelRenderer#render actually reads */
    public static void copyAngles(ModelRenderer source, ModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
        dest.offsetX = source.offsetX;
        dest.offsetY = source.offsetY;
        dest.offsetZ = source.offsetZ;
    }
}

