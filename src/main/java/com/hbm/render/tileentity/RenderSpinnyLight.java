package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.tileentity.deco.TileEntitySpinnyLight;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderSpinnyLight extends TileEntitySpecialRenderer<TileEntitySpinnyLight>
    implements IItemRendererProvider {

  public static int[] coneMeshes = null;

  @Override
  public boolean isGlobalRenderer(TileEntitySpinnyLight te) {
    return true;
  }

  @Override
  public void render(
      TileEntitySpinnyLight te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    if (coneMeshes == null) {
      coneMeshes = new int[EnumDyeColor.values().length];
      for (int i = 0; i < coneMeshes.length; i++) {
        float[] color = EnumDyeColor.values()[i].getColorComponentValues();
        if (EnumDyeColor.values()[i] == EnumDyeColor.RED) {
          color = new float[] {1, 0, 0};
        }
        coneMeshes[i] = generateConeMesh(5, 3, 12, color[0], color[1], color[2]);
      }
    }
    boolean powered = (te.getBlockMetadata() & 8) > 0;
    float time =
        powered ? (te.getWorld().getTotalWorldTime() - te.timeAdded) % 10000 + partialTicks : 0;
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
    switch (te.getBlockMetadata() & 7) {
      case 0:
        GL11.glRotated(180, 1, 0, 0);
        break;
      case 1:
        break;
      case 2:
        GL11.glRotated(180, 0, 1, 0);
        GL11.glRotated(90, 1, 0, 0);
        break;
      case 3:
        GL11.glRotated(90, 1, 0, 0);
        break;
      case 4:
        GL11.glRotated(270, 0, 1, 0);
        GL11.glRotated(90, 1, 0, 0);
        break;
      case 5:
        GL11.glRotated(90, 0, 1, 0);
        GL11.glRotated(90, 1, 0, 0);
        break;
    }
    GlStateManager.translate(0, -0.5, 0);
    GlStateManager.pushMatrix();
    GL11.glRotated((time * 7) % 360, 0, 1, 0);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.spinny_light_tex);
    if (powered) OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    ResourceManager.spinny_light.renderPart("light");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    ResourceManager.spinny_light.renderPart("base");
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(
        SourceFactor.SRC_ALPHA, powered ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);
    float[] color = te.color.getColorComponentValues();
    if (te.color == EnumDyeColor.RED) {
      color = new float[] {1, 0, 0};
    }
    GlStateManager.color(color[0], color[1], color[2], 0.61F);
    ResourceManager.spinny_light.renderPart("dome");
    GlStateManager.popMatrix();

    if (powered) {
      GlStateManager.pushMatrix();
      GL11.glRotated((time * 7) % 360, 0, 1, 0);
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
      GlStateManager.depthMask(false);
      GlStateManager.disableCull();
      GL11.glCallList(coneMeshes[te.color.ordinal()]);
      GlStateManager.color(1, 1, 1, 1);
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.shadeModel(GL11.GL_FLAT);
      GlStateManager.popMatrix();
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.spinny_light);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      @Override
      protected ItemCameraTransforms getBindingTransforms(Item item) {
        return BakedModelTransforms.standardBlock();
      }

      public void renderInventory() {
        GlStateManager.scale(10, 10, 10);
        GlStateManager.rotate(22.5F, 1, 0, 0);
        GlStateManager.translate(25, -4, 0);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.spinny_light_tex);
        GlStateManager.translate(1.25, -1.25, -1.25);
        GlStateManager.scale(5, 5, 5);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.spinny_light.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }

  private static int generateConeMesh(
      float length, float radius, int sides, float r, float g, float b) {
    int list = GL11.glGenLists(1);
    GL11.glNewList(list, GL11.GL_COMPILE);

    float oX = 0F;
    float oY = 0.1708F;
    float oZ = 0F;

    float[] vertices = new float[(1 + sides) * 3];
    vertices[0] = oX;
    vertices[1] = oY;
    vertices[2] = oZ;

    Vec3 vertex = new Vec3(0, radius, 0);
    for (int i = 0; i < sides; i++) {
      vertex.rotateAroundX((float) (2 * Math.PI * (1F / (float) sides)));
      vertices[(i + 1) * 3] = (float) vertex.xCoord + oX + length;
      vertices[(i + 1) * 3 + 1] = (float) vertex.yCoord + oY;
      vertices[(i + 1) * 3 + 2] = (float) vertex.zCoord + oZ;
    }

    Tessellator tes = Tessellator.getInstance();
    BufferBuilder buf = tes.getBuffer();
    buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
    float alpha = 0.65F;
    for (int m = -1; m <= 1; m += 2) {
      for (int i = 2; i <= sides; i++) {
        buf.pos(vertices[0], vertices[1], vertices[2]).color(r, g, b, alpha).endVertex();
        buf.pos(vertices[(i - 1) * 3] * m, vertices[(i - 1) * 3 + 1], vertices[(i - 1) * 3 + 2])
            .color(r, g, b, 0)
            .endVertex();
        buf.pos(vertices[i * 3] * m, vertices[i * 3 + 1], vertices[i * 3 + 2])
            .color(r, g, b, 0)
            .endVertex();
      }
      buf.pos(vertices[0], vertices[1], vertices[2]).color(r, g, b, alpha).endVertex();
      buf.pos(vertices[sides * 3] * m, vertices[sides * 3 + 1], vertices[sides * 3 + 2])
          .color(r, g, b, 0)
          .endVertex();
      buf.pos(vertices[1 * 3] * m, vertices[1 * 3 + 1], vertices[1 * 3 + 2])
          .color(r, g, b, 0)
          .endVertex();
    }
    tes.draw();

    GL11.glEndList();
    return list;
  }
}
