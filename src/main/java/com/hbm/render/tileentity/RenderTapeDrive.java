package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineTapeDrive;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderTapeDrive extends TileEntitySpecialRenderer<TileEntityMachineTapeDrive> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineTapeDrive tapeDrive, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		switch(tapeDrive.getBlockMetadata()) {
		case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		}

		bindTexture(ResourceManager.tape_drive_tex);
		ResourceManager.tape_drive.renderPart("Frame");

		for(int i = 0; i < 12; i++) {
			if(tapeDrive.tapes[i] == TileEntityMachineTapeDrive.SLOT_EMPTY) continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0.25 - 0.5 * (i / 6), 0.3125 - (i % 6) * 0.125);
			ResourceManager.tape_drive.renderPart("Drive");
			GlStateManager.popMatrix();
		}

		RenderArcFurnace.fullbright(true);
		GlStateManager.disableTexture2D();

		for(int i = 0; i < 12; i++) {
			byte tape = tapeDrive.tapes[i];
			if(tape == TileEntityMachineTapeDrive.SLOT_EMPTY) continue;

			if(tape == TileEntityMachineTapeDrive.SLOT_ANY) GlStateManager.color(1F, 0F, 0F);
			if(tape == TileEntityMachineTapeDrive.SLOT_EMPTY_TAPE) GlStateManager.color(1F, 0.75F, 0F);
			if(tape == TileEntityMachineTapeDrive.SLOT_FILLED_TAPE) GlStateManager.color(0F, 1F, 0F);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0.25 - 0.5 * (i / 6), 0.3125 - (i % 6) * 0.125);
			ResourceManager.tape_drive.renderPart("Light");
			GlStateManager.popMatrix();
		}

		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.enableTexture2D();
		RenderArcFurnace.fullbright(false);

		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_tape_drive);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -3, 0);
				double scale = 5;
				GlStateManager.scale(scale, scale, scale);
			}
			public void renderCommon(ItemStack stack) {
				GlStateManager.scale(2, 2, 2);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.tape_drive_tex);
				ResourceManager.tape_drive.renderPart("Frame");
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}};
	}
}
