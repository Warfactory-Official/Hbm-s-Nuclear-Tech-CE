package com.hbm.entity.grenade;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@AutoRegister(name = "entity_waste_pearl", trackingRange = 64)
public class EntityWastePearl extends EntityGrenadeBase {

	public EntityWastePearl(World world) {
		super(world);
	}

	public EntityWastePearl(World world, EntityLivingBase thrower, EnumHand hand) {
		super(world, thrower, hand);
	}

	public EntityWastePearl(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public void explode() {

		if(world.isRemote) return;

		this.setDead();

		int x = (int) Math.floor(posX);
		int y = (int) Math.floor(posY);
		int z = (int) Math.floor(posZ);

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for(int ix = x - 3; ix <= x + 3; ix++) {
			for(int iy = y - 3; iy <= y + 3; iy++) {
				for(int iz = z - 3; iz <= z + 3; iz++) {
					pos.setPos(ix, iy, iz);

					if(world.rand.nextInt(3) == 0 && world.getBlockState(pos).getBlock().isReplaceable(world, pos) && ModBlocks.fallout.canPlaceBlockAt(world, pos)) {
						world.setBlockState(pos.toImmutable(), ModBlocks.fallout.getDefaultState());
					} else if(world.isAirBlock(pos)) {
						world.setBlockState(pos.toImmutable(), rand.nextBoolean() ? ModBlocks.gas_radon.getDefaultState() : ModBlocks.gas_radon_dense.getDefaultState());
					}
				}
			}
		}
	}
}
