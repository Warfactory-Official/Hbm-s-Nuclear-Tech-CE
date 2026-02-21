package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerLaunchPadRusted;
import com.hbm.items.ModItems;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.item.ItemRenderMissileGeneric;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUILaunchPadRusted extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/gui_launch_pad_rusted.png");
    private TileEntityLaunchPadRusted launchpad;

    public GUILaunchPadRusted(InventoryPlayer invPlayer, TileEntityLaunchPadRusted tedf) {
        super(new ContainerLaunchPadRusted(invPlayer, tedf));
        launchpad = tedf;

        this.xSize = 176;
        this.ySize = 236;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);
        drawCustomInfoStat(mouseX, mouseY, guiLeft + 26, guiTop + 36, 16, 16, mouseX, mouseY, new String[]{TextFormatting.YELLOW + "Release Missile", "Missile is locked in lauch position,", "releasing may cause damage to the missile.", "Damaged missile can not be put back", "into launching position."});
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(guiLeft + 26 <= x && guiLeft + 26 + 16 > x && guiTop + 36 < y && guiTop + 36 + 16 >= y) {

            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("release", true);
            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, launchpad.getPos().getX(), launchpad.getPos().getY(), launchpad.getPos().getZ()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.launchpad.hasCustomName() ? this.launchpad.getDefaultName() : I18n.format(this.launchpad.getDefaultName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 4, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        ItemStack codesStack = launchpad.inventory.getStackInSlot(1);
        ItemStack keyStack = launchpad.inventory.getStackInSlot(2);
        boolean hasCodes = !codesStack.isEmpty() && !codesStack.isEmpty() && codesStack.getItem() == ModItems.launch_code;
        boolean hasKey = !keyStack.isEmpty() && !keyStack.isEmpty() && keyStack.getItem() == ModItems.launch_key;

        if (hasCodes) {
            drawTexturedModalRect(guiLeft + 121, guiTop + 32, 192, 0, 6, 8);
        }
        if (hasKey) {
            drawTexturedModalRect(guiLeft + 139, guiTop + 32, 192, 0, 6, 8);
        }

        if (hasCodes && hasKey && launchpad.missileLoaded) {
            BlockPos pos = launchpad.getPos();
            Random rand = new Random((long) pos.getX() * 131_071L + pos.getZ());
            int launchCodes = rand.nextInt(100_000_000);

            for (int i = 0; i < 8; i++) {
                int magnitude = (int) Math.pow(10, i);
                int digit = (launchCodes % (magnitude * 10)) / magnitude;
                drawTexturedModalRect(guiLeft + 109 + 6 * i, guiTop + 85, 192 + 6 * digit, 8, 6, 8);
            }
        }

        if (launchpad.missileLoaded) {
            Consumer<TextureManager> renderer = ItemRenderMissileGeneric.renderers.get(new RecipesCommon.ComparableStack(ModItems.missile_doomsday_rusted).makeSingular());
            if (renderer != null) {
                GlStateManager.pushMatrix();
                float translateX = guiLeft + 70.0F;
                float translateY = guiTop + 120.0F;
                GlStateManager.translate(translateX, translateY, 100.0F);

                float scale = 0.875F;
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.scale(-8.0F, -8.0F, -8.0F);

                GlStateManager.pushMatrix();
                GlStateManager.rotate(75.0F, 0.0F, 1.0F, 0.0F);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.popMatrix();

                GlStateManager.enableRescaleNormal();
                renderer.accept(Minecraft.getMinecraft().getTextureManager());
                GlStateManager.enableRescaleNormal();
                GlStateManager.popMatrix();
            }
        }
    }
}
