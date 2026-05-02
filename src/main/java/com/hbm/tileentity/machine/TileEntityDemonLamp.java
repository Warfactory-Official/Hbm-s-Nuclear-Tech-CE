package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AutoRegister
public class TileEntityDemonLamp extends TileEntity implements ITickable {
	private AxisAlignedBB bb;

	@Override
	public void update(){
		if(!world.isRemote) {
			radiate(world, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	@SuppressWarnings("deprecation")
	private void radiate(World world, int x, int y, int z){

		float rads = 100000F;
		double range = 25D;

		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).grow(range, range, range));
		for(EntityLivingBase e : entities) {

			Vec3 vec = Vec3.createVectorHelper(e.posX - (x + 0.5), (e.posY + e.getEyeHeight()) - (y + 0.5), e.posZ - (z + 0.5));
			double len = vec.length();
			vec = vec.normalize();

			float res = 0;

			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(x + 0.5 + vec.xCoord * i);
				int iy = (int)Math.floor(y + 0.5 + vec.yCoord * i);
				int iz = (int)Math.floor(z + 0.5 + vec.zCoord * i);
				
				BlockPos pos = new BlockPos(ix, iy, iz);
				IBlockState state = world.getBlockState(pos);
				res += state.getBlock().getExplosionResistance(null);
			}

			if(res < 1)
				res = 1;

			float eRads = rads;
			eRads /= (float)res;
			eRads /= (float)(len * len);

			ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);
			if(len < 2) {
				e.attackEntityFrom(DamageSource.IN_FIRE, 100);
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		if (bb == null) bb = new AxisAlignedBB(pos.getX() - 16, pos.getY() - 1, pos.getZ() - 16, pos.getX() + 17, pos.getY() + 2, pos.getZ() + 17);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 65536.0D;
	}
}
