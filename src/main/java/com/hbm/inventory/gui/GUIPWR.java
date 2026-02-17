package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerPWR;
import com.hbm.items.ModItems;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.util.GaugeUtil;
import com.hbm.tileentity.machine.TileEntityPWRController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Locale;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIPWR extends GuiInfoContainer {

    protected TileEntityPWRController controller;
    private final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/reactors/gui_pwr.png");

    private GuiTextField field;

    public GUIPWR(InventoryPlayer inventory, TileEntityPWRController controller) {
        super(new ContainerPWR(inventory, controller));
        this.controller = controller;

        this.xSize = 176;
        this.ySize = 188;
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.field = new GuiTextField(0, this.fontRenderer, guiLeft + 57, guiTop + 63, 30, 8);
        this.field.setTextColor(0x00ff00);
        this.field.setDisabledTextColour(0x008000);
        this.field.setEnableBackgroundDrawing(false);
        this.field.setMaxStringLength(3);

        this.field.setText(String.valueOf(100 - controller.rodTarget));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 115, guiTop + 31, 18, 18, mouseX, mouseY, new String[] { "Core: " + String.format(Locale.US, "%,d", controller.coreHeat) + " / " + String.format(Locale.US, "%,d", controller.coreHeatCapacity) + " TU" });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 151, guiTop + 31, 18, 18, mouseX, mouseY, new String[] { "Hull: " + String.format(Locale.US, "%,d", controller.hullHeat) + " / " + String.format(Locale.US, "%,d", TileEntityPWRController.hullHeatCapacityBase) + " TU" });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 52, guiTop + 31, 36, 18, mouseX, mouseY, new String[] { ((int) (controller.progress * 100 / controller.processTime)) + "%" });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 52, guiTop + 53, 54, 4, mouseX, mouseY, new String[] {"Control rod level: " + (100 - (Math.round(controller.rodLevel * 100)/100)) + "%"});
        if(controller.typeLoaded != -1 && controller.amountLoaded > 0) {
            ItemStack display = new ItemStack(ModItems.pwr_fuel, 1, controller.typeLoaded);
            if(guiLeft + 88 <= mouseX && guiLeft + 88 + 18 > mouseX && guiTop + 4 < mouseY && guiTop + 4 + 18 >= mouseY) {
                this.renderToolTip(display, mouseX, mouseY);
            }
        }
        controller.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 5, 16, 52);
        controller.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 5, 16, 52);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

        GlStateManager.pushMatrix();
        double scale = 1.25;
        String flux = String.format(Locale.US, "%,.1f", controller.flux);
        GlStateManager.scale(1 / scale, 1 / scale, 1);
        this.fontRenderer.drawString(flux, (int) (165 * scale - this.fontRenderer.getStringWidth(flux)), (int)(64 * scale), 0x00ff00);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(controller.hullHeat > TileEntityPWRController.hullHeatCapacityBase * 0.8 || controller.coreHeat > controller.coreHeatCapacity * 0.8)
            drawTexturedModalRect(guiLeft + 147, guiTop, 176, 14, 26, 26);

        int p = (int) (controller.progress * 33 / controller.processTime);
        drawTexturedModalRect(guiLeft + 54, guiTop + 33, 176, 0, p, 14);

        int c = (int) (controller.rodLevel * 52 / 100);
        drawTexturedModalRect(guiLeft + 53, guiTop + 54, 176, 40, c, 2);

        GaugeUtil.drawSmoothGauge(guiLeft + 124, guiTop + 40, this.zLevel, (double) controller.coreHeat / (double) controller.coreHeatCapacity, 5, 2, 1, 0x7F0000);
        GaugeUtil.drawSmoothGauge(guiLeft + 160, guiTop + 40, this.zLevel, (double) controller.hullHeat / (double) TileEntityPWRController.hullHeatCapacityBase, 5, 2, 1, 0x7F0000);

        if(controller.typeLoaded != -1 && controller.amountLoaded > 0) {
            RenderHelper.enableGUIStandardItemLighting();
            ItemStack display = new ItemStack(ModItems.pwr_fuel, 1, controller.typeLoaded);
            int x = guiLeft + 89;
            int y = guiTop + 5;
            String text = TextFormatting.YELLOW + "" + controller.amountLoaded + "/" + controller.rodCount;

            this.itemRender.renderItemAndEffectIntoGUI(display, x, y);
            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, display, x, y, text);
            RenderHelper.disableStandardItemLighting();
        }

        controller.tanks[0].renderTank(guiLeft + 8, guiTop + 57, this.zLevel, 16, 52);
        controller.tanks[1].renderTank(guiLeft + 26, guiTop + 57, this.zLevel, 16, 52);

        this.field.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int i) throws IOException {
        super.mouseClicked(mouseX, mouseY, i);
        this.field.mouseClicked(mouseX, mouseY, i);

        if(guiLeft + 88 <= mouseX && guiLeft + 88 + 18 > mouseX && guiTop + 58 < mouseY && guiTop + 58 + 18 >= mouseY) {

            if(NumberUtils.isCreatable(field.getText())) {
                int level = MathHelper.clamp(NumberUtils.toInt(field.getText(), 0), 0, 100);
                field.setText(String.valueOf(level));

                NBTTagCompound control = new NBTTagCompound();
                control.setInteger("control", 100 - level);
                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, controller.getPos()));
                playClickSound();
            }
        }
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if(this.field.textboxKeyTyped(c, i)) return;
        super.keyTyped(c, i);
    }
}