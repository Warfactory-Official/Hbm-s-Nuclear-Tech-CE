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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SubElementLinker extends SubElement {

	public static ResourceLocation inv_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_linker_add_element.png");
	
	public GuiButton clear;
	public GuiButton accept;
	public GuiButton pageLeft;
	public GuiButton pageRight;
	public GuiButton cont;
	public GuiButton back;

	public List<IControllable> linked = new ArrayList<>();
	private final Set<BlockPos> unresolvedLinks = new LinkedHashSet<>();
	public List<GuiButton> linkedButtons = new ArrayList<>();
	private final List<BlockPos> listedLinkPositions = new ArrayList<>();
	public int numPages = 1;
	public int currentPage = 1;
	
	public SubElementLinker(GuiControlEdit gui){
		super(gui);
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
		for(GuiButton b : linkedButtons){
			b.visible = false;
			b.enabled = false;
		}
		int idx = (currentPage-1)*3;
		for(int i = idx; i < idx+3; i ++) {
			if(i >= linkedButtons.size()) //TODO: when block gone, remove from linked
				break;
			linkedButtons.get(i).visible = true;
			linkedButtons.get(i).enabled = true;
		}
		boolean showPaging = numPages > 1;
		pageLeft.visible = showPaging && currentPage > 1;
		pageLeft.enabled = pageLeft.visible;
		pageRight.visible = showPaging && currentPage < numPages;
		pageRight.enabled = pageRight.visible;
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
							if (te instanceof IControllable) {
								addLinked((IControllable) te);
							}
						}
						refreshButtons();
						gui.returnControlInputToPlayerInventory();
					}
				}
			}
		} else if(button == clear){
			linked.clear();
			unresolvedLinks.clear();
			refreshButtons();
		} else if(button == back){
			gui.returnControlInputToPlayerInventory();
			gui.popElement();
		} else if(button == cont){
			syncCurrentEditControlConnections();
			gui.eventEditor.accumulateEventTypes(linked);
			gui.eventEditor.populateDefaultNodes();
			gui.returnControlInputToPlayerInventory();
			gui.pushElement(gui.eventEditor);
		} else if(button == pageLeft){
			currentPage = Math.max(1, currentPage - 1);
			recalculateVisibleButtons();
		} else if(button == pageRight){
			currentPage = Math.min(numPages, currentPage + 1);
			recalculateVisibleButtons();
		} else if(linkedButtons.contains(button)){
			int idx = linkedButtons.indexOf(button);
			if(idx >= 0 && idx < listedLinkPositions.size()) {
				removeLinked(listedLinkPositions.get(idx));
			}
			refreshButtons();
		}
	}

	void reloadLinkedFromCurrentEditControl() {
		linked.clear();
		unresolvedLinks.clear();
		if(gui.currentEditControl == null) {
			refreshButtons();
			return;
		}

		World world = gui.control.getWorld();
		for(BlockPos pos : gui.currentEditControl.connectedSet) {
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IControllable) {
				addLinked((IControllable) te);
			} else {
				unresolvedLinks.add(pos);
			}
		}
		refreshButtons();
	}

	void syncCurrentEditControlConnections() {
		if(gui.currentEditControl == null) {
			return;
		}

		LinkedHashSet<BlockPos> resolvedConnections = new LinkedHashSet<>();
		for(BlockPos pos : gui.currentEditControl.connectedSet) {
			if(unresolvedLinks.contains(pos) || containsLinked(pos)) {
				resolvedConnections.add(pos);
			}
		}
		for(IControllable controllable : linked) {
			resolvedConnections.add(controllable.getControlPos());
		}
		gui.currentEditControl.connectedSet.clear();
		gui.currentEditControl.connectedSet.addAll(resolvedConnections);
	}

	private void addLinked(IControllable controllable) {
		BlockPos pos = controllable.getControlPos();
		unresolvedLinks.remove(pos);
		if(!containsLinked(pos)) {
			linked.add(controllable);
		}
	}

	private boolean containsLinked(BlockPos pos) {
		for(IControllable controllable : linked) {
			if(controllable.getControlPos().equals(pos)) {
				return true;
			}
		}
		return false;
	}

	private void removeLinked(BlockPos pos) {
		linked.removeIf(controllable -> controllable.getControlPos().equals(pos));
		unresolvedLinks.remove(pos);
	}
	
	protected void refreshButtons(){
		gui.getButtons().removeAll(linkedButtons);
		linkedButtons.clear();
		listedLinkPositions.clear();
		int i = 0;
		int cX = gui.width/2;
		int cY = gui.height/2;

		for(IControllable c : linked){
			BlockPos pos = c.getControlPos();
			listedLinkPositions.add(pos);
			linkedButtons.add(new ButtonHoverText(gui.currentButtonId(), cX-73, cY-90 + i*22, 170, 20, formatLinkLabel(pos, false), "<Click to remove>"));
			i = (i+1)%3;
		}
		for(BlockPos pos : unresolvedLinks) {
			listedLinkPositions.add(pos);
			linkedButtons.add(new ButtonHoverText(gui.currentButtonId(), cX-73, cY-90 + i*22, 170, 20, formatLinkLabel(pos, true), "<Click to remove>"));
			i = (i+1)%3;
		}
		for(GuiButton b : linkedButtons)
			gui.addButton(b);
		numPages = Math.max(1, (listedLinkPositions.size()+2)/3);
		currentPage = MathHelper.clamp(currentPage, 1, numPages);
		recalculateVisibleButtons();
	}

	private static String formatLinkLabel(BlockPos pos, boolean unresolved) {
		String label = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
		return unresolved ? label + " [Unresolved]" : label;
	}
	
	@Override
	protected void enableButtons(boolean enable) {
		if(enable){
			recalculateVisibleButtons();
		} else {
			for(GuiButton b : linkedButtons){
				b.visible = false;
				b.enabled = false;
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
		cont.enabled = enable;
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
