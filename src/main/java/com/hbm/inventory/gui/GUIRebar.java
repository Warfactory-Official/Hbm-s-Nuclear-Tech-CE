package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerRebar;
import com.hbm.items.tool.ItemRebarPlacer;
import com.hbm.util.I18nUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GUIRebar extends GuiInfoContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/gui_rebar.png");
    private final ItemRebarPlacer.InventoryRebar inventory;

    public GUIRebar(InventoryPlayer invPlayer, ItemRebarPlacer.InventoryRebar box) {
        super(new ContainerRebar(invPlayer, box));
        inventory = box;

        xSize = 176;
        ySize = 182;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);

        if (isMouseOverSlot(inventorySlots.getSlot(0), mouseX, mouseY) && !inventorySlots.getSlot(0).getHasStack()) {
            List<Object[]> lines = new ArrayList<>();
            List<ItemStack> list = new ArrayList<>();

            for (Pair<net.minecraft.block.Block, Integer> conk : ItemRebarPlacer.ACCEPTABLE_CONK) {
                list.add(new ItemStack(conk.getKey(), 1, conk.getValue()));
            }

            ItemStack selected = list.isEmpty() ? new ItemStack(ModBlocks.concrete_rebar) : list.get(0);

            if (list.size() > 1) {
                int cycle = (int) ((System.currentTimeMillis() % (1000L * list.size())) / 1000L);
                ItemStack cycleStack = list.get(cycle).copy();
                selected = cycleStack;
                list.set(cycle, cycleStack);
            }

            if (list.size() < 10) {
                lines.add(list.toArray());
            } else if (list.size() < 24) {
                lines.add(list.subList(0, list.size() / 2).toArray());
                lines.add(list.subList(list.size() / 2, list.size()).toArray());
            } else {
                int bound0 = (int) Math.ceil(list.size() / 3D);
                int bound1 = (int) Math.ceil(list.size() / 3D * 2D);
                lines.add(list.subList(0, bound0).toArray());
                lines.add(list.subList(bound0, bound1).toArray());
                lines.add(list.subList(bound1, list.size()).toArray());
            }

            lines.add(new Object[]{I18nUtil.resolveKey(selected.getDisplayName())});
            drawStackText(lines, mouseX, mouseY, fontRenderer, -1);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18n.format("container.rebar");
        if (inventory.target.hasDisplayName()) {
            name = inventory.target.getDisplayName();
        }
        fontRenderer.drawString(name, xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        ItemStack slot = inventory.getStackInSlot(0);
        if (slot.isEmpty() || !ItemRebarPlacer.isValidConk(slot.getItem(), slot.getMetadata())) {
            drawTexturedModalRect(guiLeft + 87, guiTop + 17, 176, 0, 56, 56);
        }
    }
}
