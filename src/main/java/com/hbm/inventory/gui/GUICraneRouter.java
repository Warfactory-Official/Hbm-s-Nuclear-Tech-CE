package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerCraneRouter;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityCraneRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUICraneRouter extends GuiInfoContainer {
    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crane_router.png");
    private TileEntityCraneRouter router;

    public GUICraneRouter(InventoryPlayer invPlayer, TileEntityCraneRouter tedf) {
        super(new ContainerCraneRouter(invPlayer, tedf));
        router = tedf;

        this.xSize = 256;
        this.ySize = 201;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 3; k++) {
                int buttonX = guiLeft + 7 + j * 222;
                int buttonY = guiTop + 16 + k * 26;

                if (buttonX <= mouseX && mouseX < buttonX + 18 && buttonY < mouseY && mouseY <= buttonY + 18) {
                    playClickSound();
                    NBTTagCompound data = new NBTTagCompound();
                    data.setInteger("toggle", j * 3 + k);
                    PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, router.getPos()));
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 3; k++) {
                int buttonX = guiLeft + 7 + j * 222;
                int buttonY = guiTop + 15 + k * 26;

                if (buttonX <= mouseX && mouseX < buttonX + 18 && buttonY < mouseY && mouseY <= buttonY + 18) {
                    String[] text = new String[2];
                    int index = j * 3 + k;

                    switch (router.modes[index]) {
                        case 0:
                            text = new String[]{"OFF"};
                            break;
                        case 1:
                            text[0] = "WHITELIST";
                            text[1] = "Route if filter matches";
                            break;
                        case 2:
                            text[0] = "BLACKLIST";
                            text[1] = "Route if filter doesn't match";
                            break;
                        case 3:
                            text[0] = "WILDCARD";
                            text[1] = "Route if no other route is valid";
                            break;
                    }

                    drawHoveringText(Arrays.asList(text), mouseX, mouseY);
                }
            }
        }

        if (mc.player.inventory.getItemStack().isEmpty()) {
            for (int i = 0; i < 30; ++i) {
                Slot slot = this.inventorySlots.getSlot(i);
                ModulePatternMatcher matcher = router.patterns[i / 5];
                int index = i % 5;

                if (isMouseOverSlot(slot, mouseX, mouseY) && matcher.modes[index] != null) {
                    String label = TextFormatting.YELLOW + "";

                    switch (matcher.modes[index]) {
                        case "exact":
                            label += "Item and meta match";
                            break;
                        case "wildcard":
                            label += "Item matches";
                            break;
                        default:
                            label += "Ore dict key matches: " + matcher.modes[index];
                            break;
                    }

                    drawHoveringText(Arrays.asList(TextFormatting.RED + "Right click to change", label), mouseX, mouseY - 30);
                }
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.router.hasCustomName() ? this.router.getName() : I18n.format(this.router.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8 + 39, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 93);
        drawTexturedModalRect(guiLeft + 39, guiTop + 101, 39, 101, 176, 100);

        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 3; k++) {
                int index = j * 3 + k;
                int mode = router.modes[index];
                drawTexturedModalRect(guiLeft + 7 + j * 222, guiTop + 16 + k * 26, 238, 93 + mode * 18, 18, 18);
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
                Slot s = this.inventorySlots.getSlot(i);
                this.fontRenderer.drawStringWithShadow(i + "", guiLeft + s.xPos + 2, guiTop + s.yPos, 0xffffff);
                this.fontRenderer.drawStringWithShadow(s.getSlotIndex() + "", guiLeft + s.xPos + 2, guiTop + s.yPos + 8, 0xff8080);
            }
        }
    }
}
