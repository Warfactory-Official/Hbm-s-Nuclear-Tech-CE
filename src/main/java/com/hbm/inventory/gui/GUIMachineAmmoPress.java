package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerMachineAmmoPress;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.inventory.recipes.AmmoPressRecipes.AmmoPressRecipe;
import com.hbm.items.ModItems;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineAmmoPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hbm.main.ModEventHandlerClient.getMouseOverStack;

public class GUIMachineAmmoPress extends GuiInfoContainer {
  private static final ResourceLocation texture =
      new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_ammo_press.png");
  private final TileEntityMachineAmmoPress press;

  private final ArrayList<AmmoPressRecipe> recipes = new ArrayList<>();
  int index;
  int size;
  int selection;
  private GuiTextField search;

  public GUIMachineAmmoPress(InventoryPlayer invPlayer, TileEntityMachineAmmoPress press) {
    super(new ContainerMachineAmmoPress(invPlayer, press));
    this.press = press;

    this.xSize = 176;
    this.ySize = 200;

    this.selection = press.selectedRecipe;

    regenerateRecipes();
  }

  @Override
  public void initGui() {

    super.initGui();

    Keyboard.enableRepeatEvents(true);
    this.search = new GuiTextField(0, this.fontRenderer, guiLeft + 10, guiTop + 75, 66, 12);
    this.search.setTextColor(-1);
    this.search.setDisabledTextColour(-1);
    this.search.setEnableBackgroundDrawing(false);
    this.search.setMaxStringLength(25);
  }

  private void regenerateRecipes() {

    this.recipes.clear();
    this.recipes.addAll(AmmoPressRecipes.recipes);

    resetPaging();
  }

  private void search(String search) {

    search = search.toLowerCase(Locale.US);

    this.recipes.clear();

    if (search.isEmpty()) {
      this.recipes.addAll(AmmoPressRecipes.recipes);

    } else {
      for (AmmoPressRecipe recipe : AmmoPressRecipes.recipes) {
        if (recipe.output.getDisplayName().toLowerCase(Locale.US).contains(search)) {
          this.recipes.add(recipe);
        }
      }
    }

    resetPaging();
  }

  private void resetPaging() {

    this.index = 0;
    this.size = Math.max(0, (int) Math.ceil((this.recipes.size() - 12) / 3D));
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);

    for (Slot slot : this.inventorySlots.inventorySlots) {

      // if the mouse is over a slot, cancel
      if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.getHasStack()) {
        return;
      }
    }

    if (guiLeft <= mouseX
        && guiLeft + xSize > mouseX
        && guiTop < mouseY
        && guiTop + ySize >= mouseY
        && getMouseOverStack().isEmpty()) {
      if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && Mouse.next()) {
        int scroll = Mouse.getEventDWheel();

        if (scroll > 0 && this.index > 0) this.index--;
        if (scroll < 0 && this.index < this.size) this.index++;
      }
    }

    for (int i = index * 3; i < index * 3 + 12; i++) {

      if (i >= this.recipes.size()) break;

      int ind = i - index * 3;

      int ix = 16 + 18 * (ind / 3);
      int iy = 17 + 18 * (ind % 3);
      if (guiLeft + ix <= mouseX
          && guiLeft + ix + 18 > mouseX
          && guiTop + iy < mouseY
          && guiTop + iy + 18 >= mouseY) {
        AmmoPressRecipe recipe = this.recipes.get(i);
        this.renderToolTip(recipe.output, mouseX, mouseY);
      }
    }

    super.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void mouseClicked(int x, int y, int k) throws IOException {
    super.mouseClicked(x, y, k);

    this.search.mouseClicked(x, y, k);

    if (guiLeft + 7 <= x && guiLeft + 7 + 9 > x && guiTop + 17 < y && guiTop + 17 + 54 >= y) {
      playPressSound();
      if (this.index > 0) this.index--;
      return;
    }

    if (guiLeft + 88 <= x && guiLeft + 88 + 9 > x && guiTop + 17 < y && guiTop + 17 + 54 >= y) {
      playPressSound();
      if (this.index < this.size) this.index++;
      return;
    }

    for (int i = index * 3; i < index * 3 + 12; i++) {

      if (i >= this.recipes.size()) break;

      int ind = i - index * 3;

      int ix = 16 + 18 * (ind / 3);
      int iy = 17 + 18 * (ind % 3);
      if (guiLeft + ix <= x && guiLeft + ix + 18 > x && guiTop + iy < y && guiTop + iy + 18 >= y) {

        int newSelection = AmmoPressRecipes.recipes.indexOf(this.recipes.get(i));

        if (this.selection != newSelection) this.selection = newSelection;
        else this.selection = -1;

        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("selection", this.selection);
          PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(
                  data, press.getPos().getX(), press.getPos().getY(), press.getPos().getZ()));
          playPressSound();
        return;
      }
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int i, int j) {
    this.fontRenderer.drawString(
        I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
    super.drawDefaultBackground();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    if (guiLeft + 7 <= x && guiLeft + 7 + 9 > x && guiTop + 17 < y && guiTop + 17 + 54 >= y) {
      drawTexturedModalRect(guiLeft + 7, guiTop + 17, 176, 0, 9, 54);
    }
    if (guiLeft + 88 <= x && guiLeft + 88 + 9 > x && guiTop + 17 < y && guiTop + 17 + 54 >= y) {
      drawTexturedModalRect(guiLeft + 88, guiTop + 17, 185, 0, 9, 54);
    }

    if (this.search.isFocused()) {
      drawTexturedModalRect(guiLeft + 8, guiTop + 72, 176, 54, 70, 16);
    }

    for (int i = index * 3; i < index * 3 + 12; i++) {
      if (i >= recipes.size()) break;

      int ind = i - index * 3;
      int col = ind / 3;
      int row = ind % 3;

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.enableRescaleNormal();

      AmmoPressRecipe recipe = recipes.get(i);

      FontRenderer font = null;
      if (recipe.output != null) font = recipe.output.getItem().getFontRenderer(recipe.output);
      if (font == null) font = fontRenderer;

      itemRender.zLevel = 100.0F;
      itemRender.renderItemAndEffectIntoGUI(
              Objects.requireNonNull(recipe.output),
              guiLeft + 17 + 18 * col,
              guiTop + 18 + 18 * row);
      itemRender.zLevel = 0.0F;

      GlStateManager.enableAlpha();
      GlStateManager.disableLighting();
      this.mc.getTextureManager().bindTexture(texture);
      this.zLevel = 300.0F;

      if (selection == AmmoPressRecipes.recipes.indexOf(this.recipes.get(i))) {
        this.drawTexturedModalRect(
                guiLeft + 16 + 18 * col, guiTop + 17 + 18 * row, 194, 0, 18, 18);
      } else {
        this.drawTexturedModalRect(
                guiLeft + 16 + 18 * col, guiTop + 17 + 18 * row, 212, 0, 18, 18);
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(
              guiLeft + 17 + 18 * col + 8,
              guiTop + 18 + 18 * row + 8,
              0);
      GlStateManager.scale(0.5F, 0.5F, 1F);
      itemRender.renderItemOverlayIntoGUI(font, recipe.output, 0, 0, recipe.output.getCount() + "");
      GlStateManager.popMatrix();
    }

    if (selection >= 0 && selection < AmmoPressRecipes.recipes.size()) {
      AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(selection);

      for (int i = 0; i < 9; i++) {
        AStack stack = recipe.input[i];
        if (stack == null) continue;
        if (!press.inventory.getStackInSlot(i).isEmpty()) continue;
        List<ItemStack> inputs = stack.extractForJEI();
        ItemStack input =
                inputs.isEmpty()
                        ? new ItemStack(ModItems.nothing)
                        : inputs.get((int) (Math.abs(System.currentTimeMillis() / 1000) % inputs.size()));

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();

        FontRenderer font = input.getItem().getFontRenderer(input);
        if (font == null) font = fontRenderer;

        itemRender.zLevel = 10.0F;
        itemRender.renderItemAndEffectIntoGUI(
                input, guiLeft + 116 + 18 * (i % 3), guiTop + 18 + 18 * (i / 3));
        itemRender.renderItemOverlayIntoGUI(
                font,
                input,
                guiLeft + 116 + 18 * (i % 3),
                guiTop + 18 + 18 * (i / 3),
                input.getCount() > 1 ? (input.getCount() + "") : null);
        itemRender.zLevel = 0.0F;

        GlStateManager.enableAlpha();
        GlStateManager.disableLighting();

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        this.zLevel = 300.0F;
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1F, 1F, 1F, 0.5F);
        GlStateManager.enableBlend();
        drawTexturedModalRect(
                guiLeft + 116 + 18 * (i % 3),
                guiTop + 18 + 18 * (i / 3),
                116 + 18 * (i % 3),
                18 + 18 * (i / 3),
                18,
                18);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();
      }
    }

    RenderHelper.disableStandardItemLighting();
    GlStateManager.color(1F, 1F, 1F, 1F);
    this.search.drawTextBox();
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {

    if (this.search.textboxKeyTyped(c, key)) {
      search(this.search.getText());
    } else {
      super.keyTyped(c, key);
    }
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }
}
