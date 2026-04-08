package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.control_panel.ContainerControlEdit.SlotDisableable;
import com.hbm.inventory.control_panel.ContainerControlEdit.SlotItemHandlerDisableable;
import com.hbm.items.tool.ItemMultiDetonator;
import com.hbm.tileentity.machine.TileEntityDummy;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.Map.Entry;

public class SubElementLinker extends SubElement {

	public static ResourceLocation inv_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_linker_add_element.png");
	
	public GuiButton clear;
	public GuiButton accept;
	public GuiButton pageLeft;
	public GuiButton pageRight;
	public GuiButton cont;
	public GuiButton back;

	public List<GuiLinkerButton> linkedButtons = new ArrayList<>();
	public Map<GuiLinkerButton,BlockPos> linkerToPosMap = new HashMap<>();
	public final Set<BlockPos> linkedPositions = new LinkedHashSet<>();
	public final Map<BlockPos,String> tags = new LinkedHashMap<>();
	public int numPages = 1;
	public int currentPage = 1;
	public boolean invalid = false;
	
	public SubElementLinker(GuiControlEdit gui){
		super(gui);
		Keyboard.enableRepeatEvents(true);
	}
	
	@Override
	protected void initGui() {
		int cX = gui.width/2;
		int cY = gui.height/2;
		back = gui.addButton(new GuiButton(gui.currentButtonId(), gui.getGuiLeft()+219, gui.getGuiTop()+13, 30, 20, "Back"));
		clear = gui.addButton(new GuiButton(gui.currentButtonId(), cX-121, cY-93, 40, 20, "Clear"));
		accept = gui.addButton(new GuiButton(gui.currentButtonId(), cX-101, cY-116, 20, 20, ">"));
		pageLeft = gui.addButton(new GuiButton(gui.currentButtonId(), cX-60, cY-16, 20, 20, "<"));
		pageRight = gui.addButton(new GuiButton(gui.currentButtonId(), cX+90, cY-16, 20, 20, ">"));
		cont = gui.addButton(new GuiButton(gui.currentButtonId(), cX-60, cY+6, 170, 20, "Continue"));

		super.initGui();
		refreshButtons();
	}
	
	@Override
	protected void drawScreen() {
		int cX = gui.width / 2;
		int cY = gui.height / 2;

		ItemStack stack = gui.container.inventorySlots.get(0).getStack();
		accept.enabled = !stack.isEmpty() && stack.getItem() instanceof ItemMultiDetonator;

		String text = currentPage + "/" + numPages;
		gui.getFontRenderer().drawString(text, cX + 16, cY - 10, 0xFF777777, false);
		text = "Create Links";
		gui.getFontRenderer().drawString(text, cX - gui.getFontRenderer().getStringWidth(text) / 2F + 10, cY - 110, 0xFF777777, false);
	}
	
	@Override
	protected void renderBackground() {
		gui.mc.getTextureManager().bindTexture(inv_tex);
		gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
	}
	
	private void recalculateVisibleButtons(){
		for(GuiLinkerButton b : linkedButtons){
			b.setVisible(false);
			b.setEnabled(false);
		}
		int idx = (currentPage-1)*3;
		for(int i = idx; i < idx+3; i ++) {
			if(i >= linkedButtons.size()) //TODO: when block gone, remove from linked
				break;
			linkedButtons.get(i).setVisible(true);
			linkedButtons.get(i).setEnabled(true);
		}
		boolean showPaging = numPages > 1;
		pageLeft.visible = showPaging && currentPage > 1;
		pageLeft.enabled = pageLeft.visible;
		pageRight.visible = showPaging && currentPage < numPages;
		pageRight.enabled = pageRight.visible;
	}

	@Override
	public void onClose() {
		super.onClose();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button){
		World world = gui.control.getWorld();

		if(button == accept){
			ItemStack stack = gui.container.inventorySlots.get(0).getStack();
			if(!stack.isEmpty()){
				if(stack.getItem() instanceof ItemMultiDetonator){
					int[][] locs = ItemMultiDetonator.getLocations(stack);
					if (locs != null) {
						for (int i = 0; i < locs[0].length; i++) {
							BlockPos pos = new BlockPos(locs[0][i], locs[1][i], locs[2][i]);
							Block b = world.getBlockState(pos).getBlock();
							if (b instanceof BlockDummyable) {
								int[] core = ((BlockDummyable) b).findCore(world, pos.getX(), pos.getY(), pos.getZ());
								if (core != null) {
									pos = new BlockPos(core[0], core[1], core[2]);
								}
							}
							TileEntity te = world.getTileEntity(pos);
							if (te instanceof TileEntityDummy) {
								BlockPos bpos = ((TileEntityDummy) te).target;
								if (bpos != null)
									te = world.getTileEntity(((TileEntityDummy) te).target);
							}
							if (te instanceof IControllable controllable) {
								BlockPos p = controllable.getControlPos();
								linkedPositions.add(p);
								tags.putIfAbsent(p, formatLinkLabel(p));
							}
						}
						refreshButtons();
						gui.returnControlInputToPlayerInventory();
					}
				}
			}
		} else if(button == clear){
			linkedPositions.clear();
			tags.clear();
			refreshButtons();
		} else if(button == back){
			gui.returnControlInputToPlayerInventory();
			gui.popElement();
		} else if(button == cont){
			if (!invalid) {
				syncCurrentEditControlConnections();
				gui.eventEditor.accumulateEventTypes(getLinked());
				gui.eventEditor.populateDefaultNodes();
				gui.returnControlInputToPlayerInventory();
				gui.pushElement(gui.eventEditor);
			}
		} else if(button == pageLeft){
			currentPage = Math.max(1, currentPage - 1);
			recalculateVisibleButtons();
		} else if(button == pageRight){
			currentPage = Math.min(numPages, currentPage + 1);
			recalculateVisibleButtons();
		} else if(linkerToPosMap.containsKey(button)){
			BlockPos p = linkerToPosMap.get(button);
			linkedPositions.remove(p);
			tags.remove(p);
			refreshButtons();
		}
	}

	List<IControllable> getLinked() {
		World world = gui.control.getWorld();
		List<IControllable> list = new ArrayList<>();
		for (BlockPos p : linkedPositions) {
			if (world.getTileEntity(p) instanceof IControllable ctrl)
				list.add(ctrl);
		}
		return list;
	}

	void reloadLinkedFromCurrentEditControl() {
		linkedPositions.clear();
		tags.clear();
		if(gui.currentEditControl == null) {
			refreshButtons();
			return;
		}

		for (Entry<String,BlockPos> entry : gui.currentEditControl.taggedLinks.entrySet()) {
			linkedPositions.add(entry.getValue());
			tags.put(entry.getValue(),entry.getKey());
		}
		refreshButtons();
	}

	void syncCurrentEditControlConnections() {
		if(gui.currentEditControl == null) {
			return;
		}
		gui.currentEditControl.taggedLinks.clear();
		for (Entry<BlockPos,String> entry : tags.entrySet())
			gui.currentEditControl.taggedLinks.put(entry.getValue(),entry.getKey());
	}
	
	protected void refreshButtons(){
		gui.getButtons().removeAll(linkedButtons);
		linkedButtons.clear();
		linkerToPosMap.clear();
		int i = 0;
		int cX = gui.width/2;
		int cY = gui.height/2;
		for (BlockPos pos : linkedPositions) {
			GuiLinkerButton button = new GuiLinkerButton(gui.mc.fontRenderer,gui.currentButtonId(), cX-73, cY-90 + i*22, 170, 20, tags.getOrDefault(pos,"ERROR"));
			linkedButtons.add(button);
			linkerToPosMap.put(button,pos);
			i = (i+1)%3;
		}
		for(GuiButton b : linkedButtons)
			gui.addButton(b);
		numPages = Math.max(1, (linkedPositions.size()+2)/3);
		currentPage = MathHelper.clamp(currentPage, 1, numPages);
		recalculateVisibleButtons();
	}

	private static String formatLinkLabel(BlockPos pos) {
		return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
	}

	@Override
	protected void keyTyped(char typedChar,int code) {
		super.keyTyped(typedChar,code);
		for(GuiLinkerButton b : linkedButtons)
			b.keyTyped(typedChar,code);
	}

	@Override
	protected void mouseClicked(int mouseX,int mouseY,int button) {
		super.mouseClicked(mouseX,mouseY,button);
		for(GuiLinkerButton b : linkedButtons)
			b.mouseClicked(mouseX,mouseY,button);
	}

	@Override
	protected void update() {
		super.update();
		for(GuiLinkerButton b : linkedButtons)
			b.update();
		invalid = false;
		tags.clear();
		Set<String> seenTags = new HashSet<>();
		Set<String> invalidTags = new HashSet<>();
		for(GuiLinkerButton b : linkedButtons) {
			BlockPos p = linkerToPosMap.get(b);
			String tag = b.field.getText();
			b.field.setTextColor(0xFFFFFF);
			if (!seenTags.add(tag)) {
				invalid = true;
				invalidTags.add(tag);
			} else
				tags.put(p,tag);
		}
		for(GuiLinkerButton b : linkedButtons) {
			String tag = b.field.getText();
			if (invalidTags.contains(tag))
				b.field.setTextColor(0xFF0000);
		}
		cont.enabled = lastEnable && !invalid;
	}

	boolean lastEnable = true;

	@Override
	protected void enableButtons(boolean enable) {
		lastEnable = enable;
		if(enable){
			recalculateVisibleButtons();
		} else {
			for(GuiLinkerButton b : linkedButtons){
				b.setVisible(false);
				b.setEnabled(false);
			}
		}
		clear.enabled = enable;
		clear.visible = enable;
		accept.enabled = enable;
		accept.visible = enable;
		if(enable){
			pageLeft.visible = numPages > 1 && currentPage > 1;
			pageLeft.enabled = pageLeft.visible;
			pageRight.visible = numPages > 1 && currentPage < numPages;
			pageRight.enabled = pageRight.visible;
		} else {
			pageLeft.visible = false;
			pageLeft.enabled = false;
			pageRight.visible = false;
			pageRight.enabled = false;
		}
		cont.enabled = enable && !invalid;
		cont.visible = enable;
		back.enabled = enable;
		back.visible = enable;
		SlotItemHandlerDisableable s = (SlotItemHandlerDisableable)gui.container.inventorySlots.get(0);
		s.isEnabled = enable;
		for(SlotDisableable slot : gui.container.invSlots){
			slot.isEnabled = enable;
		}
	}
	
}
