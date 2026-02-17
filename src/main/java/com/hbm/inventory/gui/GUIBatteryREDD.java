package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerBatteryREDD;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIBatteryREDD extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_battery_redd.png");
    private final TileEntityBatteryREDD battery;

    public GUIBatteryREDD(InventoryPlayer invPlayer, TileEntityBatteryREDD tedf) {
        super(new ContainerBatteryREDD(invPlayer, tedf));
        battery = tedf;

        this.xSize = 176;
        this.ySize = 181;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);

        String lang = switch (battery.priority) {
            case LOW -> "low";
            case HIGH -> "high";
            default -> "normal";
        };

        List<String> priority = new ArrayList<>();
        priority.add(I18nUtil.resolveKey("battery.priority." + lang));
        priority.add(I18nUtil.resolveKey("battery.priority.recommended"));
        String[] desc = I18nUtil.resolveKeyArray("battery.priority." + lang + ".desc");
        Collections.addAll(priority, desc);

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 152, guiTop + 35, 16, 16, mouseX, mouseY, priority);
    }

    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        NBTTagCompound data = new NBTTagCompound();

        if (this.checkClick(x, y, 133, 16, 18, 18)) {
            playClickSound();
            data.setBoolean("low", true);
        }
        if (this.checkClick(x, y, 133, 52, 18, 18)) {
            playClickSound();
            data.setBoolean("high", true);
        }
        if (this.checkClick(x, y, 152, 35, 16, 16)) {
            playClickSound();
            data.setBoolean("priority", true);
        }

        if (!data.isEmpty()) PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, battery.getPos()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.battery.hasCustomName() ? this.battery.getName() : I18n.format(this.battery.getDefaultName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

        GlStateManager.pushMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.scale(0.5, 0.5, 1);

        String label = String.format(Locale.US, "%,d", battery.power) + " HE";
        this.fontRenderer.drawString(label, 242 - this.fontRenderer.getStringWidth(label), 45, 0x00ff00);

        String deltaText = String.format(Locale.US, "%,d", battery.delta) + " HE/s";

        int comp = battery.delta.compareTo(BigInteger.ZERO);
        if (comp > 0) deltaText = TextFormatting.GREEN + "+" + deltaText;
        else if (comp < 0) deltaText = TextFormatting.RED + deltaText;
        else deltaText = TextFormatting.YELLOW + "+" + deltaText;

        this.fontRenderer.drawString(deltaText, 242 - this.fontRenderer.getStringWidth(deltaText), 65, 0x00ff00);

        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        drawTexturedModalRect(guiLeft + 133, guiTop + 16, 176, 52 + battery.redLow * 18, 18, 18);
        drawTexturedModalRect(guiLeft + 133, guiTop + 52, 176, 52 + battery.redHigh * 18, 18, 18);
        drawTexturedModalRect(guiLeft + 152, guiTop + 35, 194, 52 + battery.priority.ordinal() * 16 - 16, 16, 16);
    }
}
