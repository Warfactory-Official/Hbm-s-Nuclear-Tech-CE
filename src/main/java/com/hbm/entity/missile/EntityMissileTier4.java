package com.hbm.entity.missile;

import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.util.MutableVec3d;
import com.hbm.world.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityMissileTier4 extends EntityMissileBaseNT {
	public static final DataParameter<Byte> ROT_IDX =
			EntityDataManager.createKey(EntityMissileTier4.class, DataSerializers.BYTE);

	public EntityMissileTier4(World world) { super(world); }
	public EntityMissileTier4(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(ROT_IDX, (byte) 5);
	}

	@Override
	public List<ItemStack> getDebris() {
		List<ItemStack> list = new ArrayList<>();
		list.add(new ItemStack(ModItems.plate_titanium, 16));
		list.add(new ItemStack(ModItems.plate_steel, 20));
		list.add(new ItemStack(ModItems.plate_aluminium, 12));
		list.add(new ItemStack(ModItems.thruster_large, 1));
		return list;
	}

	@Override
	public String getTranslationKey() {
		return "radar.target.tier4";
	}

	@Override
	public int getBlipLevel() {
		return IRadarDetectableNT.TIER4;
	}

	@Override
	protected void spawnContrail() {
		
		byte rot = this.dataManager.get(ROT_IDX);

		MutableVec3d thrust = new MutableVec3d(0, 0, 1);
		switch (rot) {
			case 2 -> thrust.rotateYawSelf((float) -Math.PI / 2F);
			case 4 -> thrust.rotateYawSelf((float) -Math.PI);
			case 3 -> thrust.rotateYawSelf((float) -Math.PI / 2F * 3F);
		}
		thrust.rotateYawSelf((this.rotationYaw + 90) * (float) Math.PI / 180F);
		thrust.rotatePitchSelf(this.rotationPitch * (float) Math.PI / 180F);
		thrust.rotateYawSelf(-(this.rotationYaw + 90) * (float) Math.PI / 180F);

		this.spawnControlWithOffset(thrust.x, thrust.y, thrust.z);
		this.spawnControlWithOffset(0, 0, 0);
		this.spawnControlWithOffset(-thrust.x, -thrust.z, -thrust.z);
	}
	@AutoRegister(name = "entity_missile_nuclear", trackingRange = 1000)
	public static class EntityMissileNuclear extends EntityMissileTier4 {
		public EntityMissileNuclear(World world) { super(world); }
		public EntityMissileNuclear(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFac(world, BombConfig.missileRadius, posX, posY, posZ).setDetonator(thrower));
			EntityNukeTorex.statFac(world, posX, posY, posZ, BombConfig.missileRadius);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_nuclear); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_nuclear); }
	}
	@AutoRegister(name = "entity_missile_mirv", trackingRange = 1000)
	public static class EntityMissileMirv extends EntityMissileTier4 {
		public EntityMissileMirv(World world) { super(world); }
		public EntityMissileMirv(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFac(world, BombConfig.missileRadius * 2, posX, posY, posZ).setDetonator(thrower));
			EntityNukeTorex.statFac(world, posX, posY, posZ, BombConfig.missileRadius * 2);
		}
		@Override public List<ItemStack> getDebris() {
			List<ItemStack> list = new ArrayList<ItemStack>();
			list.add(new ItemStack(ModItems.plate_titanium, 16));
			list.add(new ItemStack(ModItems.plate_steel, 20));
			list.add(new ItemStack(ModItems.plate_aluminium, 12));
			list.add(new ItemStack(ModItems.thruster_large, 1));
			return list;
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_mirv); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_nuclear_cluster); }
	}
	@AutoRegister(name = "entity_missile_volcano", trackingRange = 1000)
	public static class EntityMissileVolcano extends EntityMissileTier4 {
		public EntityMissileVolcano(World world) { super(world); }
		public EntityMissileVolcano(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			ExplosionLarge.explode(world, thrower, posX, posY, posZ, 10.0F, true, true, true);
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for(int x = -1; x <= 1; x++) for(int y = -1; y <= 1; y++) for(int z = -1; z <= 1; z++) world.setBlockState(pos.setPos((int) Math.floor(posX + x), (int) Math.floor(posY + y), (int) Math.floor(posZ + z)), ModBlocks.volcanic_lava_block.getDefaultState());
			world.setBlockState(getPosition(), ModBlocks.volcano_core.getDefaultState());
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_volcano); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_volcano); }
	}
	@AutoRegister(name = "entity_missile_doomsday", trackingRange = 1000)
	public static class EntityMissileDoomsday extends EntityMissileTier4 {
		public EntityMissileDoomsday(World world) { super(world); }
		public EntityMissileDoomsday(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFac(world, BombConfig.missileRadius * 2, posX, posY, posZ).moreFallout(100).setDetonator(thrower));
			EntityNukeTorex.statFac(world, posX, posY, posZ, BombConfig.missileRadius * 2);
		}
		@Override public List<ItemStack> getDebris() { return null; }
		@Override public ItemStack getDebrisRareDrop() { return null; }
		@Override public String getTranslationKey() { return "radar.target.doomsday"; }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_doomsday); }
	}
	@AutoRegister(name = "entity_missile_doomsday_rusted", trackingRange = 1000)
	public static class EntityMissileDoomsdayRusted extends EntityMissileDoomsday {
		public EntityMissileDoomsdayRusted(World world) { super(world); }
		public EntityMissileDoomsdayRusted(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFac(world, BombConfig.missileRadius, posX, posY, posZ).moreFallout(100).setDetonator(thrower));
			EntityNukeTorex.statFac(world, posX, posY, posZ, BombConfig.missileRadius);
		}
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_doomsday_rusted); }
	}
	//mlbv: n2 missile does not exist in upstream
	@AutoRegister(name = "entity_missile_n2", trackingRange = 1000)
	public static class EntityMissileN2 extends EntityMissileTier4 {
		public EntityMissileN2(World world) { super(world); }
		public EntityMissileN2(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFacNoRad(world, (BombConfig.n2Radius/12) * 5, posX, posY, posZ).setDetonator(thrower));
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(world, this.posX, this.posY, this.posZ, ((float)BombConfig.n2Radius/12) * 5);
			}
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_n2); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_n2); }
	}
}
