package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockCustomMachine;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.NTMRenderHelper;
import com.hbm.tileentity.machine.TileEntityCustomMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;

import static com.hbm.render.util.SmallBlockPronter.renderSimpleBlockAt;
import static com.hbm.render.util.SmallBlockPronter.startDrawing;

@AutoRegister
public class RenderCustomMachine extends TileEntitySpecialRenderer<TileEntityCustomMachine> {

	@Override
	public void render(@NotNull TileEntityCustomMachine custom, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		CustomMachineConfigJSON.MachineConfiguration config = custom.config;

		IBlockState controller = custom.getWorld().getBlockState(custom.getPos());
		if(controller.getBlock() != ModBlocks.custom_machine) return;

		ForgeDirection dir = ForgeDirection.getOrientation(controller.getValue(BlockCustomMachine.FACING).getIndex());
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		if(config != null && !custom.structureOK) {

			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);

			startDrawing();
			NTMRenderHelper.bindBlockTexture();
			NTMRenderHelper.startDrawingTexturedQuads();

			for(CustomMachineConfigJSON.MachineConfiguration.ComponentDefinition comp : config.components) {
				int rx = -dir.offsetX * comp.x + rot.offsetX * comp.x;
				int ry = +comp.y;
				int rz = -dir.offsetZ * comp.z + rot.offsetZ * comp.z;
				if(dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) {
					rx = +dir.offsetZ * comp.z - rot.offsetZ * comp.z;
					rz = +dir.offsetX * comp.x - rot.offsetX * comp.x;
				}

				int index = (int) ((System.currentTimeMillis() / 1000) % comp.metas.size());
				IBlockState state = comp.block.getStateFromMeta(comp.metas.get(index).getAsInt());
				TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
				renderSimpleBlockAt(sprite, rx, ry, rz);
			}

			NTMRenderHelper.draw();

			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
