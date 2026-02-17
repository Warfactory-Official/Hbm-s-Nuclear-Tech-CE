package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.api.entity.RadarEntry;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Arrays;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineRadarNT extends GuiScreen {

    public static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_radar_nt.png");

    protected TileEntityMachineRadarNT radar;
    protected int xSize = 216;
    protected int ySize = 234;
    protected int guiLeft;
    protected int guiTop;

    public int lastMouseX;
    public int lastMouseY;

    public GUIMachineRadarNT(TileEntityMachineRadarNT tile) {
        this.radar = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        String cmd = null;

        if(checkClick(x, y, -10, 88, 8, 8)) cmd = "missiles";
        if(checkClick(x, y, -10, 98, 8, 8)) cmd = "shells";
        if(checkClick(x, y, -10, 108, 8, 8)) cmd = "players";
        if(checkClick(x, y, -10, 118, 8, 8)) cmd = "smart";
        if(checkClick(x, y, -10, 128, 8, 8)) cmd = "red";
        if(checkClick(x, y, -10, 138, 8, 8)) cmd = "map";
        if(checkClick(x, y, -10, 158, 8, 8)) cmd = "gui1";
        if(checkClick(x, y, -10, 178, 8, 8)) cmd = "clear";

        if(cmd != null) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean(cmd, true);
            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, radar.getPos().getX(), radar.getPos().getY(), radar.getPos().getZ()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GlStateManager.enableLighting();
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    private void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        if(checkClick(mouseX, mouseY, 8, 221, 200, 7)) this.drawHoveringText(Arrays.asList(BobMathUtil.getShortNumber(radar.power) + "/" + BobMathUtil.getShortNumber(radar.maxPower) + "HE"), mouseX, mouseY);

        if(checkClick(mouseX, mouseY, -10, 88, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.detectMissiles")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 98, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.detectShells")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 108, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.detectPlayers")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 118, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.smartMode")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 128, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.redMode")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 138, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.showMap")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 158, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.toggleGui")), mouseX, mouseY);
        if(checkClick(mouseX, mouseY, -10, 178, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.clearMap")), mouseX, mouseY);

        if(!radar.entries.isEmpty()) {
            for(RadarEntry m : radar.entries) {
                int x = guiLeft + (int)((m.posX - radar.getPos().getX()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 108;
                int z = guiTop + (int)((m.posZ - radar.getPos().getZ()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 117;

                if(mouseX + 5 > x && mouseX - 4 <= x && mouseY + 5 > z && mouseY - 4 <= z) {

                    String[] text = new String[] { I18nUtil.resolveKey(m.unlocalizedName), m.posX + " / " + m.posZ, "Alt.: " + m.posY };
                    this.drawHoveringText(Arrays.asList(text), x, z);
                    return;
                }
            }
        }

        if(checkClick(mouseX, mouseY, 8, 17, 200, 200)) {
            int tX = (int) ((lastMouseX - guiLeft - 108) * ((double) radar.getRange() * 2 + 1) / 192D + radar.getPos().getX());
            int tZ = (int) ((lastMouseY - guiTop - 117) * ((double) radar.getRange() * 2 + 1) / 192D + radar.getPos().getZ());
            this.drawHoveringText(Arrays.asList(tX + " / " + tZ), lastMouseX, lastMouseY);
        }
    }

    private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawTexturedModalRect(guiLeft - 14, guiTop + 84, 224, 0, 14, 66);
        drawTexturedModalRect(guiLeft - 14, guiTop + 154, 224, 66, 14, 36);

        if(radar.power > 0) {
            int i = (int) (radar.power * 200 / radar.maxPower);
            drawTexturedModalRect(guiLeft + 8, guiTop + 221, 0, 234, i, 16);
        }

        if(radar.scanMissiles ^ (radar.jammed && radar.getWorld().rand.nextBoolean())) drawTexturedModalRect(guiLeft - 10, guiTop + 88, 238, 4, 8, 8);
        if(radar.scanShells ^ (radar.jammed && radar.getWorld().rand.nextBoolean())) drawTexturedModalRect(guiLeft - 10, guiTop + 98, 238, 14, 8, 8);
        if(radar.scanPlayers ^ (radar.jammed && radar.getWorld().rand.nextBoolean())) drawTexturedModalRect(guiLeft - 10, guiTop + 108, 238, 24, 8, 8);
        if(radar.smartMode ^ (radar.jammed && radar.getWorld().rand.nextBoolean())) drawTexturedModalRect(guiLeft - 10, guiTop + 118, 238, 34, 8, 8);
        if(radar.redMode ^ (radar.jammed && radar.getWorld().rand.nextBoolean())) drawTexturedModalRect(guiLeft - 10, guiTop + 128, 238, 44, 8, 8);
        if(radar.showMap ^ (radar.jammed && radar.getWorld().rand.nextBoolean())) drawTexturedModalRect(guiLeft - 10, guiTop + 138, 238, 54, 8, 8);

        if(radar.power < radar.consumption) return;

        if(radar.jammed) {
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5; j++) {
                    drawTexturedModalRect(guiLeft + 8 + i * 40, guiTop + 17 + j * 40, 216, 118 + radar.getWorld().rand.nextInt(81), 40, 40);
                }
            }
            return;
        }

        if(radar.showMap) {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            GlStateManager.disableTexture2D();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < 40_000; i++) {
                int iX = i % 200;
                int iZ = i / 200;
                byte b = radar.map[i];
                if (b > 0) {
                    int color = ((b - 50) * 255 / 78) << 8;
                    buf.pos(guiLeft + 8 + iX, guiTop + 18 + iZ, this.zLevel).color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255).endVertex();
                    buf.pos(guiLeft + 9 + iX, guiTop + 18 + iZ, this.zLevel).color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255).endVertex();
                    buf.pos(guiLeft + 9 + iX, guiTop + 17 + iZ, this.zLevel).color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255).endVertex();
                    buf.pos(guiLeft + 8 + iX, guiTop + 17 + iZ, this.zLevel).color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255).endVertex();
                }
            }
            tess.draw();
            GlStateManager.enableTexture2D();
        }

        Vec3 tr = Vec3.createVectorHelper(100, 0, 0);
        Vec3 tl = Vec3.createVectorHelper(100, 0, 0);
        Vec3 bl = Vec3.createVectorHelper(0, -5, 0);
        float rot = (float) -Math.toRadians(radar.prevRotation + (radar.rotation - radar.prevRotation) * f + 180F);
        tr.rotateAroundZ(rot);
        tl.rotateAroundZ(rot + 0.25F);
        bl.rotateAroundZ(rot);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(guiLeft + 108, guiTop + 117, this.zLevel).color(0, 255, 0, 0).endVertex();
        buf.pos(guiLeft + 108 + tr.xCoord, guiTop + 117 + tr.yCoord, this.zLevel).color(0, 255, 0, 255).endVertex();
        buf.pos(guiLeft + 108 + tl.xCoord, guiTop + 117 + tl.yCoord, this.zLevel).color(0, 255, 0, 0).endVertex();
        buf.pos(guiLeft + 108 + bl.xCoord, guiTop + 117 + bl.yCoord, this.zLevel).color(0, 255, 0, 0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        if(!radar.entries.isEmpty()) {
            for(RadarEntry m : radar.entries) {
                double x = (m.posX - radar.getPos().getX()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D) - 4D;
                double z = (m.posZ - radar.getPos().getZ()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D) - 4D;
                int t = m.blipLevel;
                drawTexturedModalRectDouble(guiLeft + 108 + x, guiTop + 117 + z, 216, 8 * t, 8, 8);
            }
        }
    }

    public void drawTexturedModalRectDouble(double x, double y, int sourceX, int sourceY, int sizeX, int sizeY) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x, y + sizeY, this.zLevel).tex((sourceX + 0) * f, (sourceY + sizeY) * f1).endVertex();
        buf.pos(x + sizeX, y + sizeY, this.zLevel).tex((sourceX + sizeX) * f, (sourceY + sizeY) * f1).endVertex();
        buf.pos(x + sizeX, y, this.zLevel).tex((sourceX + sizeX) * f, (sourceY + 0) * f1).endVertex();
        buf.pos(x, y, this.zLevel).tex((sourceX + 0) * f, (sourceY + 0) * f1).endVertex();
        tess.draw();
    }

    protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
        return guiLeft + left <= x && guiLeft + left + sizeX > x && guiTop + top < y && guiTop + top + sizeY >= y;
    }

    @Override
    protected void keyTyped(char c, int key) {
        if(key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }

        if(checkClick(lastMouseX, lastMouseY, 8, 17, 200, 200) && c >= '1' && c <= '8') {

            int id = c - '1';

            if(!radar.entries.isEmpty()) {
                for(RadarEntry m : radar.entries) {
                    int x = guiLeft + (int) ((m.posX - radar.getPos().getX()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 108;
                    int z = guiTop + (int) ((m.posZ - radar.getPos().getZ()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 117;

                    if(lastMouseX + 5 > x && lastMouseX - 4 <= x && lastMouseY + 5 > z && lastMouseY - 4 <= z) {
                        NBTTagCompound data = new NBTTagCompound();
                        data.setInteger("launchEntity", m.entityID);
                        data.setInteger("link", id);
                        PacketThreading.createSendToServerThreadedPacket(
                                new NBTControlPacket(data, radar.getPos().getX(), radar.getPos().getY(), radar.getPos().getZ()));
                        return;
                    }
                }
            }

            int tX = (int) ((lastMouseX - guiLeft - 108) * ((double) radar.getRange() * 2 + 1) / 192D + radar.getPos().getX());
            int tZ = (int) ((lastMouseY - guiTop - 117) * ((double) radar.getRange() * 2 + 1) / 192D + radar.getPos().getZ());
            NBTTagCompound data = new NBTTagCompound();
            data.setInteger("launchPosX", tX);
            data.setInteger("launchPosZ", tZ);
            data.setInteger("link", id);
            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, radar.getPos().getX(), radar.getPos().getY(), radar.getPos().getZ()));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if(!this.mc.player.isEntityAlive() || this.mc.player.isDead) {
            this.mc.player.closeScreen();
        }
    }
}
