package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePrecAss;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
@AutoRegister
public class RenderPrecAss extends TileEntitySpecialRenderer<TileEntityMachinePrecAss> implements IItemRendererProvider {

    public static EntityItem dummy;

    @Override
    public void render(TileEntityMachinePrecAss assembler, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(assembler.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(0F, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(90F, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(180F, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(270F, 0F, 1F, 0F); break;
        }

        bindTexture(ResourceManager.precass_tex);
        ResourceManager.assembly_machine.renderPart("Base");
        if(assembler.frame) ResourceManager.assembly_machine.renderPart("Frame");

        GlStateManager.pushMatrix();

        double spin = BobMathUtil.interp(assembler.prevRing, assembler.ring, partialTicks);

        double[] arm = new double[] {
                BobMathUtil.interp(assembler.prevArmAngles[0], assembler.armAngles[0], partialTicks),
                BobMathUtil.interp(assembler.prevArmAngles[1], assembler.armAngles[1], partialTicks),
                BobMathUtil.interp(assembler.prevArmAngles[2], assembler.armAngles[2], partialTicks)
        };

        GlStateManager.rotate((float) spin, 0F, 1F, 0F);
        ResourceManager.assembly_machine.renderPart("Ring");
        ResourceManager.assembly_machine.renderPart("Ring2");

        for(int i = 0; i < 4; i++) {
            renderArm(arm, BobMathUtil.interp(assembler.prevStrikers[i], assembler.strikers[i], partialTicks));
            GlStateManager.rotate(-90F, 0F, 1F, 0F);
        }

        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GenericRecipe recipe = PrecAssRecipes.INSTANCE.recipeNameMap.get(assembler.assemblerModule.recipe);
        if(recipe != null && MainRegistry.proxy.me().getDistanceSq(assembler.getPos().getX() + 0.5, assembler.getPos().getY() + 1, assembler.getPos().getZ() + 0.5) < 35 * 35) {

            GlStateManager.rotate(90F, 0F, 1F, 0F);
            GlStateManager.translate(0, 1.0625, 0);

            ItemStack stack = recipe.getIcon();
            stack.setCount(1);

            if(stack.getItem() instanceof ItemBlock) {
                boolean is3D = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, assembler.getWorld(), null).isGui3d();
                if(is3D) {
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

            if(dummy == null || dummy.world != assembler.getWorld()) dummy = new EntityItem(assembler.getWorld(), 0, 0, 0, stack);
            dummy.setItem(stack);
            dummy.hoverStart = 0.0F;

            Minecraft.getMinecraft().getRenderManager().renderEntity(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, false);
        }

        GlStateManager.popMatrix();
    }

    public static void renderArm(double[] arm, double striker) {

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(0, 1.625, 0.9375);
            GlStateManager.rotate((float) arm[0], 1F, 0F, 0F);
            GlStateManager.translate(0, -1.625, -0.9375);
            ResourceManager.assembly_machine.renderPart("ArmLower1");

            GlStateManager.translate(0, 2.375, 0.9375);
            GlStateManager.rotate((float) arm[1], 1F, 0F, 0F);
            GlStateManager.translate(0, -2.375, -0.9375);
            ResourceManager.assembly_machine.renderPart("ArmUpper1");

            GlStateManager.translate(0, 2.375, 0.4375);
            GlStateManager.rotate((float) arm[2], 1F, 0F, 0F);
            GlStateManager.translate(0, -2.375, -0.4375);
            ResourceManager.assembly_machine.renderPart("Head1");
            GlStateManager.translate(0, striker, 0);
            ResourceManager.assembly_machine.renderPart("Spike1");
        } GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_precass);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2.75, 0);
                GlStateManager.scale(4.5, 4.5, 4.5);
            }
            public void renderCommon(ItemStack item) {
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                GlStateManager.scale(0.75, 0.75, 0.75);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                RenderPrecAss.this.bindTexture(ResourceManager.precass_tex);
                ResourceManager.assembly_machine.renderPart("Base");
                ResourceManager.assembly_machine.renderPart("Frame");
                ResourceManager.assembly_machine.renderPart("Ring");
                ResourceManager.assembly_machine.renderPart("Ring2");
                double[] arm = new double[] {45, -30, 45};
                for(int i = 0; i < 4; i++) {
                    renderArm(arm, 0);
                    GlStateManager.rotate(90F, 0F, 1F, 0F);
                }
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}