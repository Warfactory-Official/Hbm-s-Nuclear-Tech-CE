package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerPASource;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.albion.TileEntityPASource;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIPASource extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/particleaccelerator/gui_source.png");
    private final TileEntityPASource source;

    public GUIPASource(InventoryPlayer player, TileEntityPASource source) {
        super(new ContainerPASource(player, source));
        this.source = source;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        source.coolantTanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 36, 16, 52);
        source.coolantTanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 36, 16, 52);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 18, 16, 52, source.power, source.getMaxPower());

        List<String> info = new ArrayList<>();
        info.add(TextFormatting.BLUE + "Last momentum: " + TextFormatting.RESET + String.format(Locale.US, "%,d", source.lastSpeed));
        String[] message = I18nUtil.resolveKeyArray("pa." + this.source.state.name().toLowerCase(Locale.US) + ".desc");
        for (String s : message) info.add(TextFormatting.YELLOW + s);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 105, guiTop + 18, 10, 10, mouseX, mouseY, info);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 105, guiTop + 30, 10, 10, mouseX, mouseY, new String[]{TextFormatting.RED + "Cancel " +
                "operation"});
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if (guiLeft + 105 <= x && guiLeft + 105 + 10 > x && guiTop + 30 < y && guiTop + 30 + 10 >= y) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("cancel", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, source.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {

        String name = this.source.hasCustomName() ? this.source.getName() : I18n.format(this.source.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 - 9, 4, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

        this.fontRenderer.drawString(TextFormatting.AQUA + "/123K", 136, 22, 4210752);
        int heat = (int) Math.ceil(source.temperature);
        String label = (heat > 123 ? TextFormatting.RED : TextFormatting.AQUA) + "" + heat + "K";
        this.fontRenderer.drawString(label, 166 - this.fontRenderer.getStringWidth(label), 12, 4210752);

        String state = I18n.format("pa." + this.source.state.name().toLowerCase(Locale.US));
        this.fontRenderer.drawString(state, 79 - this.fontRenderer.getStringWidth(state) / 2, 76, this.source.state.color);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int j = (int) (source.power * 52 / source.getMaxPower());
        drawTexturedModalRect(guiLeft + 8, guiTop + 70 - j, 184, 52 - j, 16, j);

        int heat = (int) Math.ceil(source.temperature);
        if (heat <= 123) drawTexturedModalRect(guiLeft + 44, guiTop + 18, 176, 8, 8, 8);
        if (source.power >= TileEntityPASource.usage) drawTexturedModalRect(guiLeft + 44, guiTop + 43, 176, 8, 8, 8);

        int color = source.state.color;
        float red = (color & 0xff0000) >> 16;
        float green = (color & 0x00ff00) >> 8;
        float blue = (color & 0x0000ff);

        GlStateManager.color(red, green, blue, 1.0F);
        drawTexturedModalRect(guiLeft + 45, guiTop + 73, 176, 52, 68, 14);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        source.coolantTanks[0].renderTank(guiLeft + 134, guiTop + 88, this.zLevel, 16, 52);
        source.coolantTanks[1].renderTank(guiLeft + 152, guiTop + 88, this.zLevel, 16, 52);
    }
}
