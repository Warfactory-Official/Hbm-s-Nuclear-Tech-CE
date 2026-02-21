package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityUndeadSoldier.class, factory = "FACTORY")
public class RenderUndeadSoldier extends RenderBiped<EntityUndeadSoldier> {

    public static final IRenderFactory<EntityUndeadSoldier> FACTORY = man -> new RenderUndeadSoldier(man);

    public static final ResourceLocation TEXTURE_ZOMBIE = new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");
    public static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation("minecraft", "textures/entity/skeleton/skeleton.png");

    public static final ModelBiped MODEL_ZOMBIE = new ModelZombie();
    public static final ModelBiped MODEL_SKELETON = new ModelSkeleton();

    public RenderUndeadSoldier(RenderManager renderManager) {
        super(renderManager, MODEL_ZOMBIE, 0.5F);
    }

    @Override
    protected void preRenderCallback(EntityUndeadSoldier entity, float partialTickTime) {
        byte type = entity.getDataManager().get(EntityUndeadSoldier.DW_TYPE);
        if (type == EntityUndeadSoldier.TYPE_ZOMBIE) {
            this.mainModel = MODEL_ZOMBIE;
        }
        if (type == EntityUndeadSoldier.TYPE_SKELETON) {
            this.mainModel = MODEL_SKELETON;
        }
    }

    @Override
    public void doRender(EntityUndeadSoldier entity, double x, double y, double z, float entityYaw, float partialTicks) {
        byte type = entity.getDataManager().get(EntityUndeadSoldier.DW_TYPE);
        this.mainModel = (type == EntityUndeadSoldier.TYPE_SKELETON) ? MODEL_SKELETON : MODEL_ZOMBIE;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityUndeadSoldier entity) {
        byte type = entity.getDataManager().get(EntityUndeadSoldier.DW_TYPE);
        if (type == EntityUndeadSoldier.TYPE_ZOMBIE) return TEXTURE_ZOMBIE;
        if (type == EntityUndeadSoldier.TYPE_SKELETON) return TEXTURE_SKELETON;
        return TEXTURE_ZOMBIE;
    }
}
