package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderDieselGen extends TileEntitySpecialRenderer<TileEntityMachineDiesel> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineDiesel tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (tile.getBlockMetadata()) {
            case 3:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.dieselgen_tex);

        ResourceManager.dieselgen.renderPart("Generator");

        if (tile.hasAcceptableFuel() && tile.tank.getFill() > 0) {
            double swingSide = Math.sin(System.currentTimeMillis() / 50D) * 0.005;
            double swingFront = Math.sin(System.currentTimeMillis() / 25D) * 0.005;
            GlStateManager.translate(swingFront, 0, swingSide);
        }

        ResourceManager.dieselgen.renderPart("Engine");
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_diesel);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2.5, 0);
                double scale = 5;
                GlStateManager.scale(scale, scale, scale);
            }

            public void renderCommon() {
                GlStateManager.scale(2, 2, 2);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.dieselgen_tex);
                ResourceManager.dieselgen.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
