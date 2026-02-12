package com.hbm.blocks.machine;

import com.hbm.blocks.ILookOverlay;
import com.hbm.lib.NTMBlockContainer;
import com.hbm.tileentity.machine.TileEntityMachineTeleporter;
import com.hbm.util.I18nUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;

public class MachineTeleporter extends NTMBlockContainer implements ILookOverlay {

	public MachineTeleporter(Material materialIn, String name) {
		super(materialIn, name);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityMachineTeleporter();
	}
	
	@Override
	public void printHook(Pre event, World world, BlockPos pos) {
		
		TileEntity tile = world.getTileEntity(pos);
		
		if(!(tile instanceof TileEntityMachineTeleporter tele)) return;

        List<String> text = new ArrayList<>();
		
		text.add((tele.power >= TileEntityMachineTeleporter.consumption ? "§a" : "§c") + String.format("%,d", tele.power) + " / " + String.format("%,d", tele.maxPower));
		if(tele.target == null) {
			text.add("§cNo destination set!");
		} else {
			text.add("Destination: " + tele.target.getX() + " / " + tele.target.getY() + " / " + tele.target.getZ());
		}
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
	
	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
