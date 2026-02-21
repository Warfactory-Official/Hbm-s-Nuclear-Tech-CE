package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderAssemblyFactory extends TileEntitySpecialRenderer<TileEntityMachineAssemblyFactory> implements IItemRendererProvider {

    public static EntityItem dummy;

    @Override
    public void render(TileEntityMachineAssemblyFactory assemfac, double x, double y, double z, float interp, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch (assemfac.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(270, 0F, 1F, 0F);
        }

        bindTexture(ResourceManager.assembly_factory_tex);
        ResourceManager.assembly_factory.renderPart("Base");
        if(assemfac.frame) ResourceManager.assembly_factory.renderPart("Frame");

        double slide1 = assemfac.animations[0].getSlider(interp);
        double slide2 = assemfac.animations[1].getSlider(interp);
        double[] arm1 = assemfac.animations[0].striker.getPositions(interp);
        double[] arm2 = assemfac.animations[0].saw.getPositions(interp);
        double[] arm3 = assemfac.animations[1].striker.getPositions(interp);
        double[] arm4 = assemfac.animations[1].saw.getPositions(interp);

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(0.5 - slide1, 0, 0);
            ResourceManager.assembly_factory.renderPart("Slider1");

            GlStateManager.translate(0, 1.625, -0.9375);
            GlStateManager.rotate((float) -arm1[0], 1, 0, 0);
            GlStateManager.translate(0, -1.625, 0.9375);
            ResourceManager.assembly_factory.renderPart("ArmLower1");

            GlStateManager.translate(0, 2.375, -0.9375);
            GlStateManager.rotate((float) -arm1[1], 1, 0, 0);
            GlStateManager.translate(0, -2.375, 0.9375);
            ResourceManager.assembly_factory.renderPart("ArmUpper1");

            GlStateManager.translate(0, 2.375, -0.4375);
            GlStateManager.rotate((float) -arm1[2], 1, 0, 0);
            GlStateManager.translate(0, -2.375, 0.4375);
            ResourceManager.assembly_factory.renderPart("Head1");
            GlStateManager.translate(0, arm1[3], 0);
            ResourceManager.assembly_factory.renderPart("Striker1");
        } GlStateManager.popMatrix();

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(-0.5 + slide1, 0, 0);
            ResourceManager.assembly_factory.renderPart("Slider2");

            GlStateManager.translate(0, 1.625, 0.9375);
            GlStateManager.rotate((float) arm2[0], 1, 0, 0);
            GlStateManager.translate(0, -1.625, -0.9375);
            ResourceManager.assembly_factory.renderPart("ArmLower2");

            GlStateManager.translate(0, 2.375, 0.9375);
            GlStateManager.rotate((float) arm2[1], 1, 0, 0);
            GlStateManager.translate(0, -2.375, -0.9375);
            ResourceManager.assembly_factory.renderPart("ArmUpper2");

            GlStateManager.translate(0, 2.375, 0.4375);
            GlStateManager.rotate((float) arm2[2], 1, 0, 0);
            GlStateManager.translate(0, -2.375, -0.4375);
            ResourceManager.assembly_factory.renderPart("Head2");
            GlStateManager.translate(0, arm2[3], 0);
            ResourceManager.assembly_factory.renderPart("Striker2");
            GlStateManager.translate(0, 1.625, 0.3125);
            GlStateManager.rotate((float) -arm2[4], 1, 0, 0);
            GlStateManager.translate(0, -1.625, -0.3125);
            ResourceManager.assembly_factory.renderPart("Blade2");
        } GlStateManager.popMatrix();

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(-0.5 + slide2, 0, 0);
            ResourceManager.assembly_factory.renderPart("Slider3");

            GlStateManager.translate(0, 1.625, 0.9375);
            GlStateManager.rotate((float) arm3[0], 1, 0, 0);
            GlStateManager.translate(0, -1.625, -0.9375);
            ResourceManager.assembly_factory.renderPart("ArmLower3");

            GlStateManager.translate(0, 2.375, 0.9375);
            GlStateManager.rotate((float) arm3[1], 1, 0, 0);
            GlStateManager.translate(0, -2.375, -0.9375);
            ResourceManager.assembly_factory.renderPart("ArmUpper3");

            GlStateManager.translate(0, 2.375, 0.4375);
            GlStateManager.rotate((float) arm3[2], 1, 0, 0);
            GlStateManager.translate(0, -2.375, -0.4375);
            ResourceManager.assembly_factory.renderPart("Head3");
            GlStateManager.translate(0, arm3[3], 0);
            ResourceManager.assembly_factory.renderPart("Striker3");
        } GlStateManager.popMatrix();

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(0.5 - slide2, 0, 0);
            ResourceManager.assembly_factory.renderPart("Slider4");

            GlStateManager.translate(0, 1.625, -0.9375);
            GlStateManager.rotate((float) -arm4[0], 1, 0, 0);
            GlStateManager.translate(0, -1.625, 0.9375);
            ResourceManager.assembly_factory.renderPart("ArmLower4");

            GlStateManager.translate(0, 2.375, -0.9375);
            GlStateManager.rotate((float) -arm4[1], 1, 0, 0);
            GlStateManager.translate(0, -2.375, 0.9375);
            ResourceManager.assembly_factory.renderPart("ArmUpper4");

            GlStateManager.translate(0, 2.375, -0.4375);
            GlStateManager.rotate((float) -arm4[2], 1, 0, 0);
            GlStateManager.translate(0, -2.375, 0.4375);
            ResourceManager.assembly_factory.renderPart("Head4");
            GlStateManager.translate(0, arm4[3], 0);
            ResourceManager.assembly_factory.renderPart("Striker4");
            GlStateManager.translate(0, 1.625, -0.3125);
            GlStateManager.rotate((float) arm4[4], 1, 0, 0);
            GlStateManager.translate(0, -1.625, 0.3125);
            ResourceManager.assembly_factory.renderPart("Blade4");
        } GlStateManager.popMatrix();

        if(MainRegistry.proxy.me().getDistanceSq(assemfac.getPos().getX() + 0.5, assemfac.getPos().getY() + 1, assemfac.getPos().getZ() + 0.5) < 35 * 35) {

            for(int i = 0; i < 4; i++) {

                GlStateManager.pushMatrix();
                GlStateManager.translate(1.5 - i, 0, 0);

                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.translate(0, 1.0625, 0);

                GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assemfac.assemblerModule[i].recipe);
                if(recipe != null) {
                    ItemStack stack = recipe.getIcon();
                    stack.setCount(1);

                    if (stack.getItem() instanceof ItemBlock) {
                        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, assemfac.getWorld(), null);
                        if (model.isGui3d()) {
                            GlStateManager.translate(0, -0.0625, 0);
                        } else {
                            GlStateManager.translate(0, -0.125, 0);
                            GlStateManager.scale(0.5, 0.5, 0.5);
                        }
                    } else {
                        GlStateManager.rotate(-90F, 1F, 0F, 0F);
                        GlStateManager.translate(0, -0.25, 0);
                    }

                    GlStateManager.scale(1.25, 1.25, 1.25);

                    if (dummy == null || dummy.world != assemfac.getWorld()) {
                        dummy = new EntityItem(assemfac.getWorld(), 0, 0, 0, stack);
                    }
                    dummy.setItem(stack);
                    dummy.hoverStart = 0.0F;

                    Minecraft.getMinecraft().getRenderManager().renderEntity(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, false); // FIXME incorrect position
                }
                GlStateManager.popMatrix();
            }

            RenderArcFurnace.fullbright(true);
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            bindTexture(ResourceManager.assembly_factory_sparks_tex);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            double wide = 0.1875D;
            double narrow = 0.0D;
            double length = 1.25D;
            double uMin = ((assemfac.getWorld().getTotalWorldTime() / 10D) + interp) % 10D;
            double uMax = uMin + 1D;
            double epsilon = 0.01D;

            GlStateManager.pushMatrix();
            if (arm2[3] <= -0.375D) {
                GlStateManager.translate(0.5D + slide1, 1.0625D, -arm2[2] / 45D);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(-epsilon, -wide, length).tex(uMin + 0.5D, 0D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(-epsilon, wide, length).tex(uMin + 0.5D, 1D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(-epsilon, narrow, 0D).tex(uMax + 0.5D, 1D).color(1F, 1F, 1F, 1F).endVertex();
                buffer.pos(-epsilon, -narrow, 0D).tex(uMax + 0.5D, 0D).color(1F, 1F, 1F, 1F).endVertex();

                buffer.pos(epsilon, -wide, length).tex(uMin, 1D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(epsilon, wide, length).tex(uMin, 0D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(epsilon, narrow, 0D).tex(uMax, 0D).color(1F, 1F, 1F, 1F).endVertex();
                buffer.pos(epsilon, -narrow, 0D).tex(uMax, 1D).color(1F, 1F, 1F, 1F).endVertex();
                tessellator.draw();
            }
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            if (arm4[3] <= -0.375D) {
                GlStateManager.translate(-0.5D - slide2, 1.0625D, arm4[2] / 45D);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(-epsilon, -wide, -length).tex(uMin + 0.5D, 0D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(-epsilon, wide, -length).tex(uMin + 0.5D, 1D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(-epsilon, narrow, 0D).tex(uMax + 0.5D, 1D).color(1F, 1F, 1F, 1F).endVertex();
                buffer.pos(-epsilon, -narrow, 0D).tex(uMax + 0.5D, 0D).color(1F, 1F, 1F, 1F).endVertex();

                buffer.pos(epsilon, -wide, -length).tex(uMin, 1D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(epsilon, wide, -length).tex(uMin, 0D).color(1F, 1F, 1F, 0F).endVertex();
                buffer.pos(epsilon, narrow, 0D).tex(uMax, 0D).color(1F, 1F, 1F, 1F).endVertex();
                buffer.pos(epsilon, -narrow, 0D).tex(uMax, 1D).color(1F, 1F, 1F, 1F).endVertex();
                tessellator.draw();
            }
            GlStateManager.popMatrix();

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            RenderArcFurnace.fullbright(false);
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_assembly_factory);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {

        return new ItemRenderBase() {

            public void renderInventory() {
                GlStateManager.translate(0, -1.5, 0);
                GlStateManager.scale(3, 3, 3);
            }
            public void renderCommon(ItemStack item) {
                GlStateManager.scale(0.75, 0.75, 0.75);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.assembly_factory_tex);
                ResourceManager.assembly_factory.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
