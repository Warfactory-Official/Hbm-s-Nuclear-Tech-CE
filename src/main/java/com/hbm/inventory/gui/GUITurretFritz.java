package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import com.hbm.tileentity.turret.TileEntityTurretFritz;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUITurretFritz extends GUITurretBase {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/gui_turret_fritz.png");

    public GUITurretFritz(InventoryPlayer invPlayer, TileEntityTurretBaseNT tile) {
        super(invPlayer, tile);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        ((TileEntityTurretFritz) this.turret).tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 63, 7, 52);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        
        FluidTankNTM tank = ((TileEntityTurretFritz) this.turret).tank;

        tank.renderTank(guiLeft + 134, guiTop + 115, this.zLevel, 7, 52);

        GlStateManager.disableBlend();
    }

    protected ResourceLocation getTexture() {
        return texture;
    }


}
