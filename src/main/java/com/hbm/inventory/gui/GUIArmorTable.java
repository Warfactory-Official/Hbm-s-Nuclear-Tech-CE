package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.container.ContainerArmorTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;

public class GUIArmorTable extends GuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_armor_modifier.png");

    public GUIArmorTable(InventoryPlayer player) {
        super(new ContainerArmorTable(player));

        this.xSize = 176 + 22;
        this.ySize = 222;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        if (this.mc.player.inventory.getItemStack().isEmpty()) {
            String[] unloc = new String[]{"armorMod.type.helmet", "armorMod.type.chestplate", "armorMod.type.leggings", "armorMod.type.boots",
                    "armorMod.type.servo", "armorMod.type.cladding", "armorMod.type.insert", "armorMod.type.special", "armorMod.type.battery",
                    "armorMod.insertHere"};

            for (int i = 0; i < ArmorModHandler.MOD_SLOTS + 1; ++i) {
                Slot slot = this.inventorySlots.getSlot(i);

                if (this.isMouseOverSlot(slot, mouseX, mouseY) && !slot.getHasStack()) {
                    String text = (i < ArmorModHandler.MOD_SLOTS ? TextFormatting.LIGHT_PURPLE : TextFormatting.YELLOW) + I18n.format(unloc[i]);
                    this.drawHoveringText(Collections.singletonList(text), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18n.format("container.armorTable");
        this.fontRenderer.drawString(name, 22 + (176 / 2 - this.fontRenderer.getStringWidth(name) / 2), 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 22 + 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft + 22, this.guiTop, 0, 0, 176, this.ySize);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop + 31, 176, 96, 22, 100);

        ItemStack armor = this.inventorySlots.getSlot(ArmorModHandler.MOD_SLOTS).getStack();

        if (!armor.isEmpty()) {
            if (armor.getItem() instanceof ItemArmor) this.drawTexturedModalRect(this.guiLeft + 41 + 22, this.guiTop + 60, 176, 74, 22, 22);
            else this.drawTexturedModalRect(this.guiLeft + 41 + 22, this.guiTop + 60, 176, 52, 22, 22);
        } else {
            if (System.currentTimeMillis() % 1000 < 500) this.drawTexturedModalRect(this.guiLeft + 41 + 22, this.guiTop + 60, 176, 52, 22, 22);
        }

        for (int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
            Slot slot = this.inventorySlots.getSlot(i);
            drawIndicator(i, slot.xPos - 1, slot.yPos - 1);
        }
    }

    private void drawIndicator(int index, int x, int y) {
        ItemStack mod = this.inventorySlots.getSlot(index).getStack();
        ItemStack armor = this.inventorySlots.getSlot(ArmorModHandler.MOD_SLOTS).getStack();

        if (mod.isEmpty()) return;

        if (ArmorModHandler.isApplicable(armor, mod)) {
            this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 176, 34, 18, 18);
        } else {
            this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 176, 16, 18, 18);
        }
    }
}