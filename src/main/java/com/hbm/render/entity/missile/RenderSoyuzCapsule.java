package com.hbm.render.entity.missile;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.missile.EntitySoyuzCapsule;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderSoyuzCapsule extends Render<EntitySoyuzCapsule>
    implements IItemRendererProvider {

  public static final IRenderFactory<EntitySoyuzCapsule> FACTORY =
      (RenderManager man) -> {
        return new RenderSoyuzCapsule(man);
      };

  protected RenderSoyuzCapsule(RenderManager renderManager) {
    super(renderManager);
  }

  @Override
  public void doRender(
      EntitySoyuzCapsule entity,
      double x,
      double y,
      double z,
      float entityYaw,
      float partialTicks) {
    GlStateManager.pushMatrix();

    GlStateManager.translate(x, y, z);

    double interp = partialTicks * 0.05D;
    double time = (entity.world.getTotalWorldTime() * 0.05D) % (Math.PI * 2) + interp;
    double sine = Math.sin(time) * 5;
    double sin3 = Math.sin(time + Math.PI * 0.5) * 5;
    int height = 7;
    GlStateManager.translate(0.0F, height, 0.0F);
    GlStateManager.rotate((float) (sine), 0, 0, 1);
    GlStateManager.rotate((float) (sin3), 1, 0, 0);
    GlStateManager.translate(0.0F, -height, 0.0F);

    GlStateManager.enableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.soyuz_lander_tex);
    ResourceManager.soyuz_lander.renderPart("Capsule");
    bindTexture(ResourceManager.soyuz_chute_tex);
    ResourceManager.soyuz_lander.renderPart("Chute");
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.soyuz_capsule);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4, 0);
        GlStateManager.scale(5, 5, 5);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.soyuz_lander_tex);
        ResourceManager.soyuz_lander.renderPart("Capsule");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }

  @Override
  protected ResourceLocation getEntityTexture(EntitySoyuzCapsule entity) {
    return ResourceManager.soyuz_lander_tex;
  }
}
