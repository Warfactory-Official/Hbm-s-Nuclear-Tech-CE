package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerPADipole;
import com.hbm.items.ModItems;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.albion.TileEntityPADipole;
import com.hbm.util.Vec3NT;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIPADipole extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/particleaccelerator/gui_dipole.png");
    private final TileEntityPADipole dipole;

    protected GuiTextField threshold;

    public GUIPADipole(InventoryPlayer player, TileEntityPADipole dipole) {
        super(new ContainerPADipole(player, dipole));
        this.dipole = dipole;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.threshold = new GuiTextField(0, this.fontRenderer, guiLeft + 47, guiTop + 77, 66, 8);
        this.threshold.setTextColor(0x00ff00);
        this.threshold.setDisabledTextColour(0x00ff00);
        this.threshold.setEnableBackgroundDrawing(false);
        this.threshold.setMaxStringLength(9);
        this.threshold.setText(String.valueOf(dipole.threshold));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        dipole.coolantTanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 36, 16, 52);
        dipole.coolantTanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 36, 16, 52);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 18, 16, 52, dipole.power, dipole.getMaxPower());

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 62, guiTop + 29, 12, 12, mouseX, mouseY, new String[]{TextFormatting.BLUE + "Player " +
                "orientation", TextFormatting.RED + "Output orientation:", TileEntityPADipole.ditToForgeDir(dipole.dirLower).name()});
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 62, guiTop + 43, 12, 12, mouseX, mouseY, new String[]{TextFormatting.BLUE + "Player " +
                "orientation", TextFormatting.RED + "Output orientation:", TileEntityPADipole.ditToForgeDir(dipole.dirUpper).name()});
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 62, guiTop + 57, 12, 12, mouseX, mouseY, new String[]{TextFormatting.BLUE + "Player " +
                "orientation", TextFormatting.RED + "Output orientation:", TileEntityPADipole.ditToForgeDir(dipole.dirRedstone).name()});
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.threshold.mouseClicked(mouseX, mouseY, mouseButton);

        if (isPointInRegion(62, 29, 12, 12, mouseX, mouseY)) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("lower", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, dipole.getPos()));
        }

        if (isPointInRegion(62, 43, 12, 12, mouseX, mouseY)) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("upper", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, dipole.getPos()));
        }

        if (isPointInRegion(62, 57, 12, 12, mouseX, mouseY)) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("redstone", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, dipole.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.dipole.hasCustomName() ? this.dipole.getName() : I18n.format(this.dipole.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 - 9, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

        this.fontRenderer.drawString(TextFormatting.AQUA + "/123K", 136, 22, 4210752);
        int heat = (int) Math.ceil(dipole.temperature);
        String label = (heat > 123 ? TextFormatting.RED : TextFormatting.AQUA) + "" + heat + "K";
        this.fontRenderer.drawString(label, 166 - this.fontRenderer.getStringWidth(label), 12, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int j = (int) (dipole.power * 52 / dipole.getMaxPower());
        drawTexturedModalRect(guiLeft + 8, guiTop + 70 - j, 184, 52 - j, 16, j);

        int heat = (int) Math.ceil(dipole.temperature);
        if (heat <= 123) drawTexturedModalRect(guiLeft + 93, guiTop + 54, 176, 8, 8, 8);
        ItemStack coilStack = dipole.inventory.getStackInSlot(1);
        if (!coilStack.isEmpty() && coilStack.getItem() == ModItems.pa_coil)
            drawTexturedModalRect(guiLeft + 103, guiTop + 54, 176, 8, 8, 8);
        if (dipole.power >= TileEntityPADipole.usage) drawTexturedModalRect(guiLeft + 83, guiTop + 54, 176, 8, 8, 8);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.glLineWidth(3.0F);

        Vec3NT vec = new Vec3NT(0, 0, 0);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        float playerYaw = this.mc.player.rotationYaw;

        addLine(bufferbuilder, 68, 35, 0x8080ff, vec, 180);
        addLine(bufferbuilder, 68, 35, 0xff0000, vec, playerYaw - dipole.dirLower * 90);
        addLine(bufferbuilder, 68, 49, 0x8080ff, vec, 180);
        addLine(bufferbuilder, 68, 49, 0xff0000, vec, playerYaw - dipole.dirUpper * 90);
        addLine(bufferbuilder, 68, 63, 0x8080ff, vec, 180);
        addLine(bufferbuilder, 68, 63, 0xff0000, vec, playerYaw - dipole.dirRedstone * 90);
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        this.threshold.drawTextBox();

        dipole.coolantTanks[0].renderTank(guiLeft + 134, guiTop + 88, this.zLevel, 16, 52);
        dipole.coolantTanks[1].renderTank(guiLeft + 152, guiTop + 88, this.zLevel, 16, 52);
    }

    public void addLine(BufferBuilder buffer, int x, int y, int color, Vec3NT vec, float yaw) {
        vec.set(0, 6, 0);
        vec.rotateAroundZDeg(yaw);

        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = 1.0F;

        buffer.pos(guiLeft + x, guiTop + y, this.zLevel).color(r, g, b, a).endVertex();
        buffer.pos(guiLeft + x + vec.x, guiTop + y + vec.y, this.zLevel).color(r, g, b, a).endVertex();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.threshold.textboxKeyTyped(typedChar, keyCode)) {
            String text = this.threshold.getText();
            if (text.length() > 1 && text.startsWith("0")) {
                this.threshold.setText(text.substring(1));
            }
            if (this.threshold.getText().isEmpty()) {
                this.threshold.setText("0");
            }
            if (NumberUtils.isDigits(this.threshold.getText())) {
                int num = NumberUtils.toInt(this.threshold.getText());
                NBTTagCompound data = new NBTTagCompound();
                data.setInteger("threshold", num);
                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, dipole.getPos()));
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}