package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.mob.*;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.entity.layers.LayerCreeperChargeUniversal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityCreeperNuclear.class, factory = "factoryNuclear")
@AutoRegister(entity = EntityCreeperTainted.class, factory = "factoryTainted")
@AutoRegister(entity = EntityCreeperPhosgene.class, factory = "factoryPhosgene")
@AutoRegister(entity = EntityCreeperVolatile.class, factory = "factoryVolatile")
@AutoRegister(entity = EntityCreeperGold.class, factory = "factoryGold")
public class RenderCreeperUniversal extends RenderCreeper {
    private static final ResourceLocation TEX_NUCLEAR = new ResourceLocation(Tags.MODID, "textures/entity/creeper.png");
    private static final ResourceLocation ARMOR = new ResourceLocation(Tags.MODID, "textures/entity/creeper_armor.png");
    private static final ResourceLocation TEX_TAINTED = new ResourceLocation(Tags.MODID, "textures/entity/creeper_tainted.png");
    private static final ResourceLocation ARMOR_TAINTED = new ResourceLocation(Tags.MODID,"textures/entity/creeper_armor_taint.png");
    private static final ResourceLocation TEX_PHOSGENE = new ResourceLocation(Tags.MODID, "textures/entity/creeper_phosgene.png");
    private static final ResourceLocation TEX_VOLATILE = new ResourceLocation(Tags.MODID, "textures/entity/creeper_volatile.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(Tags.MODID, "textures/entity/creeper_gold.png");
    public static final IRenderFactory<EntityCreeperNuclear> factoryNuclear = manager -> new RenderCreeperUniversal(manager, TEX_NUCLEAR, ARMOR, 5.0F);
    public static final IRenderFactory<EntityCreeperTainted> factoryTainted = manager -> new RenderCreeperUniversal(manager, TEX_TAINTED, ARMOR_TAINTED);
    public static final IRenderFactory<EntityCreeperPhosgene> factoryPhosgene = manager -> new RenderCreeperUniversal(manager, TEX_PHOSGENE, ARMOR);
    public static final IRenderFactory<EntityCreeperVolatile> factoryVolatile = manager -> new RenderCreeperUniversal(manager, TEX_VOLATILE, ARMOR);
    public static final IRenderFactory<EntityCreeperGold> factoryGold = manager -> new RenderCreeperUniversal(manager, TEX_GOLD, ARMOR);

    private final ResourceLocation creeperTexture;
    private final float swellMod;

    public RenderCreeperUniversal(RenderManager manager, ResourceLocation texture, ResourceLocation overlay, float swellMod) {
        super(manager);
        this.creeperTexture = texture;
        for (int i = 0; i < this.layerRenderers.size(); i++) {
            LayerRenderer<EntityCreeper> layer = this.layerRenderers.get(i);
            if (layer instanceof LayerCreeperCharge && !(layer instanceof LayerCreeperChargeUniversal)) {
                this.layerRenderers.set(i, new LayerCreeperChargeUniversal(this, overlay));
                break;
            }
        }
        this.swellMod = swellMod;
    }

    public RenderCreeperUniversal(RenderManager rendermanagerIn, ResourceLocation creeperTexture, ResourceLocation creeperArmor) {
        this(rendermanagerIn, creeperTexture, creeperArmor, 1.0F);
    }

    @Override
    protected void preRenderCallback(EntityCreeper entitylivingbaseIn, float partialTickTime) {
        float swell = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);
        float flash = 1.0F + MathHelper.sin(swell * 100.0F) * swell * 0.01F;
        swell = MathHelper.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        swell *= swellMod;
        float f2 = (1.0F + swell * 0.4F) * flash;
        float f3 = (1.0F + swell * 0.1F) / flash;
        GlStateManager.scale(f2, f3, f2);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCreeper entity) {
        return creeperTexture;
    }
}
