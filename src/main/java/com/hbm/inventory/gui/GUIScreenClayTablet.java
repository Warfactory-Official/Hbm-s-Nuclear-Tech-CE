package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GUIScreenClayTablet extends GuiScreen {
    protected int xSize = 142;
    protected int ySize = 84;
    protected int guiLeft;
    protected int guiTop;
    protected int tabletMeta = 0;

    protected static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/gui/guide_pedestal.png");

    public GUIScreenClayTablet() { }

    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer();
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        EntityPlayer player = Minecraft.getMinecraft().player;
        if(!player.getHeldItemMainhand().isEmpty()) tabletMeta = player.getHeldItemMainhand().getItemDamage();
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        int tabletOffset = tabletMeta == 1 ? 84 : 0;
        int iconOffset = tabletMeta == 1 ? 16 : 0;
        float revealChance = tabletMeta == 1 ? 0.25F : 0.5F;
        drawTexturedModalRect(guiLeft, guiTop, 0,  tabletOffset, xSize, ySize);

        ArrayList<PedestalRecipes.PedestalRecipe> recipeSet = PedestalRecipes.recipeSets[Math.abs(tabletMeta) % PedestalRecipes.recipeSets.length];

        if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().hasTagCompound() && player.getHeldItemMainhand().getTagCompound().hasKey("tabletSeed") && !recipeSet.isEmpty()) {
            Random rand = new Random(player.getHeldItemMainhand().getTagCompound().getLong("tabletSeed"));
            PedestalRecipes.PedestalRecipe recipe = recipeSet.get(rand.nextInt(recipeSet.size()));

            if(recipe.extra == PedestalRecipes.PedestalExtraCondition.FULL_MOON) drawTexturedModalRect(guiLeft + 120, guiTop + 62, 142 + iconOffset, 32, 16, 16);
            if(recipe.extra == PedestalRecipes.PedestalExtraCondition.NEW_MOON) drawTexturedModalRect(guiLeft + 120, guiTop + 62, 142 + iconOffset, 48, 16, 16);
            if(recipe.extra == PedestalRecipes.PedestalExtraCondition.SUN) drawTexturedModalRect(guiLeft + 120, guiTop + 62, 142 + iconOffset, 64, 16, 16);
            if(recipe.extra == PedestalRecipes.PedestalExtraCondition.GOOD_KARMA) drawTexturedModalRect(guiLeft + 120, guiTop + 62, 142 + iconOffset, 80, 16, 16);
            if(recipe.extra == PedestalRecipes.PedestalExtraCondition.BAD_KARMA) drawTexturedModalRect(guiLeft + 120, guiTop + 62, 142 + iconOffset, 96, 16, 16);

            for(int l = 0; l < 3; l++) {
                for(int r = 0; r < 3; r++) {
                    if(rand.nextFloat() > revealChance) {
                        drawTexturedModalRect(guiLeft + 7 + r * 27, guiTop + 7 + l * 27, 142 + iconOffset, 16, 16, 16);
                    } else {

                        RecipesCommon.AStack ingredient = recipe.input[r + l * 3];

                        if(ingredient == null) {
                            drawTexturedModalRect(guiLeft + 7 + r * 27, guiTop + 7 + l * 27, 142 + iconOffset, 0, 16, 16);
                            continue;
                        }

                        List<ItemStack> inputs = ingredient.extractForJEI();
                        ItemStack input = inputs.isEmpty() ? new ItemStack(ModItems.nothing) : inputs.get((int) (Math.abs(System.currentTimeMillis() / 1000) % inputs.size()));

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.enableRescaleNormal();

                        FontRenderer font = null;
                        if(input != null) font = input.getItem().getFontRenderer(recipe.output);
                        if(font == null) font = fontRenderer;

                        itemRender.zLevel = 300.0F;
                        itemRender.renderItemAndEffectIntoGUI(null, input, guiLeft + 7 + r * 27, guiTop + 7 + l * 27);
                        itemRender.renderItemOverlayIntoGUI(font, input, guiLeft + 7 + r * 27, guiTop + 7 + l * 27, input.getCount() > 1 ? (input.getCount() + "") : null);
                        itemRender.zLevel = 0.0F;

                        GlStateManager.disableLighting();
                        this.mc.getTextureManager().bindTexture(texture);
                        this.zLevel = 300.0F;
                    }
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableRescaleNormal();

            FontRenderer font = null;
            if(recipe.output != null) font = recipe.output.getItem().getFontRenderer(recipe.output);
            if(font == null) font = fontRenderer;

            itemRender.zLevel = 300.0F;
            itemRender.renderItemAndEffectIntoGUI(null, recipe.output, guiLeft + xSize / 2 - 8, guiTop - 20);
            itemRender.renderItemOverlayIntoGUI(font, recipe.output, guiLeft + xSize / 2 - 8, guiTop - 20, recipe.output.getCount() > 1 ? (recipe.output.getCount() + "") : null);
            itemRender.zLevel = 0.0F;

            GlStateManager.disableLighting();

            this.mc.getTextureManager().bindTexture(texture);
            this.zLevel = 300.0F;

            GlStateManager.disableDepth();
            String label = recipe.output.getDisplayName();
            font.drawString(label, guiLeft + (xSize - font.getStringWidth(label)) / 2, guiTop - 30, 0xffffff);

        } else {

            for(int l = 0; l < 3; l++) {
                for(int r = 0; r < 3; r++) {
                    drawTexturedModalRect(guiLeft + 7 + r * 27, guiTop + 7 + l * 27, 142 + iconOffset, 16, 16, 16);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        if(key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
