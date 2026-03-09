package com.hbm.inventory.gui;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.hbm.Tags;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMachineAnnihilator;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineAnnihilator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GUIMachineAnnihilator extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_annihilator.png");
    private final TileEntityMachineAnnihilator annihilator;

    protected GuiTextField pool;

    public GUIMachineAnnihilator(InventoryPlayer invPlayer, TileEntityMachineAnnihilator tedf) {
        super(new ContainerMachineAnnihilator(invPlayer, tedf.inventory));
        annihilator = tedf;

        this.xSize = 176;
        this.ySize = 208;
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.pool = new GuiTextField(0, this.fontRenderer, guiLeft + 31, guiTop + 85, 80, 8);
        this.pool.setTextColor(0x00ff00);
        this.pool.setDisabledTextColour(0x00ff00);
        this.pool.setEnableBackgroundDrawing(false);
        this.pool.setMaxStringLength(20);
        this.pool.setText(annihilator.pool);
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);
        this.pool.mouseClicked(x, y, i);
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);
        super.renderHoveredToolTip(x, y);

        if(!annihilator.inventory.getStackInSlot(8).isEmpty() && this.checkClick(x, y, 151, 35, 18, 18)) {
            String name = annihilator.inventory.getStackInSlot(8).getDisplayName();
            if(annihilator.inventory.getStackInSlot(8).getItem() instanceof IItemFluidIdentifier id) {
                FluidType type = id.getType(null, 0, 0, 0, annihilator.inventory.getStackInSlot(8));
                name = type.getLocalizedName();
            }
            this.drawHoveringText(List.of(new String[]{name + ":", String.format(Locale.US, "%,d", annihilator.monitorBigInt)}), x, y);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.annihilator.hasCustomName() ? this.annihilator.getName() : I18n.format(this.annihilator.getDefaultName());

        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        this.pool.drawTextBox();
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if(this.pool.textboxKeyTyped(c, i)) {
            String text = this.pool.getText();
            NBTTagCompound data = new NBTTagCompound();
            data.setString("pool", text);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, annihilator.getPos()));
            return;
        }
        super.keyTyped(c, i);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}
