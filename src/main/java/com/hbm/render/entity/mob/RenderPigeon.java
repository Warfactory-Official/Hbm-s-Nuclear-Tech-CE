package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityPigeon;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelPigeon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(entity = EntityPigeon.class, factory = "FACTORY")
public class RenderPigeon extends RenderLiving<EntityPigeon> {

    public static final IRenderFactory<EntityPigeon> FACTORY = man -> new RenderPigeon(man, new ModelPigeon(), 0.3F);

    public RenderPigeon(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPigeon entity) {
        return new ResourceLocation(Tags.MODID, "textures/entity/pigeon.png");
    }

    @Override
    protected float handleRotationFloat(EntityPigeon entity, float partialTicks) {
        float fallTime = entity.prevFallTime + (entity.fallTime - entity.prevFallTime) * partialTicks;
        float dest = entity.prevDest + (entity.dest - entity.prevDest) * partialTicks;
        return (MathHelper.sin(fallTime) + 1.0F) * dest;
    }
}
