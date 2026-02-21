package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerOilburner;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Locale;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIOilburner extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_oilburner.png");
    private TileEntityHeaterOilburner heater;

    public GUIOilburner(InventoryPlayer player, TileEntityHeaterOilburner heater) {
        super(new ContainerOilburner(player, heater));

        this.heater = heater;

        this.xSize = 176;
        this.ySize = 203;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 116, guiTop + 17, 16, 52, mouseX, mouseY, new String[]{String.format("%,d", Math.min(heater.heatEnergy, TileEntityHeaterOilburner.maxHeatEnergy)) + " / " + String.format("%,d", TileEntityHeaterOilburner.maxHeatEnergy) + " TU"});

        if(heater.tank.getTankType().hasTrait(FT_Flammable.class)) {
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 34, 18, 18, mouseX, mouseY, new String[] { heater.setting + " mB/t", String.format(Locale.US, "%,d", (int)(heater.tank.getTankType().getTrait(FT_Flammable.class).getHeatEnergy() / 1000) * heater.setting) + " TU/t" });
        }

        heater.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 44, guiTop + 17, 16, 52);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (guiLeft + 80 <= mouseX && guiLeft + 80 + 16 > mouseX && guiTop + 54 < mouseY && guiTop + 54 + 14 >= mouseY) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("toggle", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, heater.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.heater.hasCustomName() ? this.heater.getName() : I18n.format(this.heater.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int i = heater.heatEnergy * 52 / TileEntityHeaterOilburner.maxHeatEnergy;
        drawTexturedModalRect(guiLeft + 116, guiTop + 69 - i, 194, 52 - i, 16, i);

        if (heater.isOn) {
            drawTexturedModalRect(guiLeft + 70, guiTop + 54, 210, 0, 35, 14);

            if(heater.tank.getFill() > 0 && heater.tank.getTankType().hasTrait(FT_Flammable.class)) {
                drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 0, 18, 18);
            }
        }

        heater.tank.renderTank(guiLeft + 44, guiTop + 69, this.zLevel, 16, 52);
    }
}
