package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityFoundrySlagtap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class FoundrySlagtap extends FoundryOutlet {

	public FoundrySlagtap(String s) {
		super(s);
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":foundry_slagtap_top");
		this.iconSide = iconRegister.registerIcon(RefStrings.MODID + ":foundry_slagtap_side");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":foundry_slagtap_bottom");
		this.iconInner = iconRegister.registerIcon(RefStrings.MODID + ":foundry_slagtap_inner");
		this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":foundry_slagtap_front");
	}*/

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundrySlagtap();
	}

}