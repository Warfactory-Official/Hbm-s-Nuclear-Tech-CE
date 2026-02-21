package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerPneumoTube;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.util.GaugeUtil;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.uninos.networkproviders.PneumaticNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Arrays;

public class GUIPneumoTube extends GuiInfoContainer {
    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/gui/storage/gui_pneumatic_pipe.png");
    private static final ResourceLocation texture_endpoint = new ResourceLocation(Tags.MODID, "textures/gui/storage/gui_pneumatic_endpoint.png");
    public TileEntityPneumoTube tube;
    public boolean endpointOnly;

    public GUIPneumoTube(InventoryPlayer invPlayer, TileEntityPneumoTube tube, boolean endpointOnly) {
        super(new ContainerPneumoTube(invPlayer, tube));
        this.tube = tube;
        this.endpointOnly = endpointOnly;

        this.xSize = 176;
        this.ySize = 185;
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);

        if(!endpointOnly) {
            tube.compair.renderTankInfo(this, x, y, guiLeft + 7, guiTop + 16, 18, 18);

            this.drawCustomInfoStat(x, y, guiLeft + 7, guiTop + 52, 18, 18, x, y, new String[] { (tube.redstone ? (TextFormatting.GREEN + "ON ") : (TextFormatting.RED + "OFF ")) + TextFormatting.RESET + "with Redstone" });
            this.drawCustomInfoStat(x, y, guiLeft + 6, guiTop + 36, 20, 8, x, y, new String[] { "Compressor: " + tube.compair.getPressure() + " PU", "Max range: " + TileEntityPneumoTube.getRangeFromPressure(tube.compair.getPressure()) + "m" });

            this.drawCustomInfoStat(x, y, guiLeft + 151, guiTop + 16, 18, 18, x, y, new String[] { TextFormatting.YELLOW + "Receiver order:", tube.receiveOrder == PneumaticNetwork.RECEIVE_ROBIN ? "Round robin" : "Random" });
            this.drawCustomInfoStat(x, y, guiLeft + 151, guiTop + 52, 18, 18, x, y, new String[] { TextFormatting.YELLOW + "Provider slot order:", tube.sendOrder == PneumaticNetwork.SEND_FIRST ? "First to last" : tube.sendOrder == PneumaticNetwork.SEND_LAST ? "Last to first" : "Random" });
        }


        if(this.mc.player.inventory.getItemStack().isEmpty()) {
            for(int i = 0; i < 15; ++i) {
                Slot slot = this.inventorySlots.inventorySlots.get(i);

                if(this.isMouseOverSlot(slot, x, y) && tube.pattern.modes[i] != null) {
                    this.drawHoveringText(Arrays.asList(TextFormatting.RED + "Right click to change", ModulePatternMatcher.getLabel(tube.pattern.modes[i])), x, y - 30);
                }
            }
        }
        super.renderHoveredToolTip(x, y);
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(!endpointOnly) {
            click(x, y, 7, 52, 18, 18, "redstone");
            click(x, y, 6, 36, 20, 8, "pressure");
            click(x, y, 151, 16, 18, 18, "receive");
            click(x, y, 151, 52, 18, 18, "send");
        }
        click(x, y, 128, 30, 14, 26, "whitelist");
    }

    public void click(int x, int y, int left, int top, int sizeX, int sizeY, String name) {
        if(checkClick(x, y, left, top, sizeX, sizeY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean(name, true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, tube.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.tube.hasCustomName() ? this.tube.getName() : I18n.format(this.tube.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(endpointOnly ? texture_endpoint : texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(tube.whitelist) {
            drawTexturedModalRect(guiLeft + 139, guiTop + 33, 176, 0, 3, 6);
        } else {
            drawTexturedModalRect(guiLeft + 139, guiTop + 47, 176, 0, 3, 6);
        }

        if(!endpointOnly) {
            if(tube.redstone) drawTexturedModalRect(guiLeft + 7, guiTop + 52, 179, 0, 18, 18);

            drawTexturedModalRect(guiLeft + 151, guiTop + 16, 197, 18 * tube.receiveOrder, 18, 18);
            drawTexturedModalRect(guiLeft + 151, guiTop + 52, 215, 18 * tube.sendOrder, 18, 18);

            drawTexturedModalRect(guiLeft + 6 + 4 * (tube.compair.getPressure() - 1), guiTop + 36, 179, 18, 4, 8);
            GaugeUtil.drawSmoothGauge(guiLeft + 16, guiTop + 25, this.zLevel, (double) tube.compair.getFill() / (double) tube.compair.getMaxFill(), 5, 2, 1, 0xCA6C43, 0xAB4223);
        }
    }
}
