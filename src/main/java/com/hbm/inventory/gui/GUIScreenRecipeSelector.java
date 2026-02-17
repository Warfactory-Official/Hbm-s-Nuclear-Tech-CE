package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.packet.toserver.NBTControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIScreenRecipeSelector extends GuiScreen {

    protected static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_recipe_selector.png");

    //basic GUI setup
    protected int xSize = 176;
    protected int ySize = 132;
    protected int guiLeft;
    protected int guiTop;
    // search crap
    protected GenericRecipes recipeSet;
    protected List<GenericRecipe> recipes = new ArrayList<>();
    protected GuiTextField search;
    protected int pageIndex;
    protected int size;
    protected String selection;
    public static final String NULL_SELECTION = "null";
    // callback
    protected int index;
    protected IControlReceiver tile;
    protected GuiScreen previousScreen;
    protected String installedPool;

    public static void openSelector(GenericRecipes recipeSet, IControlReceiver tile, String selection, int index, String installedPool, GuiScreen previousScreen) {
        FMLCommonHandler.instance().showGuiScreen(new GUIScreenRecipeSelector(recipeSet, tile, selection, index, installedPool, previousScreen));
    }

    public GUIScreenRecipeSelector(GenericRecipes recipeSet, IControlReceiver tile, String selection, int index, String installedPool, GuiScreen previousScreen) {
        this.recipeSet = recipeSet;
        this.tile = tile;
        this.selection = selection;
        this.index = index;
        this.installedPool = installedPool;
        this.previousScreen = previousScreen;
        if(this.selection == null) this.selection = NULL_SELECTION;

        regenerateRecipes();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        Keyboard.enableRepeatEvents(true);
        this.search = new GuiTextField(0, this.fontRenderer, guiLeft + 28, guiTop + 111, 102, 12);
        this.search.setTextColor(-1);
        this.search.setDisabledTextColour(-1);
        this.search.setEnableBackgroundDrawing(false);
        this.search.setMaxStringLength(32);
    }

    private void regenerateRecipes() {

        this.recipes.clear();

        for(Object o : recipeSet.recipeOrderedList) {
            GenericRecipe recipe = (GenericRecipe) o;
            if(!recipe.isPooled() || (this.installedPool != null && recipe.isPartOfPool(installedPool))) this.recipes.add(recipe);
        }

        resetPaging();
    }

    private void search(String search) {
        this.recipes.clear();

        if(search.isEmpty()) {
            regenerateRecipes();
        } else {
            for(Object o : recipeSet.recipeOrderedList) {
                GenericRecipe recipe = (GenericRecipe) o;
                if(recipe.matchesSearch(search)) {
                    if(!recipe.isPooled() || (this.installedPool != null && recipe.isPartOfPool(installedPool))) this.recipes.add(recipe);
                }
            }

            resetPaging();
        }
    }

    private void resetPaging() {
        this.pageIndex = 0;
        this.size = Math.max(0, (int)Math.ceil((this.recipes.size() - 40) / 8D));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, f);
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.handleScroll();

        if(guiLeft + 7 <= mouseX && guiLeft + 7 + 144 > mouseX && guiTop + 17 < mouseY && guiTop + 17 + 90 >= mouseY) {
            for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
                if(i >= this.recipes.size()) break;

                int ind = i - pageIndex * 8;
                int ix = 7 + 18 * (ind % 8);
                int iy = 17 + 18 * (ind / 8);

                if(guiLeft + ix <= mouseX && guiLeft + ix + 18 > mouseX && guiTop + iy < mouseY && guiTop + iy + 18 >= mouseY) {
                    GenericRecipe recipe = recipes.get(i);
                    this.drawHoveringText(recipe.print(), mouseX, mouseY); // Th3_Sl1ze: I don't think pos should be fixed rly
                }
            }
        }

        if(guiLeft + 151 <= mouseX && guiLeft + 151 + 18 > mouseX && guiTop + 71 < mouseY && guiTop + 71 + 18 >= mouseY) {
            if(this.selection != null && this.recipeSet.recipeNameMap.containsKey(selection)) {
                GenericRecipe recipe = (GenericRecipe) this.recipeSet.recipeNameMap.get(selection);
                this.drawHoveringText(recipe.print(), mouseX, mouseY);
            }
        }

        if(guiLeft + 152 <= mouseX && guiLeft + 152 + 16 > mouseX && guiTop + 90 < mouseY && guiTop + 90 + 16 >= mouseY) {
            this.drawHoveringText(TextFormatting.YELLOW + "Close", mouseX, mouseY);
        }

        if(guiLeft + 134 <= mouseX && guiLeft + 134 + 16 > mouseX && guiTop + 108 < mouseY && guiTop + 108 + 16 >= mouseY) {
            this.drawHoveringText(TextFormatting.YELLOW + "Clear search", mouseX, mouseY);
        }

        if(guiLeft + 8 <= mouseX && guiLeft + 8 + 16 > mouseX && guiTop + 108 < mouseY && guiTop + 108 + 16 >= mouseY) {
            this.drawHoveringText(TextFormatting.ITALIC + "Press ENTER to toggle focus", mouseX, mouseY);
        }
    }

    protected void handleScroll() {

        if(!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && Mouse.next()) {
            int scroll = Mouse.getEventDWheel();
            if(scroll > 0 && this.pageIndex > 0) this.pageIndex--;
            if(scroll < 0 && this.pageIndex < this.size) this.pageIndex++;
        }
    }

    private void drawGuiContainerForegroundLayer(int x, int y) {
        this.search.drawTextBox();
    }

    private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(this.search.isFocused()) {
            drawTexturedModalRect(guiLeft + 26, guiTop + 108, 0, 132, 106, 16);
        }

        if(guiLeft + 152 <= mouseX && guiLeft + 152 + 16 > mouseX && guiTop + 18 < mouseY && guiTop + 18 + 16 >= mouseY) {
            drawTexturedModalRect(guiLeft + 152, guiTop + 18, 176, 0, 16, 16);
        }

        if(guiLeft + 152 <= mouseX && guiLeft + 152 + 16 > mouseX && guiTop + 36 < mouseY && guiTop + 36 + 16 >= mouseY) {
            drawTexturedModalRect(guiLeft + 152, guiTop + 36, 176, 16, 16, 16);
        }

        if(guiLeft + 152 <= mouseX && guiLeft + 152 + 16 > mouseX && guiTop + 90 < mouseY && guiTop + 90 + 16 >= mouseY) {
            drawTexturedModalRect(guiLeft + 152, guiTop + 90, 176, 32, 16, 16);
        }

        if(guiLeft + 134 <= mouseX && guiLeft + 134 + 16 > mouseX && guiTop + 108 < mouseY && guiTop + 108 + 16 >= mouseY) {
            drawTexturedModalRect(guiLeft + 134, guiTop + 108, 176, 48, 16, 16);
        }

        if(guiLeft + 8 <= mouseX && guiLeft + 8 + 16 > mouseX && guiTop + 108 < mouseY && guiTop + 108 + 16 >= mouseY) {
            drawTexturedModalRect(guiLeft + 8, guiTop + 108, 176, 64, 16, 16);
        }

        for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
            if(i >= recipes.size()) break;
            int ind = i - pageIndex * 8;
            GenericRecipe recipe = recipes.get(i);
            if(recipe.getInternalName().equals(this.selection)) this.drawTexturedModalRect(guiLeft + 7 + 18 * (ind % 8), guiTop + 17 + 18 * (ind / 8), 192, 0, 18, 18);
        }

        for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
            if(i >= recipes.size()) break;

            int ind = i - pageIndex * 8;
            GenericRecipe recipe = recipes.get(i);

            this.renderItem(recipe.getIcon(), 8 + 18 * (ind % 8), 18 + 18 * (ind / 8));
            this.mc.getTextureManager().bindTexture(texture);
        }

        if(this.selection != null && this.recipeSet.recipeNameMap.containsKey(selection)) {
            GenericRecipe recipe = (GenericRecipe) this.recipeSet.recipeNameMap.get(selection);
            this.renderItem(recipe.getIcon(), 152, 72);
        }
    }

    public void renderItem(ItemStack stack, int x, int y) {

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        itemRender.zLevel = 100.0F;
        itemRender.renderItemAndEffectIntoGUI(stack, guiLeft + x, guiTop + y);
        itemRender.zLevel = 0.0F;

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    protected void mouseClicked(int x, int y, int k) throws IOException {
        super.mouseClicked(x, y, k);

        this.search.mouseClicked(x, y, k);

        if(guiLeft + 152 <= x && guiLeft + 152 + 16 > x && guiTop + 18 < y && guiTop + 18 + 16 >= y) {
            playClickSound();
            if(this.pageIndex > 0) this.pageIndex--;
            return;
        }

        if(guiLeft + 152 <= x && guiLeft + 152 + 16 > x && guiTop + 36 < y && guiTop + 36 + 16 >= y) {
            playClickSound();
            if(this.pageIndex < this.size) this.pageIndex++;
            return;
        }

        if(guiLeft + 134 <= x && guiLeft + 134 + 16 > x && guiTop + 108 < y && guiTop + 108 + 16 >= y) {
            this.search.setText("");
            this.search("");
            this.search.setFocused(true);
            return;
        }

        for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
            if(i >= this.recipes.size()) break;

            int ind = i - pageIndex * 8;
            int ix = 7 + 18 * (ind % 8);
            int iy = 17 + 18 * (ind / 8);

            if(guiLeft + ix <= x && guiLeft + ix + 18 > x && guiTop + iy < y && guiTop + iy + 18 >= y) {

                String newSelection = ((GenericRecipe) recipes.get(i)).getInternalName();

                if(!newSelection.equals(selection))
                    this.selection = newSelection;
                else
                    this.selection = NULL_SELECTION;

                playClickSound();
                return;
            }
        }

        if(guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 71 < y && guiTop + 71 + 18 >= y) {
            if(!NULL_SELECTION.equals(this.selection)) {
                this.selection = this.NULL_SELECTION;
                playClickSound();
                return;
            }
        }

        if(guiLeft + 152 <= x && guiLeft + 152 + 16 > x && guiTop + 90 < y && guiTop + 90 + 16 >= y) {
            FMLCommonHandler.instance().showGuiScreen(previousScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("index", this.index);
        data.setString("selection", this.selection);
        TileEntity te = (TileEntity) tile;
        PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {

        if(keyCode == Keyboard.KEY_RETURN) {
            this.search.setFocused(!this.search.isFocused());
            return;
        }

        if(this.search.textboxKeyTyped(typedChar, keyCode)) {
            search(this.search.getText());
            return;
        }

        if(keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            FMLCommonHandler.instance().showGuiScreen(previousScreen);
        }
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
