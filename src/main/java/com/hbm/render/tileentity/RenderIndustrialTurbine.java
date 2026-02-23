package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderIndustrialTurbine extends TileEntitySpecialRenderer<TileEntityMachineIndustrialTurbine> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineIndustrialTurbine tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(tile.getBlockMetadata() - 10) {
            case 2: GlStateManager.rotate(180F, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(270F, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(0F, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(90F, 0F, 1F, 0F); break;
        }

        this.bindTexture(ResourceManager.industrial_turbine_tex);
        ResourceManager.industrial_turbine.renderPart("Turbine");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0, 1.5, 0.0);
        GlStateManager.rotate((float)(135 - (tile.tanks[0].getTankType().getID() - Fluids.STEAM.getID()) * 90), 0F, 0F, 1F);
        GlStateManager.translate(0.0, -1.5, 0.0);
        ResourceManager.industrial_turbine.renderPart("Gauge");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0, 1.5, 0.0);
        GlStateManager.rotate((float)(tile.lastRotor + (tile.rotor - tile.lastRotor) * partialTicks), 0F, 0F, -1F);
        GlStateManager.translate(0.0, -1.5, 0.0);
        ResourceManager.industrial_turbine.renderPart("Flywheel");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_industrial_turbine);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase( ) {
            public void renderInventory() {
                GlStateManager.translate(1, 0, 0);
                GlStateManager.scale(3, 3, 3);
            }
            public void renderCommon() {
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.scale(0.75, 0.75, 0.75);
                GlStateManager.translate(0.5, 0, 0);
                GL11.glShadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.industrial_turbine_tex);

                ResourceManager.industrial_turbine.renderPart("Turbine");

                GlStateManager.translate(0, 1.5, 0);
                GlStateManager.rotate(135, 0, 0, 1);
                GlStateManager.translate(0, -1.5, 0);
                ResourceManager.industrial_turbine.renderPart("Gauge");

                double rot = ((double) System.currentTimeMillis() / 5) % 336D;
                GlStateManager.translate(0, 1.5, 0);
                GlStateManager.rotate(rot, 0, 0, -1);
                GlStateManager.translate(0, -1.5, 0);
                ResourceManager.industrial_turbine.renderPart("Flywheel");

                GL11.glShadeModel(GL11.GL_FLAT);
            }
        };
    }
}
