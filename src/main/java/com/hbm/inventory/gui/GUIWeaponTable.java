package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerWeaponTable;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GUIWeaponTable extends GuiInfoContainer {

    public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_weapon_modifier.png");

    public double yaw = 20;
    public double pitch = -10;

    public GUIWeaponTable(InventoryPlayer player) {
        super(new ContainerWeaponTable(player));
        this.xSize = 176;
        this.ySize = 240;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);

        if (guiLeft + 8 <= mouseX && guiLeft + 8 + 160 > mouseX && guiTop + 18 < mouseY && guiTop + 18 + 79 >= mouseY) {
            if (org.lwjgl.input.Mouse.isButtonDown(0)) {
                double distX = (guiLeft + 8 + 80) - mouseX;
                double distY = (guiTop + 18 + 39.5) - mouseY;
                yaw = distX / 80D * -180D;
                pitch = distY / 39.5D * 90D;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (guiLeft + 26 <= mouseX && guiLeft + 26 + 7 > mouseX && guiTop + 111 < mouseY && guiTop + 111 + 10 >= mouseY) {
            ContainerWeaponTable container = (ContainerWeaponTable) this.inventorySlots;
            ItemStack gun = container.gun.getStackInSlot(0);
            if (!gun.isEmpty() && gun.getItem() instanceof ItemGunBaseNT) {
                int configs = ((ItemGunBaseNT) gun.getItem()).getConfigCount();
                if (configs > 1) {
                    container.configIndex++;
                    container.configIndex %= configs;
                    this.handleMouseClick(null, container.configIndex, 999_999, ClickType.PICKUP);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = net.minecraft.client.resources.I18n.format("container.weaponsTable");
        this.fontRenderer.drawString(name, (this.xSize) / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
        this.fontRenderer.drawString(net.minecraft.client.resources.I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        ContainerWeaponTable container = (ContainerWeaponTable) this.inventorySlots;
        ItemStack gun = container.gun.getStackInSlot(0);

        if (!gun.isEmpty() && gun.getItem() instanceof ItemGunBaseNT) {
            drawTexturedModalRect(guiLeft + 35, guiTop + 112, 176 + 6 * container.configIndex, 0, 6, 8);

            GlStateManager.pushMatrix();
            GlStateManager.translate(guiLeft + 88F, guiTop + 57F, 100F);

            TileEntityItemStackRenderer render = gun.getItem().getTileEntityItemStackRenderer();

            if(render instanceof ItemRenderWeaponBase renderGun) {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(180, 1, 0, 0);
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.popMatrix();
                GlStateManager.rotate((float) yaw, 0F, 1F, 0F);
                GlStateManager.rotate((float) pitch, 1F, 0F, 0F);
                GlStateManager.enableRescaleNormal();
                renderGun.setupModTable(gun);
                renderGun.renderModTable(gun, container.configIndex);
                GlStateManager.disableRescaleNormal();
            }
            GlStateManager.popMatrix();
        }
    }
}
