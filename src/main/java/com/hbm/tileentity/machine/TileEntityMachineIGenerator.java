package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_igenerator")
public class TileEntityMachineIGenerator extends TileEntityMachineBase implements ITickable {

	public TileEntityMachineIGenerator() {
		super(21);
	}

	@Override
	public String getDefaultName() {
		return "container.iGenerator";
	}

	@Override
	public void update() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
