package com.hbm.render.item;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.render.util.NTMImmediate;
import com.hbm.render.util.RenderMiscEffects;
import com.hbm.util.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "meteorite_sword_seared", constructorArgsString = "1.0F, 0.5F, 0.0F")
@AutoRegister(item = "meteorite_sword_reforged", constructorArgsString = "0.5F, 1.0F, 1.0F")
@AutoRegister(item = "meteorite_sword_hardened", constructorArgsString = "0.25F, 0.25F, 0.25F")
@AutoRegister(item = "meteorite_sword_alloyed", constructorArgsString = "0.0F, 0.5F, 1.0F")
@AutoRegister(item = "meteorite_sword_machined", constructorArgsString = "1.0F, 1.0F, 0.0F")
@AutoRegister(item = "meteorite_sword_treated", constructorArgsString = "0.5F, 1.0F, 0.5F")
@AutoRegister(item = "meteorite_sword_etched", constructorArgsString = "1.0F, 1.0F, 0.5F")
@AutoRegister(item = "meteorite_sword_bred", constructorArgsString = "0.5F, 0.5F, 0.0F")
@AutoRegister(item = "meteorite_sword_irradiated", constructorArgsString = "0.75F, 1.0F, 0.0F")
@AutoRegister(item = "meteorite_sword_fused", constructorArgsString = "1.0F, 0.0F, 0.5F")
@AutoRegister(item = "meteorite_sword_baleful", constructorArgsString = "0.0F, 1.0F, 0.0F")
public class ItemRendererMeteorSword extends TEISRBase {

    float r;
    float g;
    float b;

    public ItemRendererMeteorSword(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public ModelBinding createModelBinding(Item item) {
        return ModelBinding.inventoryModel(item, BakedModelTransforms.defaultItemTransforms(), new ResourceLocation(Tags.MODID, "items/meteorite_sword"));
    }

    @Override
    public void renderByItem(ItemStack stack) {
        boolean prevBlend = RenderUtil.isBlendEnabled();
        int prevSrc = RenderUtil.getBlendSrcFactor();
        int prevDst = RenderUtil.getBlendDstFactor();
        int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
        int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();

        GlStateManager.translate(0.5, 0.5, 0.5);

        Minecraft mc = Minecraft.getMinecraft();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, itemModel);

        mc.renderEngine.bindTexture(RenderMiscEffects.glint);

        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        for (int j1 = 0; j1 < 2; ++j1) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE);
            float f2 = (float) (Minecraft.getSystemTime() % (long) (3000 + j1 * 1873)) / (3000.0F + (float) (j1 * 1873)) / 8F;

            float in = 0.36F;

            GlStateManager.color(r * in, g * in, b * in, 1.0F);

            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GL11.glScaled(8, 8, 8);
            GlStateManager.translate(f2, 0, 0);
            GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            BufferBuilder bufferbuilder = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

            int color = (0xFF << 24) | ((byte) ((r * in) * 255) << 16) | ((byte) ((g * in) * 255) << 8) | ((byte) ((b * in) * 255));

            for (EnumFacing enumfacing : EnumFacing.VALUES) {
                Minecraft.getMinecraft().getRenderItem().renderQuads(bufferbuilder, itemModel.getQuads((IBlockState) null, enumfacing, 0L), color, stack);
            }

            Minecraft.getMinecraft().getRenderItem().renderQuads(bufferbuilder, itemModel.getQuads((IBlockState) null, (EnumFacing) null, 0L), color, stack);
            NTMImmediate.INSTANCE.draw();

            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (prevBlend) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
    }
}
