package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.misc.DiamondPronter;
import com.hbm.render.misc.EnumSymbol;
import com.hbm.tileentity.machine.TileEntityBarrel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
@AutoRegister
public class RenderFluidBarrel extends TileEntitySpecialRenderer<TileEntityBarrel> {

	@Override
	public void render(TileEntityBarrel barrel, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.enableLighting();
		FluidType type = barrel.tankNew.getTankType();

		if(type != Fluids.NONE) {
			RenderHelper.disableStandardItemLighting();
			GlStateManager.pushMatrix();

			int poison = type.poison;
			int flammability = type.flammability;
			int reactivity = type.reactivity;
			EnumSymbol symbol = type.symbol;

			for(int j = 0; j < 4; j++) {

				GlStateManager.pushMatrix();
				GlStateManager.translate(0.4, 0.25, -0.15);
				GlStateManager.scale(1.0F, 0.35F, 0.35F);
				DiamondPronter.pront(poison, flammability, reactivity, symbol);
				GlStateManager.popMatrix();
				GlStateManager.rotate(90, 0, 1, 0);
			}

			GlStateManager.popMatrix();
			RenderHelper.enableStandardItemLighting();
		}

		GlStateManager.popMatrix();
	}
}
