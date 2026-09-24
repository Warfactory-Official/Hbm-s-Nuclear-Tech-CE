package com.hbm.tileentity.bomb;

import com.hbm.blocks.bomb.Landmine;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AutoRegister
public class TileEntityLandmine extends TileEntity implements ITickable {

	private boolean isPrimed = false;
	public boolean waitingForPlayer = false;
	private AxisAlignedBB bb;

	@Override
	public void update() {
		if (world.isRemote) return;

		Block block = world.getBlockState(pos).getBlock();

		if (!(block instanceof Landmine)) return;
		Landmine landmine = (Landmine) block;

		double range = landmine.range;
		double height = landmine.height;

		if (waitingForPlayer) {
			range = 25;
			height = 25;
		} else if (!isPrimed) {
			range *= 2;
			height *= 2;
		}

		AxisAlignedBB box = new AxisAlignedBB(
				pos.getX() - range, pos.getY() - height, pos.getZ() - range,
				pos.getX() + range + 1, pos.getY() + height, pos.getZ() + range + 1
		);

		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, box);

		for (Entity entity : list) {
			if (entity instanceof EntityBat) continue;

			if (waitingForPlayer) {
				if (entity instanceof EntityPlayer) {
					waitingForPlayer = false;
					return;
				}
			} else {
				if (entity instanceof EntityLivingBase) {
					if (isPrimed) {
						landmine.explode(world, pos, entity);

						if (entity instanceof EntityPlayer) {
							//((EntityPlayer) entity).addStat(MainRegistry.statMines, 1); //FIXME Unsure what this is
						}
					}
					return;
				}
			}
		}


			if(!isPrimed && !waitingForPlayer) {

				this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.fstbmbStart, SoundCategory.BLOCKS, 2.0F, 1.0F);
				isPrimed = true;
			}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		isPrimed = compound.getBoolean("primed");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("primed", isPrimed);
		return super.writeToNBT(compound);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		return bb;
	}

}
