package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.gui.element.GUIScrollingList;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.render.NTMRenderHelper.bindTexture;
import static com.hbm.util.GuiUtil.checkMouseBoundary;
import static com.hbm.util.SoundUtil.playClickSound;

public class GUITurretTargetFilter extends GuiScreen {
    private final static ResourceLocation backgroundTexture = new ResourceLocation(Tags.MODID, "textures/gui/gui_turret_target_filter.png");

    private static final int sizeX = 242;
    private static final int sizeY = 124;

    private final List<String> mobList = EntityList.getEntityNameList().stream().filter(key -> {
        Class<? extends Entity> clazz = EntityList.getClass(key);
        return clazz != null && EntityLiving.class.isAssignableFrom(clazz);
    }).map(ResourceLocation::toString).sorted().toList();
    private final List<String> filteredMobList = new ArrayList<>();

    private int guiLeft;
    private int guiTop;

    private GUIScrollingList mobScrollList;
    private GUIScrollingList filterScrollingList;
    private GuiTextField mobSearchField;

    private final TileEntityTurretBaseNT turret;

    public GUITurretTargetFilter(TileEntityTurretBaseNT turret) {
        this.turret = turret;
    }

    @Override
    public void initGui() {
        super.initGui();

        filteredMobList.addAll(mobList);

        this.guiLeft = (this.width - sizeX) / 2;
        this.guiTop = (this.height - sizeY) / 2;

        this.mobScrollList = new GUIScrollingList(fontRenderer, filteredMobList, this::getMobName, backgroundTexture, guiLeft, guiTop, 6, 31, 4, 6, 86, 83, 8, 97, 32, 12, 15, 83, 1, 10, 0.7F);
        this.filterScrollingList = new GUIScrollingList(fontRenderer, turret.mobFilter, this::getMobName, backgroundTexture, guiLeft, guiTop, 132, 31, 4, 6, 86, 83, 8, 223, 32, 12, 15, 83, 1, 10, 0.7F);

        this.mobSearchField = new GuiTextField(0, this.fontRenderer, guiLeft + 8, guiTop + 14, 84, 14);
        this.mobSearchField.setEnableBackgroundDrawing(false);
        this.mobSearchField.setTextColor(-1);
        this.mobSearchField.setFocused(true);
        this.mobSearchField.setMaxStringLength(20);

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawGuiContainerBackgroundLayer();
        drawGuiContainerForegroundLayer();

        mobScrollList.drawScreen(mouseX, mouseY);
        filterScrollingList.drawScreen(mouseX, mouseY);
        mobSearchField.drawTextBox();
    }

    private String getMobName(String id) {
        String translationName = EntityList.getTranslationName(new ResourceLocation(id));

        if (translationName == null) {
            return id;
        }

        String result = TextFormatting.getTextWithoutFormattingCodes(I18n.format("entity." + translationName + ".name"));

        if (result == null) {
            return id;
        }

        return result;
    }

    private void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(backgroundTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, sizeX, sizeY);
    }

    private void drawGuiContainerForegroundLayer() {
        int textureX = 0;

        if (!turret.isBlacklistFilter) {
            textureX = 10;
        }

        drawTexturedModalRect(guiLeft + 227, guiTop + 5, textureX, 124, 10, 12);
    }

    private void updateMobList() {
        filteredMobList.clear();
        filteredMobList.addAll(mobList.stream().filter(id -> {
            String name = getMobName(id);
            String searchName = mobSearchField.getText();
            return id.toLowerCase().contains(searchName) || name.toLowerCase().contains(searchName);
        }).toList());

        mobScrollList.selectedSlot = -1;
        mobScrollList.resetScroll();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mobScrollList.mouseClicked(mouseX, mouseY, mouseButton);
        filterScrollingList.mouseClicked(mouseX, mouseY, mouseButton);
        mobSearchField.mouseClicked(mouseX, mouseY, mouseButton);

        final BlockPos turretPos = turret.getPos();

        // toggle (black/white)list
        if (checkMouseBoundary(guiLeft, guiTop, mouseX, mouseY, 227, 5, 10, 12)) {
            playClickSound();

            PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turretPos, 0, 6));
        }

        // add entity to filter
        if (checkMouseBoundary(guiLeft, guiTop, mouseX, mouseY, 112, 31, 18, 18)) {
            playClickSound();

            if (mobScrollList.selectedSlot == -1) {
                return;
            }

            NBTTagCompound data = new NBTTagCompound();
            data.setString("addFilter", mobList.get(mobScrollList.selectedSlot));
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turretPos));
        }

        // remove entity from filter
        if (checkMouseBoundary(guiLeft, guiTop, mouseX, mouseY, 112, 52, 18, 18)) {
            playClickSound();

            if (filterScrollingList.selectedSlot == -1) {
                return;
            }

            NBTTagCompound data = new NBTTagCompound();
            data.setString("removeFilter", turret.mobFilter.get(filterScrollingList.selectedSlot));
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turretPos));

            if(!turret.mobFilter.isEmpty()) {
                filterScrollingList.selectedSlot = 0;
            } else {
                filterScrollingList.selectedSlot = -1;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        mobScrollList.mouseReleased();
        filterScrollingList.mouseReleased();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        mobScrollList.handleMouseInput();
        filterScrollingList.handleMouseInput();
    }

    @Override
    protected void keyTyped(char c, int i) {
        if (this.mobSearchField.textboxKeyTyped(c, i)) {
            updateMobList();
            return;
        }

        if (i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
