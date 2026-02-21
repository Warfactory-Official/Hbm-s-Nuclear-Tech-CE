package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.hbm.util.BobMathUtil;
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
import java.util.Collections;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIBatterySocket extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_battery_socket.png");
    private final TileEntityBatterySocket battery;

    public GUIBatterySocket(InventoryPlayer invPlayer, TileEntityBatterySocket tedf) {
        super(new ContainerBatterySocket(invPlayer, tedf));
        battery = tedf;

        this.xSize = 176;
        this.ySize = 181;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);

        if (!battery.inventory.getStackInSlot(0).isEmpty() && battery.inventory.getStackInSlot(0).getItem() instanceof IBatteryItem item) {
            //this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 62, guiTop + 69 - 52, 52, 52, battery.power, battery.getMaxPower());

            String deltaText = BobMathUtil.getShortNumber(Math.abs(battery.delta)) + "HE/s";

            if (battery.delta > 0) deltaText = TextFormatting.GREEN + "+" + deltaText;
            else if (battery.delta < 0) deltaText = TextFormatting.RED + "-" + deltaText;
            else deltaText = TextFormatting.YELLOW + "+" + deltaText;

            String[] info = {BobMathUtil.getShortNumber(item.getCharge(battery.inventory.getStackInSlot(0))) + "/" + BobMathUtil.getShortNumber(item.getMaxCharge(battery.inventory.getStackInSlot(0))) + "HE", deltaText};

            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 62, guiTop + 69 - 52, 34, 52, mouseX, mouseY, info);
        }

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

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 125, guiTop + 35, 16, 16, mouseX, mouseY, priority);
    }

    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        NBTTagCompound data = new NBTTagCompound();

        if (this.checkClick(x, y, 106, 16, 18, 18)) {
            playClickSound();
            data.setBoolean("low", true);
        }
        if (this.checkClick(x, y, 106, 52, 18, 18)) {
            playClickSound();
            data.setBoolean("high", true);
        }
        if (this.checkClick(x, y, 125, 35, 16, 16)) {
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
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (!battery.inventory.getStackInSlot(0).isEmpty() && battery.inventory.getStackInSlot(0).getItem() instanceof IBatteryItem item) {
            long power = item.getCharge(battery.inventory.getStackInSlot(0));
            long maxPower = item.getMaxCharge(battery.inventory.getStackInSlot(0));

            if (power > Long.MAX_VALUE / 100) {
                power /= 100;
                maxPower /= 100;
            }
            if (maxPower <= 1) maxPower = 1;
            int p = (int) (power * 52 / maxPower); // won't work then flying too close to the sun (the limits of the LONG data type)
            drawTexturedModalRect(guiLeft + 62, guiTop + 69 - p, 176, 52 - p, 34, p);
        }

        drawTexturedModalRect(guiLeft + 106, guiTop + 16, 176, 52 + battery.redLow * 18, 18, 18);
        drawTexturedModalRect(guiLeft + 106, guiTop + 52, 176, 52 + battery.redHigh * 18, 18, 18);
        drawTexturedModalRect(guiLeft + 125, guiTop + 35, 194, 52 + battery.priority.ordinal() * 16 - 16, 16, 16);
    }
}
