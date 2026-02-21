package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityParasiteMaggot;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityParasiteMaggot.class, factory = "FACTORY")
public class RenderMaggot extends RenderLiving<EntityParasiteMaggot> {
    public static final IRenderFactory<EntityParasiteMaggot> FACTORY = RenderMaggot::new;
    public static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/entity/parasite_maggot.png");

    public RenderMaggot(RenderManager renderManager) {
        super(renderManager, new ModelSilverfish(), 0.3F);
    }

    @Override
    protected float getDeathMaxRotation(EntityParasiteMaggot entity) {
        return 180.0F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityParasiteMaggot entity) {
        return texture;
    }
}
