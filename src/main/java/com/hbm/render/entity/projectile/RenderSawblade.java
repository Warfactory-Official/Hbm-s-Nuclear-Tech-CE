package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister(factory = "FACTORY")
public class RenderSawblade extends Render<EntitySawblade> {

    public static final IRenderFactory<EntitySawblade> FACTORY = RenderSawblade::new;

    protected RenderSawblade(RenderManager renderManager){
        super(renderManager);
    }
    @Override
    public void doRender(EntitySawblade cog, double x, double y, double z, float f0, float f1) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int orientation = cog.getDataManager().get(EntitySawblade.ORIENTATION);
        switch(orientation % 6) {
            case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
        }

        GlStateManager.translate(0, 0, -1);


        if(orientation < 6) {
            GL11.glRotated(System.currentTimeMillis() % (360 * 5) / 3D, 0.0D, 0.0D, -1.0D);
        }

        GlStateManager.translate(0, -1.375, 0);

        this.bindEntityTexture(cog);
        ResourceManager.sawmill.renderPart("Blade");

        GlStateManager.popMatrix();

    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull EntitySawblade entity) {
        return ResourceManager.sawmill_tex;
    }
}
