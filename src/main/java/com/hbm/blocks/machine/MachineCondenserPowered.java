package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCondenserPowered;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.lib.ForgeDirection;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachineCondenserPowered extends BlockDummyable implements ILookOverlay {

	public MachineCondenserPowered(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		if(meta >= 12)
			return new TileEntityCondenserPowered();
		// go fuck yourself you two xd
		// metallolom just WHY do you set the (meta >= 8) when it is (meta >= 6) dude it's just ctrl+c ctrl+v goddammit
		if(meta >= 6) return new TileEntityProxyCombo().power().fluid();

		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 3, 3};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		this.makeExtra(world, x + rot.offsetX * 3, y + 1, z + rot.offsetZ * 3);
		this.makeExtra(world, x - rot.offsetX * 3, y + 1, z - rot.offsetZ * 3);
		this.makeExtra(world, x + dir.offsetX + rot.offsetX, y + 1, z + dir.offsetZ + rot.offsetZ);
		this.makeExtra(world, x + dir.offsetX - rot.offsetX, y + 1, z + dir.offsetZ - rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX + rot.offsetX, y + 1, z - dir.offsetZ + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX - rot.offsetX, y + 1, z - dir.offsetZ - rot.offsetZ);
	}

	@Override
	public void printHook(Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

		TileEntity te = world.getTileEntity(corePos);

		if(!(te instanceof TileEntityCondenserPowered tower)) return;

        List<String> text = new ArrayList();

		text.add(BobMathUtil.getShortNumber(tower.power) + "HE / " + BobMathUtil.getShortNumber(tower.maxPower) + "HE");

		for(int i = 0; i < tower.tanks.length; i++)
			text.add((i < 1 ? (TextFormatting.GREEN + "-> ") : (TextFormatting.RED + "<- ")) + TextFormatting.RESET + tower.tanks[i].getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", tower.tanks[i].getFill()) + "/" + String.format(Locale.US, "%,d", tower.tanks[i].getMaxFill()) + "mB");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}
