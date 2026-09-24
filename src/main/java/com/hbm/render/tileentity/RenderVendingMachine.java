package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockVendingMachine;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderVendingMachine extends TileEntitySpecialRenderer<BlockVendingMachine.TileEntityVendingMachine> implements IItemRendererProvider {
    @Override
    public void render(BlockVendingMachine.TileEntityVendingMachine te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch (te.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(90, 0f, 1f, 0f); break;
            case 4: GlStateManager.rotate(180, 0f, 1f, 0f); break;
            case 3: GlStateManager.rotate(270, 0f, 1f, 0f); break;
            case 5: GlStateManager.rotate(0, 0f, 1f, 0f); break;
        }

        World world = te.getWorld();
        if (world.getBlockState(te.getPos().up()).getBlock() == te.getBlockType()) {
            bindTexture(ResourceManager.vending_machine_tex);
            ResourceManager.vending_machine.renderPart(world.getBlockState(te.getPos().up()).getValue(BlockDummyable.META) >= BlockDummyable.extra ? "Obamna" : "Soda");
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.vending_machine);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0, -4, 0);
                GlStateManager.scale(6.25, 6.25, 6.25);
            }

            @Override
            public void renderCommon(ItemStack stack) {
                bindTexture(ResourceManager.vending_machine_tex);
                ResourceManager.vending_machine.renderPart(stack.getItemDamage() % 2 == 0 ? "Soda" : "Obamna");
            }
        };
    }
}
