package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.config.MachineConfig;
import com.hbm.inventory.recipes.AssemblerRecipes;
import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemAssemblyTemplate;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.machine.ItemStamp;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.ItemFolderPacket;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIScreenTemplateFolder extends GuiScreen {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/gui_planner.png");
	private static final ResourceLocation TEXTURE_JOURNAL = new ResourceLocation(Tags.MODID, "textures/gui/gui_planner_journal.png");
	private final boolean isJournal;
	private final List<ItemStack> allStacks = new ArrayList<>();
	private final List<ItemStack> stacks = new ArrayList<>();
	private final List<FolderButton> buttons = new ArrayList<>();
	protected int xSize = 176;
	protected int ySize = 229;
	protected int guiLeft;
	protected int guiTop;
	private int currentPage = 0;
	private GuiTextField search;

	public GUIScreenTemplateFolder(EntityPlayer player) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem.isEmpty()) {
			heldItem = player.getHeldItemOffhand();
		}

		if (heldItem.isEmpty()) {
			this.isJournal = false;
			return;
		}

		this.isJournal = heldItem.getItem() != ModItems.template_folder;

		if (!this.isJournal) {
			// Stamps
			for(ItemStack i : ItemStamp.stamps.get(ItemStamp.StampType.PLATE)) allStacks.add(i.copy());
			for(ItemStack i : ItemStamp.stamps.get(ItemStamp.StampType.WIRE)) allStacks.add(i.copy());
			for(ItemStack i : ItemStamp.stamps.get(ItemStamp.StampType.CIRCUIT)) allStacks.add(i.copy());
			// Tracks
			for (int i = 1; i < ItemCassette.TrackType.VALUES.size(); i++) {
				allStacks.add(new ItemStack(ModItems.siren_track, 1, i));
			}
		}

        if (MachineConfig.enableOldTemplates) {
            Item heldFolderItem = heldItem.getItem();
            AssemblerRecipes.recipes.forEach((compStack, recipe) -> {
                if (recipe.folders.contains(heldFolderItem)) {
                    allStacks.add(ItemAssemblyTemplate.writeType(new ItemStack(ModItems.assembly_template), compStack));
                }
            });
        }

        if (!this.isJournal) {
            if (MachineConfig.enableOldTemplates) ChemplantRecipes.recipes.forEach(recipe -> allStacks.add(new ItemStack(ModItems.chemistry_template, 1, recipe.getId())));
            // Crucible Templates
            CrucibleRecipes.recipes.forEach(recipe -> {
                allStacks.add(new ItemStack(ModItems.crucible_template, 1, recipe.getId()));
            });
        }

		search(null);
	}

	private void search(String sub) {
		stacks.clear();
		this.currentPage = 0;

		if (sub == null || sub.isEmpty()) {
			stacks.addAll(allStacks);
			updateButtons();
			return;
		}

		sub = sub.toLowerCase(Locale.US);

		outer:
		for (ItemStack stack : allStacks) {
			for (String line : stack.getTooltip(this.mc.player, ITooltipFlag.TooltipFlags.NORMAL)) {
				if (line.toLowerCase(Locale.US).contains(sub)) {
					stacks.add(stack);
					continue outer;
				}
			}
		}
		updateButtons();
	}

	private int getPageCount() {
		if (stacks.isEmpty()) return 0;
		return (stacks.size() - 1) / 35;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (currentPage < 0) currentPage = 0;
		if (currentPage > getPageCount()) currentPage = getPageCount();
		this.search.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.search.drawTextBox();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		updateButtons();

		Keyboard.enableRepeatEvents(true);
		this.search = new GuiTextField(0, this.fontRenderer, guiLeft + 61, guiTop + 213, 48, 12);
		this.search.setTextColor(0xffffff);
		this.search.setDisabledTextColour(0xffffff);
		this.search.setEnableBackgroundDrawing(false);
		this.search.setMaxStringLength(100);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private void updateButtons() {
		if (!buttons.isEmpty())
			buttons.clear();

		for (int i = currentPage * 35; i < Math.min(currentPage * 35 + 35, stacks.size()); i++) {
			buttons.add(new FolderButton(guiLeft + 25 + (27 * (i % 5)), guiTop + 26 + (27 * (int) Math.floor((i / 5D))) - currentPage * 27 * 7,
					stacks.get(i)));
		}

		if (currentPage != 0)
			buttons.add(new FolderButton(guiLeft + 25 - 18, guiTop + 26 + (27 * 3), 1, "Previous"));
		if (currentPage != getPageCount())
			buttons.add(new FolderButton(guiLeft + 25 + (27 * 4) + 18, guiTop + 26 + (27 * 3), 2, "Next"));
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			if (scroll > 0 && currentPage > 0) { // Scroll up
				currentPage--;
				updateButtons();
			} else if (scroll < 0 && currentPage < getPageCount()) { // Scroll down
				currentPage++;
				updateButtons();
			}
		}
	}

	protected void mouseClicked(int i, int j, int k) {
		if(i >= guiLeft + 45 && i < guiLeft + 117 && j >= guiTop + 211 && j < guiTop + 223) {
			this.search.setFocused(true);
		} else  {
			this.search.setFocused(false);
		}

		try {
			for (FolderButton b : buttons)
				if (b.isMouseOnButton(i, j))
					b.executeAction();
		} catch (Exception ex) {
			updateButtons();
		}
	}

	protected void drawGuiContainerForegroundLayer(int i, int j) {

		this.fontRenderer.drawString(I18n.format((currentPage + 1) + "/" + (getPageCount() + 1)),
				guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(I18n.format((currentPage + 1) + "/" + (getPageCount() + 1))) / 2, guiTop + 10, 4210752);

		for(FolderButton b : buttons)
			if(b.isMouseOnButton(i, j))
				b.drawString(i, j);
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(isJournal ? TEXTURE_JOURNAL : TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (search.isFocused()) {
			drawTexturedModalRect(guiLeft + 45, guiTop + 211, 176, 54, 72, 12);
		}

		for (FolderButton b : buttons) {
			b.drawButton(b.isMouseOnButton(mouseX, mouseY));
			b.drawIcon(b.isMouseOnButton(mouseX, mouseY));
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (this.search.textboxKeyTyped(typedChar, keyCode)) {
			this.search(this.search.getText());
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}


	class FolderButton {
		final int xPos;
		final int yPos;
		final int type; // 0: regular, 1: prev, 2: next
		final String info;
		final ItemStack stack;

		FolderButton(int x, int y, int t, String i) {
			this.xPos = x;
			this.yPos = y;
			this.type = t;
			this.info = i;
			this.stack = ItemStack.EMPTY;
		}

		FolderButton(int x, int y, ItemStack stack) {
			this.xPos = x;
			this.yPos = y;
			this.type = 0;
			this.info = stack.getDisplayName();
			this.stack = stack.copy();
		}

		boolean isMouseOnButton(int mouseX, int mouseY) {
			return mouseX >= this.xPos && mouseX < this.xPos + 18 && mouseY >= this.yPos && mouseY < this.yPos + 18;
		}

		void drawButton(boolean isHovering) {
			mc.getTextureManager().bindTexture(isJournal ? TEXTURE_JOURNAL : TEXTURE);
			int u = isHovering ? 176 + 18 : 176;
			int v = 0;
			if (type == 1) v = 18;
			if (type == 2) v = 36;
			drawTexturedModalRect(xPos, yPos, u, v, 18, 18);
		}

		void drawIcon(boolean isHovering) {
			if (stack.isEmpty()) return;

			ItemStack toRender = stack;
			// Special rendering logic for templates
			if (stack.getItem() == ModItems.assembly_template) {
				toRender = AssemblerRecipes.getOutputFromTempate(stack);
			} else if (stack.getItem() == ModItems.chemistry_template) {
				toRender = new ItemStack(ModItems.chemistry_icon, 1, stack.getMetadata());
			} else if (stack.getItem() == ModItems.crucible_template) {
				toRender = CrucibleRecipes.indexMapping.get(stack.getMetadata()).icon;
			}

			if (toRender != null && !toRender.isEmpty()) {
				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(toRender, xPos + 1, yPos + 1);
				RenderHelper.disableStandardItemLighting();
			}
		}

		void drawString(int x, int y) {
			if (stack.isEmpty()) {
				if (info != null && !info.isEmpty()) {
					drawHoveringText(info, x, y);
				}
			} else {
				renderToolTip(stack, x, y);
			}
		}

		void executeAction() {
			playClickSound();
			switch (type) {
				case 0 -> PacketDispatcher.wrapper.sendToServer(new ItemFolderPacket(stack.copy()));
				case 1 -> {
					if (currentPage > 0) {
						currentPage--;
						updateButtons();
					}
				}
				case 2 -> {
					if (currentPage < getPageCount()) {
						currentPage++;
						updateButtons();
					}
				}
			}
		}
	}
}
