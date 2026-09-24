package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineOreSlopper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderOreSlopper extends TileEntitySpecialRenderer<TileEntityMachineOreSlopper> implements IItemRendererProvider {

    private static final ItemStack bedrockOreStack = new ItemStack(ModItems.bedrock_ore, 1, 0);

    private static ItemStack getCachedBedrockOreStack() {
        return bedrockOreStack;
    }

    @Override
    public void render(TileEntityMachineOreSlopper slopper, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        int meta = slopper.getBlockMetadata() - BlockDummyable.offset;
        float rotationY = switch (meta) {
            case 3 -> 180f;
            case 5 -> 270f;
            case 4 -> 90f;
            default -> 0f;
        };
        GlStateManager.rotate(rotationY, 0F, 1F, 0F);

        int oldShadeModel = GlStateManager.glGetInteger(GL11.GL_SHADE_MODEL);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.ore_slopper_tex);
        ResourceManager.ore_slopper.renderPart("Base");

        GlStateManager.pushMatrix();
        double slide = slopper.prevSlider + (slopper.slider - slopper.prevSlider) * partialTicks;
        GlStateManager.translate(0, 0, slide * -3);
        ResourceManager.ore_slopper.renderPart("Slider");

        double extend = (slopper.prevBucket + (slopper.bucket - slopper.prevBucket) * partialTicks) * 1.5;
        double clamp1 = MathHelper.clamp(extend - 0.25, 0, 1.25);
        double clamp2 = MathHelper.clamp(extend, 0, 1.25);

        GlStateManager.translate(0, -clamp1, 0);
        ResourceManager.ore_slopper.renderPart("Hydraulics");
        GlStateManager.translate(0, -clamp2, 0);
        ResourceManager.ore_slopper.renderPart("Bucket");

        if (slopper.animation == TileEntityMachineOreSlopper.SlopperAnimation.LIFTING) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0D, 4.3125D, 2D);
            GlStateManager.rotate(90, 0F, 1F, 0F);
            GlStateManager.rotate(-90, 1F, 0F, 0F);
            // 1.75 * 0.5128205 = 0.897435875, same happens to RenderSolderingStation
            GlStateManager.scale(0.897435875F, 0.897435875F, 0.897435875F);

            final ItemStack stack = getCachedBedrockOreStack();
            if (!stack.isEmpty()) {
                Minecraft mc = Minecraft.getMinecraft();
                IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, slopper.getWorld(), null);
                // Don't use FIXED here! we handle the rotation manually.
                model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.NONE, false);

                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                mc.getRenderItem().renderItem(stack, model);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        bindTexture(ResourceManager.ore_slopper_tex);

        double blades = slopper.prevBlades + (slopper.blades - slopper.prevBlades) * partialTicks;
        double fan = slopper.prevFan + (slopper.fan - slopper.prevFan) * partialTicks;

        renderRotatingPart(0.375, 2.75, 0, blades, "BladesLeft");
        renderRotatingPart(-0.375, 2.75, 0, -blades, "BladesRight");
        renderRotatingPart(0, 1.875, -1, -fan, "Fan", true);

        GlStateManager.shadeModel(oldShadeModel);
        GlStateManager.popMatrix();
    }

    // Helper for rotating part rendering
    private void renderRotatingPart(double x, double y, double z, double angle, String part) {
        renderRotatingPart(x, y, z, angle, part, false);
    }

    private void renderRotatingPart(double x, double y, double z, double angle, String part, boolean fanMode) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (fanMode) {
            GlStateManager.rotate((float) angle, 1, 0, 0);
        } else {
            GlStateManager.rotate((float) angle, 0, 0, 1);
        }
        GlStateManager.translate(-x, -y, -z);
        ResourceManager.ore_slopper.renderPart(part);
        GlStateManager.popMatrix();
    }


    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_ore_slopper);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -3, 0);
                GlStateManager.scale(3.75, 3.75, 3.75);
            }

            public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(-90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.ore_slopper_tex);
                ResourceManager.ore_slopper.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
