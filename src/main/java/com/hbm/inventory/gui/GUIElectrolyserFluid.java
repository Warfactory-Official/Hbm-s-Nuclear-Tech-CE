package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerElectrolyserFluid;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIElectrolyserFluid extends GuiInfoContainer {

    public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_electrolyser_fluid.png");
    private TileEntityElectrolyser electrolyser;

    public GUIElectrolyserFluid(InventoryPlayer invPlayer, TileEntityElectrolyser electrolyser) {
        super(new ContainerElectrolyserFluid(invPlayer, electrolyser));
        this.electrolyser = electrolyser;

        this.xSize = 210;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);

        electrolyser.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 42, guiTop + 18, 16, 52);
        electrolyser.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 96, guiTop + 18, 16, 52);
        electrolyser.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 18, 16, 52);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 186, guiTop + 18, 16, 89, electrolyser.power, electrolyser.maxPower);
    }

    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(guiLeft + 8 <= x && guiLeft + 8 + 54 > x && guiTop + 82 < y && guiTop + 82 + 12 >= y) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("sgm", true);
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

        int p = (int) (electrolyser.power * 89 / electrolyser.maxPower);
        drawTexturedModalRect(guiLeft + 186, guiTop + 107 - p, 210, 89 - p, 16, p);

        if(electrolyser.power >= electrolyser.usageFluid)
            drawTexturedModalRect(guiLeft + 190, guiTop + 4, 226, 40, 9, 12);

        int e = electrolyser.processFluidTime > 0 ? electrolyser.progressFluid * 41 / electrolyser.processFluidTime : 0;
        drawTexturedModalRect(guiLeft + 62, guiTop + 26, 226, 0, 12, e);

        electrolyser.tanks[0].renderTank(guiLeft + 42, guiTop + 70, this.zLevel, 16, 52);
        electrolyser.tanks[1].renderTank(guiLeft + 96, guiTop + 70, this.zLevel, 16, 52);
        electrolyser.tanks[2].renderTank(guiLeft + 116, guiTop + 70, this.zLevel, 16, 52);
    }
}
