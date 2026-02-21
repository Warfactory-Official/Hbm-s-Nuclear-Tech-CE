package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityDummy;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityDummy.class, factory = "FACTORY")
public class RenderDummy extends RenderBiped<EntityDummy> {

    public static final IRenderFactory<EntityDummy> FACTORY = man -> new RenderDummy(man);

    public RenderDummy(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBiped(), 0.5F);
        this.addLayer(new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                this.modelLeggings = new ModelBiped(0.5F);
                this.modelArmor = new ModelBiped(1.0F);
            }
        });
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDummy entity) {
        return new ResourceLocation(Tags.MODID, "textures/entity/dummy.png");
    }
}
