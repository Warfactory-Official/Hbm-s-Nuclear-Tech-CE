package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderAssemblyMachine extends TileEntitySpecialRenderer<TileEntityMachineAssemblyMachine> implements IItemRendererProvider {

    public static EntityItem dummy;

    @Override
    public void render(TileEntityMachineAssemblyMachine tileEntity, double x, double y, double z, float interp, int destroyStage, float alpha) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch (tileEntity.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(270, 0F, 1F, 0F);
        }

        bindTexture(ResourceManager.assembly_machine_tex);
        ResourceManager.assembly_machine.renderPart("Base");
        if(tileEntity.frame) ResourceManager.assembly_machine.renderPart("Frame");

        GlStateManager.pushMatrix();
        // idk somehow rebooting my laptop made IDE decide that GLStateManager.rotate doesn't accept doubles anymore
        double spin = BobMathUtil.interp(tileEntity.prevRing, tileEntity.ring, interp);
        double[] arm1 = tileEntity.arms[0].getPositions(interp);
        double[] arm2 = tileEntity.arms[1].getPositions(interp);

        GlStateManager.rotate((float) spin, 0, 1, 0);
        ResourceManager.assembly_machine.renderPart("Ring");

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(0, 1.625, 0.9375);
            GlStateManager.rotate((float) arm1[0], 1, 0, 0);
            GlStateManager.translate(0, -1.625, -0.9375);
            ResourceManager.assembly_machine.renderPart("ArmLower1");

            GlStateManager.translate(0, 2.375, 0.9375);
            GlStateManager.rotate((float) arm1[1], 1, 0, 0);
            GlStateManager.translate(0, -2.375, -0.9375);
            ResourceManager.assembly_machine.renderPart("ArmUpper1");

            GlStateManager.translate(0, 2.375, 0.4375);
            GlStateManager.rotate((float) arm1[2], 1, 0, 0);
            GlStateManager.translate(0, -2.375, -0.4375);
            ResourceManager.assembly_machine.renderPart("Head1");
            GlStateManager.translate(0, arm1[3], 0);
            ResourceManager.assembly_machine.renderPart("Spike1");
        } GlStateManager.popMatrix();

        GlStateManager.pushMatrix(); {
            GlStateManager.translate(0, 1.625, -0.9375);
            GlStateManager.rotate((float) -arm2[0], 1, 0, 0);
            GlStateManager.translate(0, -1.625, 0.9375);
            ResourceManager.assembly_machine.renderPart("ArmLower2");

            GlStateManager.translate(0, 2.375, -0.9375);
            GlStateManager.rotate((float) -arm2[1], 1, 0, 0);
            GlStateManager.translate(0, -2.375, 0.9375);
            ResourceManager.assembly_machine.renderPart("ArmUpper2");

            GlStateManager.translate(0, 2.375, -0.4375);
            GlStateManager.rotate((float) -arm2[2], 1, 0, 0);
            GlStateManager.translate(0, -2.375, 0.4375);
            ResourceManager.assembly_machine.renderPart("Head2");
            GlStateManager.translate(0, arm2[3], 0);
            ResourceManager.assembly_machine.renderPart("Spike2");
        } GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(tileEntity.assemblerModule.recipe);
        if (recipe != null && MainRegistry.proxy.me().getDistanceSq(tileEntity.getPos().getX() + 0.5, tileEntity.getPos().getY() + 1, tileEntity.getPos().getZ() + 0.5) < 35 * 35) {

            GlStateManager.pushMatrix();

            GlStateManager.translate(0, 1.0625, 0);

            ItemStack stack = recipe.getIcon();
            if (stack != null && !stack.isEmpty()) {
                stack.setCount(1);

                IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, tileEntity.getWorld(), null);
                // note: these are not from upstream at all. these values I've inputted aren't perfect but they should suffice.
                if (model.isGui3d()) {
                    GlStateManager.translate(0, 0.125, 0);
                    GlStateManager.scale(1.25, 1.25, 1.25);
                } else {
                    GlStateManager.translate(0, 0.015, 0);
                    GlStateManager.rotate(-90F, 1F, 0F, 0F);
                    GlStateManager.rotate(-90F, 0F, 0F, 1F);
                    GlStateManager.scale(0.85, 0.85, 0.85);
                }

                Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_assembly_machine);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {

        return new ItemRenderBase() {

            public void renderInventory() {
                GlStateManager.translate(0, -2.75, 0);
                GlStateManager.scale(4.5, 4.5, 4.5);
            }
            public void renderCommon(ItemStack item) {
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.scale(0.75, 0.75, 0.75);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.assembly_machine_tex);
                ResourceManager.assembly_machine.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
