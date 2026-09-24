package com.hbm.render.item;

import com.hbm.animloader.AnimationWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;

import static com.hbm.render.NTMRenderHelper.bindTexture;

@AutoRegister(item = "jetpack_glider")
public class ItemRenderJetpackGlider extends ItemRenderBaseLegacy {
  @Override
  public void renderInventory() {
    GlStateManager.translate(-5, -6, 0);
    GlStateManager.scale(1.5, 1.5, 1.5);
  }

  @Override
  public void renderCommon() {
    bindTexture(ResourceManager.jetpack_tex);
    AnimationWrapper w = new AnimationWrapper(0, ResourceManager.jetpack_activate);
    ResourceManager.jetpack.controller.setAnim(w);
    ResourceManager.jetpack.renderAnimated(ResourceManager.jetpack_activate.length);
  }
}
