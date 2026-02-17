package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public abstract class GUITurretBase extends GuiInfoContainer {

	protected static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/gui_turret_base.png");
	protected TileEntityTurretBaseNT turret;
	protected GuiTextField field;
	int index;
	
	public GUITurretBase(InventoryPlayer invPlayer, TileEntityTurretBaseNT tedf) {
		super(new ContainerTurretBase(invPlayer, tedf));
		turret = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	public void initGui() {

		super.initGui();

		Keyboard.enableRepeatEvents(true);
		if (mc.player != null) {
			TileEntityTurretBaseNT.openInventory(mc.player);
		}
		this.field = new GuiTextField(0, this.fontRenderer, guiLeft + 10, guiTop + 65, 50, 14);
		this.field.setTextColor(-1);
		this.field.setDisabledTextColour(-1);
		this.field.setEnableBackgroundDrawing(false);
		this.field.setMaxStringLength(25);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if (mc.player != null) {
			TileEntityTurretBaseNT.closeInventory(mc.player);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 45, 16, 52, turret.power, turret.getMaxPower());

		String on = "§a" + I18nUtil.resolveKey("turret.on");
		String off = "§c" + I18nUtil.resolveKey("turret.off");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 8, guiTop + 29, 10, 10, mouseX, mouseY, I18nUtil.resolveKeyArray("turret.players", turret.targetPlayers ? on : off));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 22, guiTop + 29, 10, 10, mouseX, mouseY, I18nUtil.resolveKeyArray("turret.animals", turret.targetAnimals ? on : off));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 36, guiTop + 29, 10, 10, mouseX, mouseY, I18nUtil.resolveKeyArray("turret.mobs", turret.targetMobs ? on : off));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 50, guiTop + 29, 10, 10, mouseX, mouseY, I18nUtil.resolveKeyArray("turret.machines", turret.targetMachines ? on : off));

		drawAmmo(mouseX, mouseY);

		super.renderHoveredToolTip(mouseX, mouseY);
	}

	public void drawAmmo(int mouseX, int mouseY){
		if(this.guiLeft + 79 <= mouseX && guiLeft + 79 + 54 > mouseX && guiTop + 61 < mouseY && guiTop + 61 + 54 >= mouseY) {
			
			boolean draw = true;
			for(int i = 1; i < 10; i++) {
				if(this.isMouseOverSlot(this.inventorySlots.getSlot(i), mouseX, mouseY) && this.inventorySlots.getSlot(i).getHasStack()) {
					draw = false;
					break;
				}
			}
			
			if(draw) {
				List<ItemStack> list = new ArrayList<>(turret.getAmmoTypesForDisplay());
				List<Object[]> lines = new ArrayList<>();
				try {
					ItemStack selected = list.get(0);
					int highLight = 0;
					if (list.size() > 1) {
						int cycle = (int) ((System.currentTimeMillis() % (1000 * list.size())) / 1000);
						selected = list.get(cycle).copy();
						highLight = cycle;
						list.set(cycle, selected);
					}

					if (list.size() < 10) {
						lines.add(list.toArray());
					} else if (list.size() < 24) {
						lines.add(list.subList(0, list.size() / 2).toArray());
						lines.add(list.subList(list.size() / 2, list.size()).toArray());
					} else {
						int bound0 = (int) Math.ceil(list.size() / 3D);
						int bound1 = (int) Math.ceil(list.size() / 3D * 2D);
						lines.add(list.subList(0, bound0).toArray());
						lines.add(list.subList(bound0, bound1).toArray());
						lines.add(list.subList(bound1, list.size()).toArray());
					}

					lines.add(new Object[]{I18nUtil.resolveKey(selected.getDisplayName())});
					this.drawStackText(lines, mouseX, mouseY, this.fontRenderer, highLight);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);

		final BlockPos turretPos = turret.getPos();

		boolean flag = x >= this.field.x && x < this.field.x + this.field.width && y >= this.field.y && y < this.field.y + this.field.height;
		this.field.setFocused(flag);

		if(guiLeft + 115 <= x && guiLeft + 115 + 18 > x && guiTop + 25 < y && guiTop + 25 + 18 >= y) {

			playClickSound();
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turretPos, 0, 0));
			return;
		}

		if(guiLeft + 8 <= x && guiLeft + 8 + 10 > x && guiTop + 29 < y && guiTop + 29 + 10 >= y) {

			playClickSound();
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turretPos, 0, 1));
			return;
		}

		if(guiLeft + 22 <= x && guiLeft + 22 + 10 > x && guiTop + 29 < y && guiTop + 29 + 10 >= y) {

			playClickSound();
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turretPos, 0, 2));
			return;
		}

		if(guiLeft + 36 <= x && guiLeft + 36 + 10 > x && guiTop + 29 < y && guiTop + 29 + 10 >= y) {

			playClickSound();
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turretPos, 0, 3));
			return;
		}

		if(guiLeft + 50 <= x && guiLeft + 50 + 10 > x && guiTop + 29 < y && guiTop + 29 + 10 >= y) {

			playClickSound();
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turretPos, 0, 4));
			return;
		}
		
		int count = getCount();
		
		if(count > 0) {
			
			if(guiLeft + 7 <= x && guiLeft + 7 + 18 > x && guiTop + 80 < y && guiTop + 80 + 18 >= y) {
	
				index--;
				if(index < 0)
					index = count - 1;
				playClickSound();
				return;
			}

			if(guiLeft + 43 <= x && guiLeft + 43 + 18 > x && guiTop + 80 < y && guiTop + 80 + 18 >= y) {
	
				index++;
				index %= count;
				playClickSound();
				return;
			}
		}
		
		if(guiLeft + 7 <= x && guiLeft + 7 + 18 > x && guiTop + 98 < y && guiTop + 98 + 18 >= y) {
			
			playClickSound();
			
			if(this.field.getText().isEmpty())
				return;
			
			NBTTagCompound data = new NBTTagCompound();
			data.setString("name", this.field.getText());
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turretPos));

            this.field.setText("");
			return;
		}

		if(guiLeft + 43 <= x && guiLeft + 43 + 18 > x && guiTop + 98 < y && guiTop + 98 + 18 >= y) {
			
			playClickSound();
			
			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("del", this.index);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, turretPos));
        }
	}

	public int getTurretFontColor(){
		return 0x808080;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.turret.hasCustomName() ? this.turret.getName() : I18n.format(this.turret.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, getTurretFontColor());
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, getTurretFontColor());
		
		List<String> names = turret.getWhitelist();
		
		String n = TextFormatting.ITALIC + "None";
		
		while(this.index >= this.getCount())
			this.index--;
		
		if(index < 0)
			index = 0;
		
		if(names != null) {
			n = names.get(index);
		}
		
		String t = this.field.getText();
		
		String cursor = System.currentTimeMillis() % 1000 < 500 ? " " : "||";
		
		if(this.field.isFocused())
			t = t.substring(0, this.field.getCursorPosition()) + cursor + t.substring(this.field.getCursorPosition(), t.length());
		
		double scale = 2;
		
		GL11.glScaled(1D / scale, 1D / scale, 1);
		this.fontRenderer.drawString(n, (int)(12 * scale), (int)(51 * scale), 0x00ff00);
		this.fontRenderer.drawString(t, (int)(12 * scale), (int)(69 * scale), 0x00ff00);
		GL11.glScaled(scale, scale, 1);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int mX, int mY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.getTexture());
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(guiLeft + 7 <= mX && guiLeft + 7 + 18 > mX && guiTop + 80 < mY && guiTop + 80 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 7, guiTop + 80, 176, 58, 18, 18);
		}
		if(guiLeft + 43 <= mX && guiLeft + 43 + 18 > mX && guiTop + 80 < mY && guiTop + 80 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 43, guiTop + 80, 194, 58, 18, 18);
		}
		if(guiLeft + 7 <= mX && guiLeft + 7 + 18 > mX && guiTop + 98 < mY && guiTop + 98 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 7, guiTop + 98, 176, 76, 18, 18);
		}
		if(guiLeft + 43 <= mX && guiLeft + 43 + 18 > mX && guiTop + 98 < mY && guiTop + 98 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 43, guiTop + 98, 194, 76, 18, 18);
		}
		
		int i = turret.getPowerScaled(53);
		drawTexturedModalRect(guiLeft + 152, guiTop + 97 - i, 194, 52 - i, 16, i);
		
		if(turret.isOn)
			drawTexturedModalRect(guiLeft + 115, guiTop + 26, 176, 40, 18, 18);
		
		if(turret.targetPlayers)
			drawTexturedModalRect(guiLeft + 8, guiTop + 30, 176, 0, 10, 10);
		
		if(turret.targetAnimals)
			drawTexturedModalRect(guiLeft + 22, guiTop + 30, 176, 10, 10, 10);
		
		if(turret.targetMobs)
			drawTexturedModalRect(guiLeft + 36, guiTop + 30, 176, 20, 10, 10);
		
		if(turret.targetMachines)
			drawTexturedModalRect(guiLeft + 50, guiTop + 30, 176, 30, 10, 10);
		
		int tallies = turret.stattrak;
		
		if(tallies >= 36) {
			
			drawTexturedModalRect(guiLeft + 77, guiTop + 50, 176, 120, 63, 6);
			
		} else {
			
			int steps = (int)Math.ceil(tallies / 5D);
			
			for(int s = 0; s < steps; s++) {
				
				int m = tallies % 5;
				
				if(s < steps - 1 || m == 0) {
					drawTexturedModalRect(guiLeft + 77 + 9 * s, guiTop + 50, 194, 94, 9, 6);
				} else {
					
					drawTexturedModalRect(guiLeft + 77 + 9 * s, guiTop + 50, 176, 94, m * 2, 6);
				}
			}
		}
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}
	
	private int getCount() {
		
		List<String> names = turret.getWhitelist();
		
		if(names == null)
			return 0;
		
		return names.size();
	}
	
	@Override
	protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException {
		
		if(this.field.textboxKeyTyped(p_73869_1_, p_73869_2_)) {
			
		} else {
			super.keyTyped(p_73869_1_, p_73869_2_);
		}
	}
}
