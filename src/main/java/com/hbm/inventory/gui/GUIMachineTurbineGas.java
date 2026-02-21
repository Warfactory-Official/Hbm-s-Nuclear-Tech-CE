package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerMachineTurbineGas;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineTurbineGas extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/generators/gui_turbinegas.png");
    private static final ResourceLocation gauge_tex = new ResourceLocation(Tags.MODID + ":textures/gui/gauges/button_big.png");
    private final TileEntityMachineTurbineGas turbinegas;

    private int yStart;
    private int slidStart;
    private int numberToDisplay = 0; //for startup
    private int digitNumber = 0;
    private int exponent = 0;

    public GUIMachineTurbineGas(InventoryPlayer invPlayer, TileEntityMachineTurbineGas te) {
        super(new ContainerMachineTurbineGas(invPlayer, te));
        turbinegas = te;

        this.xSize = 176;
        this.ySize = 223;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        super.mouseClicked(mouseX, mouseY, mouseButton);

        slidStart = turbinegas.powerSliderPos;
        yStart = mouseY;

        if (Math.sqrt(Math.pow((mouseX - (guiLeft + 88)), 2) + Math.pow((mouseY - (guiTop + 40)), 2)) <= 8) {
            if (turbinegas.counter == 0 || turbinegas.counter == 579) {
                int state = turbinegas.state - 1; //offline(0) to startup(-1), online(1) to offline(0)
                playClickSound();
                NBTTagCompound data = new NBTTagCompound();
                data.setInteger("state", state);
                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turbinegas.getPos()));
            }
        }
        if (turbinegas.state == 1 && mouseX > guiLeft + 74 && mouseX <= guiLeft + 74 + 29 && mouseY >= guiTop + 86 && mouseY < guiTop + 86 + 13) {
            //auto mode button
            boolean automode = !turbinegas.autoMode;
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("autoMode", automode);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turbinegas.getPos()));
        }

        if (turbinegas.state == 1 && (guiTop + 97 - slidStart) <= yStart && (guiTop + 103 - slidStart) > yStart && guiLeft + 36 < mouseX && guiLeft + 52 >= mouseX) { //power slider

            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("autoMode", false); //if you click the slider with automode on, turns off automode
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turbinegas.getPos()));
            playClickSound();
        }
    }

    @Override
    protected void mouseClickMove(int x, int y, int p_146273_3_, long p_146273_4_) {

        super.mouseClickMove(x, y, p_146273_3_, p_146273_4_);

        int slidPos;

        if (!turbinegas.autoMode && turbinegas.state == 1 && guiLeft + 36 < x && guiLeft + 52 >= x && guiTop + 37 < y && guiTop + 103 >= y) {
            //area in which the slider can move

            if ((guiTop + 97 - slidStart) <= yStart && (guiTop + 103 - slidStart) > yStart) {
                slidPos = guiTop + 100 - y;

                if (slidPos > 60) slidPos = 60;
                else if (slidPos < 0) slidPos = 0;

                NBTTagCompound data = new NBTTagCompound();
                data.setDouble("slidPos", slidPos);
                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turbinegas.getPos()));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {

        super.drawScreen(mouseX, mouseY, f);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 108, 142, 16, turbinegas.power, turbinegas.getMaxPower());
        if (turbinegas.state == 1) {
            double consumption = TileEntityMachineTurbineGas.fuelMaxCons.getOrDefault(turbinegas.tanks[0].getTankType(), 5.0D);
            double consumptionRate = 20 * (consumption * 0.05D + consumption * (turbinegas.powerSliderPos / 60.0D));
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 36, guiTop + 36, 16, 66, mouseX, mouseY,
                    new String[]{"Fuel consumption: " + String.format("%.1f", consumptionRate) + " mb/s"});
        } else {
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 36, guiTop + 36, 16, 66, mouseX, mouseY, new String[]{"Generator offline"});
        }

        if (turbinegas.temp >= 20)
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 133, guiTop + 23, 8, 72, mouseX, mouseY,
                    new String[]{"Temperature: " + (turbinegas.temp) + "°C"});
        else this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 133, guiTop + 23, 8, 72, mouseX, mouseY, new String[]{"Temperature: 20°C"});

        turbinegas.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 16, 16, 48);
        turbinegas.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 70, 16, 32);
        turbinegas.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 147, guiTop + 61, 16, 36);
        turbinegas.tanks[3].renderTankInfo(this, mouseX, mouseY, guiLeft + 147, guiTop + 21, 16, 36);

        String[] info = I18nUtil.resolveKeyArray("desc.gui.turbinegas.automode");
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 34, 16, 16, guiLeft - 8, guiTop + 44 + 16, info);

        List<String> fuels = new ArrayList<>();
        fuels.add(I18nUtil.resolveKey("desc.gui.turbinegas.fuels"));
        for (FluidType type : Fluids.getInNiceOrder()) {
            if (type.hasTrait(FT_Combustible.class) && type.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS) {
                fuels.add("  " + type.getLocalizedName());
            }
        }
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 34 + 16, 16, 16, guiLeft - 8, guiTop + 44 + 16, fuels);

        String[] warning = I18nUtil.resolveKeyArray("desc.gui.turbinegas.warning");
        if (turbinegas.tanks[0].getFill() < 5000 || turbinegas.tanks[1].getFill() < 1000)
            this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 34 + 32, 16, 16, guiLeft - 8, guiTop + 44 + 16, warning);

        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float iinterpolation, int x, int y) {
        super.drawDefaultBackground();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize); //the main thing

        if (turbinegas.autoMode) drawTexturedModalRect(guiLeft + 74, guiTop + 86, 194, 11, 29, 13); //auto mode button
        else drawTexturedModalRect(guiLeft + 74, guiTop + 86, 194, 24, 29, 13);

        switch (turbinegas.state) {
            case 0 -> drawTexturedModalRect(guiLeft + 80, guiTop + 32, 178, 38, 16, 16);
            //red button
            case -1 -> {
                drawTexturedModalRect(guiLeft + 80, guiTop + 32, 194, 38, 16, 16); //orange button
                displayStartup();
            }
            case 1 -> {
                drawTexturedModalRect(guiLeft + 80, guiTop + 32, 210, 38, 16, 16); //green button
                drawPowerMeterDisplay(20 * turbinegas.instantPowerOutput);
            }
            default -> {
            }
        }

        drawTexturedModalRect(guiLeft + 36, guiTop + 97 - turbinegas.powerSliderPos, 178, 0, 16, 6); //power slider

        int power = (int) (turbinegas.power * 142 / TileEntityMachineTurbineGas.maxPower); //power storage
        drawTexturedModalRect(guiLeft + 26, guiTop + 109, 0, 223, power, 16);

        drawRPMGauge(turbinegas.rpm);
        drawThermometer(turbinegas.temp);

        this.drawInfoPanel(guiLeft - 16, guiTop + 34, 16, 16, 3); //info
        this.drawInfoPanel(guiLeft - 16, guiTop + 34 + 16, 16, 16, 2); //fuels
        if ((turbinegas.tanks[0].getFill()) < 5000 || turbinegas.tanks[1].getFill() < 1000)
            this.drawInfoPanel(guiLeft - 16, guiTop + 34 + 32, 16, 16, 7);
        if (turbinegas.tanks[0].getFill() == 0 || turbinegas.tanks[1].getFill() == 0) this.drawInfoPanel(guiLeft - 16, guiTop + 34 + 32, 16, 16, 6);

        turbinegas.tanks[0].renderTank(guiLeft + 8, guiTop + 65, this.zLevel, 16, 48);
        turbinegas.tanks[1].renderTank(guiLeft + 8, guiTop + 103, this.zLevel, 16, 32);
        turbinegas.tanks[2].renderTank(guiLeft + 147, guiTop + 98, this.zLevel, 16, 36);
        turbinegas.tanks[3].renderTank(guiLeft + 147, guiTop + 58, this.zLevel, 16, 36);
    }

    private void displayStartup() {

        if (numberToDisplay < 8888888 && turbinegas.counter < 60) { //48 frames needed to complete

            digitNumber++;
            if (digitNumber == 9) {
                digitNumber = 1;
                exponent++;
            }
            numberToDisplay += Math.pow(10, exponent);
        }

        if (turbinegas.counter > 50) numberToDisplay = 0;

        drawPowerMeterDisplay(numberToDisplay);
    }

    private void drawPowerMeterDisplay(int number) { //display code

        int firstDigitX = 65;
        int firstDigitY = 62;

        int[] digit = new int[7];

        for (int i = 6; i >= 0; i--) { //creates an array of digits that represent the numbers

            digit[i] = number % 10;

            number = number / 10;

            drawTexturedModalRect(guiLeft + firstDigitX + i * 7, guiTop + 9 + firstDigitY, 194 + digit[i] * 5, 0, 5, 11);
        }

        int uselessZeros = 0;

        for (int i = 0; i < 6; i++) { //counts how much zeros there are before the number, to display 57 instead of 000057

            if (digit[i] == 0) uselessZeros++;
            else break;
        }

        for (int i = 0; i < uselessZeros; i++) { //turns off the useless zeros

            drawTexturedModalRect(guiLeft + firstDigitX + i * 7, guiTop + 9 + firstDigitY, 244, 0, 5, 11);
        }
    }

    private void drawThermometer(int temp) {

        int xPos = guiLeft + 136;
        int yPos = guiTop + 28;

        int width = 2;
        int height = 64;

        int maxTemp = 800;

        double uMin = (176D / 256D);
        double uMax = (178D / 256D);
        double vMin = ((64D - 64D * temp / maxTemp) / 256D);
        double vMax = (64D / 256D);

        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xPos, yPos + height, 0.0D).tex(uMin, vMax).endVertex();
        bufferbuilder.pos(xPos + width, yPos + height, 0.0D).tex(uMax, vMax).endVertex();
        bufferbuilder.pos(xPos + width, yPos + 64 - (64D * temp / maxTemp), 0.0D).tex(uMax, vMin).endVertex();
        bufferbuilder.pos(xPos, yPos + 64 - (64D * temp / maxTemp), 0.0D).tex(uMin, vMin).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
    }

    private void drawRPMGauge(int position) {

        int xPos = guiLeft + 64;
        int yPos = guiTop + 16;

        int squareSideLenght = 48;

        double uMin = (48D / 4848D) * position;
        double uMax = (48D / 4848D) * (position + 1);
        double vMin = 0D;
        double vMax = 1D;

        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture(gauge_tex); //long boi

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xPos, yPos + squareSideLenght, 0.0D).tex(uMin, vMax).endVertex();
        bufferbuilder.pos(xPos + squareSideLenght, yPos + squareSideLenght, 0.0D).tex(uMax, vMax).endVertex();
        bufferbuilder.pos(xPos + squareSideLenght, yPos, 0.0D).tex(uMax, vMin).endVertex();
        bufferbuilder.pos(xPos, yPos, 0.0D).tex(uMin, vMin).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.turbinegas.hasCustomName() ? this.turbinegas.getName() : I18n.format(this.turbinegas.getDefaultName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 94, 4210752);
    }
}