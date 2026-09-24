package com.hbm.render.tileentity;

import com.hbm.blocks.machine.FoundryTank;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.TileEntityFoundryTank;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.FastTESR;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

@AutoRegister
public class RenderFoundryTank extends FastTESR<TileEntityFoundryTank> {

	public static TextureAtlasSprite lava;

	@Override
	public void renderTileEntityFast(@NotNull TileEntityFoundryTank tank, double x, double y, double z, float partialTicks, int destroyStage, float partial, @NotNull BufferBuilder buffer) {

		if(lava == null || tank.type == null || tank.amount <= 0) return;

		World world = tank.getWorld();
		BlockPos pos = tank.getPos();

		boolean conNegY = FoundryTank.isTank(world, pos.down());
		boolean conPosY = FoundryTank.isTank(world, pos.up());
		boolean conPosX = FoundryTank.isTank(world, pos.east());
		boolean conNegX = FoundryTank.isTank(world, pos.west());
		boolean conPosZ = FoundryTank.isTank(world, pos.south());
		boolean conNegZ = FoundryTank.isTank(world, pos.north());

		double max = 0.75D + (conNegY ? 0.125D : 0) + (conPosY ? 0.125D : 0);
		double level = tank.amount * max / tank.getCapacity();
		double bottom = conNegY ? 0D : 0.125D;
		double top = bottom + level;

		Color color = new Color(tank.type.moltenColor).brighter();
		double brightener = 0.7D;
		int r = (int) (255D - (255D - color.getRed()) * brightener);
		int g = (int) (255D - (255D - color.getGreen()) * brightener);
		int b = (int) (255D - (255D - color.getBlue()) * brightener);

		float uMin = lava.getMinU();
		float uMax = lava.getMaxU();
		float vMin = lava.getMinV();
		float vMax = lava.getMaxV();
		float vTop = lava.getInterpolatedV((1D - top) * 16D);
		float vBottom = lava.getInterpolatedV((1D - bottom) * 16D);

		vertex(buffer, x,     y + top, z,     uMin, vMin, r, g, b);
		vertex(buffer, x,     y + top, z + 1, uMin, vMax, r, g, b);
		vertex(buffer, x + 1, y + top, z + 1, uMax, vMax, r, g, b);
		vertex(buffer, x + 1, y + top, z,     uMax, vMin, r, g, b);

		if(conPosX) {
			vertex(buffer, x + 1, y + bottom, z,     uMin, vBottom, r, g, b);
			vertex(buffer, x + 1, y + top,    z,     uMin, vTop,    r, g, b);
			vertex(buffer, x + 1, y + top,    z + 1, uMax, vTop,    r, g, b);
			vertex(buffer, x + 1, y + bottom, z + 1, uMax, vBottom, r, g, b);
		}

		if(conNegX) {
			vertex(buffer, x, y + bottom, z + 1, uMin, vBottom, r, g, b);
			vertex(buffer, x, y + top,    z + 1, uMin, vTop,    r, g, b);
			vertex(buffer, x, y + top,    z,     uMax, vTop,    r, g, b);
			vertex(buffer, x, y + bottom, z,     uMax, vBottom, r, g, b);
		}

		if(conPosZ) {
			vertex(buffer, x + 1, y + bottom, z + 1, uMin, vBottom, r, g, b);
			vertex(buffer, x + 1, y + top,    z + 1, uMin, vTop,    r, g, b);
			vertex(buffer, x,     y + top,    z + 1, uMax, vTop,    r, g, b);
			vertex(buffer, x,     y + bottom, z + 1, uMax, vBottom, r, g, b);
		}

		if(conNegZ) {
			vertex(buffer, x,     y + bottom, z, uMin, vBottom, r, g, b);
			vertex(buffer, x,     y + top,    z, uMin, vTop,    r, g, b);
			vertex(buffer, x + 1, y + top,    z, uMax, vTop,    r, g, b);
			vertex(buffer, x + 1, y + bottom, z, uMax, vBottom, r, g, b);
		}
	}

	private static void vertex(BufferBuilder buffer, double x, double y, double z, float u, float v, int r, int g, int b) {
		buffer.pos(x, y, z).color(r, g, b, 255).tex(u, v).lightmap(240, 240).endVertex();
	}
}
