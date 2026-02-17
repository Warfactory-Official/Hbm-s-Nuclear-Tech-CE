package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerElectrolyserMetal;
import com.hbm.inventory.material.Mats;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIElectrolyserMetal extends GuiInfoContainer {

    public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_electrolyser_metal.png");
    private TileEntityElectrolyser electrolyser;

    public GUIElectrolyserMetal(InventoryPlayer invPlayer, TileEntityElectrolyser electrolyser) {
        super(new ContainerElectrolyserMetal(invPlayer, electrolyser));
        this.electrolyser = electrolyser;

        this.xSize = 210;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);

        electrolyser.tanks[3].renderTankInfo(this, mouseX, mouseY, guiLeft + 36, guiTop + 18, 16, 52);

        if(electrolyser.leftStack != null) {
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 58, guiTop + 18, 34, 42, mouseX, mouseY, new String[]{TextFormatting.YELLOW + I18nUtil.resolveKey(electrolyser.leftStack.material.getTranslationKey()) + ": " + Mats.formatAmount(electrolyser.leftStack.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))});
        } else {
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 58, guiTop + 18, 34, 42, mouseX, mouseY, new String[]{TextFormatting.RED + "Empty"});
        }

        if(electrolyser.rightStack != null) {
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 96, guiTop + 18, 34, 42, mouseX, mouseY, new String[]{TextFormatting.YELLOW + I18nUtil.resolveKey(electrolyser.rightStack.material.getTranslationKey()) + ": " + Mats.formatAmount(electrolyser.rightStack.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))});
        } else {
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 96, guiTop + 18, 34, 42, mouseX, mouseY, new String[]{TextFormatting.RED + "Empty"});
        }

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 186, guiTop + 18, 16, 89, electrolyser.power, electrolyser.maxPower);
    }

    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(guiLeft + 8 <= x && guiLeft + 8 + 54 > x && guiTop + 82 < y && guiTop + 82 + 12 >= y) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("sgf", true);
            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, electrolyser.getPos().getX(), electrolyser.getPos().getY(), electrolyser.getPos().getZ()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.electrolyser.hasCustomName() ? this.electrolyser.getName() : I18n.format(this.electrolyser.getName());

        this.fontRenderer.drawString(name, (this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2) - 16, 7, 0xffffff);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(electrolyser.leftStack != null) {
            int p = electrolyser.leftStack.amount * 42 / electrolyser.maxMaterial;
            Color color = new Color(electrolyser.leftStack.material.moltenColor);
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            drawTexturedModalRect(guiLeft + 58, guiTop + 60 - p, 210, 131 - p, 34, p);
        }

        if(electrolyser.rightStack != null) {
            int p = electrolyser.rightStack.amount * 42 / electrolyser.maxMaterial;
            Color color = new Color(electrolyser.rightStack.material.moltenColor);
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            drawTexturedModalRect(guiLeft + 96, guiTop + 60 - p, 210, 131 - p, 34, p);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int p = (int) (electrolyser.power * 89 / electrolyser.maxPower);
        drawTexturedModalRect(guiLeft + 186, guiTop + 107 - p, 210, 89 - p, 16, p);

        if(electrolyser.power >= electrolyser.usageOre)
            drawTexturedModalRect(guiLeft + 190, guiTop + 4, 226, 25, 9, 12);

        int o = electrolyser.processOreTime > 0 ? electrolyser.progressOre * 26 / electrolyser.processOreTime : 0;
        drawTexturedModalRect(guiLeft + 7, guiTop + 71 - o, 226, 25 - o, 22, o);

        electrolyser.tanks[3].renderTank(guiLeft + 36, guiTop + 70, this.zLevel, 16, 52);
    }
}
