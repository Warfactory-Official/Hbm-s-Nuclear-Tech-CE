package com.hbm.render.entity.item;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister(factory = "FACTORY")
public class RenderMovingPackage extends Render<EntityMovingPackage> {

    private ItemStack dummy;

    public static final IRenderFactory<EntityMovingPackage> FACTORY = man -> new RenderMovingPackage(man);

    protected RenderMovingPackage(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityMovingPackage entity, double x, double y, double z, float f1, float f2) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.3125, z);

        if(this.dummy == null) {
            this.dummy = new ItemStack(ModBlocks.crate);
        }

        double scale = 1.25;
        GlStateManager.scale(scale, scale, scale);
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(dummy, entity.world, null);
        model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.FIXED, false);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getRenderItem().renderItem(dummy, model);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMovingPackage p_110775_1_) {
        return null;
    }
}
