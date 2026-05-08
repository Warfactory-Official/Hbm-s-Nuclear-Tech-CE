package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCargoElevator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderCargoElevator extends TileEntitySpecialRenderer<TileEntityCargoElevator> implements IItemRendererProvider {

    @Override
    public void render(TileEntityCargoElevator elevator, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        bindTexture(ResourceManager.cargo_elevator_tex);

        if (elevator.renderPlatform) {
            double extension = elevator.prevExtension + (elevator.extension - elevator.prevExtension) * partialTicks;
            ResourceManager.cargo_elevator.renderPart("Base");

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, extension, 0.0D);
            ResourceManager.cargo_elevator.renderPart("Platform");
            for (int i = 0; i < extension + 1; i++) {
                ResourceManager.cargo_elevator.renderPart("Piston");
                GlStateManager.translate(0.0D, -1.0D, 0.0D);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        for (int i = 0; i <= elevator.height; i++) {
            ResourceManager.cargo_elevator.renderPart("Guides");
            GlStateManager.translate(0.0D, 1.0D, 0.0D);
        }
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.cargo_elevator);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0.0D, -2.75D, 0.0D);
                GlStateManager.scale(3.25D, 3.25D, 3.25D);
            }

            @Override
            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.cargo_elevator_tex);
                ResourceManager.cargo_elevator.renderPart("Base");
                ResourceManager.cargo_elevator.renderPart("Piston");
                ResourceManager.cargo_elevator.renderPart("Guides");
                GlStateManager.translate(0.0D, 1.0D, 0.0D);
                ResourceManager.cargo_elevator.renderPart("Piston");
                ResourceManager.cargo_elevator.renderPart("Guides");
                ResourceManager.cargo_elevator.renderPart("Platform");
                GlStateManager.translate(0.0D, 1.0D, 0.0D);
                ResourceManager.cargo_elevator.renderPart("Guides");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
