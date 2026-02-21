package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCompactLauncher;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePronter;
import com.hbm.tileentity.bomb.TileEntityCompactLauncher;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class GUICompactLauncher extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_launch_table_small.png");
	private final TileEntityCompactLauncher launcher;
	
	public GUICompactLauncher(InventoryPlayer invPlayer, TileEntityCompactLauncher tile) {
		super(new ContainerCompactLauncher(invPlayer, tile));
		launcher = tile;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		launcher.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 36, 16, 34);
		launcher.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 36, 16, 34);
		String[] text2 = I18nUtil.resolveKeyArray("desc.solidfuellaunch", launcher.solid);
		
		this.drawCustomInfo(mouseX, mouseY, guiLeft + 152, guiTop + 88 - 52, 16, 52, text2);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 113, 34, 6, launcher.power, TileEntityCompactLauncher.maxPower);

		String[] text = I18nUtil.resolveKeyArray("desc.guimachcomplauncher1");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, text);
		
		String[] text1 = I18nUtil.resolveKeyArray("desc.guimachcomplauncher2");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36 + 16, 16, 16, guiLeft - 8, guiTop + 36 + 16, text1);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.launcher.hasCustomName() ? this.launcher.getName() : I18n.format(this.launcher.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int i = (int)launcher.getPowerScaled(34);
		drawTexturedModalRect(guiLeft + 134, guiTop + 113, 176, 96, i, 6);
		
		int j = launcher.getSolidScaled(52);
		drawTexturedModalRect(guiLeft + 152, guiTop + 88 - j, 176, 96 - j, 16, j);
		
		if(launcher.isMissileValid())
			drawTexturedModalRect(guiLeft + 25, guiTop + 35, 176, 26, 18, 18);
		
		if(launcher.hasDesignator())
			drawTexturedModalRect(guiLeft + 25, guiTop + 71, 176, 26, 18, 18);
		
		if(launcher.liquidState() == 1)
			drawTexturedModalRect(guiLeft + 121, guiTop + 23, 176, 0, 6, 8);
		if(launcher.liquidState() == 0)
			drawTexturedModalRect(guiLeft + 121, guiTop + 23, 182, 0, 6, 8);
		
		if(launcher.oxidizerState() == 1)
			drawTexturedModalRect(guiLeft + 139, guiTop + 23, 176, 0, 6, 8);
		if(launcher.oxidizerState() == 0)
			drawTexturedModalRect(guiLeft + 139, guiTop + 23, 182, 0, 6, 8);
		
		if(launcher.solidState() == 1)
			drawTexturedModalRect(guiLeft + 157, guiTop + 23, 176, 0, 6, 8);
		if(launcher.solidState() == 0)
			drawTexturedModalRect(guiLeft + 157, guiTop + 23, 182, 0, 6, 8);
		
		this.drawInfoPanel(guiLeft - 16, guiTop + 36, 16, 16, 2);
		this.drawInfoPanel(guiLeft - 16, guiTop + 36 + 16, 16, 16, 11);

		launcher.tanks[0].renderTank(guiLeft + 116, guiTop + 70, this.zLevel, 16, 34);
		launcher.tanks[1].renderTank(guiLeft + 134, guiTop + 70, this.zLevel, 16, 34);
		
		/// DRAW MISSILE START
		GlStateManager.pushMatrix();

		MissileMultipart missile;
		
		if(launcher.isMissileValid()) {
			ItemStack custom = launcher.inventory.getStackInSlot(0);

			missile = new MissileMultipart();

			missile = MissileMultipart.loadFromStruct(ItemCustomMissile.getStruct(custom));
		
			GlStateManager.translate(guiLeft + 88, guiTop + 115, 100);
			
			double size = 5 * 18;
			double scale = size / Math.max(missile.getHeight(), 6);

			GlStateManager.rotate(90, 0, 1, 0);
			GlStateManager.translate(missile.getHeight() / 2D * scale, 0, 0);
			GL11.glScaled(scale, scale, scale);
			
			GlStateManager.scale(-1, -1, -1);
			
			MissilePronter.prontMissile(missile, Minecraft.getMinecraft().getTextureManager());
		}
		
		GlStateManager.popMatrix();
		/// DRAW MISSILE END
	}
}