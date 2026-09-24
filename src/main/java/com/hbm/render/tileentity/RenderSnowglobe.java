package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSnowglobe.SnowglobeType;
import com.hbm.blocks.generic.BlockSnowglobe.TileEntitySnowglobe;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.util.EnumUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderSnowglobe extends TileEntitySpecialRenderer<TileEntitySnowglobe> implements IItemRendererProvider {

    public static final ResourceLocation socket = new ResourceLocation(Tags.MODID, "textures/models/trinkets/snowglobe.png");
    public static final ResourceLocation glass = new ResourceLocation(Tags.MODID, "textures/models/trinkets/snowglobe_glass.png");
    public static final ResourceLocation features = new ResourceLocation(Tags.MODID, "textures/models/trinkets/snowglobe_features.png");

    @Override
    public void render(TileEntitySnowglobe te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);

        GlStateManager.rotate(22.5D * te.getBlockMetadata() + 90, 0, -1, 0);

        renderSnowglobe(te.type);

        GlStateManager.popMatrix();
    }

    public static void renderSnowglobe(SnowglobeType type) {
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();

        double scale = 0.0625D;
        GlStateManager.scale(scale, scale, scale);

        Minecraft.getMinecraft().getTextureManager().bindTexture(socket);
        ResourceManager.snowglobe.renderPart("Socket");
        Minecraft.getMinecraft().getTextureManager().bindTexture(glass);
        ResourceManager.snowglobe.renderPart("Glass");

        Minecraft.getMinecraft().getTextureManager().bindTexture(features);

        switch (type) {
            case RIVETCITY:
                ResourceManager.snowglobe.renderPart("RivetCity");
                break;
            case TENPENNYTOWER:
                ResourceManager.snowglobe.renderPart("TenpennyTower");
                break;
            case LUCKY38:
                ResourceManager.snowglobe.renderPart("Lucky38");
                break;
            case SIERRAMADRE:
                ResourceManager.snowglobe.renderPart("SierraMadre");
                break;
            case PRYDWEN:
                ResourceManager.snowglobe.renderPart("Prydwen");
                break;
            default:
                break;
        }

        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.disableLighting();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        float f3 = 0.05F;
        GlStateManager.translate(4.025, 0.5, 0);
        GlStateManager.scale(f3, -f3, f3);
        GlStateManager.translate(0, -font.FONT_HEIGHT / 2F, font.getStringWidth(type.label) * 0.5D);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.depthMask(false);
        GlStateManager.translate(0, 1, 0);
        font.drawString(type.label, 0, 0, 0xffffff);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.snowglobe);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(6, 6, 6);
            }

            public void renderCommon(ItemStack item) {
                GlStateManager.translate(0, 0.25, 0);
                GlStateManager.scale(3, 3, 3);
                SnowglobeType type = EnumUtil.grabEnumSafely(SnowglobeType.values(), item.getItemDamage());
                renderSnowglobe(type);
                GlStateManager.enableRescaleNormal();
            }
        };
    }
}
