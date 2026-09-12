package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPneumoStorageAccess;
import com.hbm.inventory.container.ContainerPneumoStorageAccess.SlotPneumo;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityPneumoStorageAccess;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class GUIPneumoStorageAccess extends GuiInfoContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_pneumatic_access.png");

    protected final TileEntityPneumoStorageAccess access;
    protected final ContainerPneumoStorageAccess container;
    protected GuiTextField search;

    protected int scrollIndex = 0;
    protected int scrollBounds = 1;
    protected boolean wasClicking = false;
    protected boolean draggingScroll = false;
    protected boolean wasMouseInGUI = false;

    protected static int sorting = ContainerPneumoStorageAccess.SORT_STACK_SIZE;
    protected static boolean startFocussed = false;
    protected static boolean detailedSearch = false;

    public GUIPneumoStorageAccess(InventoryPlayer invPlayer, TileEntityPneumoStorageAccess access) {
        super(new ContainerPneumoStorageAccess(invPlayer, access));
        this.container = (ContainerPneumoStorageAccess) this.inventorySlots;
        this.access = access;
        this.xSize = 176 + 34;
        this.ySize = 251;
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        search = new GuiTextField(0, this.fontRenderer, guiLeft + 45 + 34, guiTop + 127, 86, 12);
        search.setTextColor(0xffffff);
        search.setDisabledTextColour(0xa0a0a0);
        search.setEnableBackgroundDrawing(false);
        search.setMaxStringLength(50);
        search.setText("");
        search.setFocused(startFocussed);

        sendControl("sorting", sorting);
        sendControlBool("detailed", detailedSearch);
    }

    private void sendControl(String key, int value) {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger(key, value);
        PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, access.getPos()));
    }

    private void sendControlBool(String key, boolean value) {
        NBTTagCompound data = new NBTTagCompound();
        data.setBoolean(key, value);
        PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, access.getPos()));
    }

    private void sendSearch() {
        NBTTagCompound data = new NBTTagCompound();
        data.setString("search", search.getText());
        PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, access.getPos()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.wasMouseInGUI = this.checkClick(mouseX, mouseY, 0, 0, xSize, ySize);

        this.scrollBounds = (int) Math.ceil(container.getStackCount() / (double) ContainerPneumoStorageAccess.DISPLAY_COLUMNS - ContainerPneumoStorageAccess.DISPLAY_ROWS);
        if (this.scrollBounds < 1) this.scrollBounds = 1;
        if (this.scrollIndex < 0) this.setScroll(0);
        if (this.scrollIndex > scrollBounds) this.setScroll(scrollBounds);

        boolean isClicking = Mouse.isButtonDown(0);
        if (!isClicking) this.draggingScroll = false;

        if (!wasClicking && isClicking && guiLeft + 153 + 34 <= mouseX && guiLeft + 153 + 34 + 14 > mouseX && guiTop + 16 < mouseY && guiTop + 16 + 108 >= mouseY) {
            draggingScroll = true;
        }

        if (draggingScroll) {
            int range = 92;
            int sY = MathHelper.clamp(mouseY - guiTop - 24, 0, range);
            this.setScroll((int) Math.round(scrollBounds * ((double) sY / (double) range)));
        }

        this.wasClicking = isClicking;

        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 7, 18, 18, mouseX, mouseY, new String[] { I18nUtil.resolveKey("pneumo.sorting") + " " + TextFormatting.YELLOW + I18nUtil.resolveKey("pneumo.sorting.amount") });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 25, 18, 18, mouseX, mouseY, new String[] { I18nUtil.resolveKey("pneumo.sorting") + " " + TextFormatting.YELLOW + I18nUtil.resolveKey("pneumo.sorting.id") });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 43, 18, 18, mouseX, mouseY, new String[] { I18nUtil.resolveKey("pneumo.sorting") + " " + TextFormatting.YELLOW + I18nUtil.resolveKey("pneumo.sorting.name") });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 61, 18, 18, mouseX, mouseY, new String[] { I18nUtil.resolveKey("pneumo.sorting") + " " + TextFormatting.YELLOW + I18nUtil.resolveKey("pneumo.sorting.internal") });

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 79, 18, 18, mouseX, mouseY, new String[] { I18nUtil.resolveKey("pneumo.autofocus") + " " + (startFocussed ? TextFormatting.GREEN + I18nUtil.resolveKey("pneumo.on") : TextFormatting.RED + I18nUtil.resolveKey("pneumo.off")) });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 97, 18, 18, mouseX, mouseY, new String[] { I18nUtil.resolveKey("pneumo.deepsearch") + " " + (detailedSearch ? TextFormatting.GREEN + I18nUtil.resolveKey("pneumo.on") : TextFormatting.RED + I18nUtil.resolveKey("pneumo.off")) });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);

        if (this.checkClick(mouseX, mouseY, 7, 7, 18, 18)) { SoundUtil.playClickSound(); setSorting(ContainerPneumoStorageAccess.SORT_STACK_SIZE); }
        if (this.checkClick(mouseX, mouseY, 7, 25, 18, 18)) { SoundUtil.playClickSound(); setSorting(ContainerPneumoStorageAccess.SORT_ID); }
        if (this.checkClick(mouseX, mouseY, 7, 43, 18, 18)) { SoundUtil.playClickSound(); setSorting(ContainerPneumoStorageAccess.SORT_LOCALIZED); }
        if (this.checkClick(mouseX, mouseY, 7, 61, 18, 18)) { SoundUtil.playClickSound(); setSorting(ContainerPneumoStorageAccess.SORT_INTERNAL); }

        if (this.checkClick(mouseX, mouseY, 7, 79, 18, 18)) { SoundUtil.playClickSound(); startFocussed = !startFocussed; }
        if (this.checkClick(mouseX, mouseY, 7, 97, 18, 18)) { SoundUtil.playClickSound(); detailedSearch = !detailedSearch; sendControlBool("detailed", detailedSearch); }

        search.mouseClicked(mouseX, mouseY, button);
    }

    private void setSorting(int mode) {
        sorting = mode;
        this.scrollIndex = 0;
        sendControl("sorting", mode);
    }

    @Override
    public void handleMouseInput() throws IOException {

        int scrollDir = Mouse.getEventDWheel();

        if (scrollDir != 0 && wasMouseInGUI) {
            this.setScroll(this.getScroll() - (scrollDir > 0 ? 1 : -1));
            return;
        }

        super.handleMouseInput();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18nUtil.resolveKey("container.pneumoStorageAccess");
        this.fontRenderer.drawString(name, 34 + 176 / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
        this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 34 + 8, this.ySize - 96 + 2, 4210752);

        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        double scale = 0.5D;
        GlStateManager.scale(scale, scale, 1);

        for (Slot slot : this.inventorySlots.inventorySlots) {
            if (!(slot instanceof SlotPneumo)) continue;
            if (!slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();
            if (!stack.hasTagCompound()) continue;

            long amount = stack.getTagCompound().getLong(ContainerPneumoStorageAccess.STACK_SIZE_KEY);
            if (amount <= 0) continue;

            String label = BobMathUtil.getShortNumber(amount);
            int ix = (int) ((slot.xPos + 16) / scale) - this.fontRenderer.getStringWidth(label);
            int iy = (int) ((slot.yPos + 16) / scale) - this.fontRenderer.FONT_HEIGHT;
            this.fontRenderer.drawStringWithShadow(label, ix, iy, -1);
        }

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        RenderHelper.enableGUIStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft + 34, guiTop, 0, 0, 176, ySize);

        drawTexturedModalRect(guiLeft, guiTop, 176, 15, 32, 122);

        drawTexturedModalRect(guiLeft + 7, guiTop + 7 + sorting * 18, 208, 0, 18, 18);
        if (startFocussed) drawTexturedModalRect(guiLeft + 7, guiTop + 79, 208, 18, 18, 18);
        if (detailedSearch) drawTexturedModalRect(guiLeft + 7, guiTop + 97, 208, 18, 18, 18);

        drawTexturedModalRect(guiLeft + 34 + 154, guiTop + getScrollBarYPos(), draggingScroll ? 188 : 176, 0, 12, 15);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 2, 0);
        search.drawTextBox();
        GlStateManager.popMatrix();
    }

    public int getScrollBarYPos() {
        int scrollArea = 106 - 15;
        double scrollProgress = (double) getScroll() / (double) scrollBounds;
        if (scrollProgress > 1) scrollProgress = 1;
        return 17 + (int) (scrollProgress * scrollArea);
    }

    public int getScroll() {
        return MathHelper.clamp(scrollIndex, 0, scrollBounds);
    }

    public void setScroll(int scroll) {
        int prevScroll = getScroll();
        this.scrollIndex = MathHelper.clamp(scroll, 0, scrollBounds);
        if (prevScroll != this.scrollIndex) sendControl("scroll", this.scrollIndex);
    }

    @Override
    protected void renderToolTip(ItemStack stack, int x, int y) {
        List<String> list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

        for(int line = 0; line < list.size(); ++line) {
            if(line == 0) {
                list.set(line, stack.getRarity().color + list.get(line));
            } else {
                list.set(line, TextFormatting.GRAY + list.get(line));
            }
        }

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if(font == null) font = this.fontRenderer;
        GUIElements.drawHoveringText(list, x, y, font, itemRender, width, height, GUIElements.STANDARD_HEADER_OFFSET, GUIElements.STANDARD_LINE_DIST, GUIElements.STANDARD_COLOR_BACKGROUND, GUIElements.STANDARD_COLOR_BACKGROUND, 0xD57C4F, 0xAB4223);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if (search.isFocused() && keyCode != 1) {
            if (search.textboxKeyTyped(typedChar, keyCode)) {
                this.scrollIndex = 0;
                sendSearch();
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
