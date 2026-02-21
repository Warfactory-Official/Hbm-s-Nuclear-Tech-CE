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
import java.util.List;

public class SubElementLinker extends SubElement {

	public static ResourceLocation inv_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_linker_add_element.png");
	
	public GuiButton clear;
	public GuiButton accept;
	public GuiButton pageLeft;
	public GuiButton pageRight;
	public GuiButton cont;
	public GuiButton back;

	public List<IControllable> linked = new ArrayList<>();
	public List<GuiButton> linkedButtons = new ArrayList<>();
	public int numPages = 1;
	public int currentPage = 1;
	
	public SubElementLinker(GuiControlEdit gui){
		super(gui);
	}
	
	@Override
	protected void initGui() {
		int cX = gui.width/2;
		int cY = gui.height/2;
		clear = gui.addButton(new GuiButton(gui.currentButtonId(), cX-121, cY-93, 40, 20, "Clear"));
		accept = gui.addButton(new GuiButton(gui.currentButtonId(), cX-101, cY-116, 20, 20, ">"));
		pageLeft = gui.addButton(new GuiButton(gui.currentButtonId(), cX-60, cY-16, 20, 20, "<"));
		pageRight = gui.addButton(new GuiButton(gui.currentButtonId(), cX+90, cY-16, 20, 20, ">"));
		cont = gui.addButton(new GuiButton(gui.currentButtonId(), cX-60, cY+6, 170, 20, "Continue"));

		super.initGui();
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
			if(i >= linkedButtons.size() || i < 1) //TODO: when block gone, remove from linked
				break;
			linkedButtons.get(i).visible = true;
			linkedButtons.get(i).enabled = true;
		}
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
							if (te instanceof IControllable && !linked.contains(te)) {
								linked.add((IControllable) te);
							}
						}
						refreshButtons();
					}
				}
			}
		} else if(button == clear){
			linked.clear();
			gui.currentEditControl.connectedSet.clear();
			refreshButtons();
		} else if(button == cont){
			gui.eventEditor.accumulateEventTypes(linked);
			gui.eventEditor.populateDefaultNodes();
			for(IControllable c : gui.linker.linked) {
				if (!gui.currentEditControl.connectedSet.contains(c.getControlPos()))
					gui.currentEditControl.connectedSet.add(c.getControlPos());
			}
			gui.pushElement(gui.eventEditor);
		} else if(button == pageLeft){
			currentPage = Math.max(1, currentPage - 1);
			recalculateVisibleButtons();
		} else if(button == pageRight){
			currentPage = Math.min(numPages, currentPage + 1);
			recalculateVisibleButtons();
		} else if(linkedButtons.contains(button)){
			int idx = linkedButtons.indexOf(button);
			gui.currentEditControl.connectedSet.remove(linked.get(idx).getControlPos());
			linked.remove(idx);
			refreshButtons();
		}
	}
	
	protected void refreshButtons(){
		gui.getButtons().removeAll(linkedButtons);
		linkedButtons.clear();
		int i = 0;
		int cX = gui.width/2;
		int cY = gui.height/2;

		for(IControllable c : linked){
			BlockPos pos = c.getControlPos();
			linkedButtons.add(new ButtonHoverText(gui.currentButtonId(), cX-73, cY-90 + i*22, 170, 20, "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")", "<Click to remove>"));
			i = (i+1)%3;
		}
		for(GuiButton b : linkedButtons)
			gui.addButton(b);
		numPages = (linked.size()+2)/3;
		currentPage = MathHelper.clamp(currentPage, 1, numPages);
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
		pageLeft.enabled = enable;
		pageLeft.visible = enable;
		pageRight.enabled = enable;
		pageRight.visible = enable;
		cont.enabled = enable;
		cont.visible = enable;
		SlotItemHandlerDisableable s = (SlotItemHandlerDisableable)gui.container.inventorySlots.get(0);
		s.isEnabled = enable;
		for(SlotDisableable slot : gui.container.invSlots){
			slot.isEnabled = enable;
		}
	}
	
}
