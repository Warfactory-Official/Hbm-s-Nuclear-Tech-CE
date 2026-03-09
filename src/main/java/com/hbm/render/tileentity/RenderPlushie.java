package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import com.hbm.blocks.generic.BlockPlushie.TileEntityPlushie;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.util.HorsePronter;
import com.hbm.util.EnumUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderPlushie extends TileEntitySpecialRenderer<TileEntityPlushie> implements IItemRendererProvider {

    public static final IModelCustom yomiModel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/trinkets/yomi.obj")).asVBO();
    public static final IModelCustom hundunModel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/trinkets/hundun.obj")).asVBO();
    public static final IModelCustom dergModel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/trinkets/derg.obj")).asVBO();
    public static final ResourceLocation yomiTex = new ResourceLocation(Tags.MODID, "textures/models/trinkets/yomi.png");
    public static final ResourceLocation numbernineTex = new ResourceLocation(Tags.MODID, "textures/models/horse/numbernine.png");
    public static final ResourceLocation hundunTex = new ResourceLocation(Tags.MODID, "textures/models/trinkets/hundun.png");
    public static final ResourceLocation dergTex = new ResourceLocation(Tags.MODID, "textures/models/trinkets/derg.png");

    @Override
    public void render(TileEntityPlushie te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableCull();

        GlStateManager.rotate(22.5D * te.getBlockMetadata() + 90, 0, -1, 0);

        if (te.squishTimer > 0) {
            double squish = te.squishTimer - partialTicks;
            GlStateManager.scale(1, 1 + (-(Math.sin(squish)) * squish) * 0.025, 1);
        }

        switch (te.type) {
            case NONE, DERG:
                break;
            case YOMI:
                GlStateManager.scale(0.5, 0.5, 0.5);
                break;
            case NUMBERNINE:
                GlStateManager.scale(0.75, 0.75, 0.75);
                break;
            case HUNDUN:
                GlStateManager.scale(1, 1, 1);
                break;
        }
        renderPlushie(te.type, te.squishTimer > 0);

        GlStateManager.popMatrix();
    }

    public static void renderPlushie(PlushieType type, boolean squish) {

        GlStateManager.enableRescaleNormal();

        switch (type) {
            case NONE:
                break;
            case YOMI:
                Minecraft.getMinecraft().getTextureManager().bindTexture(yomiTex);
                yomiModel.renderAll();
                break;
            case NUMBERNINE:
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.rotate(15, -1, 0, 0);
                GlStateManager.translate(0, -0.25, 0.75);
                Minecraft.getMinecraft().getTextureManager().bindTexture(numbernineTex);
                HorsePronter.reset();
                double r = 45;
                HorsePronter.pose(HorsePronter.id_body, 0, -r, 0);
                HorsePronter.pose(HorsePronter.id_tail, 0, 60, 90);
                HorsePronter.pose(HorsePronter.id_lbl, 0, -75 + r, 35);
                HorsePronter.pose(HorsePronter.id_rbl, 0, -75 + r, -35);
                HorsePronter.pose(HorsePronter.id_lfl, 0, r - 25, 5);
                HorsePronter.pose(HorsePronter.id_rfl, 0, r - 25, -5);
                HorsePronter.pose(HorsePronter.id_head, 0, r + 15, 0);
                HorsePronter.pront();
                GlStateManager.rotate(15, 1, 0, 0);
                GlStateManager.pushMatrix();
                GlStateManager.disableCull();
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                GlStateManager.translate(0, 1, -0.6875);
                double s = 1.125D;
                GlStateManager.scale(0.0625 * s, 0.0625 * s, 0.0625 * s);
                GlStateManager.rotate(180, 1, 0, 0);
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.no9);
                ResourceManager.armor_no9.renderPart("Helmet");
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.no9_insignia);
                ResourceManager.armor_no9.renderPart("Insignia");
                GlStateManager.shadeModel(GL11.GL_FLAT);
                GlStateManager.enableCull();
                GlStateManager.popMatrix();
                ItemStack stack = new ItemStack(ModItems.cigarette);
                double scale = 0.25;
                GlStateManager.translate(-0.06, 1.13, -0.42);
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.rotate(90, 0, -1, 0);
                GlStateManager.rotate(60, 0, 0, -1);
                Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
                break;
            case HUNDUN:
                Minecraft.getMinecraft().getTextureManager().bindTexture(hundunTex);
                hundunModel.renderPart("goober_posed");
                break;
            case DERG:
                Minecraft.getMinecraft().getTextureManager().bindTexture(dergTex);
                dergModel.renderPart("Derg");
                dergModel.renderPart(squish ? "Blep" : "ColonThree");
                break;
        }
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.plushie);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.rotate(180, 0, 1, 0);
                GlStateManager.translate(0, -6, 0);
                GlStateManager.scale(6, 6, 6);
            }

            public void renderGround() {
                GlStateManager.scale(3, 3, 3);
            }

            public void renderCommon(ItemStack item) {
                GlStateManager.rotate(180.0, 0, 1, 0);
                GlStateManager.translate(0, 0.25, 0);
                GlStateManager.enableCull();
                PlushieType type = EnumUtil.grabEnumSafely(PlushieType.values(), item.getItemDamage());

                switch (type) {
                    case NONE:
                        break;
                    case YOMI:
                        GlStateManager.scale(1.25, 1.25, 1.25);
                        break;
                    case NUMBERNINE:
                        GlStateManager.translate(0, 0.25, 0.25);
                        GlStateManager.scale(1.25, 1.25, 1.25);
                        break;
                    case HUNDUN:
                        GlStateManager.translate(0.5, 0.42, 0);
                        GlStateManager.scale(1.25, 1.25, 1.25);
                        break;
                    case DERG:
                        GlStateManager.scale(1.5, 1.5, 1.5);
                        break;
                }
                renderPlushie(type, false);
            }
        };
    }
}